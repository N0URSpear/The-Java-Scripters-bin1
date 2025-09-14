package com.example.addressbook;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import java.io.IOException;

public class TypingNinjaApp extends Application {

    public static final String TITLE = "TYPING NINJA";

    @Override
    public void start(Stage stage) throws IOException {

        // Load font
        Font font = Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/Jaro-Regular-VariableFont_opsz.ttf"), 14);

        // Load FXML
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/addressbook/Ninja-view.fxml"));
        Parent root = fxmlLoader.load();

        // Calculate window size
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double WIDTH = screenBounds.getWidth() * 0.75;
        double HEIGHT = screenBounds.getHeight() * 0.75;

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
