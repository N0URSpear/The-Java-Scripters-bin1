package com.example.addressbook.controllers;

import com.example.addressbook.SessionManager;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

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

    private final SqliteContactDAO dao = new SqliteContactDAO();
    private int userId = -1;
    private boolean initialized = false;

    private static final String GOLD_SHURIKEN = "/com/example/addressbook/gold_shuriken.png";
    private static final String GRAY_SHURIKEN = "/com/example/addressbook/gray_shuriken.png";

    public ProfilePageController() {}

    public void setUserId(int userId) {
        this.userId = userId;
        if (userId > 0) {
            ensureDataAndRefresh(); // ✅ 始终刷新
        }
    }

    @FXML
    private void initialize() {
        initialized = true;

        editUserBtn.setOnAction(e -> openEditUserDialog());
        wireGoalEditor(editHoursBtn, GoalField.HOURS);
        wireGoalEditor(editWpmBtn,   GoalField.WPM);
        wireGoalEditor(editAccBtn,   GoalField.ACCURACY);

        lessonHistoryBtn.getStyleClass().add("action-button");
        certBtn.getStyleClass().add("action-button");
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
        int full = (int) Math.round(Math.max(0, Math.min(5, rating)));
        Image gold = safeImage(GOLD_SHURIKEN);
        Image gray = safeImage(GRAY_SHURIKEN);

        for (int i = 0; i < 5; i++) {
            Image img = (i < full ? gold : gray);
            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setPreserveRatio(true);
                iv.setFitWidth(22);
                iv.setFitHeight(22);
                box.getChildren().add(iv);
            } else {
                Label star = new Label(i < full ? "★" : "☆");
                star.setStyle("-fx-text-fill: gold; -fx-font-size: 18;");
                box.getChildren().add(star);
            }
        }
    }

    private Image safeImage(String path) {
        try { Image img = new Image(path, true); return img.isError() ? null : img; }
        catch (Exception e) { return null; }
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
}
