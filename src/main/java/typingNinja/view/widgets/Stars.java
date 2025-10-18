package typingNinja.view.widgets;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class Stars {

    private static final String STAR_ON  = "/images/star_on.png";
    private static final String STAR_OFF = "/images/star_off.png";

    /**
     * Convert a percentage value into a star count (e.g., 0–100% → 0–5 stars).
     *
     * @param p the percentage in the range 0–100
     * @return the computed star count (typically 0–5)
     */
    //rate to star
    private static int percentToStars(double p) {
        if (p >= 90) return 5;
        if (p >= 80) return 4;
        if (p >= 70) return 3;
        if (p >= 40) return 2;
        return 1;
    }

    /**
     * Build a horizontal row of star icons sized and spaced as specified.
     *
     * @param percent    the percentage used to determine how many stars are filled
     * @param starHeight the height of each star icon in pixels
     * @param gap        the horizontal gap between adjacent stars in pixels
     * @return an HBox containing the star nodes
     */
    //generate star
    public static HBox create(double percent, double starHeight, double gap) {
        int stars = percentToStars(percent);
        Image on  = new Image(Stars.class.getResourceAsStream(STAR_ON));
        Image off = new Image(Stars.class.getResourceAsStream(STAR_OFF));

        HBox box = new HBox(gap);
        box.setAlignment(Pos.CENTER);

        for (int i = 1; i <= 5; i++) {
            ImageView iv = new ImageView(i <= stars ? on : off);
            iv.setPreserveRatio(true);
            iv.setFitHeight(starHeight);
            box.getChildren().add(iv);
        }
        return box;
    }
}
