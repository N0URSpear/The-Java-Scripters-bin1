package typingNinja.controllers;

import typingNinja.INinjaContactDAO;
import typingNinja.SqliteContactDAO;
import typingNinja.NinjaUser;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class LoginController {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private boolean loginSuccessful = false;
    private boolean forgotPassword = false;
    private boolean testMode = false;
    private final INinjaContactDAO NinjaDAO;
    public LoginController() {this.NinjaDAO = new SqliteContactDAO();}
    public LoginController(INinjaContactDAO mockDAO) {
        this.NinjaDAO = mockDAO;
    }

    /**
     * Sets test mode for unit tests.
     *
     * @param testMode sets test mode
     */
    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * Logic for when the login button is clicked.
     */
    @FXML
    private void onLoginClicked() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        doLogin(username, password);
    }

    /**
     * Takes a username and password and validates them.
     *
     * @param username provided username
     * @param password provided password
     */
    void doLogin(String username, String password) {

        //check credentials
        NinjaUser ninja = NinjaDAO.getNinjaUser(username);
        if (ninja == null) {
            showAlert(Alert.AlertType.ERROR,"Login Error", "Username not found!");
            return;
        }
        if (!BCrypt.checkpw(password,ninja.getPasswordHash())) {
            showAlert(Alert.AlertType.ERROR,"Login Error","Incorrect password!");
            return;
        }

        showAlert(Alert.AlertType.INFORMATION,"Login Successful","Welcome, " + ninja.getUserName() + "!"  );
        loginSuccessful = true;

        //close popup after login
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Method to show alert popups. Has a test mode check for unit testing.
     *
     * @param type the type of alert to be shown
     * @param title the title text for the alert popup
     * @param message the message the alert popup should display
     */
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

    /**
     * Logic for when the cancel button is clicked.
     */
    @FXML
    private void onCancelClicked() {
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Logic for when the forgot password button is clicked.
     */
    @FXML
    private void onForgotPasswordClicked() {
        ForgotPassword();
    }

    /**
     * Functionality when the forgot password button is clicked.
     */
    void ForgotPassword() {
        forgotPassword = true;
        if (usernameField != null && usernameField.getScene() != null) {
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.close();
        }
    }

    /**
     * Helper method for external functionality.
     *
     * @return returns whether login was successful or not
     */
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }

    /**
     * Helper method for external functionality.
     *
     * @return returns whether forgot password was clicked or not
     */
    public boolean isForgotPassword() {
        return forgotPassword;
    }
}
