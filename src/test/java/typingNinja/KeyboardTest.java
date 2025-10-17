package typingNinja;

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
import typingNinja.Keyboard;   // <-- 引用生产代码（已导出）

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class KeyboardTest {

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
    @DisplayName("create(): root sized to requested width/height and has background rectangle")
    void root_and_background() {
        double w = 521, h = 223;

        Node root = Keyboard.create(w, h, Map.of());
        assertNotNull(root, "Root should not be null");
        assertTrue(root instanceof StackPane || root instanceof Pane,
                "Root should be a StackPane or Pane");

        Rectangle bg = find(root, Rectangle.class);
        assertNotNull(bg, "A background Rectangle should exist");
        assertEquals(w, bg.getWidth(), 1e-4, "Background width should match");
        assertEquals(h, bg.getHeight(), 1e-4, "Background height should match");

        // 圆角非必需，但若存在应为非负
        assertTrue(bg.getArcWidth() >= 0 && bg.getArcHeight() >= 0,
                "Arc sizes should be non-negative");
    }

    @Test
    @DisplayName("create(): common key labels are present (Q/A/SPACE or variants)")
    void common_labels_present() {
        // 验证：常见键标签存在，便于后续热力映射断言
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
        // 验证：热键与冷键的底色不同；弱单调性检查：热键红通道不低于冷键
        Map<String, Integer> counts = new HashMap<>();
        counts.put("Q", 100);   // 高计数（热）
        counts.put("A", 0);     // 低计数（冷）
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
            assertTrue(chroma(hot) + 1e-6 >= chroma(cold),
                    "Hot key should be at least as saturated as cold key (weak monotonicity check)");
        } else {
            Rectangle bg = find(root, Rectangle.class);
            assertNotNull(bg, "Background should exist");
            Color bgColor = (Color) bg.getFill();
            assertTrue(colorsDiffer(hot, bgColor),
                    "Hot key color should differ from background color");
        }
    }

    // ----------------- 辅助方法（中文注释） -----------------

    // 在树中查找第一个指定类型的节点
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

    // 收集“键节点”：包含 Rectangle（底）与 Text（标签）的 StackPane
    private static Map<String, KeyNode> collectKeyNodes(Node root) {
        Map<String, KeyNode> map = new HashMap<>();
        collectAll(root, n -> {
            if (n instanceof StackPane sp) {
                Rectangle rect = null;
                Text label = null;
                for (Node c : sp.getChildren()) {
                    if (c instanceof Rectangle r) rect = r;
                    else if (c instanceof Text t) label = t;
                }
                if (rect != null && label != null && label.getText() != null) {
                    map.put(label.getText(), new KeyNode(sp, rect, label));
                }
            }
        });
        return map;
    }

    // 深度优先遍历整棵节点树
    private static void collectAll(Node root, java.util.function.Consumer<Node> fn) {
        fn.accept(root);
        if (root instanceof Parent p) {
            for (Node c : p.getChildrenUnmodifiable()) {
                collectAll(c, fn);
            }
        }
    }

    // 选择可用的键名：优先候选，否则退化为任意单字母键/任意键
    private static String pickKey(Map<String, KeyNode> map, List<String> preferred, boolean mustExist) {
        for (String k : preferred) if (map.containsKey(k)) return k;
        Optional<String> anyLetter = map.keySet().stream().filter(k -> k.length() == 1).findFirst();
        if (anyLetter.isPresent()) return anyLetter.get();
        if (mustExist) throw new AssertionError("No key labels found in layout");
        return map.keySet().stream().findFirst().orElse(null);
    }

    // 选择与 hotKey 不同的冷键
    private static String pickKey(Map<String, KeyNode> map, List<String> preferred, boolean mustExist, String hotKey) {
        for (String k : preferred) if (map.containsKey(k) && !k.equals(hotKey)) return k;
        Optional<String> anyOther = map.keySet().stream()
                .filter(k -> !k.equals(hotKey) && k.length() == 1)
                .findFirst();
        if (anyOther.isPresent()) return anyOther.get();
        if (mustExist) throw new AssertionError("No alternative key label found");
        return null;
    }

    // 提取键底色（不是 Color 则返回 null）
    private static Color getFillColor(KeyNode key) {
        if (key == null) return null;
        if (key.rect.getFill() instanceof Color c) return c;
        return null;
    }

    // 判断两颜色是否“显著不同”
    private static boolean colorsDiffer(Color a, Color b) {
        if (a == null || b == null) return true;
        double dr = Math.abs(a.getRed()   - b.getRed());
        double dg = Math.abs(a.getGreen() - b.getGreen());
        double db = Math.abs(a.getBlue()  - b.getBlue());
        double da = Math.abs(a.getOpacity()- b.getOpacity());
        return (dr + dg + db + da) > 1e-6;
    }

    // 键节点包装
    private static final class KeyNode {
        final StackPane pane;  // 键容器
        final Rectangle rect;  // 键底色
        final Text label;      // 键标签
        KeyNode(StackPane p, Rectangle r, Text t) { this.pane = p; this.rect = r; this.label = t; }
    }

    // 计算颜色“饱和度”（近似为通道最大值 - 最小值）；灰/白≈0，纯色较高
    private static double chroma(Color c) {
        if (c == null) return 0.0;
        double r = c.getRed(), g = c.getGreen(), b = c.getBlue();
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        return max - min;
    }

}
