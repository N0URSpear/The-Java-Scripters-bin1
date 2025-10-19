package typingNinja;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingNinja.view.CertificatesScene;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CertificatesSceneTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { /* no-op */ });
        } catch (IllegalStateException ignore) { }
    }

    @Test
    @DisplayName("createView(): can be wrapped into a 1280x720 Scene; shows 'CERTIFICATES' title")
    void creates_scene_with_expected_size_and_title() {
        runOnFxThread(() -> {
            Stage stage = new Stage();

            Parent root = CertificatesScene.createView(stage);
            assertNotNull(root, "Root view should not be null");

            Scene scene = new Scene(root, 1280, 720);
            assertEquals(1280, scene.getWidth(), 1e-6, "Scene width should be 1280");
            assertEquals(720,  scene.getHeight(), 1e-6, "Scene height should be 720");

            List<String> texts = collectAllText(root);
            boolean hasTitle = texts.stream().anyMatch(s -> s.equals("certificates"));
            assertTrue(hasTitle, "View should show a 'CERTIFICATES' title");
        });
    }

    @Test
    @DisplayName("createView(): contains a ScrollPane whose content is set")
    void has_scrollpane_with_content() {
        runOnFxThread(() -> {
            Parent root = CertificatesScene.createView(new Stage());
            List<ScrollPane> panes = findAll(root, ScrollPane.class);
            assertFalse(panes.isEmpty(), "A ScrollPane should exist");
            ScrollPane sp = panes.get(0);
            assertNotNull(sp.getContent(), "ScrollPane content should be set");
        });
    }

    @Test
    @DisplayName("createView(): has a Back button with an action handler")
    void has_back_button_with_handler() {
        runOnFxThread(() -> {
            Parent root = CertificatesScene.createView(new Stage());
            List<Button> buttons = findAll(root, Button.class);

            Button back = buttons.stream()
                    .filter(b -> textEquals(b.getText(), "Back") || textEquals(b.getText(), "BACK"))
                    .findFirst().orElse(null);

            assertNotNull(back, "Back button should exist");
            assertNotNull(back.getOnAction(), "Back button should have an action handler");
        });
    }

    @Test
    @DisplayName("createView(): any 'Download PDF' buttons are actionable (if present)")
    void download_buttons_have_handlers_if_present() {
        runOnFxThread(() -> {
            Parent root = CertificatesScene.createView(new Stage());
            List<Button> buttons = findAll(root, Button.class);

            List<Button> downloads = new ArrayList<>();
            for (Button b : buttons) {
                String t = b.getText();
                if (t != null) {
                    String s = t.toLowerCase(Locale.ROOT);
                    if (s.contains("download") && s.contains("pdf")) downloads.add(b);
                }
            }
            for (Button b : downloads) {
                assertNotNull(b.getOnAction(), "'Download PDF' button should have an action handler");
            }
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
