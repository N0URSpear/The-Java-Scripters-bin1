package typingninja.typing_ninja_1;

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
            {"Q","W","E","R","T","Y","U","I","O","P",},
            {"A","S","D","F","G","H","J","K","L",},
            {"Z","X","C","V","B","N","M",},
            {" "},
    };

    // 行的水平偏移（让布局更像真实键盘的错列；单位：一个键宽的比例）
    private static final double[] ROW_OFFSET_IN_KEYW = new double[]{
            0.0,   // 第一行不偏移
            0.0,  // 第二行稍右
            0.0,  // 第三行更多
            0.0   // 第四行最多
    };

    private static final int MAX_COUNT = 10; // 0..10


    public static Node create(double width, double height, Map<String, Integer> counts) {
        // 背板：圆角浅灰，风格与其他面板一致
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(16 * 2);
        bg.setArcHeight(16 * 2);
        bg.setFill(Color.web("#D9D9D9"));

        Pane keysLayer = new Pane();
        keysLayer.setPadding(new Insets(10));
        keysLayer.setPrefSize(width, height);
        keysLayer.setMinSize(width, height);
        keysLayer.setMaxSize(width, height);

        // 布局参数
        double pad = 14;           // 外边距
        double gapX = 6;           // 键之间横向间距
        double gapY = 8;           // 行间距
        int rows = ROWS.length;

        // 每行键高
        double keyH = (height - pad * 2 - gapY * (rows - 1)) / rows;

        // 逐行绘制
        double y = pad;
        for (int r = 0; r < rows; r++) {
            String[] rowKeys = ROWS[r];
            int cols = rowKeys.length;

            // 先用“未偏移”的可用宽度估算键宽
            double rowW = (width - pad * 2);
            double keyW = (rowW - gapX * (cols - 1)) / cols;

            // 根据该行偏移量，重新计算 X 起点
            double offset = (r < ROW_OFFSET_IN_KEYW.length ? ROW_OFFSET_IN_KEYW[r] : 0.0) * keyW;
            double x = pad + offset;

            for (int c = 0; c < cols; c++) {
                String k = rowKeys[c];

                // 取 0..10 的计数
                int count = 0;
                if (counts != null) {
                    Integer v = counts.get(k);
                    if (v == null) v = counts.get(k.toLowerCase());
                    if (v == null) v = counts.get(k.toUpperCase());
                    count = (v == null) ? 0 : clampCount(v);
                }

                double t = (double) count / MAX_COUNT; // 归一化到 0..1

                // 键帽矩形
                Rectangle keyRect = new Rectangle(keyW, keyH);
                keyRect.setArcWidth(10);
                keyRect.setArcHeight(10);
                keyRect.setFill(colorFor(t));               // 白→红
                keyRect.setStroke(Color.web("#B3B3B3"));
                keyRect.setStrokeWidth(1.0);

                // 字符标签
                Text label = new Text(k);
                label.setFill(Color.BLACK);
                label.setFont(Font.font(12));

                // 用 StackPane 居中，不用手算文本宽高
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

    //  颜色映射：0..1 → 白(#FFFFFF) → 淡红(#FF6B6B) → 深红(#D80000)
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

    private static Color lerp(Color a, Color b, double t) {
        t = clamp01(t);
        return new Color(
                a.getRed()   + (b.getRed()   - a.getRed())   * t,
                a.getGreen() + (b.getGreen() - a.getGreen()) * t,
                a.getBlue()  + (b.getBlue()  - a.getBlue())  * t,
                1.0
        );
    }

    private static double clamp01(double v) { return Math.max(0.0, Math.min(1.0, v)); }
    private static int clampCount(int c)     { return Math.max(0, Math.min(MAX_COUNT, c)); }
}
