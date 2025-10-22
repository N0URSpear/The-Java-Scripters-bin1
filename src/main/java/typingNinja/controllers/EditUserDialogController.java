package typingNinja.controllers;

import typingNinja.model.NinjaUser;
import typingNinja.model.SessionManager;
import typingNinja.model.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Arrays;

/**
 * Controller class for the Edit User dialog in Typing Ninja.
 * This dialog allows users to review their stored credentials and security
 * questions in a secure, read-only format. Users can toggle visibility of
 * sensitive information such as passwords and secret answers for verification.
 * All data is retrieved from the {@link SqliteContactDAO} and displayed using
 * JavaFX controls. The dialog does not modify data, it only provides a safe
 * interface for reviewing account details.
 */
public class EditUserDialogController {

    @FXML private Button backBtn;
    @FXML private Label usernameValueLabel;

    @FXML private Label passwordMask;
    @FXML private Label secretQ1Mask;
    @FXML private Label secretQ2Mask;
    @FXML private Label passwordPlain;
    @FXML private Label secretQ1Plain;
    @FXML private Label secretQ2Plain;

    @FXML private Button togglePasswordBtn;
    @FXML private Button toggleQ1Btn;
    @FXML private Button toggleQ2Btn;

    @FXML private ComboBox<String> secretQ1ComboBox;
    @FXML private ComboBox<String> secretQ2ComboBox;

    private final SqliteContactDAO dao = new SqliteContactDAO();
    private NinjaUser currentUser;

    /**
     * Initializes the Edit User dialog after its FXML elements are loaded.
     * This method:
     * <ul>
     *     <li>Populates the ComboBoxes with preset security questions.</li>
     *     <li>Fetches the current user from the database via {@link SqliteContactDAO}.</li>
     *     <li>Displays user information in masked format (password and answers hidden).</li>
     *     <li>Sets up toggle buttons to switch between masked and plain text views.</li>
     *     <li>Registers the Back button to close the dialog window.</li>
     * </ul>
     */
    @FXML
    private void initialize() {
        var questions = Arrays.asList(
                "What is your birth city?",
                "What is your mother's maiden name?",
                "What is your favorite food?",
                "What was your first pet's name?"
        );
        secretQ1ComboBox.getItems().setAll(questions);
        secretQ2ComboBox.getItems().setAll(questions);
        secretQ1ComboBox.setDisable(true);
        secretQ2ComboBox.setDisable(true);

        String uname = SessionManager.getCurrentUsername();
        currentUser = dao.getNinjaUser(uname);

        if (currentUser != null) {
            usernameValueLabel.setText(currentUser.getUserName());
            secretQ1ComboBox.setValue(currentUser.getSecretQuestion1());
            secretQ2ComboBox.setValue(currentUser.getSecretQuestion2());

            passwordMask.setText("********");
            secretQ1Mask.setText("********");
            secretQ2Mask.setText("********");

            passwordPlain.setText(SessionManager.getCurrentPassword());
            secretQ1Plain.setText(SessionManager.getCurrentSecretAnswer1());
            secretQ2Plain.setText(SessionManager.getCurrentSecretAnswer2());

            passwordPlain.setVisible(false);
            secretQ1Plain.setVisible(false);
            secretQ2Plain.setVisible(false);
        }

        togglePasswordBtn.setOnAction(e -> toggle(passwordMask, passwordPlain, togglePasswordBtn));
        toggleQ1Btn.setOnAction(e -> toggle(secretQ1Mask, secretQ1Plain, toggleQ1Btn));
        toggleQ2Btn.setOnAction(e -> toggle(secretQ2Mask, secretQ2Plain, toggleQ2Btn));

        backBtn.setOnAction(e -> closeWindow());
    }

    /**
     * Toggles between showing and hiding the plain text version of a masked field.
     * When toggled on, the plain label becomes visible and the mask is hidden.
     * The associated eye icon is updated to reflect visibility state.
     *
     * @param mask the label displaying masked text (e.g., ********)
     * @param plain the label displaying plain text (visible when toggled)
     * @param btn the button whose eye icon changes according to state
     */
    private void toggle(Label mask, Label plain, Button btn) {
        boolean showPlain = !plain.isVisible();
        plain.setVisible(showPlain);
        mask.setVisible(!showPlain);

        ImageView iv = (ImageView) btn.getGraphic();
        String icon = showPlain ? "/images/eye-off.png" : "/images/eye-icon.png";
        iv.setImage(new Image(getClass().getResource(icon).toExternalForm()));
    }

    /**
     * Closes the current Edit User dialog window and returns control to the profile screen.
     */
    private void closeWindow() {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }
}
