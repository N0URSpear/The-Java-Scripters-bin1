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
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Username cannot be Empty!");
                alert.showAndWait();
                return;
            }
            ninja = NinjaDAO.getNinjaUser(username);
            if (ninja == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Username cannot be Found!");
                alert.showAndWait();
                return;
            }
            setSecretMessage(ninja.getSecretQuestion1(), ninja.getSecretQuestion2());
        }
        if (stageNumber == 2) {
            if (SecretQuestion1Answer.getText().isBlank() || SecretQuestion2Answer.getText().isBlank()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Please answer the secret questions!");
                alert.showAndWait();
                return;
            }
            if (!BCrypt.checkpw(SecretQuestion1Answer.getText(),ninja.getSecretQuestion1Answer()) || !BCrypt.checkpw(SecretQuestion2Answer.getText(), ninja.getSecretQuestion2Answer())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Your answer to Secret Question 1 or 2 is wrong!");
                alert.showAndWait();
                return;
            }
        }
        if (stageNumber == 3) {
            if (passwordField.getText().isBlank() || passwordField.getText() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Please enter a password!");
                alert.showAndWait();
                return;
            }
            if (confirmPasswordField.getText().isBlank() || confirmPasswordField.getText() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Please confirm your password!");
                alert.showAndWait();
                return;
            }
            if (!Objects.equals(passwordField.getText(), confirmPasswordField.getText())) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Forgot Password Error!");
                alert.setContentText("Passwords do not match!");
                alert.showAndWait();
                return;
            }
            ninja.setPasswordHash(BCrypt.hashpw(confirmPasswordField.getText(),BCrypt.gensalt()));

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Forgot Password");
            alert.setHeaderText(null);
            alert.setContentText("Password changed successfully!");
            alert.showAndWait();

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
            case 1: getUsername.setDisable(false);
                    getSecretQuestions.setDisable(true);
                    changePassword.setDisable(true);
                    break;

            case 2: getUsername.setDisable(true);
                    getSecretQuestions.setDisable(false);
                    changePassword.setDisable(true);
                    break;

            case 3: getUsername.setDisable(true);
                    getSecretQuestions.setDisable(true);
                    changePassword.setDisable(false);
                    break;
        }
    }
}
