package com.impulso.controller;

import com.impulso.App;
import com.impulso.db.DataAccessException;
import com.impulso.util.Dialogs;
import javafx.fxml.FXML;

public class WelcomeController {

    @FXML
    private void entrar() {
        try {
            App.entrarEnLaApp();
        } catch (DataAccessException | IllegalStateException e) {
            Dialogs.error("No se pudo abrir la aplicación",
                    "Comprueba la conexión a la base de datos (config.properties) y que has ejecutado schema.sql.\n\n"
                            + e.getMessage());
        }
    }
}
