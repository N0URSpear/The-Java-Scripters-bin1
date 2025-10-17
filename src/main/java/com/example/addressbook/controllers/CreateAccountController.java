package com.example.addressbook.controllers;

import com.example.addressbook.INinjaContactDAO;
import com.example.addressbook.SqliteContactDAO;
import com.example.addressbook.NinjaUser;
import com.example.addressbook.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
        ObservableList<String> secretQuestions = FXCollections.observableArrayList(
                "What is your birth city?",
                "What is your mother's maiden name?",
                "What high school did you attend?",
                "Where is the first place that you travelled to overseas?"
        );

        SecretQuestion1ComboBox.setItems(secretQuestions);
        SecretQuestion2ComboBox.setItems(FXCollections.observableArrayList(secretQuestions));

        SecretQuestion1ComboBox.getSelectionModel().selectFirst();
        SecretQuestion2ComboBox.getSelectionModel().select(1);

        SecretQuestion1ComboBox.valueProperty().addListener(
                (obs, oldVal, newVal) -> enforceUniqueSelection(SecretQuestion1ComboBox, SecretQuestion2ComboBox, secretQuestions));

        SecretQuestion2ComboBox.valueProperty().addListener(
                (obs, oldVal, newVal) -> enforceUniqueSelection(SecretQuestion2ComboBox, SecretQuestion1ComboBox, secretQuestions));
    }

    private void enforceUniqueSelection(ComboBox<String> comboBox1, ComboBox<String> comboBox2, ObservableList<String> allSecretQuestions) {
        String sourceSelection = comboBox1.getValue();
        String otherSelection = comboBox2.getValue();

        ObservableList<String> newOptions = FXCollections.observableArrayList(allSecretQuestions);
        newOptions.remove(sourceSelection);
        comboBox2.setItems(newOptions);

        if (sourceSelection != null && sourceSelection.equals(otherSelection)) {
            comboBox2.getSelectionModel().clearSelection();
            if (!newOptions.isEmpty()) {
                comboBox2.setValue(newOptions.getFirst());
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

    void doCreateAccount(String username, String password, String repeatPassword,
                         String secretQ1, String secretQ2, String answer1, String answer2) {

        isCreateAccountSuccessful = false;

        if (username.isEmpty()) { showError("Username cannot be empty."); return; }
        if (password.isEmpty()) { showError("Password cannot be empty."); return; }
        if (!password.equals(repeatPassword)) { showError("Passwords do not match."); return; }
        if (secretQ1 == null || secretQ2 == null) { showError("You must select both secret questions."); return; }
        if (secretQ1.equals(secretQ2)) { showError("Secret questions must be different."); return; }
        if (answer1.isEmpty() || answer2.isEmpty()) { showError("Both secret answers must be filled."); return; }

        if (NinjaDAO.getNinjaUser(username) != null) {
            showError("That username is already taken. Please choose another.");
            return;
        }

        // 🔐 Hash sensitive data for DB
        String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
        String answer1Hash = BCrypt.hashpw(answer1, BCrypt.gensalt());
        String answer2Hash = BCrypt.hashpw(answer2, BCrypt.gensalt());

        NinjaUser newUser = new NinjaUser(username, passwordHash, secretQ1, secretQ2, answer1Hash, answer2Hash);
        SqliteContactDAO dao = new SqliteContactDAO();
        dao.addNinjaUser(newUser);

        // ✅ 初始化对应的 Goals & Statistics 记录
        dao.safeInitUserData(newUser.getId());
        dao.recalcUserStatistics(newUser.getId());
        System.out.println("✅ Initialized Goals & Statistics for new user ID=" + newUser.getId());

        // ✅ 临时保存明文（不进数据库）
        SessionManager.setUser(newUser.getId(), newUser.getUserName());
        SessionManager.setCurrentPassword(password);
        SessionManager.setCurrentSecretAnswers(answer1, answer2);

        showSuccess();
        isCreateAccountSuccessful = true;

        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    private void showError(String message) {
        if (testMode) return;
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess() {
        if (testMode) return;
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
