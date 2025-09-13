package typingninja.typing_ninja_1;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CertificatesScene {

    // 设计基准
    private static final double DESIGN_W = 1920, DESIGN_H = 1080;
    // 颜色与字体
    private static final String BG = "#140B38", GREEN = "#2EFF04", JARO = "/fonts/Jaro-Regular.ttf";

    // 标题（Jaro 180）
    private static final double TITLE_X = 36, TITLE_Y = 0, TITLE_SIZE = 180;

    // ScrollPane：1569×724，位置与大小
    private static final double SCROLL_X = 178, SCROLL_Y = 249; // = 249 + 200
    private static final double SCROLL_W = 1569, SCROLL_H = 724;

    // 内容占位（高于 ScrollPane 以产生滚动）
    private static final double CONTENT_W = 1569, CONTENT_H = 1000;

    // 右上角返回按钮
    private static final double BACK_X = 1734, BACK_Y = 52;

    // 底部 3 个文字“按钮”
    private static final double NAV_Y = 1000, NAV_FONT = 40;
    private static final double NAV_MM_X = 600, NAV_PF_X = 900, NAV_ST_X = 1150;

    public static Scene createScene(Stage stage) {
        // -------- 设计层 --------
        Pane design = new Pane();
        design.setPrefSize(DESIGN_W, DESIGN_H);
        design.setMinSize(DESIGN_W, DESIGN_H);
        design.setMaxSize(DESIGN_W, DESIGN_H);

        Font jaro180 = loadFont(JARO, TITLE_SIZE, Font.font("System", TITLE_SIZE));
        Label title  = label("CERTIFICATES", jaro180, Color.WHITE, TITLE_X, TITLE_Y);

        // ===== ScrollPane，严格限制为 1569×724 =====
        ScrollPane sp = new ScrollPane();
        sp.setLayoutX(SCROLL_X);
        sp.setLayoutY(SCROLL_Y);

        sp.setMinSize(SCROLL_W, SCROLL_H);
        sp.setPrefSize(SCROLL_W, SCROLL_H);
        sp.setMaxSize(SCROLL_W, SCROLL_H);

        sp.setFitToWidth(true);
        sp.setFitToHeight(false);

        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        sp.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");

        // 内容容器（灰色占位，高于 ScrollPane → 可滚动）
        Pane content = new Pane();
        content.setPrefSize(CONTENT_W, CONTENT_H);
        content.setMinHeight(CONTENT_H);
        content.setMaxHeight(CONTENT_H);
        content.setStyle("-fx-background-color: lightgray;");

        sp.setContent(content);

        Button backBtn = new Button("Back");
        backBtn.setLayoutX(BACK_X);
        backBtn.setLayoutY(BACK_Y);
        backBtn.setPrefWidth(139.64);   // 宽度
        backBtn.setPrefHeight(57.57);   // 高度

        backBtn.setStyle("-fx-background-color: " + GREEN + "; -fx-background-radius: 10;");
        backBtn.setOnAction(e -> stage.setScene(CongratulationsScene.createScene(stage)));

        Label mainMenu = label("MAIN MENU", Font.font("System", NAV_FONT), Color.WHITE, NAV_MM_X, NAV_Y);
        Label profile  = label("PROFILE",    Font.font("System", NAV_FONT), Color.LIME,  NAV_PF_X, NAV_Y);
        Label settings = label("SETTINGS",   Font.font("System", NAV_FONT), Color.WHITE, NAV_ST_X, NAV_Y);

        design.getChildren().addAll(title, sp, backBtn, mainMenu, profile, settings);

        // -------- 缩放容器：等比缩放 + 居中 + 背景 --------
        Group scalable = new Group(design);
        StackPane viewport = new StackPane(scalable);
        viewport.setAlignment(Pos.CENTER);
        viewport.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(viewport, 1280, 720, Color.web(BG));

        scalable.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(scene.getWidth() / DESIGN_W, scene.getHeight() / DESIGN_H),
                scene.widthProperty(), scene.heightProperty()
        ));
        scalable.scaleYProperty().bind(scalable.scaleXProperty());

        return scene;
    }

    // —— 小工具 —— //
    private static Label label(String text, Font font, Color color, double x, double y) {
        Label l = new Label(text);
        l.setFont(font);
        l.setTextFill(color);
        l.setLayoutX(x);
        l.setLayoutY(y);
        return l;
    }
    private static Font loadFont(String path, double size, Font fallback) {
        Font f = Font.loadFont(CertificatesScene.class.getResourceAsStream(path), size);
        return f != null ? f : fallback;
    }
}
