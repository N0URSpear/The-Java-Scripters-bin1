package typingNinja.controllers;

import typingNinja.model.SessionManager;
import typingNinja.model.SqliteContactDAO;
import typingNinja.view.MainMenu;
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
    @FXML private Label personalBestLabel;
    @FXML private Button backBtn;
    @FXML private Label mainMenuLabel;
    @FXML private Label profileLabel;
    @FXML private Label settingsLabel;
    @FXML private Button exportCsvBtn;
    @FXML private Button exportPdfBtn;

    private boolean returnToProfile = false;
    private final Connection conn = typingNinja.model.SqliteConnection.getInstance();
    private final SqliteContactDAO dao = new SqliteContactDAO();

    public void setReturnToProfile(boolean value) {
        this.returnToProfile = value;
    }

    @FXML
    public void initialize() {
        int userId = SessionManager.getCurrentUserId();
        if (userId <= 0) {
            System.out.println("⚠ No user logged in — LessonHistory skipped");
            return;
        }

        loadLessonList(userId);
        plotLessonTrend(userId);
        loadPersonalBest(userId);

        mainMenuLabel.setOnMouseClicked(e -> openMainMenu());
        profileLabel.setOnMouseClicked(e -> openProfilePage());
        settingsLabel.setOnMouseClicked(e -> openSettings());
        backBtn.setOnAction(e -> openProfilePage());

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
                                            ? "/images/star_on.png"
                                            : "/images/star_off.png"
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

    private void openMainMenu() {
        try {
            Stage stage = (Stage) mainMenuLabel.getScene().getWindow();

            MainMenu mm = new MainMenu();
            mm.show(stage);

            stage.setFullScreen(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openProfilePage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/ProfilePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) profileLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            if (returnToProfile) {
                stage.setFullScreen(true);
            } else {
                stage.setFullScreen(true);
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/Settings.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) settingsLabel.getScene().getWindow();
            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            stage.setFullScreen(true);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

            WritableImage snapshot = lessonList.snapshot(null, null);
            BufferedImage buffered = SwingFXUtils.fromFXImage(snapshot, null);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(file));
            document.open();

            com.itextpdf.text.Image listImage = com.itextpdf.text.Image.getInstance(buffered, null);
            listImage.scaleToFit(500, 700);
            listImage.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
            document.add(listImage);

            document.close();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setHeaderText("Export Successful");
            alert.setContentText("Lesson History exported as PDF:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onBackClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/ProfilePage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) backBtn.getScene().getWindow();

            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            if (returnToProfile) {
                stage.setFullScreen(true);
            }

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
