package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.SqliteContactDAO;
import com.example.addressbook.NinjaUser;
import com.example.addressbook.SessionManager; // ✅ 引入 SessionManager
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
    private boolean isCreateAccountSuccessful = false;
    private boolean testMode = false;
    private final INinjaContactDAO NinjaDAO;

    public CreateAccountController() {
        this.NinjaDAO = new SqliteContactDAO();
    }

    public CreateAccountController(INinjaContactDAO mockDao) {
        this.NinjaDAO = mockDao;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

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
                other.setValue(newOptions.getFirst());
            }
        }
    }

    @FXML
    public void onCreateAccountClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String repeatPassword = passwordRepeatField.getText().trim();
        String secretQ1 = SecretQuestion1ComboBox.getValue();
        String secretQ2 = SecretQuestion2ComboBox.getValue();
        String answer1 = SecretQuestion1Answer.getText().trim();
        String answer2 = SecretQuestion2Answer.getText().trim();

        doCreateAccount(username, password, repeatPassword, secretQ1, secretQ2, answer1, answer2);
    }

    void doCreateAccount(String username, String password, String repeatPassword, String secretQ1, String secretQ2, String answer1, String answer2) {
        isCreateAccountSuccessful = false;
        if (username == null || username.isEmpty()) {
            showError("Username cannot be empty.");
            return;
        }

        if (password == null || password.isEmpty()) {
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

        if (answer1 == null || answer1.isEmpty() || answer2 == null || answer2.isEmpty()) {
            showError("Both secret question answers must be filled.");
            return;
        }

        // Check for existing username before attempting to insert
        if (NinjaDAO.getNinjaUser(username) != null) {
            showError("That username is already taken. Please choose a different one.");
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

        // ✅ 获取刚刚插入的用户 ID
        int newUserId = ((SqliteContactDAO)NinjaDAO).getUserIdByUsername(username);
        System.out.println("DEBUG: Created new userId = " + newUserId);

        // ✅ 初始化 Goals & Statistics（全部默认 0）S
        ((SqliteContactDAO)NinjaDAO).initUserData(newUserId);
        System.out.println("DEBUG: Init data for userId = " + newUserId);

        // ✅ 设置当前会话用户，保证 ProfilePage 能拿到正确的 userId 和 username
        SessionManager.setUser(newUserId, username);
        System.out.println("DEBUG: Session set with userId = " + newUserId + ", username = " + username);

        showSuccess();

        isCreateAccountSuccessful = true;

        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    private void showError(String message) {
        if (testMode) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess() {
        if (testMode) {
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Account created successfully!");
        alert.showAndWait();
    }

    @FXML
    public void onCancelClicked() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    public boolean isCreateAccountSuccessful() {
        return isCreateAccountSuccessful;
    }
}
