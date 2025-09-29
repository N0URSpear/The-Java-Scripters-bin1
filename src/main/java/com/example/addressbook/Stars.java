package com.example.addressbook;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class Stars {

    private static final String STAR_ON  = "/images/star_on.png";
    private static final String STAR_OFF = "/images/star_off.png";

    // 百分比 》 星数
    private static int percentToStars(double p) {
        if (p >= 90) return 5;
        if (p >= 80) return 4;
        if (p >= 70) return 3;
        if (p >= 40) return 2;
        return 1;
    }

    //生成星星组件
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
