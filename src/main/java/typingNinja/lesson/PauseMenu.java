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
        // Solid project purple (no transparency) + outer padding
        root.setStyle("-fx-background-color: #140B38; -fx-background-radius: 24; -fx-padding: 40;");

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
        statusLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 24px; -fx-font-weight: 700;");
        leftBox.getChildren().addAll(logoView, statusLabel);

        // Vertical buttons with no horizontal gap from logo
        VBox buttons = new VBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);
        resumeButton = createOverlayButton("Resume", "resume.png");
        resumeButton.setOnAction(e -> startResumeCountdown());
        settingsButton = createOverlayButton("Settings", "settings.png");
        settingsButton.setOnAction(e -> {
            resumeImmediately();
            if (settingsAction != null) settingsAction.run();
        });
        homeButton = createOverlayButton("Home", "home.png");
        homeButton.setOnAction(e -> {
            resumeImmediately();
            if (homeAction != null) homeAction.run();
        });
        buttons.getChildren().addAll(resumeButton, settingsButton, homeButton);

        row.getChildren().addAll(leftBox, buttons);
        root.setCenter(row);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.setOnCloseRequest(e -> {
            e.consume();
            resumeImmediately();
        });
        return stage;
    }

    private Button createOverlayButton(String text, String iconFileName) {
        Button btn = new Button(text);
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
