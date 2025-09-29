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

public class ActiveLessonPage extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("LessonActivePage.fxml"));
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        double WIDTH = screenBounds.getWidth() * 0.75;
        double HEIGHT = screenBounds.getHeight() * 0.75;

        // Create and Display the scene
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setTitle("TYPING NINJA");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
    launch(args);
    }
}
