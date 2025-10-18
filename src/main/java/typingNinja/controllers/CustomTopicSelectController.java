package typingNinja.controllers;

import typingNinja.model.MainLessonDAO;
import typingNinja.model.auth.Session;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import typingNinja.util.SceneNavigator;

/**
 * Controller backing the custom topic modal where students choose AI prompt settings.
 */
public class CustomTopicSelectController {

    @FXML private ToggleGroup sourceGroup;
    @FXML private ToggleButton btnWeakKeys;
    @FXML private ToggleButton btnCustom;

    @FXML private TextField promptField;

    @FXML private ToggleGroup durationGroup;
    @FXML private ToggleButton dur1, dur3, dur5, dur10;

    @FXML private CheckBox chkUpper, chkNums, chkPunct, chkSpecial;

    private final MainLessonDAO dao = new MainLessonDAO();

    /**
     * Initialises toggle defaults and field constraints.
     */
    @FXML
    private void initialize() {
        // Default to the custom prompt path and clamp the text field length up front.
        btnCustom.setSelected(true);
        if (dur3 != null) dur3.setSelected(true);

        promptField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > 50) {
                promptField.setText(newV.substring(0, 50));
            }
        });

        applySourceState();
    }

    /**
     * Updates the prompt field when the source toggle changes.
     */
    @FXML
    private void onSourceChanged() {
        // Flip the form between the weak-key preset and freeform topic entry.
        applySourceState();
    }

    private void applySourceState() {
        // When weak-key practice is enabled we lock the prompt so it matches the lesson logic.
        boolean isWeak = btnWeakKeys.isSelected();
        if (isWeak) {
            promptField.setText("PracticeWeakKeyCombos");
            promptField.setEditable(false);
        } else {
            if ("PracticeWeakKeyCombos".equals(promptField.getText())) {
                promptField.clear();
            }
            promptField.setEditable(true);
        }
    }

    /**
     * FXML hook retained for completeness; no extra logic required.
     */
    @FXML
    private void onDurationChanged() {
        // Method exists mainly for FXML binding symmetry; duration reads happen on submit.
    }

    /**
     * Closes the modal without launching a lesson.
     */
    @FXML
    private void onBack() {
        // Simply close the modal; caller remains on the selection menu.
        closeWindow();
    }

    /**
     * Validates the selections, persists the lesson row, and transitions into the active view.
     */
    @FXML
    private void onGenerate() {
        // Validate inputs, stash the lesson choice, and jump straight into the active lesson.
        int duration = getSelectedDuration();
        boolean upper = chkUpper.isSelected();
        boolean nums = chkNums.isSelected();
        boolean punct = chkPunct.isSelected();
        boolean special = chkSpecial.isSelected();

        String prompt;
        if (btnWeakKeys.isSelected()) {
            prompt = "PracticeWeakKeyCombos";
        } else {
            prompt = promptField.getText() == null ? "" : promptField.getText().trim();
            if (prompt.isEmpty()) {
                showError("Please enter a topic (max 50 characters).");
                return;
            }
        }

        dao.insertCustomTopic(Session.getCurrentUserId(), prompt, duration, upper, nums, punct, special);

        try {
            Stage popup = (Stage) promptField.getScene().getWindow();
            Stage owner = (Stage) popup.getOwner();

            SceneNavigator.load(owner, "/typingNinja/LessonActivePage.fxml", "Typing - Typing Ninja");

            popup.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return selected duration in minutes, falling back to three if undefined
     */
    private int getSelectedDuration() {
        // Toggle group only surfaces the button, so translate it back into minutes.
        Toggle t = durationGroup.getSelectedToggle();
        if (t == dur1) return 1;
        if (t == dur3) return 3;
        if (t == dur5) return 5;
        if (t == dur10) return 10;
        return 3;
    }

    /**
     * Displays a simple blocking error dialog.
     */
    private void showError(String msg) {
        // Use a simple blocking alert to keep input focus in this dialog.
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    /**
     * Closes the current window safely.
     */
    private void closeWindow() {
        // Drop the dialog without touching the owner scene.
        Stage st = (Stage) promptField.getScene().getWindow();
        st.close();
    }
}
