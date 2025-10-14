package typingNinja;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StarsTest {

    @BeforeAll
    static void initJavaFx() {
        // 启动 JavaFX Toolkit（不弹窗）。若已启动则忽略异常。
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignore) {
            // 已经启动
        }
    }

    @Test
    @DisplayName("create(): returns an HBox with 5 ImageView children and correct sizing/spacing")
    void structure_and_props() {
        // 基础结构：容器类型、子元素数量、对齐方式、间距、图像视图属性
        double starHeight = 24.0;
        double gap = 6.0;

        HBox box = typingNinja.view.widgets.Stars.create(50.0, starHeight, gap);
        assertNotNull(box, "Root container should not be null");
        assertEquals(HBox.class, box.getClass(), "Root should be an HBox");
        assertEquals(Pos.CENTER, box.getAlignment(), "Alignment should be CENTER");
        assertEquals(gap, box.getSpacing(), 1e-6, "HBox spacing should match gap");

        List<Node> children = box.getChildren();
        assertEquals(5, children.size(), "There must be exactly 5 star images");
        for (Node n : children) {
            assertTrue(n instanceof ImageView, "Each child must be an ImageView");
            ImageView iv = (ImageView) n;
            assertTrue(iv.isPreserveRatio(), "Each star should preserve ratio");
            assertEquals(starHeight, iv.getFitHeight(), 1e-6, "Each star height should match");
            assertNotNull(iv.getImage(), "Each ImageView should have an Image");
        }
    }

    @Test
    @DisplayName("create(0%): all stars are the same image (all OFF)")
    void zero_percent_all_off() {
        // 0% 应全部相同（全暗）
        HBox box = typingNinja.view.widgets.Stars.create(0.0, 20.0, 4.0);
        assertAllSameImage(box);
    }

    @Test
    @DisplayName("create(100%): all stars are the same image (all ON)")
    void hundred_percent_all_on() {
        // 100% 应全部相同（全亮）
        HBox box = typingNinja.view.widgets.Stars.create(100.0, 20.0, 4.0);
        assertAllSameImage(box);
    }

    @Test
    @DisplayName("create(60%): contains a single switch from ON to OFF (prefix ON, suffix OFF)")
    void middle_percent_has_single_switch() {
        // 中间比例应呈现前缀亮、后缀暗的“单次切换”，不应多次交替
        HBox box = typingNinja.view.widgets.Stars.create(60.0, 20.0, 4.0);
        List<Node> children = box.getChildren();

        int distinct = countDistinctImages(children);
        assertTrue(distinct == 1 || distinct == 2,
                "Within one box, images should be either all same (1) or two kinds (2)");
        assertTrue(hasSingleSwitch(children), "Images should change at most once from left to right");
    }

    @Test
    @DisplayName("create(<0% and >100%): should clamp to valid output without exceptions")
    void out_of_range_inputs_are_clamped() {
        // 越界输入不应抛异常，仍应生成 5 颗星，且两端极值保持统一的图像（全暗或全亮）
        HBox below = typingNinja.view.widgets.Stars.create(-10.0, 18.0, 4.0);
        HBox above = typingNinja.view.widgets.Stars.create(200.0, 18.0, 4.0);

        assertEquals(5, below.getChildren().size(), "Below 0% should still produce 5 stars");
        assertEquals(5, above.getChildren().size(), "Above 100% should still produce 5 stars");

        for (Node n : below.getChildren()) {
            assertTrue(n instanceof ImageView, "Children must be ImageViews");
            assertNotNull(((ImageView) n).getImage(), "Image must not be null");
        }
        for (Node n : above.getChildren()) {
            assertTrue(n instanceof ImageView, "Children must be ImageViews");
            assertNotNull(((ImageView) n).getImage(), "Image must not be null");
        }

        assertAllSameImage(below);
        assertAllSameImage(above);
    }

    // 断言：当前容器内的五颗星使用的是同一个 Image 对象（同一张图）
    private static void assertAllSameImage(HBox box) {
        List<Node> children = box.getChildren();
        assertEquals(5, children.size(), "There must be exactly 5 star images");
        int distinct = countDistinctImages(children);
        assertEquals(1, distinct, "All images should be identical in this case");
    }

    // 统计：本容器中出现了多少个不同的 Image 对象
    private static int countDistinctImages(List<Node> children) {
        Set<Image> set = new HashSet<>();
        for (Node n : children) {
            ImageView iv = (ImageView) n;
            set.add(iv.getImage());
        }
        return set.size();
    }

    // 判断是否“从左到右最多发生一次图像切换”（即前缀一种、后缀另一种）
    private static boolean hasSingleSwitch(List<Node> children) {
        if (children.isEmpty()) return true;
        Image first = ((ImageView) children.get(0)).getImage();
        boolean switched = false;
        for (Node n : children) {
            Image img = ((ImageView) n).getImage();
            if (img != first) {
                if (!switched) {
                    switched = true;     // 第一次切换
                    first = img;         // 之后应全部维持为该新图像
                } else {
                    return false;        // 出现第二次切换，判定失败
                }
            }
        }
        return true;
    }
}
