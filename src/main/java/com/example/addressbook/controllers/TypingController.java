package com.example.addressbook.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.stage.Stage;
import com.example.addressbook.MainMenu;

public class TypingController {

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            MainMenu mainMenu = new MainMenu();
            Scene scene = mainMenu.buildScene(stage);
            stage.setScene(scene);
            stage.setTitle("Main Menu - Typing Ninja");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
