package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.SqliteContactDAO;
import com.example.addressbook.NinjaUser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import com.example.addressbook.SessionManager;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private boolean loginSuccessful = false;
    private boolean forgotPassword = false;
    private boolean testMode = false;
    private final INinjaContactDAO NinjaDAO;

    public LoginController() { this.NinjaDAO = new SqliteContactDAO(); }
    public LoginController(INinjaContactDAO mockDAO) { this.NinjaDAO = mockDAO; }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        doLogin(username, password);
    }

    void doLogin(String username, String password) {
        NinjaUser ninja = NinjaDAO.getNinjaUser(username);
        if (ninja == null) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Username not found!");
            return;
        }
        if (!BCrypt.checkpw(password, ninja.getPasswordHash())) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Incorrect password!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION, "Login Successful", "Welcome, " + ninja.getUserName() + "!");
        loginSuccessful = true;

        // ✅ 保存用户信息与明文密码到 Session（仅内存）
        SessionManager.setUser(ninja.getId(), ninja.getUserName());
        SessionManager.setCurrentPassword(password);

        System.out.println("✅ Session set: userId=" + ninja.getId() + ", username=" + ninja.getUserName());

        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        if (testMode) return;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onCancelClicked() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void onForgotPasswordClicked() {
        forgotPassword = true;
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    public boolean isLoginSuccessful() { return loginSuccessful; }
    public boolean isForgotPassword() { return forgotPassword; }
}
