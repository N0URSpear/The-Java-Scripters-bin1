package typingNinja.controllers;

import typingNinja.model.MainLessonDAO;
import typingNinja.model.auth.Session;
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
        // Default to the mid-length session so the modal feels ready out of the gate.
        if (dur3 != null) dur3.setSelected(true);
    }

    @FXML
    private void onDurationChanged() {
        // Provided for FXML completeness; duration is consumed when the user confirms.
    }

    @FXML
    private void onBack() {
        // Close the dialog without altering the calling scene.
        closeWindow();
    }

    @FXML
    private void onGenerate() {
        // Record the free-typing choice and pivot the main stage into the active lesson.
        int duration = getSelectedDuration();
        dao.insertFreeType(Session.getCurrentUserId(), duration);

        try {
            Stage popup = (Stage) dur3.getScene().getWindow();
            Stage owner = (Stage) popup.getOwner();

            Parent root = FXMLLoader.load(getClass().getResource("/typingNinja/LessonActivePage.fxml"));
            owner.getScene().setRoot(root);
            owner.setTitle("Typing - Typing Ninja");
            owner.centerOnScreen();
            owner.setFullScreen(true);
            owner.setFullScreenExitHint("");

            popup.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private int getSelectedDuration() {
        // Map whichever toggle is hot back into its minute count.
        Toggle t = durationGroup.getSelectedToggle();
        if (t == dur1) return 1;
        if (t == dur3) return 3;
        if (t == dur5) return 5;
        if (t == dur10) return 10;
        return 3;
    }

    private void closeWindow() {
        // Helper for both back and success flows.
        Stage st = (Stage) dur3.getScene().getWindow();
        st.close();
    }
}
