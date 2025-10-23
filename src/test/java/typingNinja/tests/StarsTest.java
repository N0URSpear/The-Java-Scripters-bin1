package typingNinja.tests;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingNinja.view.widgets.Stars;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class StarsTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> { /* no-op */ });
        } catch (IllegalStateException ignore) { }
    }

    @Test
    @DisplayName("create(): returns an HBox with 5 ImageView children and correct sizing/spacing")
    void structure_and_props() {
        double starHeight = 24.0;
        double gap = 6.0;

        HBox box = Stars.create(50.0, starHeight, gap);
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
    @DisplayName("create(0%): by current mapping should be 1 ON then 4 OFF (single switch)")
    void zero_percent_one_on_rest_off() {
        HBox box = Stars.create(0.0, 20.0, 4.0);
        List<Node> children = box.getChildren();
        assertEquals(5, children.size(), "There must be exactly 5 stars");

        assertEquals(1, expectedStars(0.0), "By mapping, 0% should yield 1 star");
        assertTrue(hasSingleSwitch(children), "Images should change at most once");
        assertEquals(1, countFirstRunLength(children), "Leading ON-run length should be 1");
        assertEquals(2, countDistinctImages(children), "Should contain two image kinds (ON/OFF)");
    }

    @Test
    @DisplayName("create(100%): all stars are the same image (all ON)")
    void hundred_percent_all_on() {
        HBox box = Stars.create(100.0, 20.0, 4.0);
        assertAllSameImage(box);
    }

    @Test
    @DisplayName("create(60%): single switch with prefix length equal to expected star count")
    void middle_percent_has_single_switch() {
        double p = 60.0;
        HBox box = Stars.create(p, 20.0, 4.0);
        List<Node> children = box.getChildren();

        assertTrue(hasSingleSwitch(children), "Images should change at most once");
        assertEquals(expectedStars(p), countFirstRunLength(children),
                "Leading ON-run length should equal expected star count");
        int distinct = countDistinctImages(children);
        assertTrue(distinct == 1 || distinct == 2,
                "Images should be either all same (1) or two kinds (2)");
    }

    @Test
    @DisplayName("create(<0% and >100%): clamped mapping yields sensible layouts")
    void out_of_range_inputs_are_clamped() {
        HBox below = Stars.create(-10.0, 18.0, 4.0);
        HBox above = Stars.create(200.0, 18.0, 4.0);

        assertEquals(5, below.getChildren().size(), "Below 0% should still produce 5 stars");
        assertEquals(5, above.getChildren().size(), "Above 100% should still produce 5 stars");

        assertEquals(1, expectedStars(-10.0));
        assertTrue(hasSingleSwitch(below.getChildren()));
        assertEquals(1, countFirstRunLength(below.getChildren()));
        assertEquals(2, countDistinctImages(below.getChildren()));

        assertAllSameImage(above);
    }

    private static void assertAllSameImage(HBox box) {
        List<Node> children = box.getChildren();
        assertEquals(5, children.size(), "There must be exactly 5 star images");
        int distinct = countDistinctImages(children);
        assertEquals(1, distinct, "All images should be identical in this case");
    }

    private static int countDistinctImages(List<Node> children) {
        Set<Image> set = new HashSet<>();
        for (Node n : children) {
            Image img = ((ImageView) n).getImage();
            if (img != null) set.add(img);
        }
        return set.size();
    }

    private static boolean hasSingleSwitch(List<Node> children) {
        if (children.isEmpty()) return true;
        Image type = ((ImageView) children.get(0)).getImage();
        boolean switched = false;
        for (Node n : children) {
            Image img = ((ImageView) n).getImage();
            if (img != type) {
                if (switched) return false;
                switched = true;
                type = img;
            }
        }
        return true;
    }

    private static int countFirstRunLength(List<Node> children) {
        if (children.isEmpty()) return 0;
        Image first = ((ImageView) children.get(0)).getImage();
        int k = 0;
        for (Node n : children) {
            Image img = ((ImageView) n).getImage();
            if (img == first) k++; else break;
        }
        return k;
    }

    private static int expectedStars(double p) {
        if (p >= 90) return 5;
        if (p >= 80) return 4;
        if (p >= 70) return 3;
        if (p >= 40) return 2;
        return 1;
    }
}
