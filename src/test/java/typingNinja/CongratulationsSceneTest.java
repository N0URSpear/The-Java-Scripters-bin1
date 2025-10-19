package typingNinja;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.chart.Chart;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import typingNinja.view.CongratulationsScene;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CongratulationsSceneTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { /* no-op */ });
        } catch (IllegalStateException ignore) { }
    }

    @Test
    @DisplayName("createView(): can be wrapped into a 1280x720 Scene; shows title and key labels")
    void creates_scene_with_expected_size_and_labels() {
        runOnFxThread(() -> {
            Stage stage = new Stage();

            Parent root = CongratulationsScene.createView(stage);
            assertNotNull(root, "Root view should not be null");

            Scene scene = new Scene(root, 1280, 720);
            assertEquals(1280, scene.getWidth(), 1e-6, "Scene width should be 1280");
            assertEquals(720,  scene.getHeight(), 1e-6, "Scene height should be 720");

            List<String> texts = collectAllText(root);
            assertTrue(texts.stream().anyMatch(s -> s.equals("congratulations")),
                    "Should show 'CONGRATULATIONS' title");
            assertTrue(texts.stream().anyMatch(s -> s.contains("words per minute")),
                    "Should contain 'Words per minute' label");
            assertTrue(texts.stream().anyMatch(s -> s.contains("accuracy")),
                    "Should contain 'Accuracy' label");
        });
    }

    @Test
    @DisplayName("createView(): contains Print/Back buttons with action handlers")
    void has_buttons_with_handlers() {
        runOnFxThread(() -> {
            Parent root = CongratulationsScene.createView(new Stage());
            List<Button> buttons = findAll(root, Button.class);

            Button printBtn = buttons.stream()
                    .filter(b -> textEquals(b.getText(), "Print Certificate"))
                    .findFirst().orElse(null);
            Button backBtn = buttons.stream()
                    .filter(b -> textEquals(b.getText(), "Return to Main Menu"))
                    .findFirst().orElse(null);

            assertNotNull(printBtn, "Print button should exist");
            assertNotNull(backBtn,  "Back button should exist");
            assertNotNull(printBtn.getOnAction(), "Print button should have an action handler");
            assertNotNull(backBtn.getOnAction(),  "Back button should have an action handler");
        });
    }

    @Test
    @DisplayName("createView(): contains a 5-star HBox from Stars.create(...)")
    void has_five_star_hbox() {
        runOnFxThread(() -> {
            Parent root = CongratulationsScene.createView(new Stage());
            HBox stars = findFirstHBoxWithAllImageViews(root, 5);
            assertNotNull(stars, "A 5-star HBox should exist");
            assertEquals(5, stars.getChildren().size(), "Stars HBox should have 5 children");
            for (Node n : stars.getChildren()) {
                ImageView iv = (ImageView) n;
                assertNotNull(iv.getImage(), "Each star ImageView should have an Image");
                assertTrue(iv.getFitHeight() > 0, "Each star should have a positive fitHeight");
                assertTrue(iv.isPreserveRatio(), "Each star should preserve ratio");
            }
        });
    }

    @Test
    @DisplayName("createView(): includes a results chart (Table.create -> a JavaFX Chart)")
    void has_results_chart() {
        runOnFxThread(() -> {
            Parent root = CongratulationsScene.createView(new Stage());
            List<Chart> charts = findAll(root, Chart.class);
            assertFalse(charts.isEmpty(), "A JavaFX Chart (results table) should exist");
        });
    }

    private static void runOnFxThread(Runnable r) {
        RuntimeException[] ex = new RuntimeException[1];
        Error[] err = new Error[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try { r.run(); }
            catch (RuntimeException e) { ex[0] = e; }
            catch (Error e) { err[0] = e; }
            finally { latch.countDown(); }
        });
        try {
            if (!latch.await(10, TimeUnit.SECONDS))
                throw new AssertionError("Timed out waiting for FX thread");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for FX thread", e);
        }
        if (ex[0] != null) throw ex[0];
        if (err[0] != null) throw err[0];
    }

    private static <T extends Node> List<T> findAll(Parent root, Class<T> type) {
        List<T> out = new ArrayList<>();
        Deque<Node> q = new ArrayDeque<>();
        if (root != null) q.add(root);
        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            if (type.isInstance(n)) out.add(type.cast(n));
            if (n instanceof Parent p) q.addAll(p.getChildrenUnmodifiable());
        }
        return out;
    }

    private static HBox findFirstHBoxWithAllImageViews(Parent root, int expectedCount) {
        for (HBox hb : findAll(root, HBox.class)) {
            if (hb.getChildren().size() == expectedCount
                    && hb.getChildren().stream().allMatch(n -> n instanceof ImageView)) {
                return hb;
            }
        }
        return null;
    }

    private static List<String> collectAllText(Parent root) {
        List<String> texts = new ArrayList<>();
        for (Label l : findAll(root, Label.class)) {
            if (l.getText() != null && !l.getText().isBlank())
                texts.add(l.getText().toLowerCase(Locale.ROOT));
        }
        for (Text t : findAll(root, Text.class)) {
            if (t.getText() != null && !t.getText().isBlank())
                texts.add(t.getText().toLowerCase(Locale.ROOT));
        }
        return texts;
    }

    private static boolean textEquals(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
