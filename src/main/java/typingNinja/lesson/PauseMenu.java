package typingNinja.lesson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Effect;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Handles lesson pausing behaviour, including a modal overlay with navigation actions.
 */
public class PauseMenu {
    private final Button pauseBtn;
    private final Metrics metrics;
    private final TextArea input;
    private final Runnable settingsAction;
    private final Runnable homeAction;

    private boolean paused = false;
    private Stage overlayStage;
    private Label statusLabel;
    private Button resumeButton;
    private Button settingsButton;
    private Button homeButton;
    private Timeline resumeTimeline;
    private Node blurredNode;
    private Effect previousEffect;

    public PauseMenu(Button pauseBtn, Metrics metrics, TextArea input,
                     Runnable settingsAction, Runnable homeAction) {
        this.pauseBtn = pauseBtn;
        this.metrics = metrics;
        this.input = input;
        this.settingsAction = settingsAction;
        this.homeAction = homeAction;
        this.pauseBtn.setOnAction(e -> togglePause());
        render();
    }

    private void togglePause() {
        if (!paused) {
            paused = true;
            metrics.pause();
            input.setDisable(true);
            showOverlay();
            render();
        }
        else {
            startResumeCountdownUpper();
        }
    }

    private void showOverlay() {
        if (overlayStage == null) overlayStage = buildOverlay();
        Stage owner = pauseBtn.getScene() != null ? (Stage) pauseBtn.getScene().getWindow() : null;
        if (owner != null && owner.getScene() != null) {
            blurredNode = owner.getScene().getRoot();
            previousEffect = blurredNode.getEffect();
            blurredNode.setEffect(new GaussianBlur(25));
        }
        if (statusLabel != null) statusLabel.setText("");
        enableButtons(true);
        overlayStage.centerOnScreen();
        overlayStage.show();
        overlayStage.toFront();
    }

