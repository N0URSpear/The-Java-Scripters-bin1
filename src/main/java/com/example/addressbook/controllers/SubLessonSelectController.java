package com.example.addressbook.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SubLessonSelectController {

    @FXML private Label titleLabel;

    @FXML private void choose1a() { onChoose("1a"); }
    @FXML private void choose1b() { onChoose("1b"); }
    @FXML private void choose1c() { onChoose("1c"); }
    @FXML private void choose1d() { onChoose("1d"); }
    @FXML private void choose1e() { onChoose("1e"); }
    @FXML private void choose1f() { onChoose("1f"); }

    @FXML private void closeFromBack() { closeWindow(); }

    private void onChoose(String lessonType) {
        int userId = 1; // TODO: replace with Session.getCurrentUserId()
        System.out.println("Selected Lesson: " + lessonType + " for UserID: " + userId);
        // TODO: insert selection into DB here
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleLabel.getScene().getWindow();
        stage.close();
    }
}
