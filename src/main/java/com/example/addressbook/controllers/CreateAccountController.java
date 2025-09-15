package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.MainMenu;
import com.example.addressbook.MockNinjaDAO;
import com.example.addressbook.NinjaUser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class CreateAccountController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField passwordRepeatField;
    @FXML private ComboBox<String> SecretQuestion1ComboBox;
    @FXML private ComboBox<String> SecretQuestion2ComboBox;
    @FXML private TextField SecretQuestion1Answer;
    @FXML private TextField SecretQuestion2Answer;
    private final INinjaContactDAO NinjaDAO;
    public CreateAccountController() {NinjaDAO = new MockNinjaDAO();}

    @FXML
    public void initialize() {
        ObservableList<String> questions = FXCollections.observableArrayList(
                "What is your birth city?",
                "What is your mother's maiden name?",
                "What high school did you attend?",
                "Where is the first place that you travelled to overseas?"
        );

        SecretQuestion1ComboBox.setItems(questions);
        SecretQuestion2ComboBox.setItems(FXCollections.observableArrayList(questions));

        SecretQuestion1ComboBox.getSelectionModel().selectFirst();
        SecretQuestion2ComboBox.getSelectionModel().select(1);

        SecretQuestion1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            enforceUniqueSelection(SecretQuestion1ComboBox, SecretQuestion2ComboBox, questions);
        });

        SecretQuestion2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            enforceUniqueSelection(SecretQuestion2ComboBox, SecretQuestion1ComboBox, questions);
        });
    }

    private void enforceUniqueSelection(ComboBox<String> source, ComboBox<String> other, ObservableList<String> allQuestions) {
        String sourceSelection = source.getValue();
        String otherSelection = other.getValue();

        ObservableList<String> newOptions = FXCollections.observableArrayList(allQuestions);
        newOptions.remove(sourceSelection);
        other.setItems(newOptions);

        if (sourceSelection != null && sourceSelection.equals(otherSelection)) {
            other.getSelectionModel().clearSelection();
            if (!newOptions.isEmpty()) {
                other.setValue(newOptions.get(0));
            }
        }
    }

    @FXML
    public void onCreateAccountClicked() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String repeatPassword = passwordRepeatField.getText();
        String secretQ1 = SecretQuestion1ComboBox.getValue();
        String secretQ2 = SecretQuestion2ComboBox.getValue();
        String answer1 = SecretQuestion1Answer.getText();
        String answer2 = SecretQuestion2Answer.getText();

        if (username == null || username.trim().isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Password cannot be empty.");
            return;
        }

        if (!password.equals(repeatPassword)) {
            showError("Passwords do not match.");
            return;
        }

        if (secretQ1 == null || secretQ2 == null) {
            showError("You must select both secret questions.");
            return;
        }

        if (secretQ1.equals(secretQ2)) {
            showError("Secret questions must be different.");
            return;
        }

        if (answer1 == null || answer1.trim().isEmpty() ||
                answer2 == null || answer2.trim().isEmpty()) {
            showError("Both secret question answers must be filled.");
            return;
        }

        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        String answer1Hash = BCrypt.hashpw(answer1, BCrypt.gensalt());
        String answer2Hash = BCrypt.hashpw(answer2, BCrypt.gensalt());

        NinjaUser newUser = new NinjaUser(
                username,
                passwordHash,
                secretQ1,
                secretQ2,
                answer1Hash,
                answer2Hash
        );

        NinjaDAO.addNinjaUser(newUser);

        showSuccess("Account created successfully!");

        MainMenu menu = new MainMenu();
        Stage newStage = new Stage();
        newStage.setScene(menu.buildScene(newStage));
        newStage.setTitle("Main Menu - Typing Ninja");
        newStage.show();

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

    @FXML
    public void onCancelClicked() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
