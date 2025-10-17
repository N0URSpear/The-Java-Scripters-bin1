package typingNinja.tests;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingNinja.view.widgets.Keyboard;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignore) {
            // Toolkit already started
        }
    }

    @Test
    @DisplayName("create(): root sized to requested width/height and has background rectangle")
    void root_and_background() {
        double w = 521, h = 223;

        Node root = Keyboard.create(w, h, Map.of());
        assertNotNull(root, "Root should not be null");
        assertTrue(root instanceof StackPane || root instanceof Pane, "Root should be a StackPane or Pane");
        assertTrue(root instanceof Region, "Root should be a Region");

        Region region = (Region) root;
        assertEquals(w, region.getPrefWidth(), 1e-4, "Preferred width should match");
        assertEquals(h, region.getPrefHeight(), 1e-4, "Preferred height should match");

        Rectangle bg = find(root, Rectangle.class);
        assertNotNull(bg, "Background rectangle should exist");
        assertEquals(w, bg.getWidth(), 1e-4, "Background width should match");
        assertEquals(h, bg.getHeight(), 1e-4, "Background height should match");
    }

    @Test
    @DisplayName("create(): common key labels are present (Q/A/SPACE or variants)")
    void common_labels_present() {
        Node root = Keyboard.create(500, 220, Map.of());
        Map<String, KeyNode> keys = collectKeyNodes(root);

        boolean hasQ = keys.containsKey("Q");
        boolean hasA = keys.containsKey("A");
        boolean hasSpace = keys.containsKey("SPACE") || keys.containsKey("Space") || keys.containsKey(" ");

        assertTrue(hasQ || hasA, "At least one of Q/A should be present");
        assertTrue(hasSpace, "Space key should be present as SPACE/Space/' '");
    }

    @Test
    @DisplayName("heat mapping: higher count should differ from zero count")
    void heat_mapping_relative_difference() {
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Q", 100);
        counts.put("A", 0);
        counts.put("SPACE", 20);

        Node root = Keyboard.create(600, 240, counts);
        Map<String, KeyNode> map = collectKeyNodes(root);

        String hotKey = pickKey(map, List.of("Q"), true);
        String coldKey = pickKey(map, List.of("A"), false, hotKey);

        Color hot = getFillColor(map.get(hotKey));
        assertNotNull(hot, "Hot key color should not be null");

        if (coldKey != null) {
            Color cold = getFillColor(map.get(coldKey));
            assertNotNull(cold, "Cold key color should not be null");
            assertTrue(colorsDiffer(hot, cold), "Hot key color should differ from cold key color");
            assertTrue(hot.getRed() + 1e-6 >= cold.getRed(),
                    "Hot key should not be less red than cold key (weak monotonicity check)");
        } else {
            Rectangle bg = find(root, Rectangle.class);
            assertNotNull(bg, "Background should exist");
            Color bgColor = (Color) bg.getFill();
            assertTrue(colorsDiffer(hot, bgColor),
                    "Hot key color should differ from background color");
        }
    }

    private static <T extends Node> T find(Node root, Class<T> type) {
        if (type.isInstance(root)) return type.cast(root);
        if (root instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                T got = find(child, type);
                if (got != null) return got;
            }
        }
        return null;
    }

    private static Map<String, KeyNode> collectKeyNodes(Node root) {
        Map<String, KeyNode> map = new HashMap<>();
        collectAll(root, n -> {
            if (n instanceof StackPane sp) {
                Rectangle rect = find(sp, Rectangle.class);
                Text label = find(sp, Text.class);
                if (rect != null && label != null) {
                    map.put(label.getText().toUpperCase(Locale.ROOT), new KeyNode(sp, rect, label));
                }
            }
        });
        return map;
    }

    private static void collectAll(Node root, java.util.function.Consumer<Node> consumer) {
        consumer.accept(root);
        if (root instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                collectAll(child, consumer);
            }
        }
    }

    private static String pickKey(Map<String, KeyNode> map, List<String> preferred, boolean require, String... exclude) {
        for (String key : preferred) {
            if (map.containsKey(key) && (exclude.length == 0 || !key.equals(exclude[0]))) {
                return key;
            }
        }
        if (!require) {
            for (String key : map.keySet()) {
                if (exclude.length == 0 || !key.equals(exclude[0])) {
                    return key;
                }
            }
        }
        return null;
    }

    private static String pickKey(Map<String, KeyNode> map, List<String> preferred, boolean require) {
        return pickKey(map, preferred, require, new String[0]);
    }

    private static Color getFillColor(KeyNode node) {
        return node != null && node.rect.getFill() instanceof Color c ? c : null;
    }

    private static boolean colorsDiffer(Color a, Color b) {
        return Math.abs(a.getRed() - b.getRed()) > 1e-6 ||
                Math.abs(a.getGreen() - b.getGreen()) > 1e-6 ||
                Math.abs(a.getBlue() - b.getBlue()) > 1e-6;
    }

    private record KeyNode(StackPane pane, Rectangle rect, Text label) {}
}
