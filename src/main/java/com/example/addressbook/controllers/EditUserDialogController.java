package com.example.addressbook;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class EditUserDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> q1Box;
    @FXML private ComboBox<String> q2Box;
    @FXML private TextField answer1Field;
    @FXML private TextField answer2Field;

    private SqliteContactDAO dao;
    private NinjaUser currentUser;

    @FXML
    public void initialize() {
        dao = new SqliteContactDAO();
        int userId = SessionManager.getCurrentUserId();
        currentUser = dao.getNinjaUser(SessionManager.getCurrentUsername());

        // 初始化问题选项
        q1Box.getItems().addAll("City of Birth", "Mother's Maiden Name", "Favorite Teacher", "Pet's Name", "First School");
        q2Box.getItems().addAll("Favorite Color", "Favorite Movie", "First Car", "Best Friend", "Dream Job");

        if (currentUser != null) {
            usernameField.setText(currentUser.getUserName());
            q1Box.setValue(currentUser.getSecretQuestion1());
            q2Box.setValue(currentUser.getSecretQuestion2());
            answer1Field.setText(currentUser.getSecretQuestion1Answer());
            answer2Field.setText(currentUser.getSecretQuestion2Answer());
        }
    }

    @FXML
    private void onSaveClicked() {
        if (currentUser == null) return;

        String newUsername = usernameField.getText().trim();
        String newPassword = passwordField.getText().trim();
        String newQ1 = q1Box.getValue();
        String newQ2 = q2Box.getValue();
        String newAnswer1 = answer1Field.getText().trim();
        String newAnswer2 = answer2Field.getText().trim();

        if (!newUsername.isEmpty()) {
            currentUser.setUserName(newUsername);
        }
        if (!newPassword.isEmpty()) {
            currentUser.setPasswordHash(BCrypt.hashpw(newPassword, BCrypt.gensalt()));
        }
        if (newQ1 != null) {
            currentUser.setSecretQuestion1(newQ1);
        }
        if (newQ2 != null) {
            currentUser.setSecretQuestion2(newQ2);
        }
        if (!newAnswer1.isEmpty()) {
            currentUser.setSecretQuestion1Answer(BCrypt.hashpw(newAnswer1, BCrypt.gensalt()));
        }
        if (!newAnswer2.isEmpty()) {
            currentUser.setSecretQuestion2Answer(BCrypt.hashpw(newAnswer2, BCrypt.gensalt()));
        }

        dao.updateNinjaUser(currentUser);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void onCancelClicked() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
