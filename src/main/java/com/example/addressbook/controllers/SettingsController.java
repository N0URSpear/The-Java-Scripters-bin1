package com.example.addressbook.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.addressbook.MainMenu;

public class SettingsController {

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            // Get the current stage (the Settings window)
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();

            // Rebuild the Main Menu scene
            MainMenu mainMenu = new MainMenu();
            Scene scene = mainMenu.buildScene(stage);

            // Show Main Menu
            stage.setScene(scene);
            stage.setTitle("Main Menu - Typing Ninja");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
