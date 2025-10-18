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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class StarsTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignore) {
            // Toolkit already running
        }
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
    @DisplayName("create(0%): all stars are the same image (all OFF)")
    void zero_percent_all_off() {
        HBox box = Stars.create(0.0, 20.0, 4.0);
        assertAllSameImage(box);
    }

    @Test
    @DisplayName("create(100%): all stars are the same image (all ON)")
    void hundred_percent_all_on() {
        HBox box = Stars.create(100.0, 20.0, 4.0);
        assertAllSameImage(box);
    }

    @Test
    @DisplayName("create(60%): contains a single switch from ON to OFF (prefix ON, suffix OFF)")
    void middle_percent_has_single_switch() {
        HBox box = Stars.create(60.0, 20.0, 4.0);
        List<Node> children = box.getChildren();

        int distinct = countDistinctImages(children);
        assertTrue(distinct == 1 || distinct == 2,
                "Within one box, images should be either all same (1) or two kinds (2)");
        assertTrue(hasSingleSwitch(children), "Images should change at most once from left to right");
    }

    @Test
    @DisplayName("create(<0% and >100%): should clamp to valid output without exceptions")
    void out_of_range_inputs_are_clamped() {
        HBox below = Stars.create(-10.0, 18.0, 4.0);
        HBox above = Stars.create(200.0, 18.0, 4.0);

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

    private static void assertAllSameImage(HBox box) {
        List<Node> children = box.getChildren();
        assertEquals(5, children.size(), "There must be exactly 5 star images");
        int distinct = countDistinctImages(children);
        assertEquals(1, distinct, "All images should be identical in this case");
    }

    private static int countDistinctImages(List<Node> children) {
        Set<Image> set = new HashSet<>();
        for (Node n : children) {
            ImageView iv = (ImageView) n;
            set.add(iv.getImage());
        }
        return set.size();
    }

    private static boolean hasSingleSwitch(List<Node> children) {
        if (children.isEmpty()) return true;
        Image first = ((ImageView) children.get(0)).getImage();
        boolean switched = false;
        for (Node n : children) {
            Image img = ((ImageView) n).getImage();
            if (img != first) {
                if (!switched) {
                    switched = true;
                    first = img;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
