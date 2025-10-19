package typingNinja.view.widgets;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

public final class Table {

    private Table() {}

    /**
     * Build a compact results table node with two columns (WPM and Accuracy).
     *
     * @param width    the desired table width in pixels
     * @param height   the desired table height in pixels
     * @param wpm      the list of WPM (words per minute) values to display (one per row)
     * @param accuracy the list of accuracy values (0–100) aligned to the WPM list
     * @return the assembled Node representing the table
     */
    public static Node create(double width, double height,
                              List<Integer> wpm, List<Integer> accuracy) {

        Rectangle bg = new Rectangle(width, height);
        bg.setFill(Color.web("#D9D9D9"));
        bg.setArcWidth(16 * 2);
        bg.setArcHeight(16 * 2);

        //XY
        int n = Math.min(wpm.size(), accuracy.size());
        NumberAxis x = new NumberAxis(1, Math.max(1, n), 1);
        x.setLabel(null);
        NumberAxis y = new NumberAxis();
        y.setLabel(null);
        try {
            // Style axes tick labels with bold Jaro for the results page
            x.setTickLabelFont(javafx.scene.text.Font.font("Jaro", javafx.scene.text.FontWeight.BOLD, 14));
            y.setTickLabelFont(javafx.scene.text.Font.font("Jaro", javafx.scene.text.FontWeight.BOLD, 14));
        } catch (Exception ignored) {}

        //table
        LineChart<Number, Number> chart = new LineChart<>(x, y);
        chart.setLegendVisible(true);
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        //background
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setStyle("""
                -fx-background-color: transparent;
                -fx-padding: 6;
                -fx-font-family: 'Jaro';
                -fx-font-weight: 800;
                """);

        //the line
        XYChart.Series<Number, Number> sAcc = new XYChart.Series<>();
        sAcc.setName("Accuracy");
        XYChart.Series<Number, Number> sWpm = new XYChart.Series<>();
        sWpm.setName("WPM");

        for (int i = 0; i < n; i++) {
            int xIdx = i + 1;            // 横轴 1..n
            sAcc.getData().add(new XYChart.Data<>(xIdx, accuracy.get(i)));
            sWpm.getData().add(new XYChart.Data<>(xIdx, wpm.get(i)));
        }
        chart.getData().addAll(sAcc, sWpm);


        StackPane wrapper = new StackPane(chart);
        wrapper.setPadding(new Insets(10));
        wrapper.setPrefSize(width, height);
        wrapper.setMinSize(width, height);
        wrapper.setMaxSize(width, height);

        StackPane root = new StackPane(bg, wrapper);
        root.setPrefSize(width, height);
        root.setMinSize(width, height);
        root.setMaxSize(width, height);

        return root;
    }
}
