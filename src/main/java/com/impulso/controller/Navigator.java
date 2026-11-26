package com.impulso.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.io.IOException;

/**
 * Navegación entre pantallas: carga cada .fxml con FXMLLoader dentro del StackPane central
 * de main.fxml y devuelve el controlador para que quien navega le pase datos.
 */
public final class Navigator {
    private static MainController main;

    private Navigator() {
    }

    static void register(MainController controller) {
        main = controller;
    }

    /**
     * @param view  nombre del fichero sin extensión (p. ej. "oferta-list")
     * @param nav   entrada del menú lateral a resaltar: "ofertas", "empresas" o "stats"
     * @param crumb texto de la barra superior (migas de pan)
     */
    public static <T> T show(String view, String nav, String crumb) {
        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource("/com/impulso/view/" + view + ".fxml"));
            Node node = loader.load();
            main.setContent(node, nav, crumb);
            main.refreshCounts();
            return loader.getController();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar la vista " + view, e);
        }
    }

    // Atajos tipados
    public static OfertaListController ofertas() {
        return show("oferta-list", "ofertas", "Candidaturas");
    }

    public static OfertaDetailController detalle(int idOferta) {
        OfertaDetailController c = show("oferta-detail", "ofertas", "Candidaturas  ›  Candidatura #" + idOferta);
        c.cargar(idOferta);
        return c;
    }

    public static OfertaFormController formOferta(Integer idOferta) {
        String crumb = idOferta == null ? "Candidaturas  ›  Nueva candidatura"
                : "Candidaturas  ›  Candidatura #" + idOferta + " / Editar";
        OfertaFormController c = show("oferta-form", "ofertas", crumb);
        c.cargar(idOferta);
        return c;
    }

    public static EntrevistaFormController formEntrevista(int idOferta, Integer idEntrevista) {
        String crumb = "Candidaturas  ›  Candidatura #" + idOferta + (idEntrevista == null ? " / Nueva entrevista"
                : " / Editar entrevista");
        EntrevistaFormController c = show("entrevista-form", "ofertas", crumb);
        c.cargar(idOferta, idEntrevista);
        return c;
    }

    public static EmpresaListController empresas() {
        return show("empresa-list", "empresas", "Empresas");
    }

    public static EmpresaFormController formEmpresa(Integer idEmpresa) {
        String crumb = idEmpresa == null ? "Empresas  ›  Nueva empresa" : "Empresas  ›  Editar";
        EmpresaFormController c = show("empresa-form", "empresas", crumb);
        c.cargar(idEmpresa);
        return c;
    }

    public static StatsController estadisticas() {
        return show("stats", "stats", "Estadísticas");
    }

    /** Para que las vistas puedan mostrar el contenedor sin acoplarse a MainController. */
    public static StackPane host() {
        return main.host();
    }
}
