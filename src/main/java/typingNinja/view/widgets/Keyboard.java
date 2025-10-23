package typingNinja.view.widgets;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;

import java.util.Map;

public final class Keyboard {

    private Keyboard() {}

    /**
     * Load up to {@code limit} recent results from the database, enrich each with
     * lesson/user metadata, and return rows in oldest→newest order.
     * If lookups or parsing fail, defaults are used (e.g., "Unknown", "Student Name",
     * and {@link java.time.LocalDate#now()}).
     * <p>Performs blocking database I/O.</p>
     *
     * @param limit maximum number of results to fetch
     * @return list of rows in chronological order (never {@code null})
     */

    private static final String[][] ROWS = new String[][]{
            {"`","1","2","3","4","5","6","7","8","9","0","-","+","="} ,
            {"Tab","Q","W","E","R","T","Y","U","I","O","P","[","]"},
            {"Cap","A","S","D","F","G","H","J","K","L", ";","'","Enter"},
            {"Shift","Z","X","C","V","B","N","M", ",", ".","/","Shift"},
            //if there is an error with capital letter for example "A",
            //then it count as an error on both "a" and "Shift"
            {"ctrl","fn","alt","Space","ctrl","alt"}
    };


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
        Rectangle bg = new Rectangle(width, height);
        bg.setArcWidth(32);
        bg.setArcHeight(32);
        bg.setFill(Color.web("#D9D9D9"));

        Pane keysLayer = new Pane();
        keysLayer.setPadding(new Insets(10));
        keysLayer.setPrefSize(width, height);
        keysLayer.setMinSize(width, height);
        keysLayer.setMaxSize(width, height);

        double pad  = 14;
        double gapX = 6;
        double gapY = 8;
        int rows = ROWS.length;

        //counting the misstake
        int total = 0;
        if (counts != null) {
            for (Integer v : counts.values()) {
                total += (v == null ? 0 : Math.max(0, v));
            }
        }
        final int totalErrors = total;

        double keyH = (height - pad * 2 - gapY * (rows - 1)) / rows;

        double y = pad;
        for (int r = 0; r < rows; r++) {
            String[] rowKeys = ROWS[r];
            int cols = rowKeys.length;

            double rowW = (width - pad * 2);
            double keyW = (rowW - gapX * (cols - 1)) / cols;

            double offset = (r < ROW_OFFSET_IN_KEYW.length ? ROW_OFFSET_IN_KEYW[r] : 0.0) * keyW;
            double x = pad + offset;

            for (int c = 0; c < cols; c++) {
                String k = rowKeys[c];

                int count = 0;
                if (counts != null) {
                    Integer v = counts.get(k);
                    if (v == null) v = counts.get(k.toLowerCase());
                    if (v == null) v = counts.get(k.toUpperCase());
                    if (v == null && " ".equals(k)) v = counts.get("SPACE");
                    count = (v == null) ? 0 : Math.max(0, v);
                }

                double t = (totalErrors > 0) ? (count / (double) totalErrors) : 0.0; // 0.0 ~ 1.0

                Rectangle keyRect = new Rectangle(keyW, keyH);
                keyRect.setArcWidth(10);
                keyRect.setArcHeight(10);
                keyRect.setFill(colorFor(t));
                keyRect.setStroke(Color.web("#B3B3B3"));
                keyRect.setStrokeWidth(1.0);

                Text label = new Text(k);
                label.setFill(Color.BLACK);
                try {
                    label.setFont(Font.font("Jaro", FontWeight.BOLD, 12));
                } catch (Exception ignored) {
                    label.setFont(Font.font(12));
                }

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
