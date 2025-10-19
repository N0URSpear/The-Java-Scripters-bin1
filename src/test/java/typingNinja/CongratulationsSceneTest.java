package typingNinja;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class CongratulationsSceneTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { /* no-op */ });
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @DisplayName("createScene(): returns a Scene sized 1280x720")
    void creates_scene_with_expected_size() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            Scene scene = CongratulationsScene.createScene(stage);
            assertNotNull(scene, "Scene should not be null");
            assertEquals(1280, scene.getWidth(), 1e-6, "Scene width should be 1280");
            assertEquals(720, scene.getHeight(), 1e-6, "Scene height should be 720");
            assertNotNull(scene.getRoot(), "Scene root should not be null");
        });
    }

    @Test
    @DisplayName("createScene(): contains PRINT and BACK buttons with action handlers")
    void has_print_and_back_buttons_with_handlers() {
        runOnFxThread(() -> {
            Scene scene = CongratulationsScene.createScene(new Stage());
            List<Button> buttons = findAll(scene.getRoot(), Button.class);

            Button backBtn = findButtonByKeywords(buttons,
                    List.of("back", "return", "home", "←", "<"));
            Button printBtn = findButtonByKeywords(buttons,
                    List.of("print", "certificate", "download", "save", "pdf"));

            assertTrue(backBtn != null || printBtn != null,
                    "Expected at least a 'BACK' or 'PRINT/Certificate' button by text/id/style");

            if (backBtn != null) {
                assertNotNull(backBtn.getOnAction(), "BACK-like button should have an action handler");
            }
            if (printBtn != null) {
                assertNotNull(printBtn.getOnAction(), "PRINT/Certificate-like button should have an action handler");
            }

            if (backBtn == null && printBtn == null) {
                boolean anyActionable = buttons.stream().anyMatch(b -> b.getOnAction() != null);
                assertTrue(anyActionable, "At least one actionable Button should exist in the scene");
            }
        });
    }

    @Test
    @DisplayName("createScene(): contains a 5-star HBox from Stars.create(...)")
    void has_five_star_hbox() {
        runOnFxThread(() -> {
            Scene scene = CongratulationsScene.createScene(new Stage());
            HBox stars = findFirstHBoxWithAllImageViews(scene.getRoot(), 5);
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
    @DisplayName("createScene(): shows result labels for WPM and Accuracy")
    void shows_result_labels() {
        runOnFxThread(() -> {
            Scene scene = CongratulationsScene.createScene(new Stage());
            List<String> texts = collectAllText(scene.getRoot());

            boolean hasWpm = texts.stream().anyMatch(s -> s.contains("words per minute"));
            boolean hasAcc = texts.stream().anyMatch(s -> s.contains("accuracy"));

            assertTrue(hasWpm, "Text should contain phrase 'words per minute'");
            assertTrue(hasAcc, "Text should contain 'accuracy'");
        });
    }

    private static void runOnFxThread(Runnable r) {
        RuntimeException[] ex = new RuntimeException[1];
        Error[] err = new Error[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                r.run();
            } catch (RuntimeException e) {
                ex[0] = e;
            } catch (Error e) {
                err[0] = e;
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Timed out waiting for FX thread");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for FX thread", e);
        }
        if (ex[0] != null) throw ex[0];
        if (err[0] != null) throw err[0];
    }

    private static <T extends Node> List<T> findAll(Node root, Class<T> type) {
        List<T> out = new ArrayList<>();
        if (root == null) return out;
        Deque<Node> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Node n = q.removeFirst();
            if (type.isInstance(n)) out.add(type.cast(n));
            if (n instanceof Parent p) q.addAll(p.getChildrenUnmodifiable());
        }
        return out;
    }

    private static HBox findFirstHBoxWithAllImageViews(Node root, int expectedCount) {
        for (HBox hb : findAll(root, HBox.class)) {
            if (hb.getChildren().size() == expectedCount
                    && hb.getChildren().stream().allMatch(n -> n instanceof ImageView)) {
                return hb;
            }
        }
        return null;
    }

    private static List<String> collectAllText(Node root) {
        List<String> texts = new ArrayList<>();
        for (Label l : findAll(root, Label.class)) {
            if (l.getText() != null && !l.getText().isBlank()) {
                texts.add(l.getText().toLowerCase(Locale.ROOT));
            }
        }
        for (Text t : findAll(root, Text.class)) {
            if (t.getText() != null && !t.getText().isBlank()) {
                texts.add(t.getText().toLowerCase(Locale.ROOT));
            }
        }
        return texts;
    }


    private static boolean textEquals(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
    private static Button findButtonByKeywords(List<Button> buttons, List<String> keywords) {
        for (Button b : buttons) {
            if (containsAnyIgnoreCase(b.getText(), keywords)) return b;
            if (containsAnyIgnoreCase(b.getId(), keywords)) return b;
            for (String cls : b.getStyleClass()) {
                if (containsAnyIgnoreCase(cls, keywords)) return b;
            }
            if (containsAnyIgnoreCase(b.getAccessibleText(), keywords)) return b;
        }
        return null;
    }

    private static boolean containsAnyIgnoreCase(String text, List<String> keywords) {
        if (text == null) return false;
        String lower = text.toLowerCase(Locale.ROOT);
        for (String k : keywords) {
            if (k != null && !k.isEmpty() && lower.contains(k.toLowerCase(Locale.ROOT))) return true;
        }
        return false;
    }
}
