package com.impulso.controller;

import com.impulso.db.DataAccessException;
import com.impulso.model.Empresa;
import com.impulso.model.Oferta;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.List;

public class EmpresaFormController {

    private final ImpulsoService service = ImpulsoService.get();
    private Empresa empresa;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblError;
    @FXML private Label lblPanelTitulo;
    @FXML private Label lblPanelTexto;
    @FXML private Label lblPanelAviso;
    @FXML private TextField txtNombre;
    @FXML private TextField txtSector;
    @FXML private TextField txtWeb;
    @FXML private DatePicker dpFecha;
    @FXML private Button btnGuardar;
    @FXML private VBox cardCandidaturas;
    @FXML private Label lblCandResumen;
    @FXML private VBox listaCandidaturas;

    @FXML
    private void initialize() {
        dpFecha.setConverter(Fmt.DATE_CONVERTER);
    }

    /** @param idEmpresa null = alta; con id = edición. */
    public void cargar(Integer idEmpresa) {
        try {
            if (idEmpresa == null) {
                empresa = new Empresa();
                lblTitulo.setText("Nueva empresa");
                lblSubtitulo.setText("Añade una empresa a tu directorio para asociar nuevas candidaturas.");
                dpFecha.setValue(LocalDate.now());
                lblPanelTitulo.setText("Un directorio organizado");
                lblPanelTexto.setText("Crea una ficha por empresa y reutilízala cada vez que registres una nueva candidatura.");
                lblPanelAviso.setText("Evita duplicados: comprueba si la empresa ya existe antes de guardarla. "
                        + "Mantén el mismo nombre en todo tu seguimiento.");
                return;
            }
            empresa = service.empresa(idEmpresa).orElseThrow(() -> new DataAccessException("La empresa no existe."));
            lblTitulo.setText("Editar empresa");
            lblSubtitulo.setText(empresa.getNombre() + " · Registrada el " + Fmt.fecha(empresa.getFechaRegistro())
                    + " · " + empresa.getNumCandidaturas() + " candidaturas asociadas");
            txtNombre.setText(empresa.getNombre());
            txtSector.setText(empresa.getSector());
            txtWeb.setText(empresa.getWeb() == null ? "" : empresa.getWeb());
            dpFecha.setValue(empresa.getFechaRegistro());
            lblPanelTitulo.setText("Ficha de " + empresa.getNombre());
            lblPanelTexto.setText("Estos datos se muestran en las " + empresa.getNumCandidaturas()
                    + " candidaturas asociadas a " + empresa.getNombre() + ".");
            lblPanelAviso.setText("");

            List<Oferta> ofertas = service.candidaturasDeEmpresa(empresa.getId());
            cardCandidaturas.setVisible(true);
            cardCandidaturas.setManaged(true);
            lblCandResumen.setText(ofertas.size() + " oportunidades");
            listaCandidaturas.getChildren().clear();
            for (Oferta o : ofertas) {
                Label titulo = new Label("#" + o.getId() + " · " + o.getTitulo());
                titulo.getStyleClass().add("strong");
                Label estado = new Label("● " + o.getEstado());
                estado.getStyleClass().addAll("pill", o.getEstado().getCssClass() + "-pill");
                listaCandidaturas.getChildren().add(new VBox(4, titulo, estado));
            }
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo cargar la empresa", e.getMessage());
            Navigator.empresas();
        }
    }

    @FXML
    private void guardar() {
        ocultarError();
        empresa.setNombre(txtNombre.getText() == null ? "" : txtNombre.getText().trim());
        empresa.setSector(txtSector.getText() == null ? "" : txtSector.getText().trim());
        empresa.setWeb(txtWeb.getText());

        String texto = dpFecha.getEditor().getText();
        LocalDate fecha = Fmt.DATE_CONVERTER.fromString(texto);
        if (texto != null && !texto.isBlank() && fecha == null) {
            mostrarError("La fecha debe tener el formato DD/MM/AAAA.");
            return;
        }
        empresa.setFechaRegistro(fecha);

        try {
            service.guardarEmpresa(empresa);
            Navigator.empresas();
        } catch (DataAccessException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        Navigator.empresas();
    }

    private void mostrarError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
        lblError.setManaged(true);
    }

    private void ocultarError() {
        lblError.setVisible(false);
        lblError.setManaged(false);
    }
}
