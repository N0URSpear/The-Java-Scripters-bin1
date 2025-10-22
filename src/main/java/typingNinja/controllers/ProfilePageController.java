package typingNinja.controllers;

import typingNinja.model.SessionManager;
import typingNinja.model.SqliteContactDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import typingNinja.util.SceneNavigator;

import java.util.Optional;

/**
 * Controller class for the Profile Page of the Typing Ninja application.
 * This controller manages the display and interaction of user profile information,
 * including training goals, performance statistics, belt progress, and star ratings.
 * It also handles navigation to other pages such as the Lesson History, Settings,
 * and Main Menu, as well as editing user information and goals.
 * All UI elements are bound through FXML and populated dynamically using data
 * retrieved from the {@link SqliteContactDAO} and {@link SessionManager}.
 */
public class ProfilePageController {

    // Header
    @FXML private ImageView avatarImage;
    @FXML private Label usernameLabel;
    @FXML private Button editUserBtn;

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

    /**
     * Initializes the controller after its FXML elements have been loaded.
     * This method wires up button handlers, ensures that user data exists,
     * loads profile statistics, and sets navigation events for menu labels.
     */
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

    /**
     * Ensures that user-related data exists in the database and refreshes
     * the displayed statistics on the profile page.
     * It retrieves the current user's name from the SessionManager,
     * initializes the user's data if necessary, recalculates statistics,
     * and updates the display.
     */
    private void ensureDataAndRefresh() {
        String name = SessionManager.getCurrentUsername();
        if (name != null && !name.isBlank()) usernameLabel.setText(name);
        dao.safeInitUserData(userId);
        dao.recalcUserStatistics(userId);
        loadStats();
    }

    private enum GoalField { HOURS, WPM, ACCURACY }

    /**
     * Links an edit pencil button to a dialog for updating the user's goal values to set goal hours, WPM, or accuracy.
     */
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

    /**
     * Loads and updates all user statistics displayed on the profile page.
     * <p>
     * Retrieves goal and performance data (hours, WPM, accuracy, stars, belt level, etc.)
     * from the database through {@link SqliteContactDAO}, then populates all related
     * JavaFX labels and icons.
     */
    private void loadStats() {
        SqliteContactDAO.ProfileStats s = dao.getUserGoalsAndStats(userId);
        if (s == null) return;

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

    /**
     * Parses a double safely from a string.
     * Returns 0.0 if parsing fails.
     */
    private double parseDoubleSafe(String s) { try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; } }

    /**
     * Fills the specified HBox with star (shuriken) icons to visually represent a rating.
     *
     * @param box    the HBox container for the icons
     * @param rating the numeric rating
     */
    private void fillRatingWithShuriken(HBox box, double rating) {
        box.getChildren().clear();

        int full = (int) Math.floor(Math.max(0, Math.min(5, rating)));

        Image gold = new Image(getClass().getResource("/images/star_on.png").toExternalForm());
        Image gray = new Image(getClass().getResource("/images/star_off.png").toExternalForm());

        for (int i = 0; i < 5; i++) {
            Image img = (i < full ? gold : gray);
            ImageView iv = new ImageView(img);
            iv.setPreserveRatio(true);
            iv.setFitWidth(24);
            iv.setFitHeight(24);
            box.getChildren().add(iv);
        }
    }

    /**
     * Opens the "Edit User" dialog popup, allowing the user to see their
     * profile details such as username, password, or security questions.
     * Once closed, it refreshes the username label and recalculates statistics.
     */
    private void openEditUserDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/EditUserDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            String name = SessionManager.getCurrentUsername();
            if (name != null && !name.isBlank()) usernameLabel.setText(name);
            dao.recalcUserStatistics(userId);
            loadStats();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Navigates to the Lesson History page where users can view past typing sessions.
     * This method loads a new FXML scene, maintains window size, and passes control
     * to the {@link typingNinja.controllers.LessonHistoryController}.
     */
    @FXML
    private void openLessonHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/LessonHistoryPage.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) lessonHistoryBtn.getScene().getWindow();

            typingNinja.controllers.LessonHistoryController controller = loader.getController();
            controller.setReturnToProfile(true);

            Scene scene = new Scene(root, stage.getWidth(), stage.getHeight());
            stage.setScene(scene);

            stage.setFullScreen(true);

            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Opens the application settings page.
     * Uses {@link SceneNavigator} to replace the current scene with the settings view.
     */
    private void openSettings() {
        try {
            Stage stage = (Stage) settingsLabel.getScene().getWindow();
            SceneNavigator.load(stage,"/typingNinja/Settings.fxml", "Settings - Typing Ninja");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns to the main menu screen of Typing Ninja.
     * This method instantiates {@link typingNinja.view.MainMenu} and displays it
     * in the current window stage.
     */
    private void openMainMenu() {
        try {
            Stage stage = (Stage) mainMenuLabel.getScene().getWindow();
            typingNinja.view.MainMenu mm = new typingNinja.view.MainMenu();
            mm.show(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
