package com.impulso;

import com.impulso.controller.Navigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {

    private static App instance;
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;
        Parent welcome = FXMLLoader.load(App.class.getResource("/com/impulso/view/welcome.fxml"));
        scene = new Scene(welcome, 1280, 800);
        scene.getStylesheets().add(App.class.getResource("/com/impulso/css/impulso.css").toExternalForm());
        stage.setTitle("Impulso — seguimiento de candidaturas");
        stage.setMinWidth(1000);
        stage.setMinHeight(640);
        stage.setScene(scene);
        stage.show();
    }

    /** Sustituye la pantalla de bienvenida por la aplicación principal. */
    public static void entrarEnLaApp() {
        try {
            Parent main = FXMLLoader.load(App.class.getResource("/com/impulso/view/main.fxml"));
            scene.setRoot(main);
            Navigator.show("oferta-list", "ofertas", "Candidaturas");
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo cargar main.fxml", e);
        }
    }

    public static void abrirUrl(String url) {
        if (url != null && !url.isBlank() && instance != null) {
            String u = url.matches("(?i)^https?://.*") ? url : "https://" + url;
            instance.getHostServices().showDocument(u);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
