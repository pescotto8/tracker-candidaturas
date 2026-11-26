package com.impulso.controller;

import com.impulso.App;
import com.impulso.db.DataAccessException;
import com.impulso.model.Empresa;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.List;

public class EmpresaListController {

    private final ImpulsoService service = ImpulsoService.get();

    @FXML private TextField txtBuscar;
    @FXML private Label lblResumen;
    @FXML private Label lblMostrando;
    @FXML private TableView<Empresa> tabla;
    @FXML private TableColumn<Empresa, Empresa> colNombre;
    @FXML private TableColumn<Empresa, String> colSector;
    @FXML private TableColumn<Empresa, Empresa> colWeb;
    @FXML private TableColumn<Empresa, String> colRegistro;
    @FXML private TableColumn<Empresa, String> colNum;
    @FXML private TableColumn<Empresa, Empresa> colAcciones;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colNombre.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(e.getIniciales());
                badge.getStyleClass().add("company-badge");
                Label name = new Label(e.getNombre());
                name.getStyleClass().add("strong");
                HBox box = new HBox(12, badge, name);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });
        colSector.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSector()));
        colWeb.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colWeb.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                if (empty || e == null || e.getWeb() == null) {
                    setGraphic(null);
                    return;
                }
                Hyperlink link = new Hyperlink(e.getWeb().replaceFirst("(?i)^https?://", ""));
                link.setOnAction(ev -> App.abrirUrl(e.getWeb()));
                setGraphic(link);
            }
        });
        colRegistro.setCellValueFactory(c -> new SimpleStringProperty(Fmt.fecha(c.getValue().getFechaRegistro())));
        colNum.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNumCandidaturas())));
        colNum.getStyleClass().add("col-strong");
        colAcciones.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final HBox box = new HBox(12);
            private final Hyperlink editar = new Hyperlink("Editar");
            private final Hyperlink borrar = new Hyperlink("Eliminar");

            {
                box.setAlignment(Pos.CENTER_LEFT);
                borrar.getStyleClass().add("link-danger");
                box.getChildren().addAll(editar, borrar);
                editar.setOnAction(ev -> Navigator.formEmpresa(getItem().getId()));
                borrar.setOnAction(ev -> eliminar(getItem()));
            }

            @Override
            protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setGraphic(empty || e == null ? null : box);
            }
        });

        tabla.setPlaceholder(new Label("No hay empresas que coincidan."));
        txtBuscar.textProperty().addListener((obs, a, b) -> cargar());
        cargar();
    }

    private void cargar() {
        try {
            List<Empresa> lista = service.listarEmpresas(txtBuscar.getText());
            tabla.setItems(FXCollections.observableArrayList(lista));
            int cands = lista.stream().mapToInt(Empresa::getNumCandidaturas).sum();
            lblResumen.setText(lista.size() + " empresas · " + cands + " candidaturas asociadas");
            lblMostrando.setText("Mostrando " + (lista.isEmpty() ? 0 : 1) + "–" + lista.size() + " de " + lista.size() + " empresas");
        } catch (DataAccessException e) {
            Dialogs.error("No se pudieron cargar las empresas", e.getMessage());
        }
    }

    @FXML
    private void nueva() {
        Navigator.formEmpresa(null);
    }

    private void eliminar(Empresa e) {
        String aviso = e.getNumCandidaturas() == 0
                ? "¿Eliminar la empresa «" + e.getNombre() + "»?"
                : "«" + e.getNombre() + "» tiene " + e.getNumCandidaturas()
                  + " candidatura(s). Si continúas se eliminarán también ellas y sus entrevistas. ¿Continuar?";
        if (!Dialogs.confirmar("Eliminar empresa", aviso)) {
            return;
        }
        try {
            service.eliminarEmpresa(e.getId());
            Navigator.empresas();
        } catch (DataAccessException ex) {
            Dialogs.error("No se pudo eliminar la empresa", ex.getMessage());
        }
    }
}
