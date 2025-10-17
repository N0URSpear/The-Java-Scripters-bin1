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

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class CertificatesSceneTest {

    @BeforeAll
    static void initJavaFx() {
        // 启动 JavaFX Toolkit（不弹窗）。若已启动则忽略异常。
        try {
            Platform.startup(() -> { /* no-op */ });
        } catch (IllegalStateException ignore) {
            // 已经启动
        }
    }

    @Test
    @DisplayName("createScene(): returns a Scene sized 1280x720 with title 'CERTIFICATES'")
    void creates_scene_with_expected_size_and_title() {
        runOnFxThread(() -> {
            Stage stage = new Stage();
            Scene scene = CertificatesScene.createScene(stage);
            assertNotNull(scene, "Scene should not be null");
            assertEquals(1280, scene.getWidth(), 1e-6, "Scene width should be 1280");
            assertEquals(720, scene.getHeight(), 1e-6, "Scene height should be 720");
            assertNotNull(scene.getRoot(), "Scene root should not be null");

            // 标题应包含 “CERTIFICATES”（忽略大小写）
            List<String> texts = collectAllText(scene.getRoot());
            boolean hasTitle = texts.stream().anyMatch(s -> s.equals("certificates"));
            assertTrue(hasTitle, "Scene should show a 'CERTIFICATES' title");
        });
    }

    @Test
    @DisplayName("createScene(): contains a ScrollPane whose content is set")
    void has_scrollpane_with_content() {
        runOnFxThread(() -> {
            Scene scene = CertificatesScene.createScene(new Stage());
            List<ScrollPane> panes = findAll(scene.getRoot(), ScrollPane.class);
            assertFalse(panes.isEmpty(), "A ScrollPane should exist");
            ScrollPane sp = panes.get(0);
            assertNotNull(sp.getContent(), "ScrollPane content should be set");
        });
    }

    @Test
    @DisplayName("createScene(): has a Back button with an action handler")
    void has_back_button_with_handler() {
        runOnFxThread(() -> {
            Scene scene = CertificatesScene.createScene(new Stage());
            List<Button> buttons = findAll(scene.getRoot(), Button.class);

            Button back = buttons.stream()
                    .filter(b -> textEquals(b.getText(), "Back") || textEquals(b.getText(), "BACK"))
                    .findFirst().orElse(null);

            assertNotNull(back, "Back button should exist");
            assertNotNull(back.getOnAction(), "Back button should have an action handler");
        });
    }

    @Test
    @DisplayName("createScene(): any 'Download PDF' buttons are actionable (if present)")
    void download_buttons_have_handlers_if_present() {
        runOnFxThread(() -> {
            Scene scene = CertificatesScene.createScene(new Stage());
            List<Button> buttons = findAll(scene.getRoot(), Button.class);

            // 列表可能为空：如果找不到下载按钮，不视为失败；若找到了，则必须可点击。
            List<Button> downloads = new ArrayList<>();
            for (Button b : buttons) {
                if (b.getText() != null && b.getText().toLowerCase(Locale.ROOT).contains("download")
                        && b.getText().toLowerCase(Locale.ROOT).contains("pdf")) {
                    downloads.add(b);
                }
            }
            for (Button b : downloads) {
                assertNotNull(b.getOnAction(), "'Download PDF' button should have an action handler");
            }
        });
    }

    // ----------------- 辅助方法（中文注释） -----------------

    // 在 FX 线程中执行 runnable；若抛异常则在测试线程中重新抛出
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

    // 广度优先收集某类型节点
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

    // 收集所有 Label/Text 的文本并统一为小写，便于匹配
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

    // 字符串等值（忽略大小写与首尾空格）
    private static boolean textEquals(String a, String b) {
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
