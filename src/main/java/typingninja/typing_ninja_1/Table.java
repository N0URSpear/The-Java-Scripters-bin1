package com.example.addressbook;

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

    public static Node create(double width, double height,
                              List<Integer> wpm, List<Integer> accuracy) {

        // 1) 背景板
        Rectangle bg = new Rectangle(width, height);
        bg.setFill(Color.web("#D9D9D9"));
        bg.setArcWidth(16 * 2);
        bg.setArcHeight(16 * 2);

        // 2) 坐标轴
        int n = Math.min(wpm.size(), accuracy.size());
        NumberAxis x = new NumberAxis(1, Math.max(1, n), 1);
        x.setLabel(null);
        NumberAxis y = new NumberAxis();
        y.setLabel(null);

        // 3) 折线图
        LineChart<Number, Number> chart = new LineChart<>(x, y);
        chart.setLegendVisible(true);
        chart.setLegendSide(javafx.geometry.Side.BOTTOM);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);

        // 让背景透明，露出圆角灰底
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setAlternativeRowFillVisible(false);
        chart.setStyle("""
                -fx-background-color: transparent;
                -fx-padding: 6;
                """);

        // 4) 两条数据线
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

        // 5) 叠放（圆角底 + 图）
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
