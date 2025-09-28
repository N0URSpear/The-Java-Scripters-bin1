package com.example.addressbook.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CustomTopicSelectController {

    @FXML private TextField topicField;
    @FXML private CheckBox upperCase;
    @FXML private CheckBox numbers;
    @FXML private CheckBox punctuation;
    @FXML private CheckBox specialChars;

    private int durationMinutes = 3;

    @FXML private void dur1()  { durationMinutes = 1; }
    @FXML private void dur3()  { durationMinutes = 3; }
    @FXML private void dur5()  { durationMinutes = 5; }
    @FXML private void dur10() { durationMinutes = 10; }

    @FXML private void handleBack() { close(); }

    @FXML private void handleGenerate() {
        int userId = 1; // TODO: Session.getCurrentUserId()
        String topic = topicField.getText() == null ? "" : topicField.getText().trim();
        boolean u = upperCase.isSelected();
        boolean n = numbers.isSelected();
        boolean p = punctuation.isSelected();
        boolean s = specialChars.isSelected();
        System.out.println("CustomAI -> topic=" + topic + ", duration=" + durationMinutes +
                ", U=" + u + ", N=" + n + ", P=" + p + ", S=" + s + ", userId=" + userId);
        // TODO: write to DB
        close();
    }

    private void close() {
        Stage stage = (Stage) topicField.getScene().getWindow();
        stage.close();
    }
}
