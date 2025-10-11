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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
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
            startResumeCountdown();
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

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setWidth(bounds.getWidth() * 0.7);
        stage.setHeight(bounds.getHeight() * 0.5);
        stage.centerOnScreen();

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: rgba(14,11,43,0.92); -fx-background-radius: 24; -fx-padding: 40;");

        VBox container = new VBox(30);
        container.setAlignment(Pos.CENTER);

        Label logo = new Label("Typing Ninja");
        logo.setStyle("-fx-text-fill: white; -fx-font-size: 48px; -fx-font-weight: 800;");
        container.getChildren().add(logo);

        statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 28px; -fx-font-weight: 700;");
        container.getChildren().add(statusLabel);

        VBox buttons = new VBox(18);
        buttons.setAlignment(Pos.CENTER);

        resumeButton = createOverlayButton("Resume");
        resumeButton.setOnAction(e -> startResumeCountdown());

        settingsButton = createOverlayButton("Settings");
        settingsButton.setOnAction(e -> {
            resumeImmediately();
            if (settingsAction != null) settingsAction.run();
        });

        homeButton = createOverlayButton("Home");
        homeButton.setOnAction(e -> {
            resumeImmediately();
            if (homeAction != null) homeAction.run();
        });

        buttons.getChildren().addAll(resumeButton, settingsButton, homeButton);
        container.getChildren().add(buttons);
        root.setCenter(container);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            resumeImmediately();
        });
        return stage;
    }

    private Button createOverlayButton(String text) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setStyle("-fx-background-color: #2EFF04; -fx-text-fill: #0a1f05; -fx-font-size: 18px; -fx-font-weight: 700; " +
                "-fx-background-radius: 18; -fx-padding: 12 24; -fx-border-color: rgba(0,0,0,0.25); -fx-border-radius: 18; -fx-border-width: 1;");
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
