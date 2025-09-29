package com.example.addressbook.controllers;

import com.example.addressbook.NinjaUser;
import com.example.addressbook.SessionManager;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

public class EditUserDialogController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<String> secretQ1ComboBox;
    @FXML private PasswordField secretQ1Answer;
    @FXML private ComboBox<String> secretQ2ComboBox;
    @FXML private PasswordField secretQ2Answer;

    @FXML private Button editUsernameBtn;
    @FXML private Button editPasswordBtn;
    @FXML private Button editAnswer1Btn;
    @FXML private Button editAnswer2Btn;

    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    // 临时存储切换后的 TextField（明文）
    private TextField passwordVisibleField;
    private TextField answer1VisibleField;
    private TextField answer2VisibleField;

    private NinjaUser currentUser;

    @FXML
    private void initialize() {
        // 加载当前用户
        SqliteContactDAO dao = new SqliteContactDAO();
        currentUser = dao.getNinjaUser(SessionManager.getCurrentUsername());

        if (currentUser != null) {
            usernameField.setText(currentUser.getUserName());
            passwordField.setText(currentUser.getPasswordPlain());
            secretQ1ComboBox.setValue(currentUser.getSecretQuestion1());
            secretQ1Answer.setText(currentUser.getSecretAnswer1Plain());
            secretQ2ComboBox.setValue(currentUser.getSecretQuestion2());
            secretQ2Answer.setText(currentUser.getSecretAnswer2Plain());
        }

        // 默认禁用用户名
        usernameField.setDisable(true);
        editUsernameBtn.setOnAction(e -> usernameField.setDisable(false));

        // 切换明文显示
        editPasswordBtn.setOnAction(e -> toggleVisibility(passwordField, 0));
        editAnswer1Btn.setOnAction(e -> toggleVisibility(secretQ1Answer, 1));
        editAnswer2Btn.setOnAction(e -> toggleVisibility(secretQ2Answer, 2));

        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> saveUser());
    }

    /**
     * 切换 PasswordField <-> TextField
     */
    private void toggleVisibility(PasswordField pwField, int index) {
        TextField visibleField = new TextField(pwField.getText());
        visibleField.setPrefWidth(pwField.getPrefWidth());

        Integer rowObj = GridPane.getRowIndex(pwField);
        Integer colObj = GridPane.getColumnIndex(pwField);

        final int row = (rowObj == null ? 0 : rowObj);
        final int col = (colObj == null ? 0 : colObj);

        GridPane parent = (GridPane) pwField.getParent();
        parent.getChildren().remove(pwField);
        parent.add(visibleField, col, row);

        if (index == 0) {
            passwordVisibleField = visibleField;
        } else if (index == 1) {
            answer1VisibleField = visibleField;
        } else {
            answer2VisibleField = visibleField;
        }

        // 回车切回 PasswordField
        visibleField.setOnAction(e -> {
            PasswordField newPwField = new PasswordField();
            newPwField.setText(visibleField.getText());
            newPwField.setPrefWidth(visibleField.getPrefWidth());

            parent.getChildren().remove(visibleField);
            parent.add(newPwField, col, row);

            if (index == 0) {
                this.passwordField = newPwField;
                this.passwordVisibleField = null;
            } else if (index == 1) {
                this.secretQ1Answer = newPwField;
                this.answer1VisibleField = null;
            } else {
                this.secretQ2Answer = newPwField;
                this.answer2VisibleField = null;
            }
        });
    }

    /**
     * 保存用户修改
     */
    private void saveUser() {
        if (currentUser == null) return;

        String username = usernameField.getText();
        String password = (passwordVisibleField != null)
                ? passwordVisibleField.getText() : passwordField.getText();
        String q1 = secretQ1ComboBox.getValue();
        String q1Answer = (answer1VisibleField != null)
                ? answer1VisibleField.getText() : secretQ1Answer.getText();
        String q2 = secretQ2ComboBox.getValue();
        String q2Answer = (answer2VisibleField != null)
                ? answer2VisibleField.getText() : secretQ2Answer.getText();

        // 更新 currentUser
        currentUser.setUserName(username);

        // 存明文 → 仅用于 EditUserDialog 回显
        currentUser.setPasswordPlain(password);
        currentUser.setSecretAnswer1Plain(q1Answer);
        currentUser.setSecretAnswer2Plain(q2Answer);

        // 存哈希 → 用于登录验证
        currentUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));
        currentUser.setSecretQuestion1(q1);
        currentUser.setSecretQuestion2(q2);
        currentUser.setSecretQuestion1Answer(BCrypt.hashpw(q1Answer, BCrypt.gensalt()));
        currentUser.setSecretQuestion2Answer(BCrypt.hashpw(q2Answer, BCrypt.gensalt()));

        SqliteContactDAO dao = new SqliteContactDAO();
        dao.updateNinjaUser(currentUser);

        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }
}
