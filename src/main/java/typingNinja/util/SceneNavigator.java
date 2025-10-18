package typingNinja.util;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Utility class for swapping scenes on the primary stage while preserving window state.
 */
public final class SceneNavigator {

    private static final double DEFAULT_WIDTH = 1920.0;
    private static final double DEFAULT_HEIGHT = 1080.0;

    private SceneNavigator() {
    }

    /**
     * Swaps the stage root while keeping fullscreen state intact.
     */
    public static void show(Stage stage, Parent root, String title) {
        // Swap the root while keeping the same stage instance minimised to one window.
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
     * Loads an FXML file and shows it on the supplied stage.
     */
    public static Parent load(Stage stage, String fxmlPath, String title) throws IOException {
        // Load the FXML, hand it to show(), and return the root for callers that need it.
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(Objects.requireNonNull(fxmlPath)));
        Parent root = loader.load();
        show(stage, root, title);
        return root;
    }

    /**
     * Loads an FXML file and returns its controller after showing the scene.
     */
    public static <T> T loadController(Stage stage, String fxmlPath, String title) throws IOException {
        // Same as load(), but returns the controller so callers can wire extra state.
        FXMLLoader loader = new FXMLLoader(SceneNavigator.class.getResource(Objects.requireNonNull(fxmlPath)));
        Parent root = loader.load();
        show(stage, root, title);
        return loader.getController();
    }

    /**
     * Reapplies fullscreen hints without tearing down the existing stage.
     */
    public static void ensureFullscreen(Stage stage) {
        // Reapply fullscreen and maximise flags without tearing down the scene graph.
        if (stage == null) {
            return;
        }
        stage.setFullScreenExitHint("");
        stage.setMaximized(true);
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        } else {
            Platform.runLater(stage::requestFocus);
        }
    }
}
