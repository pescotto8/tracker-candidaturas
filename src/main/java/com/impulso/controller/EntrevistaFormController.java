package com.impulso.controller;

import com.impulso.db.DataAccessException;
import com.impulso.model.Entrevista;
import com.impulso.model.Oferta;
import com.impulso.model.ResultadoEntrevista;
import com.impulso.model.TipoEntrevista;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EntrevistaFormController {

    private final ImpulsoService service = ImpulsoService.get();
    private Oferta oferta;
    private Entrevista entrevista;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblContexto;
    @FXML private Label lblError;
    @FXML private Label lblOfertaEstado;
    @FXML private Label lblOfertaTitulo;
    @FXML private Label lblOfertaEmpresa;
    @FXML private Label lblOfertaMeta;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtHora;
    @FXML private ComboBox<TipoEntrevista> cmbTipo;
    @FXML private ComboBox<ResultadoEntrevista> cmbResultado;
    @FXML private TextArea txtNotas;

    @FXML
    private void initialize() {
        dpFecha.setConverter(Fmt.DATE_CONVERTER);
        cmbTipo.setItems(FXCollections.observableArrayList(TipoEntrevista.values()));
        cmbResultado.setItems(FXCollections.observableArrayList(ResultadoEntrevista.values()));
        cmbResultado.setValue(ResultadoEntrevista.PENDIENTE);
    }

    /** @param idEntrevista null = alta; con id = edición. */
    public void cargar(int idOferta, Integer idEntrevista) {
        try {
            oferta = service.oferta(idOferta).orElseThrow(() -> new DataAccessException("La candidatura no existe."));
            lblContexto.setText("🔗  #" + oferta.getId() + " · " + oferta.getTitulo() + " · " + oferta.getEmpresaNombre());
            lblOfertaEstado.setText("● " + oferta.getEstado());
            lblOfertaEstado.getStyleClass().add(oferta.getEstado().getCssClass() + "-pill");
            lblOfertaTitulo.setText(oferta.getTitulo());
            lblOfertaEmpresa.setText(oferta.getEmpresaNombre() + " · " + oferta.getEmpresaSector());
            lblOfertaMeta.setText("Postulación: " + Fmt.fecha(oferta.getFechaPostulacion()) + "\nSalario: "
                    + (oferta.getSalario() == null ? "—" : Fmt.money(oferta.getSalario()) + " brutos/año"));

            if (idEntrevista == null) {
                entrevista = new Entrevista();
                entrevista.setIdOferta(idOferta);
                lblTitulo.setText("Nueva entrevista");
                lblSubtitulo.setText("Añade un nuevo encuentro al proceso de " + oferta.getTitulo() + " en "
                        + oferta.getEmpresaNombre() + ".");
                return;
            }
            entrevista = service.entrevista(idEntrevista)
                    .orElseThrow(() -> new DataAccessException("La entrevista no existe."));
            lblTitulo.setText("Editar entrevista");
            lblSubtitulo.setText("Entrevista " + entrevista.getTipo().toString().toLowerCase() + " · "
                    + Fmt.fecha(entrevista.getFecha())
                    + (entrevista.getHora() == null ? "" : " a las " + Fmt.hora(entrevista.getHora()))
                    + " · " + oferta.getTitulo() + ", " + oferta.getEmpresaNombre());
            dpFecha.setValue(entrevista.getFecha());
            txtHora.setText(Fmt.hora(entrevista.getHora()));
            cmbTipo.setValue(entrevista.getTipo());
            cmbResultado.setValue(entrevista.getResultado());
            txtNotas.setText(entrevista.getNotas() == null ? "" : entrevista.getNotas());
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo cargar el formulario", e.getMessage());
            Navigator.ofertas();
        }
    }

    @FXML
    private void guardar() {
        ocultarError();
        String texto = dpFecha.getEditor().getText();
        LocalDate fecha = Fmt.DATE_CONVERTER.fromString(texto);
        if (texto != null && !texto.isBlank() && fecha == null) {
            mostrarError("La fecha debe tener el formato DD/MM/AAAA.");
            return;
        }
        try {
            entrevista.setHora(Fmt.parseHora(txtHora.getText()));
        } catch (DateTimeParseException e) {
            mostrarError("La hora debe tener el formato HH:mm (p. ej. 11:30).");
            return;
        }
        entrevista.setFecha(fecha);
        entrevista.setTipo(cmbTipo.getValue());
        entrevista.setResultado(cmbResultado.getValue());
        entrevista.setNotas(txtNotas.getText());

        try {
            if (entrevista.getId() == 0) {
                service.guardarEntrevistaYAvanzarEstado(entrevista);
            } else {
                service.guardarEntrevista(entrevista);
            }
            Navigator.detalle(oferta.getId());
        } catch (DataAccessException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        Navigator.detalle(oferta.getId());
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
