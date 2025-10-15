package com.example.addressbook.controllers;

import com.example.addressbook.NinjaUser;
import com.example.addressbook.SessionManager;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Arrays;

public class EditUserDialogController {

    // 顶部按钮
    @FXML private Button backBtn;

    // Username
    @FXML private Label usernameValueLabel;
    @FXML private TextField usernameField;
    @FXML private Button editUsernameBtn;

    // Password
    @FXML private Label passwordMask;
    @FXML private TextField passwordVisibleField;
    @FXML private Button editPasswordBtn;

    // Secret Question 1
    @FXML private ComboBox<String> secretQ1ComboBox;
    @FXML private Label secretQ1Mask;
    @FXML private TextField secretQ1VisibleField;
    @FXML private Button editAnswer1Btn;

    // Secret Question 2
    @FXML private ComboBox<String> secretQ2ComboBox;
    @FXML private Label secretQ2Mask;
    @FXML private TextField secretQ2VisibleField;
    @FXML private Button editAnswer2Btn;

    // 底部按钮
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    private final SqliteContactDAO dao = new SqliteContactDAO();
    private NinjaUser currentUser;

    @FXML
    private void initialize() {
        // 初始化 Secret Question 下拉列表
        secretQ1ComboBox.getItems().setAll(
                Arrays.asList("What is your birth city?",
                        "What is your mother's maiden name?",
                        "What is your favorite food?",
                        "What was your first pet's name?"));
        secretQ2ComboBox.getItems().setAll(secretQ1ComboBox.getItems());

        // 加载当前用户数据
        String uname = SessionManager.getCurrentUsername();
        currentUser = dao.getNinjaUser(uname);

        if (currentUser != null) {
            usernameValueLabel.setText(currentUser.getUserName());
            usernameField.setText(currentUser.getUserName());
            passwordMask.setText("********");
            secretQ1Mask.setText("********");
            secretQ2Mask.setText("********");

            secretQ1ComboBox.setValue(currentUser.getSecretQuestion1());
            secretQ2ComboBox.setValue(currentUser.getSecretQuestion2());
        }

        // 绑定事件
        backBtn.setOnAction(e -> closeWindow());
        cancelBtn.setOnAction(e -> closeWindow());
        saveBtn.setOnAction(e -> saveUser());

        editUsernameBtn.setOnAction(e -> toggleUsername());
        editPasswordBtn.setOnAction(e -> togglePassword());
        editAnswer1Btn.setOnAction(e -> toggleAnswer(secretQ1Mask, secretQ1VisibleField));
        editAnswer2Btn.setOnAction(e -> toggleAnswer(secretQ2Mask, secretQ2VisibleField));
    }

    // ------------------- 切换逻辑 -------------------

    private void toggleUsername() {
        boolean editing = usernameField.isVisible();
        if (editing) {
            // 保存并返回 Label
            String newName = usernameField.getText().trim();
            if (!newName.isEmpty()) {
                usernameValueLabel.setText(newName);
            }
            usernameField.setVisible(false);
            usernameField.setManaged(false);
            usernameValueLabel.setVisible(true);
            usernameValueLabel.setManaged(true);
        } else {
            // 显示可编辑框并填充当前用户名
            usernameField.setText(usernameValueLabel.getText());
            usernameValueLabel.setVisible(false);
            usernameValueLabel.setManaged(false);
            usernameField.setVisible(true);
            usernameField.setManaged(true);
            usernameField.requestFocus();
        }
    }

    private void togglePassword() {
        boolean showingPlain = passwordVisibleField.isVisible();
        if (showingPlain) {
            // 保存并回掩码
            String text = nz(passwordVisibleField.getText());
            passwordMask.setText(mask(text.length()));
            passwordVisibleField.setVisible(false);
            passwordVisibleField.setManaged(false);
            passwordMask.setVisible(true);
            passwordMask.setManaged(true);
        } else {
            // 显示明文（从 currentUser 读取）
            String text = currentUser != null ? nz(currentUser.getPasswordPlain()) : "";
            passwordVisibleField.setText(text);
            passwordMask.setVisible(false);
            passwordMask.setManaged(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setManaged(true);
            passwordVisibleField.requestFocus();
        }
    }

    private void toggleAnswer(Label maskLabel, TextField visibleField) {
        boolean showingPlain = visibleField.isVisible();
        if (showingPlain) {
            // 保存并隐藏
            String text = nz(visibleField.getText());
            maskLabel.setText(mask(text.length()));
            visibleField.setVisible(false);
            visibleField.setManaged(false);
            maskLabel.setVisible(true);
            maskLabel.setManaged(true);
        } else {
            // 显示明文
            String text = "";
            if (visibleField == secretQ1VisibleField && currentUser != null)
                text = nz(currentUser.getSecretAnswer1Plain());
            else if (visibleField == secretQ2VisibleField && currentUser != null)
                text = nz(currentUser.getSecretAnswer2Plain());

            visibleField.setText(text);
            maskLabel.setVisible(false);
            maskLabel.setManaged(false);
            visibleField.setVisible(true);
            visibleField.setManaged(true);
            visibleField.requestFocus();
        }
    }

    // ------------------- 保存逻辑 -------------------

    private void saveUser() {
        if (currentUser == null) return;

        // Username
        String newUsername = usernameValueLabel.getText();
        if (!newUsername.isBlank() && !newUsername.equals(currentUser.getUserName())) {
            currentUser.setUserName(newUsername);
            SessionManager.setUser(SessionManager.getCurrentUserId(), newUsername);
        }

        // Password
        if (passwordVisibleField.isVisible()) {
            String pwd = nz(passwordVisibleField.getText());
            if (!pwd.isBlank()) {
                currentUser.setPasswordPlain(pwd);
                currentUser.setPasswordHash(BCrypt.hashpw(pwd, BCrypt.gensalt()));
            }
        }

        // Secret Questions
        currentUser.setSecretQuestion1(secretQ1ComboBox.getValue());
        currentUser.setSecretQuestion2(secretQ2ComboBox.getValue());

        // Secret Answers
        if (secretQ1VisibleField.isVisible()) {
            String a1 = nz(secretQ1VisibleField.getText());
            if (!a1.isBlank()) {
                currentUser.setSecretAnswer1Plain(a1);
                currentUser.setSecretQuestion1Answer(BCrypt.hashpw(a1, BCrypt.gensalt()));
            }
        }
        if (secretQ2VisibleField.isVisible()) {
            String a2 = nz(secretQ2VisibleField.getText());
            if (!a2.isBlank()) {
                currentUser.setSecretAnswer2Plain(a2);
                currentUser.setSecretQuestion2Answer(BCrypt.hashpw(a2, BCrypt.gensalt()));
            }
        }

        // 保存到数据库
        dao.updateNinjaUser(currentUser);
        closeWindow();
    }

    // ------------------- 工具函数 -------------------

    private String nz(String s) { return s == null ? "" : s; }

    private String mask(int len) {
        int n = Math.max(8, len);
        return "*".repeat(n);
    }

    private void closeWindow() {
        Stage stage = (Stage) backBtn.getScene().getWindow();
        stage.close();
    }
}
