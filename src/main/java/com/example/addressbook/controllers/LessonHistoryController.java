package com.example.addressbook.controllers;

import com.example.addressbook.SessionManager;
import com.example.addressbook.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.Button;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import javafx.scene.image.WritableImage;
import javafx.scene.control.Alert;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.sql.*;
import javafx.embed.swing.SwingFXUtils;

public class LessonHistoryController {

    @FXML private VBox lessonList;
    @FXML private LineChart<String, Number> lineChart;
    @FXML private Label personalBestLabel;  // ✅ 新增：显示个人最佳结果
    @FXML private Button backBtn;
    @FXML private Label mainMenuLabel;
    @FXML private Label profileLabel;
    @FXML private Button exportCsvBtn;      // ✅ 新增
    @FXML private Button exportPdfBtn;      // ✅ 新增

    private final Connection conn = com.example.addressbook.SqliteConnection.getInstance();
    private final SqliteContactDAO dao = new SqliteContactDAO();

    @FXML
    public void initialize() {
        int userId = SessionManager.getCurrentUserId();
        if (userId <= 0) {
            System.out.println("⚠ No user logged in — LessonHistory skipped");
            return;
        }

        // 原有逻辑
        loadLessonList(userId);
        plotLessonTrend(userId);

        // ✅ 新增逻辑：加载个人最佳
        loadPersonalBest(userId);

        mainMenuLabel.setOnMouseClicked(e -> openMainMenu());
        profileLabel.setOnMouseClicked(e -> openProfile());

        // ✅ 新增：导出功能
        exportCsvBtn.setOnAction(e -> exportToCsv());
        exportPdfBtn.setOnAction(e -> exportToPdf());
    }

    private void loadLessonList(int userId) {
        lessonList.getChildren().clear();

        String sql = "SELECT LessonID, WPM, StarRating, DateStarted FROM Lesson WHERE UserID = ? ORDER BY DateStarted DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                double wpm = rs.getDouble("WPM");
                double stars = rs.getDouble("StarRating");
                String date = rs.getString("DateStarted");

                HBox row = new HBox(20);
                row.setStyle("-fx-background-color: #1C0A40; -fx-padding: 10; -fx-background-radius: 8;");
                row.getChildren().add(new Label("WPM: " + wpm));
                row.getChildren().add(new Label(date));

                HBox starsBox = new HBox(5);
                for (int i = 0; i < 5; i++) {
                    Image img = new Image(
                            getClass().getResourceAsStream(
                                    i < stars
                                            ? "/com/example/addressbook/gold_shuriken.png"
                                            : "/com/example/addressbook/gray_shuriken.png"
                            )
                    );
                    ImageView iv = new ImageView(img);
                    iv.setFitWidth(18);
                    iv.setFitHeight(18);
                    starsBox.getChildren().add(iv);
                }
                row.getChildren().add(starsBox);

                lessonList.getChildren().add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void plotLessonTrend(int userId) {
        lineChart.getData().clear();

        String sql = "SELECT DateStarted, WPM FROM Lesson WHERE UserID = ? ORDER BY DateStarted";
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Typing Speed (WPM)");

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String date = rs.getString("DateStarted");
                double wpm = rs.getDouble("WPM");
                series.getData().add(new XYChart.Data<>(date, wpm));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        lineChart.getData().add(series);
    }

    // ✅ 新增：读取数据库中最高 WPM 的记录并显示
    private void loadPersonalBest(int userId) {
        String sql = """
            SELECT WPM, Accuracy, DateStarted
            FROM Lesson
            WHERE UserID = ?
            ORDER BY WPM DESC
            LIMIT 1
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int wpm = rs.getInt("WPM");
                double acc = rs.getDouble("Accuracy");
                String date = rs.getString("DateStarted");

                String formattedAccuracy = String.format("%.0f%%", acc);
                personalBestLabel.setText(String.format(
                        "Personal Best Full Keyboard Result: %d WPM at %s accuracy (achieved %s)",
                        wpm, formattedAccuracy, date
                ));
            } else {
                personalBestLabel.setText("No personal best data available.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            personalBestLabel.setText("Error loading personal best.");
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            // 加载目标页面 FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/addressbook/ProfilePage.fxml"));
            Parent root = loader.load();

            // 获取当前 Stage
            Stage stage = (Stage) backBtn.getScene().getWindow();

            // ✅ 记住当前窗口尺寸
            double width = stage.getWidth();
            double height = stage.getHeight();

            // ✅ 切换页面但保持尺寸不变
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

    private void openProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/addressbook/ProfilePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) profileLabel.getScene().getWindow();
            double w = stage.getWidth();
            double h = stage.getHeight();

            Scene scene = new Scene(root, w, h);
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ 新增：导出 CSV
    private void exportToCsv() {
        String sql = "SELECT LessonID, WPM, Accuracy, StarRating, DateStarted FROM Lesson WHERE UserID = ?";
        int userId = SessionManager.getCurrentUserId();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Lesson History as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("LessonID,WPM,Accuracy,Stars,DateStarted");

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    writer.printf("%d,%.0f,%.1f,%d,%s%n",
                            rs.getInt("LessonID"),
                            rs.getDouble("WPM"),
                            rs.getDouble("Accuracy"),
                            rs.getInt("StarRating"),
                            rs.getString("DateStarted"));
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Export Successful");
            alert.setContentText("Lesson history exported to:\n" + file.getAbsolutePath());
            alert.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exportToPdf() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Lesson History as PDF");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File file = fileChooser.showSaveDialog(null);
            if (file == null) return;

            // ✅ 只截图左侧 lessonList，而不是整个 Scene
            WritableImage snapshot = lessonList.snapshot(null, null);
            BufferedImage buffered = SwingFXUtils.fromFXImage(snapshot, null);

            // 生成 PDF
            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            // ✅ 把截图内容写入 PDF
            com.itextpdf.text.Image listImage = com.itextpdf.text.Image.getInstance(buffered, null);
            listImage.scaleToFit(500, 700);
            listImage.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(listImage);

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Export Successful");
            alert.setContentText("Lesson History (left panel) exported as PDF:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
