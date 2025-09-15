package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.MockNinjaDAO;
import com.example.addressbook.NinjaUser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private final INinjaContactDAO NinjaDAO;
    public LoginController() {this.NinjaDAO = new MockNinjaDAO();}

    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        //check credentials
        NinjaUser ninja = NinjaDAO.getNinjaUser(username);
        if (ninja == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setContentText("Username not found!");
            alert.showAndWait();
            return;
        }
        if (!BCrypt.checkpw(password,ninja.getPasswordHash())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText(null);
            alert.setContentText("Incorrect password!");
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("Welcome, " + ninja.getUserName() + "!");
        alert.showAndWait();

        // Close popup after login
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onForgotPasswordClicked() {

    }
}
