package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.NinjaUser;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import java.util.Objects;

public class ForgotPasswordController {
    @FXML private VBox getUsername;
    @FXML private VBox getSecretQuestions;
    @FXML private VBox changePassword;
    @FXML private TextField usernameField;
    @FXML private Text secretQuestion1;
    @FXML private Text secretQuestion2;
    @FXML private TextField SecretQuestion1Answer;
    @FXML private TextField SecretQuestion2Answer;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    private int stageNumber = 1;
    private NinjaUser ninja;
    private final INinjaContactDAO NinjaDAO;
    public ForgotPasswordController() {this.NinjaDAO = new SqliteContactDAO();}

    @FXML public void initialize() {
        getStage(stageNumber);
    }

    @FXML
    private void onConfirmClicked() {
        if (stageNumber == 1) {
            String username = usernameField.getText();
            if (username == null || username.isBlank()) {
                showError("Username cannot be Empty!");
                return;
            }
            ninja = NinjaDAO.getNinjaUser(username);
            if (ninja == null) {
                showError("Username cannot be Found!");
                return;
            }
            setSecretMessage(ninja.getSecretQuestion1(), ninja.getSecretQuestion2());
        }
        if (stageNumber == 2) {
            if (SecretQuestion1Answer.getText().isBlank() || SecretQuestion2Answer.getText().isBlank()) {
                showError("Please answer the secret questions!");
                return;
            }
            if (!BCrypt.checkpw(SecretQuestion1Answer.getText(),ninja.getSecretQuestion1Answer()) || !BCrypt.checkpw(SecretQuestion2Answer.getText(), ninja.getSecretQuestion2Answer())) {
                showError("Your answer to Secret Question 1 or 2 is wrong!");
                return;
            }
        }
        if (stageNumber == 3) {
            if (passwordField.getText().isBlank() || passwordField.getText() == null) {
                showError("Please enter a password!");
                return;
            }
            if (confirmPasswordField.getText().isBlank() || confirmPasswordField.getText() == null) {
                showError("Please confirm your password!");
                return;
            }
            if (!Objects.equals(passwordField.getText(), confirmPasswordField.getText())) {
                showError("Passwords do not match!");
                return;
            }

            ninja.setPasswordHash(BCrypt.hashpw(confirmPasswordField.getText(),BCrypt.gensalt()));
            NinjaDAO.updateNinjaUser(ninja);

            showSuccess("Password changed successfully!");

            Stage stage = (Stage) getUsername.getScene().getWindow();
            stage.close();
            return;
        }

        stageNumber++;
        getStage(stageNumber);
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) getUsername.getScene().getWindow();
        stage.close();
    }

    private void setSecretMessage(String message1, String message2) {
        secretQuestion1.setText(message1);
        secretQuestion2.setText(message2);
    }

    private void getStage(int number) {
        switch (number) {
            case 1: getUsername.setVisible(true);
                    getSecretQuestions.setVisible(false);
                    changePassword.setVisible(false);
                    break;

            case 2: getUsername.setDisable(true);
                    getSecretQuestions.setVisible(true);
                    changePassword.setVisible(false);
                    break;

            case 3: getUsername.setDisable(true);
                    getSecretQuestions.setDisable(true);
                    changePassword.setVisible(true);
                    break;
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
