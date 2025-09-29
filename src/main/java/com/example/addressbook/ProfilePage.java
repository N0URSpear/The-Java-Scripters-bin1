package com.example.addressbook;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class ProfilePage extends BorderPane {

    public ProfilePage(int userId) {
        setStyle("-fx-background-color: #0A002C;");

        // === 顶部：标题 + 编辑按钮 ===
        HBox topBar = new HBox();
        topBar.setPadding(new Insets(20, 40, 20, 40));
        topBar.setAlignment(Pos.CENTER);

        Label title = new Label("PROFILE");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Jaro", FontWeight.EXTRA_BOLD, 80));
        HBox.setHgrow(title, Priority.ALWAYS);

        // ✏ 按钮
        Image img = new Image(getClass().getResourceAsStream("/com/example/addressbook/pencil-write-tool-icon.png"));
        ImageView pencilIcon = new ImageView(img);
        pencilIcon.setFitWidth(40);
        pencilIcon.setFitHeight(40);

        Button editUserBtn = new Button();
        editUserBtn.setGraphic(pencilIcon);
        editUserBtn.setStyle("-fx-background-color: transparent;");
        editUserBtn.setOnAction(e -> openEditUserDialog());

        topBar.getChildren().addAll(title, editUserBtn);
        setTop(topBar);

        // === 左右统计面板 ===
        VBox leftBox = new VBox(20);
        leftBox.setPadding(new Insets(20));
        leftBox.setAlignment(Pos.TOP_LEFT);

        VBox rightBox = new VBox(20);
        rightBox.setPadding(new Insets(20));
        rightBox.setAlignment(Pos.TOP_LEFT);

        SqliteContactDAO dao = new SqliteContactDAO();

        // ✅ 先确保用户有初始化数据
        dao.safeInitUserData(userId);

        // ⚡ 再刷新最新统计（Lessons → Statistics）
        dao.recalcUserStatistics(userId);

        SqliteContactDAO.ProfileStats stats = dao.getUserGoalsAndStats(userId);

        if (stats != null) {
            leftBox.getChildren().addAll(
                    makeWhiteLabel("Practice hours per week\nGoal: " +
                            stats.getEstHours() + " | Actual: " + stats.getTimeActiveWeek()),
                    makeWhiteLabel("Target speed (WPM)\nGoal: " +
                            stats.getEstWPM() + " | Actual: " + stats.getTotalWPMWeek()),
                    makeWhiteLabel("Target accuracy (%)\nGoal: " +
                            stats.getEstAccuracy() + " | Actual: " + stats.getTotalAccuracyWeek())
            );

            rightBox.getChildren().addAll(
                    makeWhiteLabel("Belt: " + stats.getBelt()),
                    makeWhiteLabel("Lessons completed: " + stats.getTotalLessons()),
                    makeWhiteLabel("Average WPM: " + stats.getAvgWPM()),
                    makeWhiteLabel("Highest Rating: " + stats.getHighestRating()),
                    makeWhiteLabel("Average Rating: " + stats.getAvgRating()),
                    makeWhiteLabel("Total Ninja Stars Earned: " + stats.getTotalStars())
            );
        }

        HBox centerBox = new HBox(100, leftBox, rightBox);
        centerBox.setAlignment(Pos.CENTER);
        setCenter(centerBox);
    }

    private Label makeWhiteLabel(String text) {
        Label lbl = new Label(text);
        lbl.setTextFill(Color.WHITE);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        return lbl;
    }

    // === 打开 EditUser 弹窗 ===
    private void openEditUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/addressbook/EditUserDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initOwner(this.getScene().getWindow());
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
