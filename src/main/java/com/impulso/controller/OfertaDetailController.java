package com.impulso.controller;

import com.impulso.App;
import com.impulso.db.DataAccessException;
import com.impulso.model.Empresa;
import com.impulso.model.Entrevista;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

public class OfertaDetailController {

    private final ImpulsoService service = ImpulsoService.get();
    private Oferta oferta;
    private boolean cargando;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private Label lblFecha;
    @FXML private Label lblSalario;
    @FXML private Hyperlink linkOferta;
    @FXML private Label lblDescripcion;
    @FXML private Label lblEmpIniciales;
    @FXML private Label lblEmpNombre;
    @FXML private Label lblEmpSector;
    @FXML private Hyperlink linkEmpWeb;
    @FXML private Label lblEmpMeta;
    @FXML private Label lblEntrevistasTitulo;
    @FXML private TableView<Entrevista> tablaEntrevistas;
    @FXML private TableColumn<Entrevista, String> colEFecha;
    @FXML private TableColumn<Entrevista, String> colETipo;
    @FXML private TableColumn<Entrevista, String> colEResultado;
    @FXML private TableColumn<Entrevista, Entrevista> colEAcciones;
    @FXML private Label lblNotas;

    @FXML
    private void initialize() {
        cmbEstado.setItems(FXCollections.observableArrayList(Estado.values()));
        cmbEstado.setOnAction(e -> cambiarEstado());

        colEFecha.setCellValueFactory(c -> {
            Entrevista en = c.getValue();
            String h = en.getHora() == null ? "" : " · " + Fmt.hora(en.getHora());
            return new SimpleStringProperty(Fmt.fecha(en.getFecha()) + h);
        });
        colETipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipo().toString()));
        colEResultado.setCellValueFactory(c -> new SimpleStringProperty("●  " + c.getValue().getResultado()));
        colEAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colEAcciones.setCellFactory(col -> new AccionesEntrevistaCell());
        tablaEntrevistas.setPlaceholder(new Label("Todavía no hay entrevistas."));
    }

    /** Carga (o recarga) la ficha completa: oferta + empresa (JOIN) + entrevistas. */
    public void cargar(int idOferta) {
        try {
            oferta = service.oferta(idOferta).orElse(null);
            if (oferta == null) {
                Dialogs.error("Candidatura no encontrada", "La candidatura #" + idOferta + " ya no existe.");
                Navigator.ofertas();
                return;
            }
            Empresa emp = service.empresa(oferta.getIdEmpresa()).orElse(null);

            lblTitulo.setText(oferta.getTitulo());
            lblSubtitulo.setText("Candidatura #" + oferta.getId() + " · " + oferta.getEmpresaNombre());
            cargando = true;
            cmbEstado.setValue(oferta.getEstado());
            pintarEstado(oferta.getEstado());
            cargando = false;
            lblFecha.setText(Fmt.fecha(oferta.getFechaPostulacion()));
            lblSalario.setText(oferta.getSalario() == null ? "—" : Fmt.money(oferta.getSalario()) + " / año");
            boolean hayEnlace = oferta.getEnlace() != null;
            linkOferta.setText(hayEnlace ? oferta.getEnlace().replaceFirst("(?i)^https?://", "") : "—");
            linkOferta.setDisable(!hayEnlace);
            lblDescripcion.setText(vacio(oferta.getDescripcion(), "Sin descripción."));
            lblNotas.setText(vacio(oferta.getNotas(), "Sin notas."));

            if (emp != null) {
                lblEmpIniciales.setText(emp.getIniciales());
                lblEmpNombre.setText(emp.getNombre());
                lblEmpSector.setText(emp.getSector());
                linkEmpWeb.setText(emp.getWeb() == null ? "—" : emp.getWeb().replaceFirst("(?i)^https?://", ""));
                linkEmpWeb.setDisable(emp.getWeb() == null);
                lblEmpMeta.setText("Registrada el " + Fmt.fecha(emp.getFechaRegistro()) + " · "
                        + emp.getNumCandidaturas() + (emp.getNumCandidaturas() == 1 ? " candidatura" : " candidaturas"));
            }
            recargarEntrevistas();
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo cargar la candidatura", e.getMessage());
        }
    }

    private void recargarEntrevistas() {
        var lista = service.entrevistasDe(oferta.getId());
        tablaEntrevistas.setItems(FXCollections.observableArrayList(lista));
        tablaEntrevistas.setPrefHeight(48 + Math.max(lista.size(), 2) * 40.0);
        lblEntrevistasTitulo.setText("Entrevistas (" + lista.size() + ")");
    }

    private void cambiarEstado() {
        if (cargando || oferta == null || cmbEstado.getValue() == null || cmbEstado.getValue() == oferta.getEstado()) {
            return;
        }
        try {
            service.cambiarEstado(oferta.getId(), cmbEstado.getValue());
            oferta.setEstado(cmbEstado.getValue());
            pintarEstado(oferta.getEstado());
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo cambiar el estado", e.getMessage());
            cargando = true;
            cmbEstado.setValue(oferta.getEstado());
            cargando = false;
        }
    }

    private void pintarEstado(Estado e) {
        cmbEstado.getStyleClass().removeIf(s -> s.startsWith("estado-") && !s.equals("estado-combo"));
        cmbEstado.getStyleClass().add(e.getCssClass());
    }

    private static String vacio(String s, String def) {
        return s == null || s.isBlank() ? def : s;
    }

    @FXML private void volver() { Navigator.ofertas(); }
    @FXML private void editar() { Navigator.formOferta(oferta.getId()); }
    @FXML private void nuevaEntrevista() { Navigator.formEntrevista(oferta.getId(), null); }
    @FXML private void abrirOferta() { App.abrirUrl(oferta.getEnlace()); }
    @FXML private void abrirEmpresaWeb() { App.abrirUrl(oferta.getEmpresaWeb()); }

    @FXML
    private void eliminar() {
        if (!Dialogs.confirmar("Eliminar candidatura",
                "¿Eliminar «" + oferta.getTitulo() + "» y sus entrevistas? Esta acción no se puede deshacer.")) {
            return;
        }
        try {
            service.eliminarOferta(oferta.getId());
            Navigator.ofertas();
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo eliminar", e.getMessage());
        }
    }

    private class AccionesEntrevistaCell extends TableCell<Entrevista, Entrevista> {
        private final HBox box = new HBox(12);
        private final Hyperlink editar = new Hyperlink("Editar");
        private final Hyperlink borrar = new Hyperlink("Eliminar");

        AccionesEntrevistaCell() {
            box.setAlignment(Pos.CENTER_LEFT);
            borrar.getStyleClass().add("link-danger");
            box.getChildren().addAll(editar, borrar);
            editar.setOnAction(e -> Navigator.formEntrevista(oferta.getId(), getItem().getId()));
            borrar.setOnAction(e -> {
                if (Dialogs.confirmar("Eliminar entrevista", "¿Eliminar esta entrevista?")) {
                    try {
                        service.eliminarEntrevista(getItem().getId());
                        recargarEntrevistas();
                    } catch (DataAccessException ex) {
                        Dialogs.error("No se pudo eliminar", ex.getMessage());
                    }
                }
            });
        }

        @Override
        protected void updateItem(Entrevista item, boolean empty) {
            super.updateItem(item, empty);
            setGraphic(empty || item == null ? null : box);
        }
    }
}
