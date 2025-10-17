package com.example.addressbook.controllers;

import com.example.addressbook.SessionManager;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Optional;

public class ProfilePageController {

    // Header
    @FXML private ImageView avatarImage;
    @FXML private Label usernameLabel;
    @FXML private Button editUserBtn;

    // 左列：GOALS vs Actual（用 Label 承载值）
    @FXML private Button editHoursBtn;
    @FXML private Label  hoursGoalLabel;
    @FXML private Label  hoursActualLabel;

    @FXML private Button editWpmBtn;
    @FXML private Label  wpmGoalLabel;
    @FXML private Label  wpmActualLabel;

    @FXML private Button editAccBtn;
    @FXML private Label  accGoalLabel;
    @FXML private Label  accActualLabel;

    @FXML private Button lessonHistoryBtn;
    @FXML private Button certBtn;
    @FXML private Label  starsValueLabel;

    // 右列
    @FXML private ImageView beltImage;
    @FXML private Label beltLabel;
    @FXML private Label lessonsLabel;
    @FXML private Label avgWpmLabel;
    @FXML private HBox highestRatingBox;
    @FXML private HBox avgRatingBox;
    @FXML private Label mainMenuLabel;
    @FXML private Label settingsLabel;

    private final SqliteContactDAO dao = new SqliteContactDAO();
    private int userId = -1;
    private boolean initialized = false;

    public ProfilePageController() {}

    public void setUserId(int userId) {
        this.userId = userId;
        if (initialized) ensureDataAndRefresh();
    }

    @FXML
    private void initialize() {
        initialized = true;
        if (userId <= 0) userId = SessionManager.getCurrentUserId();

        editUserBtn.setOnAction(e -> openEditUserDialog());
        wireGoalEditor(editHoursBtn, GoalField.HOURS);
        wireGoalEditor(editWpmBtn,   GoalField.WPM);
        wireGoalEditor(editAccBtn,   GoalField.ACCURACY);

        lessonHistoryBtn.getStyleClass().add("action-button");
        certBtn.getStyleClass().add("action-button");

        ensureDataAndRefresh();

        lessonHistoryBtn.setOnAction(e -> openLessonHistory());
        mainMenuLabel.setOnMouseClicked(e -> openMainMenu());
        settingsLabel.setOnMouseClicked(e -> openSettings());
    }

    private void ensureDataAndRefresh() {
        String name = SessionManager.getCurrentUsername();
        if (name != null && !name.isBlank()) usernameLabel.setText(name);
        dao.safeInitUserData(userId);
        dao.recalcUserStatistics(userId);
        loadStats();
    }

    private enum GoalField { HOURS, WPM, ACCURACY }

