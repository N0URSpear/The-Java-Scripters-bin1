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

    private void toggle(Label mask, Label plain, Button btn) {
        boolean showPlain = !plain.isVisible();
        plain.setVisible(showPlain);
        mask.setVisible(!showPlain);

        ImageView iv = (ImageView) btn.getGraphic();
        String icon = showPlain ? "/images/eye-off.png" : "/images/eye-icon.png";
        iv.setImage(new Image(getClass().getResource(icon).toExternalForm()));
    }

    private void closeWindow() {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }
}
