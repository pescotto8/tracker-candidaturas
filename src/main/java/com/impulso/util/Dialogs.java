package com.impulso.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Dialogs {
    private Dialogs() {
    }

    public static void error(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.ERROR, mensaje, ButtonType.OK);
        a.setTitle("Impulso");
        a.setHeaderText(titulo);
        a.showAndWait();
    }

    public static void info(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        a.setTitle("Impulso");
        a.setHeaderText(titulo);
        a.showAndWait();
    }

    public static boolean confirmar(String titulo, String mensaje) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, mensaje, ButtonType.OK, ButtonType.CANCEL);
        a.setTitle("Impulso");
        a.setHeaderText(titulo);
        return a.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
    }
}
