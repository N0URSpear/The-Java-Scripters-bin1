package typingNinja.controllers;

import typingNinja.MainLessonDAO;
import typingNinja.auth.Session;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class SubLessonSelectController {

    @FXML private Label headerLabel;

    @FXML private Button btn1a, btn1b, btn1c, btn1d, btn1e, btn1f;
    @FXML private Button backButton;

    private final MainLessonDAO dao = new MainLessonDAO();

    private String codePrefix = "1"; // "1","2","3","4"
    private int userId = Session.getCurrentUserId();

    private String pendingTitle = null;
    private String[] pendingLeftTexts = null;

    /** Called by MainMenu after FXML load. */
    public void configure(String title, String prefix, String[] leftTexts) {
        if (prefix != null) codePrefix = prefix;
        pendingTitle = title;
        pendingLeftTexts = leftTexts;
        applyConfigToNodes();
    }

    @FXML
    private void initialize() {
        if (backButton != null) {
            backButton.setOnAction(e -> ((Stage) backButton.getScene().getWindow()).close());
        }

        if (btn1a != null) btn1a.setOnAction(e -> pick("a"));
        if (btn1b != null) btn1b.setOnAction(e -> pick("b"));
        if (btn1c != null) btn1c.setOnAction(e -> pick("c"));
        if (btn1d != null) btn1d.setOnAction(e -> pick("d"));
        if (btn1e != null) btn1e.setOnAction(e -> pick("e"));
        if (btn1f != null) btn1f.setOnAction(e -> pick("f"));

        applyConfigToNodes();
    }

    private void applyConfigToNodes() {
        if (pendingTitle != null && headerLabel != null) {
            headerLabel.setText(pendingTitle);
        }
        if (pendingLeftTexts != null && pendingLeftTexts.length >= 6) {
            if (btn1a != null) btn1a.setText(pendingLeftTexts[0]);
            if (btn1b != null) btn1b.setText(pendingLeftTexts[1]);
            if (btn1c != null) btn1c.setText(pendingLeftTexts[2]);
            if (btn1d != null) btn1d.setText(pendingLeftTexts[3]);
            if (btn1e != null) btn1e.setText(pendingLeftTexts[4]);
            if (btn1f != null) btn1f.setText(pendingLeftTexts[5]);
        }
    }

    private void pick(String letter) {
        userId = Session.getCurrentUserId();
        String lessonType = codePrefix + letter;
        dao.insertSelection(userId, lessonType);

        try {
            Stage popup = (Stage) backButton.getScene().getWindow();
            Stage owner = (Stage) popup.getOwner();

            Parent root = FXMLLoader.load(getClass().getResource("/typingNinja/LessonActivePage.fxml"));
            owner.getScene().setRoot(root);
            owner.setTitle("Typing - Typing Ninja");
            owner.centerOnScreen();

            popup.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
