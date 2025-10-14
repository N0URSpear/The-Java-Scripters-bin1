package typingNinja.controllers;

import typingNinja.model.SettingsDAO;
import typingNinja.view.MainMenu;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class SettingsController {

    @FXML private StackPane outer;
    @FXML private VBox content;

    @FXML private ComboBox<String> cmbLanguage;
    @FXML private ComboBox<String> cmbTheme;
    @FXML private ComboBox<String> cmbFontSize;

    @FXML private CheckBox chkKeyboardSounds;
    @FXML private CheckBox chkStopOnErrors;
    @FXML private CheckBox chkErrorBuzzer;
    @FXML private CheckBox chkCelebration;

    @FXML private Label navMainMenu;
    @FXML private Label navProfile;
    @FXML private Label navSettings;

    private static final double BASE_WIDTH = 1920.0;
    private static final double BASE_HEIGHT = 1080.0;
    private static final int USER_ID = 1;

    private final SettingsDAO dao = new SettingsDAO();
    private boolean binding = false;

    @FXML
    private void initialize() {
        navSettings.setTextFill(Color.web("#2EFF04"));
        navSettings.setDisable(true);

        asButton(navMainMenu, this::goMainMenu);
        asButton(navProfile, () -> switchTo("/typingNinja/Profile.fxml", "Profile - Typing Ninja"));

        var rec = dao.fetch(USER_ID);
        binding = true;
        selectIfPresent(cmbLanguage, rec.displayLanguage);
        selectIfPresent(cmbTheme, rec.theme);
        selectIfPresent(cmbFontSize, rec.fontSize);
        chkKeyboardSounds.setSelected(rec.keyboardSounds);
        chkStopOnErrors.setSelected(rec.typingErrors);
        chkErrorBuzzer.setSelected(rec.typingErrorSounds);
        chkCelebration.setSelected(rec.lessonCompleteSound);
        binding = false;

        cmbLanguage.valueProperty().addListener((o, ov, nv) -> { if (!binding && nv != null) dao.update(USER_ID, "DisplayLanguage", nv); });
        cmbTheme.valueProperty().addListener((o, ov, nv) -> { if (!binding && nv != null) dao.update(USER_ID, "Theme", nv); });
        cmbFontSize.valueProperty().addListener((o, ov, nv) -> { if (!binding && nv != null) dao.update(USER_ID, "FontSize", nv); });
        chkKeyboardSounds.selectedProperty().addListener((o, ov, nv) -> { if (!binding) dao.update(USER_ID, "KeyboardSounds", nv); });
        chkStopOnErrors.selectedProperty().addListener((o, ov, nv) -> { if (!binding) dao.update(USER_ID, "TypingErrors", nv); });
        chkErrorBuzzer.selectedProperty().addListener((o, ov, nv) -> { if (!binding) dao.update(USER_ID, "TypingErrorSounds", nv); });
        chkCelebration.selectedProperty().addListener((o, ov, nv) -> { if (!binding) dao.update(USER_ID, "LessonCompleteSound", nv); });

        // Bind scaler after Stage is available
        javafx.application.Platform.runLater(() -> {
            Stage stage = (Stage) outer.getScene().getWindow();
            bindDpiNeutralScale(outer, content, stage);
        });
    }

    private <T> void selectIfPresent(ComboBox<T> box, T value) {
        if (box.getItems().contains(value)) {
            box.getSelectionModel().select(value);
        }
    }

    private void goMainMenu() {
        Stage stage = (Stage) navMainMenu.getScene().getWindow();
        MainMenu mm = new MainMenu();
        Scene scene = mm.buildScene(stage);
        stage.setScene(scene);
        stage.setTitle("Typing Ninja");
        stage.centerOnScreen();
        stage.setFullScreen(true);
        stage.setFullScreenExitHint("");
    }

    private void switchTo(String fxmlPath, String title) {
        try {
            Stage stage = (Stage) navProfile.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene current = stage.getScene();
            if (current == null) {
                stage.setScene(new Scene(root));
            } else {
                current.setRoot(root);
            }
            stage.setTitle(title);
            stage.centerOnScreen();
            stage.setFullScreen(true);
            stage.setFullScreenExitHint("");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void asButton(Node node, Runnable onClick) {
        node.getStyleClass().add("button");
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> { if (onClick != null) onClick.run(); });
        node.addEventHandler(MouseEvent.MOUSE_PRESSED,  e -> { node.setScaleX(0.98); node.setScaleY(0.98); });
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { node.setScaleX(1.0);  node.setScaleY(1.0);  });
    }

    // Scene-based DPI-neutral scaling (logical pixels only; no explicit DPI math)
    private void bindDpiNeutralScale(StackPane outerPane, Region contentRoot, Stage stage) {
        final double EDGE_MARGIN = 64.0;

        Runnable apply = () -> {
            Scene sc = outerPane.getScene();
            double w = (sc != null && sc.getWidth()  > 1) ? sc.getWidth()  : outerPane.getWidth();
            double h = (sc != null && sc.getHeight() > 1) ? sc.getHeight() : outerPane.getHeight();
            if (w <= 1 || h <= 1) return;

            double wEff = Math.max(0, w - EDGE_MARGIN * 2);
            double hEff = Math.max(0, h - EDGE_MARGIN * 2);

            double sx = wEff / BASE_WIDTH;
            double sy = hEff / BASE_HEIGHT;
            double s  = Math.min(sx, sy);

            contentRoot.setScaleX(s);
            contentRoot.setScaleY(s);
        };

        outerPane.widthProperty().addListener((o, ov, nv) -> apply.run());
        outerPane.heightProperty().addListener((o, ov, nv) -> apply.run());
        outerPane.layoutBoundsProperty().addListener((o, ov, nv) -> apply.run());

        outerPane.sceneProperty().addListener((o, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.widthProperty().addListener((oo, ov, nv) -> apply.run());
                newScene.heightProperty().addListener((oo, ov, nv) -> apply.run());
                newScene.windowProperty().addListener((ooo, oldW, newW) -> {
                    if (newW instanceof Stage st) {
                        st.widthProperty().addListener((oooo, ov, nv) -> apply.run());
                        st.heightProperty().addListener((oooo, ov, nv) -> apply.run());
                        st.maximizedProperty().addListener((oooo, ov, nv) -> javafx.application.Platform.runLater(apply));
                        st.fullScreenProperty().addListener((oooo, ov, nv) -> javafx.application.Platform.runLater(apply));
                        st.showingProperty().addListener((oooo, was, is) -> { if (is) javafx.application.Platform.runLater(apply); });
                        javafx.application.Platform.runLater(apply);
                        javafx.application.Platform.runLater(apply);
                    }
                });
                javafx.application.Platform.runLater(apply);
                javafx.application.Platform.runLater(apply);
            }
        });

        if (stage != null) {
            stage.widthProperty().addListener((o, ov, nv) -> apply.run());
            stage.heightProperty().addListener((o, ov, nv) -> apply.run());
            stage.maximizedProperty().addListener((o, ov, nv) -> javafx.application.Platform.runLater(apply));
            stage.fullScreenProperty().addListener((o, ov, nv) -> javafx.application.Platform.runLater(apply));
            stage.showingProperty().addListener((o, ov, nv) -> { if (nv) javafx.application.Platform.runLater(apply); });
        }

        javafx.application.Platform.runLater(apply);
        javafx.application.Platform.runLater(apply);
    }
}
