package typingNinja.controllers;

import typingNinja.MainLessonDAO;
import typingNinja.auth.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CustomTopicSelectController {

    @FXML private ToggleGroup sourceGroup;
    @FXML private ToggleButton btnWeakKeys;
    @FXML private ToggleButton btnCustom;

    @FXML private TextField promptField;

    @FXML private ToggleGroup durationGroup;
    @FXML private ToggleButton dur1, dur3, dur5, dur10;

    @FXML private CheckBox chkUpper, chkNums, chkPunct, chkSpecial;

    private final MainLessonDAO dao = new MainLessonDAO();

    @FXML
    private void initialize() {
        btnCustom.setSelected(true);
        if (dur3 != null) dur3.setSelected(true);

        promptField.textProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.length() > 50) {
                promptField.setText(newV.substring(0, 50));
            }
        });

        applySourceState();
    }

    @FXML
    private void onSourceChanged() {
        applySourceState();
    }

    private void applySourceState() {
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

    @FXML
    private void onDurationChanged() {
        // nothing extra needed
    }

    @FXML
    private void onBack() {
        closeWindow();
    }

    @FXML
    private void onGenerate() {
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
        Toggle t = durationGroup.getSelectedToggle();
        if (t == dur1) return 1;
        if (t == dur3) return 3;
        if (t == dur5) return 5;
        if (t == dur10) return 10;
        return 3;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void closeWindow() {
        Stage st = (Stage) promptField.getScene().getWindow();
        st.close();
    }
}
