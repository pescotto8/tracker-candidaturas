package com.impulso.controller;

import com.impulso.db.DataAccessException;
import com.impulso.model.Empresa;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
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
import java.util.List;

/** Formulario de alta y edición de candidaturas (con validación). */
public class OfertaFormController {

    private final ImpulsoService service = ImpulsoService.get();
    private Oferta oferta;

    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblNotas;
    @FXML private Label lblError;
    @FXML private TextField txtTitulo;
    @FXML private ComboBox<Empresa> cmbEmpresa;
    @FXML private ComboBox<Estado> cmbEstado;
    @FXML private DatePicker dpFecha;
    @FXML private TextField txtSalario;
    @FXML private TextField txtEnlace;
    @FXML private TextArea txtDescripcion;
    @FXML private TextArea txtNotas;

    @FXML
    private void initialize() {
        dpFecha.setConverter(Fmt.DATE_CONVERTER);
        cmbEstado.setItems(FXCollections.observableArrayList(Estado.values()));
    }

    /** @param idOferta null = alta; con id = edición. */
    public void cargar(Integer idOferta) {
        try {
            cmbEmpresa.setItems(FXCollections.observableArrayList(service.listarEmpresas(null)));
            if (idOferta == null) {
                oferta = new Oferta();
                lblTitulo.setText("Nueva candidatura");
                lblSubtitulo.setText("Registra una nueva oportunidad y empieza a seguir su evolución.");
                cmbEstado.setValue(Estado.PENDIENTE);
                dpFecha.setValue(LocalDate.now());
                lblNotas.setText("Notas iniciales");
                return;
            }
            oferta = service.oferta(idOferta).orElseThrow(() -> new DataAccessException("La candidatura no existe."));
            lblTitulo.setText("Editar candidatura");
            lblSubtitulo.setText(oferta.getTitulo() + " · " + oferta.getEmpresaNombre() + " · Referencia #" + oferta.getId());
            lblNotas.setText("Notas de seguimiento");
            txtTitulo.setText(oferta.getTitulo());
            cmbEmpresa.getItems().stream().filter(e -> e.getId() == oferta.getIdEmpresa())
                    .findFirst().ifPresent(cmbEmpresa::setValue);
            cmbEstado.setValue(oferta.getEstado());
            dpFecha.setValue(oferta.getFechaPostulacion());
            txtSalario.setText(oferta.getSalario() == null ? "" : Fmt.money(oferta.getSalario()).replace(" €", ""));
            txtEnlace.setText(oferta.getEnlace() == null ? "" : oferta.getEnlace());
            txtDescripcion.setText(oferta.getDescripcion() == null ? "" : oferta.getDescripcion());
            txtNotas.setText(oferta.getNotas() == null ? "" : oferta.getNotas());
        } catch (DataAccessException e) {
            Dialogs.error("No se pudo cargar el formulario", e.getMessage());
            Navigator.ofertas();
        }
    }

    @FXML
    private void guardar() {
        ocultarError();
        Oferta o = oferta;
        o.setTitulo(txtTitulo.getText() == null ? "" : txtTitulo.getText().trim());
        o.setIdEmpresa(cmbEmpresa.getValue() == null ? 0 : cmbEmpresa.getValue().getId());
        o.setEstado(cmbEstado.getValue());
        o.setEnlace(txtEnlace.getText());
        o.setDescripcion(txtDescripcion.getText());
        o.setNotas(txtNotas.getText());

        // El texto del DatePicker se parsea a mano para detectar fechas mal escritas.
        String textoFecha = dpFecha.getEditor().getText();
        LocalDate fecha = Fmt.DATE_CONVERTER.fromString(textoFecha);
        if (textoFecha != null && !textoFecha.isBlank() && fecha == null) {
            mostrarError("La fecha debe tener el formato DD/MM/AAAA.");
            return;
        }
        o.setFechaPostulacion(fecha);

        try {
            o.setSalario(Fmt.parseMoney(txtSalario.getText()));
        } catch (NumberFormatException e) {
            mostrarError("El salario debe ser un importe numérico (p. ej. 32000).");
            return;
        }

        try {
            boolean nueva = o.getId() == 0;
            service.guardarOferta(o);
            if (nueva) {
                Navigator.ofertas();
            } else {
                Navigator.detalle(o.getId());
            }
        } catch (DataAccessException e) {
            mostrarError(e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        if (oferta != null && oferta.getId() != 0) {
            Navigator.detalle(oferta.getId());
        } else {
            Navigator.ofertas();
        }
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
