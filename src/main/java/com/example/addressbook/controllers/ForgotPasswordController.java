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
    private boolean testMode = false;
    private int stageNumber = 1;
    private NinjaUser ninja;
    private final INinjaContactDAO NinjaDAO;
    public ForgotPasswordController() {this.NinjaDAO = new SqliteContactDAO();}
    public ForgotPasswordController(INinjaContactDAO mockDAO) {
        this.NinjaDAO = mockDAO;
    }

    public enum ForgotPasswordResult {
        SUCCESS,
        EMPTY_USERNAME,
        USER_NOT_FOUND,
        EMPTY_SECRET_ANSWERS,
        WRONG_ANSWERS,
        EMPTY_PASSWORD,
        EMPTY_CONFIRMATION,
        PASSWORDS_MISMATCH
    }

    private String getMessageForResult(ForgotPasswordResult result) {
        return switch (result) {
            case EMPTY_USERNAME -> "Username cannot be empty!";
            case USER_NOT_FOUND -> "Username not found!";
            case EMPTY_SECRET_ANSWERS -> "Please answer the secret questions!";
            case WRONG_ANSWERS -> "Your answer to Secret Question 1 or 2 is wrong!";
            case EMPTY_PASSWORD -> "Please enter a password!";
            case EMPTY_CONFIRMATION -> "Please confirm your password!";
            case PASSWORDS_MISMATCH -> "Passwords do not match!";
            default -> "Unknown error";
        };
    }

    // --- Validation methods for unit tests ---
    public ForgotPasswordResult validateStage1(String username) {
        if (username == null || username.isBlank()) {
            return ForgotPasswordResult.EMPTY_USERNAME;
        }
        ninja = NinjaDAO.getNinjaUser(username);
        if (ninja == null) {
            return ForgotPasswordResult.USER_NOT_FOUND;
        }
        return ForgotPasswordResult.SUCCESS;
    }

    public ForgotPasswordResult validateStage2(String answer1, String answer2) {
        if (answer1 == null || answer1.isBlank() || answer2 == null || answer2.isBlank()) {
            return ForgotPasswordResult.EMPTY_SECRET_ANSWERS;
        }
        if (!BCrypt.checkpw(answer1, ninja.getSecretQuestion1Answer())
                || !BCrypt.checkpw(answer2, ninja.getSecretQuestion2Answer())) {
            return ForgotPasswordResult.WRONG_ANSWERS;
        }
        return ForgotPasswordResult.SUCCESS;
    }

    public ForgotPasswordResult validateStage3(String password, String confirmPassword) {
        if (password == null || password.isBlank()) {
            return ForgotPasswordResult.EMPTY_PASSWORD;
        }
        if (confirmPassword == null || confirmPassword.isBlank()) {
            return ForgotPasswordResult.EMPTY_CONFIRMATION;
        }
        if (!Objects.equals(password, confirmPassword)) {
            return ForgotPasswordResult.PASSWORDS_MISMATCH;
        }
        // update password
        ninja.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        NinjaDAO.updateNinjaUser(ninja);
        return ForgotPasswordResult.SUCCESS;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @FXML public void initialize() {
        getStage(stageNumber);
    }

    @FXML
    private void onConfirmClicked() {
        ForgotPasswordResult result;
        switch (stageNumber) {
            case 1:
                result = validateStage1(usernameField.getText().trim());
                if (result == ForgotPasswordResult.SUCCESS) {
                    setSecretMessage(ninja.getSecretQuestion1(), ninja.getSecretQuestion2());
                }
                break;

            case 2:
                result = validateStage2(SecretQuestion1Answer.getText().trim(), SecretQuestion2Answer.getText().trim());
                break;

            case 3:
                result = validateStage3(passwordField.getText().trim(), confirmPasswordField.getText().trim());
                break;

            default:
                result = ForgotPasswordResult.SUCCESS;
        }

        if (result != ForgotPasswordResult.SUCCESS) {
            // Show readable error message
            showAlert(Alert.AlertType.ERROR,"Forgot Password Error!",getMessageForResult(result));
            return;
        }

        // If successful and on stage 3, finish flow and close
        if (stageNumber == 3) {
            showAlert(Alert.AlertType.INFORMATION,"Forgot Password","Password changed successfully!");

            if (getUsername != null && getUsername.getScene() != null) {
                Stage stage = (Stage) getUsername.getScene().getWindow();
                stage.close();
            }
            return;
        }

        // advance to next stage and update UI
        stageNumber++;
        getStage(stageNumber);
    }

    @FXML
    private void onCancelClicked() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    private void setSecretMessage(String message1, String message2) {
        secretQuestion1.setText(message1);
        secretQuestion2.setText(message2);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        if (testMode) {
            // Don’t show alerts while testing
            return;
        }

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void getStage(int number) {
        switch (number) {
            case 1:
                getUsername.setDisable(false);
                getSecretQuestions.setDisable(true);
                changePassword.setDisable(true);
                break;

            case 2:
                getUsername.setDisable(true);
                getSecretQuestions.setDisable(false);
                changePassword.setDisable(true);
                break;

            case 3:
                getUsername.setDisable(true);
                getSecretQuestions.setDisable(true);
                changePassword.setDisable(false);
                break;
        }
    }
}