    private Stage buildOverlay() {
        Stage owner = pauseBtn.getScene() != null ? (Stage) pauseBtn.getScene().getWindow() : null;
        Stage stage = new Stage(StageStyle.TRANSPARENT);
        if (owner != null) {
            stage.initOwner(owner);
            stage.initModality(Modality.WINDOW_MODAL);
        }

        stage.setResizable(false);

        // size will be determined by content; we'll center after sizing

        BorderPane root = new BorderPane();
        // Solid project purple (no transparency) + tighter outer padding
        root.setStyle("-fx-background-color: #140B38; -fx-background-radius: 24; -fx-padding: 24;");

        // Content row: image immediately next to buttons (no spacing)
        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(0);
        row.setAlignment(Pos.CENTER);

        // Logo image
        ImageView logoView = new ImageView();
        try {
            Image logoImg = new Image(getClass().getResource("/typingNinja/Images/Typing_Ninja_with_text.png").toExternalForm(),
                    440, 0, true, true);
            logoView.setImage(logoImg);
            logoView.setPreserveRatio(true);
        } catch (Exception ignored) {}

        // Status label under logo (kept within a VBox)
        VBox leftBox = new VBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: 900;");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setWrapText(true);
        statusLabel.setTextAlignment(TextAlignment.CENTER);
        statusLabel.setAlignment(Pos.CENTER);
        leftBox.getChildren().addAll(logoView, statusLabel);

        // Vertical buttons column with header
        VBox buttons = new VBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);
        Label pausedHeader = new Label("LESSON PAUSED");
        pausedHeader.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: 900;");
        // Use the provided icon filenames from resources
        resumeButton = createOverlayButton("Resume", "play_button.png");
        resumeButton.setOnAction(e -> startResumeCountdownUpper());
        settingsButton = createOverlayButton("Settings", "settings_button.png");
        settingsButton.setOnAction(e -> {
            resumeImmediately();
            if (settingsAction != null) settingsAction.run();
        });
        homeButton = createOverlayButton("Home", "home_button.png");
        homeButton.setOnAction(e -> {
            resumeImmediately();
            if (homeAction != null) homeAction.run();
        });
        buttons.getChildren().addAll(pausedHeader, resumeButton, settingsButton, homeButton);
        // Place logo immediately adjacent to buttons (no spacing) and set fixed size relative to screen
        row.getChildren().addAll(leftBox, buttons);
        try {
            Rectangle2D vb = Screen.getPrimary().getVisualBounds();
            double targetH = vb.getHeight() * 0.40;
            double targetW = vb.getWidth() * 0.35;
            logoView.setPreserveRatio(true);
            logoView.setFitHeight(targetH);
            logoView.setFitWidth(targetW);
            logoView.setSmooth(true);
        } catch (Exception ignored) {}
        root.setCenter(row);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setOnCloseRequest(e -> {
            e.consume();
            resumeImmediately();
        });
        return stage;
    }

    // removed height-binding helper to avoid layout oscillations

    private Button createOverlayButton(String text, String iconFileName) {
        Button btn = new Button(text.toUpperCase());
        btn.setPrefWidth(260);
        btn.setStyle("-fx-background-color: #2EFF04; -fx-text-fill: #0a1f05; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;");
        // Add icon on the right: load if present, else a fixed-size placeholder region
        try {
            var url = getClass().getResource("/typingNinja/Images/" + iconFileName);
            if (url != null) {
                ImageView iv = new ImageView(new Image(url.toExternalForm()));
                iv.setFitHeight(22);
                iv.setPreserveRatio(true);
                btn.setGraphic(iv);
            } else {
                Region placeholder = new Region();
                placeholder.setPrefSize(22, 22);
                btn.setGraphic(placeholder);
            }
            btn.setContentDisplay(ContentDisplay.RIGHT);
            btn.setGraphicTextGap(12);
        } catch (Exception ignored) {
            Region placeholder = new Region();
            placeholder.setPrefSize(22, 22);
            btn.setGraphic(placeholder);
            btn.setContentDisplay(ContentDisplay.RIGHT);
            btn.setGraphicTextGap(12);
        }
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #29d200; -fx-text-fill: #071604; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: #2EFF04; -fx-text-fill: #0a1f05; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;"));
        btn.setOnMousePressed(e -> btn.setStyle("-fx-background-color: #23b100; -fx-text-fill: #041002; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;"));
        btn.setOnMouseReleased(e -> btn.setStyle("-fx-background-color: #2EFF04; -fx-text-fill: #0a1f05; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;"));
        return btn;
    }

    private void startResumeCountdown() {
        if (resumeTimeline != null) resumeTimeline.stop();
        enableButtons(false);
        statusLabel.setText("Resuming in 3…");
        resumeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> statusLabel.setText("Resuming in 2…")),
                new KeyFrame(Duration.seconds(2), e -> statusLabel.setText("Resuming in 1…")),
                new KeyFrame(Duration.seconds(3), e -> {
                    statusLabel.setText("");
                    resumeFromOverlay();
                })
        );
        resumeTimeline.setCycleCount(1);
        resumeTimeline.playFromStart();
    }

    private void enableButtons(boolean enabled) {
        if (resumeButton != null) resumeButton.setDisable(!enabled);
        if (settingsButton != null) settingsButton.setDisable(!enabled);
        if (homeButton != null) homeButton.setDisable(!enabled);
    }

    // Uppercase, centered resume countdown text
    private void startResumeCountdownUpper() {
        if (resumeTimeline != null) resumeTimeline.stop();
        enableButtons(false);
        statusLabel.setText("RESUMING IN 3...");
        resumeTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> statusLabel.setText("RESUMING IN 2...")),
                new KeyFrame(Duration.seconds(2), e -> statusLabel.setText("RESUMING IN 1...")),
                new KeyFrame(Duration.seconds(3), e -> {
                    statusLabel.setText("");
                    resumeFromOverlay();
                })
        );
        resumeTimeline.setCycleCount(1);
        resumeTimeline.playFromStart();
    }

    private void resumeImmediately() {
        if (resumeTimeline != null) {
            resumeTimeline.stop();
            resumeTimeline = null;
        }
        statusLabel.setText("");
        enableButtons(true);
        resumeFromOverlay();
    }

    private void resumeFromOverlay() {
        if (!paused) return;
        if (overlayStage != null) {
            overlayStage.hide();
        }
        paused = false;
        if (blurredNode != null) {
            blurredNode.setEffect(previousEffect);
            blurredNode = null;
            previousEffect = null;
        }
        if (resumeTimeline != null) {
            resumeTimeline.stop();
            resumeTimeline = null;
        }
        input.setDisable(false);
        input.requestFocus();
        metrics.resume();
        render();
    }

    private void render() {
        pauseBtn.setText(paused ? "▶ Resume" : "⏸ Pause");
    }
}
