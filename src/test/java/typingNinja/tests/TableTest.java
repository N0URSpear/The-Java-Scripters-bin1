package typingNinja.tests;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import typingNinja.view.widgets.Table;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TableTest {

    @BeforeAll
    static void initJavaFx() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignore) {
        }
    }

    @Test
    @DisplayName("create(): returns root node with requested sizing")
    void create_returnsRootWithSizing() {
        double w = 485, h = 266;
        List<Integer> wpm = List.of(60, 70, 80);
        List<Integer> acc = List.of(95, 96, 97);

        Node root = Table.create(w, h, wpm, acc);
        assertNotNull(root);
        assertTrue(root instanceof StackPane, "Root should be a StackPane");

        StackPane sp = (StackPane) root;
        assertEquals(w, sp.getPrefWidth(), 0.0001);
        assertEquals(h, sp.getPrefHeight(), 0.0001);
        assertEquals(w, sp.getMinWidth(), 0.0001);
        assertEquals(h, sp.getMinHeight(), 0.0001);
        assertEquals(w, sp.getMaxWidth(), 0.0001);
        assertEquals(h, sp.getMaxHeight(), 0.0001);
    }

    @Test
    @DisplayName("create(): contains a LineChart with two series matching input lengths")
    void create_containsLineChartWithTwoSeriesMatchingLengths() {
        double w = 521, h = 223;
        List<Integer> wpm = List.of(50, 55, 58, 60, 62);
        List<Integer> acc = List.of(90, 91, 92, 93, 94);
        int n = wpm.size();

        Node root = Table.create(w, h, wpm, acc);
        LineChart<Number, Number> chart = find(root, LineChart.class);
        assertNotNull(chart, "LineChart should be present");

        List<XYChart.Series<Number, Number>> data = chart.getData();
        assertEquals(2, data.size(), "Expect two series (WPM and Accuracy)");
        for (XYChart.Series<Number, Number> s : data) {
            assertEquals(n, s.getData().size(), "Each series should match input length");
        }
    }

    @Test
    @DisplayName("create(): wrapper StackPane has 10px padding")
    void create_wrapperHasPadding10() {
        Node root = Table.create(400, 200,
                List.of(1, 2, 3), List.of(90, 91, 92));

        StackPane wrapper = findStackPaneWithChild(root, LineChart.class);
        assertNotNull(wrapper, "Wrapper around the chart should exist");
        assertEquals(10.0, wrapper.getPadding().getTop(), 0.0001);
        assertEquals(10.0, wrapper.getPadding().getRight(), 0.0001);
        assertEquals(10.0, wrapper.getPadding().getBottom(), 0.0001);
        assertEquals(10.0, wrapper.getPadding().getLeft(), 0.0001);
    }

    @Test
    @DisplayName("create(): background rectangle matches requested size")
    void create_backgroundRectangleFills() {
        double w = 300, h = 150;
        Node root = Table.create(w, h,
                List.of(10, 20), List.of(95, 96));

        Rectangle bg = find(root, Rectangle.class);
        assertNotNull(bg, "Background rectangle should exist");
        assertEquals(w, bg.getWidth(), 0.0001);
        assertEquals(h, bg.getHeight(), 0.0001);
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

    private static StackPane findStackPaneWithChild(Node root, Class<? extends Node> childType) {
        if (root instanceof StackPane sp) {
            for (Node c : sp.getChildren()) {
                if (childType.isInstance(c)) return sp;
            }
        }
        if (root instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                StackPane got = findStackPaneWithChild(child, childType);
                if (got != null) return got;
            }
        }
        return null;
    }
}
