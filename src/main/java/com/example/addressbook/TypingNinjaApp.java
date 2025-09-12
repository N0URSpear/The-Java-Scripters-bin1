package com.example.addressbook;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;

public class TypingNinjaApp extends Application {

    public static final String TITLE = "TYPING NINJA";
    Rectangle2D screenBounds = Screen.getPrimary().getBounds();
    public double WIDTH = screenBounds.getWidth() * 0.75;
    public double HEIGHT = screenBounds.getHeight() * 0.75;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(TypingNinjaApp.class.getResource("Ninja-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
