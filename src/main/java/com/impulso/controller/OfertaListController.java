package com.impulso.controller;

import com.impulso.db.DataAccessException;
import com.impulso.model.Empresa;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.model.OfertaFilter;
import com.impulso.service.CsvImporter;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Config;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class OfertaListController {

    private final ImpulsoService service = ImpulsoService.get();
    private final int pageSize = Math.max(1, Config.getInt("ui.pageSize", 7));

    @FXML private TextField txtBuscar;
    @FXML private HBox boxAvanzados;
    @FXML private ComboBox<Empresa> cmbEmpresa;
    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtSalMin;
    @FXML private TextField txtSalMax;
    @FXML private Label lblErrorFiltro;
    @FXML private Label lblResumen;
    @FXML private Label lblMostrando;
    @FXML private TableView<Oferta> tabla;
    @FXML private TableColumn<Oferta, String> colId;
    @FXML private TableColumn<Oferta, String> colTitulo;
    @FXML private TableColumn<Oferta, String> colEmpresa;
    @FXML private TableColumn<Oferta, Estado> colEstado;
    @FXML private TableColumn<Oferta, String> colFecha;
    @FXML private TableColumn<Oferta, String> colSalario;
    @FXML private TableColumn<Oferta, Oferta> colAcciones;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private HBox boxPaginas;

    private OfertaFilter filtro = OfertaFilter.vacio();
    private int pagina = 0;

    @FXML
    private void initialize() {
        dpDesde.setConverter(Fmt.DATE_CONVERTER);
        dpHasta.setConverter(Fmt.DATE_CONVERTER);
        cmbEstado.setItems(FXCollections.observableArrayList(Estado.values()));

        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colTitulo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitulo()));
        colEmpresa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmpresaNombre()));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(Fmt.fecha(c.getValue().getFechaPostulacion())));
        colSalario.setCellValueFactory(c -> new SimpleStringProperty(Fmt.money(c.getValue().getSalario())));
        colEstado.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getEstado()));
        colAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colEstado.setCellFactory(col -> new EstadoCell());
        colAcciones.setCellFactory(col -> new AccionesCell());
        colId.getStyleClass().add("col-muted");
        colTitulo.getStyleClass().add("col-strong");

        tabla.setPlaceholder(new Label("No hay candidaturas con estos filtros."));
        tabla.setRowFactory(tv -> {
            var row = new javafx.scene.control.TableRow<Oferta>();
            row.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 && !row.isEmpty()) {
                    Navigator.detalle(row.getItem().getId());
                }
            });
            return row;
        });

        recargarEmpresas();
        cargar();
    }

    private void recargarEmpresas() {
        try {
            List<Empresa> lista = service.listarEmpresas(null);
            cmbEmpresa.setItems(FXCollections.observableArrayList(lista));
        } catch (DataAccessException e) {
            Dialogs.error("No se pudieron cargar las empresas", e.getMessage());
        }
    }

    // ------------------------------------------------------------ filtros

    @FXML
    private void aplicar() {
        lblErrorFiltro.setVisible(false);
        lblErrorFiltro.setManaged(false);
        OfertaFilter f = new OfertaFilter();
        try {
            f.texto = txtBuscar.getText();
            f.idEmpresa = cmbEmpresa.getValue() == null ? null : cmbEmpresa.getValue().getId();
            f.estado = cmbEstado.getValue();
            f.desde = leerFecha(dpDesde, "Desde");
            f.hasta = leerFecha(dpHasta, "Hasta");
            try {
                f.salarioMin = Fmt.parseMoney(txtSalMin.getText());
                f.salarioMax = Fmt.parseMoney(txtSalMax.getText());
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("Los salarios deben ser importes numéricos (p. ej. 30000).");
            }
        } catch (IllegalArgumentException e) {
            lblErrorFiltro.setText(e.getMessage());
            lblErrorFiltro.setVisible(true);
            lblErrorFiltro.setManaged(true);
            return;
        }
        if (f.desde != null && f.hasta != null && f.desde.isAfter(f.hasta)) {
            lblErrorFiltro.setText("«Desde» no puede ser posterior a «Hasta».");
            lblErrorFiltro.setVisible(true);
            lblErrorFiltro.setManaged(true);
            return;
        }
        if (f.salarioMin != null && f.salarioMax != null && f.salarioMin > f.salarioMax) {
            lblErrorFiltro.setText("El salario mínimo no puede superar al máximo.");
            lblErrorFiltro.setVisible(true);
            lblErrorFiltro.setManaged(true);
            return;
        }
        filtro = f;
        pagina = 0;
        cargar();
    }

    private java.time.LocalDate leerFecha(DatePicker dp, String nombre) {
        String txt = dp.getEditor().getText();
        if (txt != null && !txt.isBlank() && Fmt.DATE_CONVERTER.fromString(txt) == null) {
            throw new IllegalArgumentException("La fecha «" + nombre + "» debe tener el formato DD/MM/AAAA.");
        }
        return Fmt.DATE_CONVERTER.fromString(txt);
    }

    @FXML
    private void limpiar() {
        txtBuscar.clear();
        cmbEmpresa.setValue(null);
        cmbEstado.setValue(null);
        dpDesde.setValue(null);
        dpDesde.getEditor().clear();
        dpHasta.setValue(null);
        dpHasta.getEditor().clear();
        txtSalMin.clear();
        txtSalMax.clear();
        filtro = OfertaFilter.vacio();
        pagina = 0;
        lblErrorFiltro.setVisible(false);
        lblErrorFiltro.setManaged(false);
        cargar();
    }

    @FXML
    private void toggleAvanzados() {
        boolean ver = !boxAvanzados.isVisible();
        boxAvanzados.setVisible(ver);
        boxAvanzados.setManaged(ver);
    }

    // ------------------------------------------------------------ datos y paginación

    private void cargar() {
        try {
            int total = service.contarOfertas(filtro);
            int paginas = Math.max(1, (int) Math.ceil(total / (double) pageSize));
            if (pagina >= paginas) {
                pagina = paginas - 1;
            }
            List<Oferta> datos = service.buscarOfertas(filtro, pageSize, pagina * pageSize);
            tabla.setItems(FXCollections.observableArrayList(datos));

            long empresas = datos.isEmpty() ? 0 : service.listarEmpresas(null).stream().filter(e -> e.getNumCandidaturas() > 0).count();
            lblResumen.setText(total + (total == 1 ? " candidatura" : " candidaturas") + "  ·  " + empresas + " empresas");
            int desde = total == 0 ? 0 : pagina * pageSize + 1;
            int hasta = pagina * pageSize + datos.size();
            lblMostrando.setText("Mostrando " + desde + "–" + hasta + " de " + total + " candidaturas");
            construirPaginador(paginas);
        } catch (DataAccessException e) {
            Dialogs.error("No se pudieron cargar las candidaturas", e.getMessage());
        }
    }

    private void construirPaginador(int paginas) {
        boxPaginas.getChildren().clear();
        for (int i = 0; i < paginas; i++) {
            final int p = i;
            Button b = new Button(String.valueOf(i + 1));
            b.getStyleClass().add("btn-page");
            if (i == pagina) {
                b.getStyleClass().add("btn-page-active");
            }
            b.setOnAction(e -> {
                pagina = p;
                cargar();
            });
            boxPaginas.getChildren().add(b);
        }
        btnAnterior.setDisable(pagina == 0);
        btnSiguiente.setDisable(pagina >= paginas - 1);
    }

    @FXML private void anterior() { if (pagina > 0) { pagina--; cargar(); } }
    @FXML private void siguiente() { pagina++; cargar(); }

    // ------------------------------------------------------------ acciones

    @FXML
    private void nueva() {
        Navigator.formOferta(null);
    }

    @FXML
    private void importarCsv() {
        Path defecto = Path.of(Config.get("import.csv", "data/candidaturas_prueba.csv"));
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar candidaturas desde CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File dir = defecto.toAbsolutePath().getParent() == null ? null : defecto.toAbsolutePath().getParent().toFile();
        if (dir != null && dir.isDirectory()) {
            fc.setInitialDirectory(dir);
        }
        File f = fc.showOpenDialog(tabla.getScene().getWindow());
        if (f == null) {
            return;
        }
        try {
            CsvImporter.Resultado r = new CsvImporter().importar(f.toPath());
            Dialogs.info("Importación completada",
                    r.ofertas() + " candidaturas importadas y " + r.empresasNuevas() + " empresas nuevas."
                            + (r.omitidas() > 0 ? " " + r.omitidas() + " ya existían y se han omitido." : ""));
            Navigator.ofertas();
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo importar el CSV", e.getMessage());
        }
    }

    private void eliminar(Oferta o) {
        if (!Dialogs.confirmar("Eliminar candidatura",
                "¿Eliminar «" + o.getTitulo() + "» (" + o.getEmpresaNombre() + ") y sus entrevistas?")) {
            return;
        }
        try {
            service.eliminarOferta(o.getId());
            cargar();
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo eliminar", e.getMessage());
        }
    }

    // ------------------------------------------------------------ celdas personalizadas

    /** Píldora de color + desplegable para cambiar el estado desde el propio listado. */
    private class EstadoCell extends TableCell<Oferta, Estado> {
        private final ComboBox<Estado> combo = new ComboBox<>(FXCollections.observableArrayList(Estado.values()));
        private boolean cargando;

        EstadoCell() {
            combo.getStyleClass().add("estado-combo");
            combo.setOnAction(e -> {
                if (cargando || getTableRow() == null) {
                    return;
                }
                Oferta o = getTableRow().getItem();
                Estado nuevo = combo.getValue();
                if (o == null || nuevo == null || nuevo == o.getEstado()) {
                    return;
                }
                try {
                    service.cambiarEstado(o.getId(), nuevo);
                    o.setEstado(nuevo);
                    pintar(nuevo);
                } catch (DataAccessException ex) {
                    Dialogs.error("No se pudo cambiar el estado", ex.getMessage());
                    cargando = true;
                    combo.setValue(o.getEstado());
                    cargando = false;
                }
            });
        }

        private void pintar(Estado e) {
            combo.getStyleClass().removeIf(s -> s.startsWith("estado-") && !s.equals("estado-combo"));
            combo.getStyleClass().add(e.getCssClass());
        }

        @Override
        protected void updateItem(Estado item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                return;
            }
            cargando = true;
            combo.setValue(item);
            pintar(item);
            cargando = false;
            setGraphic(combo);
        }
    }

    private class AccionesCell extends TableCell<Oferta, Oferta> {
        private final HBox box = new HBox(12);
        private final Hyperlink ver = new Hyperlink("Ver");
        private final Hyperlink editar = new Hyperlink("Editar");
        private final Hyperlink borrar = new Hyperlink("Eliminar");

        AccionesCell() {
            box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            borrar.getStyleClass().add("link-danger");
            box.getChildren().addAll(ver, editar, borrar);
            ver.setOnAction(e -> Navigator.detalle(getItem().getId()));
            editar.setOnAction(e -> Navigator.formOferta(getItem().getId()));
            borrar.setOnAction(e -> eliminar(getItem()));
        }

        @Override
        protected void updateItem(Oferta item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
