package typingNinja.controllers;

import typingNinja.MainLessonDAO;
import typingNinja.auth.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class FreeTypeSelectController {

    @FXML private ToggleGroup durationGroup;
    @FXML private ToggleButton dur1, dur3, dur5, dur10;

    private final MainLessonDAO dao = new MainLessonDAO();

    @FXML
    private void initialize() {
        if (dur3 != null) dur3.setSelected(true); // default
    }

    @FXML
    private void onDurationChanged() {
        // nothing extra; ToggleGroup ensures exclusivity
    }

    @FXML
    private void onBack() {
        closeWindow();
    }

    @FXML
    private void onGenerate() {
        int duration = getSelectedDuration();
        dao.insertFreeType(Session.getCurrentUserId(), duration);

        try {
            Stage popup = (Stage) dur3.getScene().getWindow();
            Stage owner = (Stage) popup.getOwner();

            Parent root = FXMLLoader.load(getClass().getResource("/com/example/addressbook/LessonActivePage.fxml"));
            owner.getScene().setRoot(root);
            owner.setTitle("Typing - Typing Ninja");
            owner.centerOnScreen();

            popup.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getSelectedDuration() {
        Toggle t = durationGroup.getSelectedToggle();
        if (t == dur1) return 1;
        if (t == dur3) return 3;
        if (t == dur5) return 5;
        if (t == dur10) return 10;
        return 3;
    }

    private void closeWindow() {
        Stage st = (Stage) dur3.getScene().getWindow();
        st.close();
    }
}
