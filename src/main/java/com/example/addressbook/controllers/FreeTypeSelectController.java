package com.example.addressbook.controllers;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

public class FreeTypeSelectController {

    @FXML private Node root; // from fx:id="root" in FXML

    private String lessonType = "FreeWeakKeys"; // default selection per spec
    private int durationMinutes = 3;            // default selection per spec

    // Exercise selection
    @FXML private void pickWeakKeys() { lessonType = "FreeWeakKeys"; }
    @FXML private void pickAnything() { lessonType = "FreeAnything"; }

    // Duration selection
    @FXML private void dur1()  { durationMinutes = 1; }
    @FXML private void dur3()  { durationMinutes = 3; }
    @FXML private void dur5()  { durationMinutes = 5; }
    @FXML private void dur10() { durationMinutes = 10; }

    @FXML
    private void handleBack() {
        close();
    }

    @FXML
    private void handleGenerate() {
        int userId = 1; // TODO: replace with Session.getCurrentUserId()
        System.out.println("FreeType -> type=" + lessonType + ", duration=" + durationMinutes + ", userId=" + userId);
        // TODO: insert into DB
        close();
    }

    private void close() {
        Stage stage = (Stage) root.getScene().getWindow();
        stage.close();
    }
}
