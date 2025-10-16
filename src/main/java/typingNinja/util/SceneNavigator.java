package typingNinja.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Centralises primary-stage scene transitions so the application stays in a single window
 * while maintaining fullscreen/maximised state.
 */
public final class SceneNavigator {

    private static final double DEFAULT_WIDTH = 1920.0;
    private static final double DEFAULT_HEIGHT = 1080.0;

    private SceneNavigator() {
        // utility class
    }

    /**
     * Replace the current root on the supplied stage while keeping the same {@link Scene}.
     * If the stage does not yet have a scene one will be created using a sensible default size.
     *
     * @param stage the primary stage
     * @param root  the new root node to display
     * @param title optional window title
     */
    public static void show(Stage stage, Parent root, String title) {
        if (stage == null || root == null) {
            return;
        }

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);
            stage.setScene(scene);
        } else {
            scene.setRoot(root);
        }

        if (title != null && !title.isBlank()) {
            stage.setTitle(title);
        }

        stage.centerOnScreen();
        ensureFullscreen(stage);
    }

    /**
     * Convenience for loading FXML and swapping it into the stage.
     *
     * @param stage    the primary stage
     * @param fxmlPath resource path for the FXML
     * @param title    optional window title
     * @return the loaded root node
     * @throws IOException when the FXML cannot be loaded
     */
    public static Parent load(Stage stage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(Objects.requireNonNull(fxmlPath)));
        Parent root = loader.load();
        show(stage, root, title);
        return root;
    }

    /**
     * Use when the caller needs the controller instance after loading.
     *
     * @param stage    the primary stage
     * @param fxmlPath resource path for the FXML
     * @param title    optional window title
     * @param <T>      controller type
     * @return the controller for the loaded FXML
     * @throws IOException when the FXML cannot be loaded
     */
    public static <T> T loadController(Stage stage, String fxmlPath, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(Objects.requireNonNull(fxmlPath)));
        Parent root = loader.load();
        show(stage, root, title);
        return loader.getController();
    }

    /**
     * Re-applies fullscreen/maximise hints without tearing down the window.
     *
     * @param stage the primary stage
     */
    public static void ensureFullscreen(Stage stage) {
        if (stage == null) {
            return;
        }
        stage.setFullScreenExitHint("");
        stage.setMaximized(true);
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        } else {
            // Nudge layout so bindings recalculate while staying fullscreen.
            Platform.runLater(stage::requestFocus);
        }
    }
}
