package com.impulso.controller;

import com.impulso.db.DataAccessException;
import com.impulso.model.Estado;
import com.impulso.model.OfertaFilter;
import com.impulso.service.ImpulsoService;
import com.impulso.util.Dialogs;
import com.impulso.util.Fmt;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Map;

public class StatsController {

    private final ImpulsoService service = ImpulsoService.get();

    @FXML private Button btnTodo;
    @FXML private Button btnMes;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private Label lblError;
    @FXML private Label kpiTotal;
    @FXML private Label kpiExito;
    @FXML private Label kpiExitoDetalle;
    @FXML private Label kpiActivos;
    @FXML private Label kpiEmpresas;
    @FXML private VBox boxEmpresas;
    @FXML private BarChart<String, Number> chartMeses;
    @FXML private Label lblTotalEstados;
    @FXML private HBox boxEstados;

    @FXML
    private void initialize() {
        dpDesde.setConverter(Fmt.DATE_CONVERTER);
        dpHasta.setConverter(Fmt.DATE_CONVERTER);
        todoElPeriodo();
    }

    @FXML
    private void todoElPeriodo() {
        dpDesde.setValue(null);
        dpHasta.setValue(null);
        marcar(btnTodo);
        refrescar(new OfertaFilter());
    }

    @FXML
    private void esteMes() {
        LocalDate hoy = LocalDate.now();
        dpDesde.setValue(hoy.withDayOfMonth(1));
        dpHasta.setValue(hoy);
        marcar(btnMes);
        OfertaFilter f = new OfertaFilter();
        f.desde = dpDesde.getValue();
        f.hasta = dpHasta.getValue();
        refrescar(f);
    }

    @FXML
    private void aplicar() {
        lblError.setVisible(false);
        lblError.setManaged(false);
        LocalDate desde = Fmt.DATE_CONVERTER.fromString(dpDesde.getEditor().getText());
        LocalDate hasta = Fmt.DATE_CONVERTER.fromString(dpHasta.getEditor().getText());
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            lblError.setText("«Desde» no puede ser posterior a «Hasta».");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }
        marcar(null);
        OfertaFilter f = new OfertaFilter();
        f.desde = desde;
        f.hasta = hasta;
        refrescar(f);
    }

    private void marcar(Button activo) {
        for (Button b : new Button[]{btnTodo, btnMes}) {
            b.getStyleClass().remove("btn-toggle-active");
        }
        if (activo != null) {
            activo.getStyleClass().add("btn-toggle-active");
        }
    }

    private void refrescar(OfertaFilter f) {
        try {
            Map<String, Integer> porEstado = service.porEstado(f);
            Map<String, Integer> porEmpresa = service.porEmpresa(f);
            Map<String, Integer> porMes = service.porMes(f);

            int total = porEstado.values().stream().mapToInt(Integer::intValue).sum();
            int aceptadas = porEstado.getOrDefault(Estado.ACEPTADO.name(), 0);
            int activos = porEstado.getOrDefault(Estado.PENDIENTE.name(), 0)
                    + porEstado.getOrDefault(Estado.ENTREVISTA.name(), 0);

            kpiTotal.setText(String.valueOf(total));
            kpiExito.setText(total == 0 ? "0,0 %" : Fmt.percent(aceptadas / (double) total));
            kpiExitoDetalle.setText(aceptadas + " aceptadas / " + total);
            kpiActivos.setText(String.valueOf(activos));
            kpiEmpresas.setText(String.valueOf(porEmpresa.size()));

            // Barras por empresa
            boxEmpresas.getChildren().clear();
            int max = porEmpresa.values().stream().mapToInt(Integer::intValue).max().orElse(1);
            porEmpresa.forEach((nombre, n) -> boxEmpresas.getChildren().add(fila(nombre, n, n / (double) max)));

            // Gráfico por mes
            XYChart.Series<String, Number> serie = new XYChart.Series<>();
            porMes.forEach((ym, n) -> serie.getData().add(new XYChart.Data<>(etiquetaMes(ym), n)));
            chartMeses.getData().setAll(java.util.List.of(serie));

            // Distribución de estados
            lblTotalEstados.setText("Total: " + total + " · mismo conjunto que el listado");
            boxEstados.getChildren().clear();
            for (Estado e : Estado.values()) {
                int n = porEstado.getOrDefault(e.name(), 0);
                double frac = total == 0 ? 0 : n / (double) total;
                Label pill = new Label("● " + e);
                pill.getStyleClass().addAll("pill", e.getCssClass() + "-pill");
                Label valor = new Label(n + " · " + Fmt.percent(frac));
                valor.getStyleClass().add("strong");
                Region espacio = new Region();
                HBox.setHgrow(espacio, Priority.ALWAYS);
                HBox cab = new HBox(pill, espacio, valor);
                cab.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                ProgressBar barra = new ProgressBar(frac);
                barra.setMaxWidth(Double.MAX_VALUE);
                barra.getStyleClass().add(e.getCssClass() + "-bar");
                VBox col = new VBox(8, cab, barra);
                HBox.setHgrow(col, Priority.ALWAYS);
                boxEstados.getChildren().add(col);
            }
        } catch (DataAccessException e) {
            Dialogs.error("No se pudieron calcular las estadísticas", e.getMessage());
        }
    }

    private static HBox fila(String nombre, int n, double frac) {
        Label etiqueta = new Label(nombre);
        etiqueta.setMinWidth(90);
        ProgressBar barra = new ProgressBar(frac);
        barra.setMaxWidth(Double.MAX_VALUE);
        barra.getStyleClass().add("company-bar");
        HBox.setHgrow(barra, Priority.ALWAYS);
        Label valor = new Label(String.valueOf(n));
        valor.getStyleClass().add("strong");
        valor.setMinWidth(20);
        HBox fila = new HBox(12, etiqueta, barra, valor);
        fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return fila;
    }

    /** "2026-04" -&gt; "Abr 26". */
    private static String etiquetaMes(String ym) {
        YearMonth m = YearMonth.parse(ym);
        String mes = m.getMonth().getDisplayName(TextStyle.SHORT, Fmt.ES).replace(".", "");
        return Character.toUpperCase(mes.charAt(0)) + mes.substring(1) + " " + String.valueOf(m.getYear()).substring(2);
    }
}