    private void wireGoalEditor(Button pencil, GoalField type) {
        pencil.setOnAction(e -> {
            String current = switch (type) {
                case HOURS    -> stripUnits(hoursGoalLabel.getText());
                case WPM      -> stripUnits(wpmGoalLabel.getText());
                case ACCURACY -> stripUnits(accGoalLabel.getText());
            };
            TextInputDialog dialog = new TextInputDialog(current);
            dialog.setTitle("Edit goal");
            dialog.setHeaderText(null);
            dialog.setContentText("New value:");
            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) return;

            int val = parseIntSafe(result.get().trim());
            switch (type) {
                case HOURS    -> dao.updateGoals(userId, val, null, null);
                case WPM      -> dao.updateGoals(userId, null, val, null);
                case ACCURACY -> dao.updateGoals(userId, null, null, val);
            }
            loadStats();
        });
    }

    private int parseIntSafe(String s) { try { return Integer.parseInt(s); } catch (Exception e) { return 0; } }
    private String nz(String s) { return (s == null || s.isBlank()) ? "0" : s; }

    private String withWpm(String v) { return nz(v) + " wpm"; }
    private String withPct(String v) { return nz(v) + "%";  }
    private String stripUnits(String v) { return v == null ? "" : v.replaceAll("[^0-9-]", ""); }

    private void loadStats() {
        SqliteContactDAO.ProfileStats s = dao.getUserGoalsAndStats(userId);
        if (s == null) return;

        // 左三列布局：中间列=GOALS（带单位），右列=Actual（带单位）
        hoursGoalLabel.setText(nz(s.getEstHours()));
        hoursActualLabel.setText(nz(s.getTimeActiveWeek()));

        wpmGoalLabel.setText(withWpm(s.getEstWPM()));
        wpmActualLabel.setText(withWpm(s.getTotalWPMWeek()));

        accGoalLabel.setText(withPct(s.getEstAccuracy()));
        accActualLabel.setText(withPct(s.getTotalAccuracyWeek()));

        starsValueLabel.setText(nz(s.getTotalStars()));

        beltLabel.setText("Belt: " + nz(s.getBelt()));
        lessonsLabel.setText("Lessons completed: " + nz(s.getTotalLessons()));
        avgWpmLabel.setText("Average WPM: " + nz(s.getAvgWPM()));

        fillRatingWithShuriken(highestRatingBox, parseDoubleSafe(s.getHighestRating()));
        fillRatingWithShuriken(avgRatingBox,     parseDoubleSafe(s.getAvgRating()));
    }

    private double parseDoubleSafe(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; } }

    private void fillRatingWithShuriken(HBox box, double rating) {
        box.getChildren().clear();

        // ✅ 使用 Math.floor 而不是 Math.round，4.5 → 4
        int full = (int) Math.floor(Math.max(0, Math.min(5, rating)));

        Image gold = new Image(getClass().getResource("/com/example/addressbook/gold_shuriken.png").toExternalForm());
        Image gray = new Image(getClass().getResource("/com/example/addressbook/gray_shuriken.png").toExternalForm());

        for (int i = 0; i < 5; i++) {
            Image img = (i < full ? gold : gray);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            box.getChildren().add(iv);
        }
    }




    private Image safeImage(String path) {
        try {
            Image img = new Image(path, true);
            if (img.isError()) {
                System.out.println("⚠️ Failed to load image: " + path);
            } else {
                System.out.println("✅ Loaded image: " + path);
            }
            return img.isError() ? null : img;
        } catch (Exception e) {
            System.out.println("❌ Exception loading: " + path);
            e.printStackTrace();
            return null;
        }
    }
    private void openEditUserDialog() {
        try {
            javafx.fxml.FXMLLoader loader =
                    new javafx.fxml.FXMLLoader(getClass().getResource("/com/example/addressbook/EditUserDialog.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new javafx.scene.Scene(root));
            dialogStage.showAndWait();

            String name = SessionManager.getCurrentUsername();
            if (name != null && !name.isBlank()) usernameLabel.setText(name);
            dao.recalcUserStatistics(userId);
            loadStats();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @FXML
    private void openLessonHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/addressbook/LessonHistoryPage.fxml"));
            Parent root = loader.load();

            // 获取当前窗口
            Stage stage = (Stage) lessonHistoryBtn.getScene().getWindow();

            // 🔹 自动计算 75% 的屏幕尺寸
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double WIDTH = screenBounds.getWidth() * 0.75;
            double HEIGHT = screenBounds.getHeight() * 0.75;

            Scene scene = new Scene(root, WIDTH, HEIGHT);
            stage.setScene(scene);

            // 居中显示
            stage.setX((screenBounds.getWidth() - WIDTH) / 2);
            stage.setY((screenBounds.getHeight() - HEIGHT) / 2);

            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ✅ 打开 Settings.fxml
    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/addressbook/Settings.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) settingsLabel.getScene().getWindow();
            double width = stage.getWidth();
            double height = stage.getHeight();

            Scene scene = new Scene(root, width, height);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openMainMenu() {
        try {
            // 获取当前 Stage
            Stage stage = (Stage) mainMenuLabel.getScene().getWindow();

            // 创建 MainMenu 实例
            com.example.addressbook.MainMenu mainMenu = new com.example.addressbook.MainMenu();

            // 构建 MainMenu 场景
            Scene mainScene = mainMenu.buildScene(stage);

            // 切换场景
            stage.setScene(mainScene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
