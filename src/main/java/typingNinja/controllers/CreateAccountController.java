package typingNinja.controllers;

import typingNinja.model.INinjaContactDAO;
import typingNinja.model.SqliteContactDAO;
import typingNinja.model.NinjaUser;
import typingNinja.model.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;
import typingNinja.model.auth.Session;

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
    public CreateAccountController() {this.NinjaDAO = new SqliteContactDAO();}

    public CreateAccountController (INinjaContactDAO mockDao) {
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

        SecretQuestion1ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> enforceUniqueSelection(SecretQuestion1ComboBox, SecretQuestion2ComboBox, secretQuestions));

        SecretQuestion2ComboBox.valueProperty().addListener((obs, oldVal, newVal) -> enforceUniqueSelection(SecretQuestion2ComboBox, SecretQuestion1ComboBox, secretQuestions));
    }

    /**
     * Ensure that the secret question options are not repeated.
     *
     * @param comboBox1 the value within combo box 1
     * @param comboBox2 the value within combo box 2
     * @param allSecretQuestions the secret questions available to choose from
     */
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

    /**
     * Logic for once the create account button is clicked.
     */
    @FXML
    public void onCreateAccountClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String repeatPassword = passwordRepeatField.getText().trim();
        String secretQ1 = SecretQuestion1ComboBox.getValue();
        String secretQ2 = SecretQuestion2ComboBox.getValue();
        String answer1 = SecretQuestion1Answer.getText().trim();
        String answer2 = SecretQuestion2Answer.getText().trim();

        doCreateAccount(username,password,repeatPassword,secretQ1,secretQ2,answer1,answer2);
    }

    /**
     * Underlying logic for account creation. Ensures a unique username and has password confirmation.
     *
     * @param username the provided username
     * @param password the provided password
     * @param repeatPassword password confirmation
     * @param secretQ1 the first selected secret question
     * @param secretQ2 the second selected secret question
     * @param answer1 the provided answer to the first question
     * @param answer2 the provided answer to the second question
     */
    public void doCreateAccount(String username, String password, String repeatPassword, String secretQ1, String secretQ2, String answer1, String answer2) {
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

        if (answer1 == null || answer1.isEmpty() ||
                answer2 == null || answer2.isEmpty()) {
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

        NinjaUser newUser = new NinjaUser(username, passwordHash, secretQ1, secretQ2, answer1Hash, answer2Hash);
        SqliteContactDAO dao = new SqliteContactDAO();
        dao.addNinjaUser(newUser);

        dao.safeInitUserData(newUser.getId());
        dao.recalcUserStatistics(newUser.getId());
        System.out.println("Initialized Goals & Statistics for new user ID=" + newUser.getId());

        SessionManager.setUser(newUser.getId(), newUser.getUserName());
        SessionManager.setCurrentPassword(password);
        SessionManager.setCurrentSecretAnswers(answer1, answer2);

        showSuccess();
        isCreateAccountSuccessful = true;

        NinjaUser ninja = new NinjaUser(
                username,
                passwordHash,
                secretQ1,
                secretQ2,
                answer1Hash,
                answer2Hash
        );

        NinjaDAO.addNinjaUser(ninja);
        Session.setCurrentUserId(ninja.getId());

        showSuccess();

        isCreateAccountSuccessful = true;

        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Creates an error alert popup.
     *
     * @param message the message that should be displayed
     */
    private void showError(String message) {
        if (testMode) {
            // Don’t show alerts while testing
            return;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid Input");
        alert.setHeaderText(null);
        alert.setContentText(message);
        try {
            if (usernameField != null && usernameField.getScene() != null) {
                Stage owner = (Stage) usernameField.getScene().getWindow();
                alert.initOwner(owner);
                alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
                alert.initStyle(javafx.stage.StageStyle.UNDECORATED);
            }
        } catch (Exception ignored) {}
        alert.showAndWait();
    }

    /**
     *  Creates a success alert popup.
     */
    private void showSuccess() {
        if (testMode) {
            // Don’t show alerts while testing
            return;
        }
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("Account created successfully!");
        try {
            if (usernameField != null && usernameField.getScene() != null) {
                Stage owner = (Stage) usernameField.getScene().getWindow();
                alert.initOwner(owner);
                alert.initModality(javafx.stage.Modality.WINDOW_MODAL);
                alert.initStyle(javafx.stage.StageStyle.UNDECORATED);
            }
        } catch (Exception ignored) {}
        alert.showAndWait();
    }

    /**
     * Logic for when the cancel button is clicked.
     */
    @FXML
    public void onCancelClicked() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * @return a boolean to the controller indicating whether account creation was successful or not.
     */
    public boolean isCreateAccountSuccessful() {
        return isCreateAccountSuccessful;
    }
}
