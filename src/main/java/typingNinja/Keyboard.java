package typingNinja;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Map;

public final class Keyboard {

    private Keyboard() {}

    // 简化的 QWERTY 布局（等宽键）
    private static final String[][] ROWS = new String[][]{
            {"0","1","2","3","4","5","6","7","8","9"} ,// 顶部数字行（按 0..9 顺序）
            {"Q","W","E","R","T","Y","U","I","O","P"},
            {"A","S","D","F","G","H","J","K","L", ";"},
            {"Z","X","C","V","B","N","M", ",", "."},
            {" "} // Space // 空格（宽度与其他键相同；如需加宽可另说）
    };

    // 行的水平偏移（全 0 = 不错列，整齐对齐）
    private static final double[] ROW_OFFSET_IN_KEYW = new double[]{
            0.0, 0.0, 0.0, 0.0
    };

    /**
     * Build a keyboard heatmap node sized to the given width/height and colored by key hit counts.
     *
     * @param width  the desired width of the keyboard node in pixels
     * @param height the desired height of the keyboard node in pixels
     * @param counts a map from key label (e.g., "A","B","Space") to hit count used for coloring
     * @return the assembled Node representing the keyboard heatmap
     */

    public static Node create(double width, double height, Map<String, Integer> counts) {
        // 背板
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(32);
        bg.setArcHeight(32);
        bg.setFill(Color.web("#D9D9D9"));

        Pane keysLayer = new Pane();
        keysLayer.setPadding(new Insets(10));
        keysLayer.setPrefSize(width, height);
        keysLayer.setMinSize(width, height);
        keysLayer.setMaxSize(width, height);

        // 布局参数
        double pad  = 14;  // 外边距
        double gapX = 6;   // 列间距
        double gapY = 8;   // 行间距
        int rows = ROWS.length;

        // 统计总错误数（用于百分比）。对 null 做保护。
        int total = 0;
        if (counts != null) {
            for (Integer v : counts.values()) {
                total += (v == null ? 0 : Math.max(0, v));
            }
        }
        final int totalErrors = total; // 防止闭包修改

        // 每行键高
        double keyH = (height - pad * 2 - gapY * (rows - 1)) / rows;

        // 逐行绘制
        double y = pad;
        for (int r = 0; r < rows; r++) {
            String[] rowKeys = ROWS[r];
            int cols = rowKeys.length;

            // 本行可用宽度 & 键宽（等宽）
            double rowW = (width - pad * 2);
            double keyW = (rowW - gapX * (cols - 1)) / cols;

            // 行偏移
            double offset = (r < ROW_OFFSET_IN_KEYW.length ? ROW_OFFSET_IN_KEYW[r] : 0.0) * keyW;
            double x = pad + offset;

            for (int c = 0; c < cols; c++) {
                String k = rowKeys[c];

                // 当前键的“错误次数”
                int count = 0;
                if (counts != null) {
                    Integer v = counts.get(k);
                    if (v == null) v = counts.get(k.toLowerCase());
                    if (v == null) v = counts.get(k.toUpperCase());
                    // 也兼容 "SPACE" 作为空格键名
                    if (v == null && " ".equals(k)) v = counts.get("SPACE");
                    count = (v == null) ? 0 : Math.max(0, v);
                }

                // —— 关键变化：按“百分比”着色 —— //
                double t = (totalErrors > 0) ? (count / (double) totalErrors) : 0.0; // 0.0 ~ 1.0

                // 键帽
                Rectangle keyRect = new Rectangle(keyW, keyH);
                keyRect.setArcWidth(10);
                keyRect.setArcHeight(10);
                keyRect.setFill(colorFor(t));               // 白→红（按百分比）
                keyRect.setStroke(Color.web("#B3B3B3"));
                keyRect.setStrokeWidth(1.0);

                // 标签
                Text label = new Text(k);
                label.setFill(Color.BLACK);
                label.setFont(Font.font(12));

                // 居中容器
                StackPane keyPane = new StackPane(keyRect, label);
                keyPane.setAlignment(Pos.CENTER);
                keyPane.setLayoutX(x);
                keyPane.setLayoutY(y);
                keyPane.setPrefSize(keyW, keyH);
                keyPane.setMinSize(keyW, keyH);
                keyPane.setMaxSize(keyW, keyH);

                keysLayer.getChildren().add(keyPane);
                x += keyW + gapX;
            }
            y += keyH + gapY;
        }

        return new StackPane(bg, keysLayer);
    }


    /**
     * Map a normalized intensity value to a heatmap color.
     *
     * @param t normalized intensity in [0, 1]
     * @return the Color corresponding to the intensity
     */
    // 颜色映射（保持你原来的两段渐变风格）
    private static Color colorFor(double t) {
        t = clamp01(t);
        if (t < 0.5) {
            double u = t / 0.5;
            return lerp(Color.web("#FFFFFF"), Color.web("#FF6B6B"), u);
        } else {
            double u = (t - 0.5) / 0.5;
            return lerp(Color.web("#FF6B6B"), Color.web("#D80000"), u);
        }
    }

    /**
     * Linearly interpolate between two colors.
     *
     * @param a the start color
     * @param b the end color
     * @param t interpolation factor in [0, 1]
     * @return the interpolated Color
     */
    private static Color lerp(Color a, Color b, double t) {
        t = clamp01(t);
        return new Color(
                a.getRed()   + (b.getRed()   - a.getRed())   * t,
                a.getGreen() + (b.getGreen() - a.getGreen()) * t,
                a.getBlue()  + (b.getBlue()  - a.getBlue())  * t,
                1.0
        );
    }
    /**
     * Clamp a value into the closed interval [0, 1].
     *
     * @param v the input value
     * @return 0 if v < 0, 1 if v > 1, otherwise v
     */
    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }
}
