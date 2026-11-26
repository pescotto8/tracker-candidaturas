package com.impulso.controller;

import com.impulso.service.ImpulsoService;
import com.impulso.util.Fmt;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.util.List;

/** Marco de la aplicación: menú lateral, barra superior y contenedor de pantallas. */
public class MainController {

    @FXML private Button navOfertas;
    @FXML private Button navEmpresas;
    @FXML private Button navStats;
    @FXML private Label badgeOfertas;
    @FXML private Label badgeEmpresas;
    @FXML private Label lblMigas;
    @FXML private Label lblFecha;
    @FXML private StackPane contentHost;

    @FXML
    private void initialize() {
        Navigator.register(this);
        lblFecha.setText("📅  " + Fmt.FECHA_LARGA.format(LocalDate.now()));
    }

    @FXML private void irOfertas() { Navigator.ofertas(); }
    @FXML private void irEmpresas() { Navigator.empresas(); }
    @FXML private void irStats() { Navigator.estadisticas(); }

    void setContent(Node node, String nav, String crumb) {
        contentHost.getChildren().setAll(node);
        lblMigas.setText(crumb);
        for (Button b : List.of(navOfertas, navEmpresas, navStats)) {
            b.getStyleClass().remove("nav-item-active");
        }
        Button activo = switch (nav) {
            case "empresas" -> navEmpresas;
            case "stats" -> navStats;
            default -> navOfertas;
        };
        activo.getStyleClass().add("nav-item-active");
    }

    StackPane host() {
        return contentHost;
    }

    /** Actualiza los contadores del menú (13 candidaturas, 6 empresas). */
    void refreshCounts() {
        try {
            badgeOfertas.setText(String.valueOf(ImpulsoService.get().totalOfertas()));
            badgeEmpresas.setText(String.valueOf(ImpulsoService.get().listarEmpresas(null).size()));
        } catch (RuntimeException e) {
            Platform.runLater(() -> {
                badgeOfertas.setText("–");
                badgeEmpresas.setText("–");
            });
        }
    }
}
