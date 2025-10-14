package typingNinja;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import typingNinja.auth.Session;


public class CongratulationsScene {

    // 设计基准
    private static final double DESIGN_W = 1920, DESIGN_H = 1080;
    //set the size in to a veriable

    // 颜色与字体
    private static final String BG = "#140B38", GREEN = "#2EFF04", JARO = "/fonts/Jaro-Regular.ttf";

    // 标题
    private static final double TITLE_X = 251, TITLE_Y = 17, TITLE_SIZE = 180;

    // 文本
    private static final double TEXT_SIZE = 40;
    private static final double RED_HINT_X = 111, RED_HINT_Y = 571;
    private static final double PREV10_X = 103, PREV10_Y = 966;

    // 中间白框
    private static final double BOX_W = 668.77, BOX_H = 134.86, BOX_R = 20;
    private static final double WPM_X = 828, WPM_Y = 534;
    private static final double ACC_X = 828, ACC_Y = 700;

    // 绿色按钮
    private static final double BTN_W = 653, BTN_H = 60, BTN_R = 20;
    private static final double PRINT_X = 841, PRINT_Y = 865.46;
    private static final double BACK_X  = 841, BACK_Y  = 956.46;


    //星星
    private static final double STARS_X = 748;
    private static final double STARS_Y = 260;
    private static final double STAR_HEIGHT = 120; // 根据图调，常见 100~140
    private static final double STAR_GAP    = 28;  // 星与星之间的间距

    //表格
    private static final double TABLE_X = 121;
    private static final double TABLE_Y = 689;
    private static final double TABLE_W = 485;
    private static final double TABLE_H = 266;


    private static final double KEY_X = 103;
    private static final double KEY_Y = 344;
    private static final double KEY_W = 521;
    private static final double KEY_H = 223;

    /**
     * Build and return the "Congratulations" scene.
     *
     * @param stage the JavaFX Stage used to size the UI and navigate between scenes
     * @return the Scene for the Congratulations screen
     */
    public static Scene createScene(Stage stage) {
        Pane design = new Pane();
        design.setPrefSize(DESIGN_W, DESIGN_H);
        design.setMinSize(DESIGN_W, DESIGN_H);
        design.setMaxSize(DESIGN_W, DESIGN_H);

        // 标题
        Font jaro180 = loadFont(JARO, TITLE_SIZE, Font.font("System", FontWeight.EXTRA_BOLD, TITLE_SIZE));
        Label title = label("CONGRATULATIONS", jaro180, Color.WHITE, TITLE_X, TITLE_Y);
        title.setFont(Font.font("Jaro", 180));


        // 两行说明文字
        Label redHint = label("Red indicates error frequency", Font.font("Jaro", TEXT_SIZE), Color.WHITE, RED_HINT_X, RED_HINT_Y);
        Label prev10  = label("Previous 10 results for this lesson", Font.font("Jaro", TEXT_SIZE), Color.WHITE, PREV10_X,  PREV10_Y);

        // 白框 + 文案
        Rectangle wpmBox = whiteBox(WPM_X, WPM_Y);
        Label wpmLabel   = label("Words per minute", Font.font("Jaro", 28), Color.BLACK, WPM_X + 19, WPM_Y + 10);

        Rectangle accBox = whiteBox(ACC_X, ACC_Y);
        Label accLabel   = label("Accuracy", Font.font("Jaro", 28), Color.BLACK, ACC_X + 19, ACC_Y + 10);

        // 读取数据库最新成绩，覆盖初始文案
        ResultsBridge.getLatest().ifPresent(latest -> {
            wpmLabel.setText("Words per minute: " + latest[0]);
            accLabel.setText("Accuracy: " + latest[1] + "%");
        });


        // 绿色按钮（示例：都跳到 Certificates）
        Button printBtn = greenButton("Print Certificate", PRINT_X, PRINT_Y);
        Button backBtn  = greenButton("Return to Main Menu", BACK_X, BACK_Y);
        printBtn.setOnAction(e -> stage.setScene(CertificatesScene.createScene(stage)));
        backBtn.setOnAction(e -> stage.setScene(new MainMenu().buildScene(stage)));


// 数据10次
        ResultsBridge.ensureTable();
        var m = ResultsBridge.loadLastN(10);      // 旧→新顺序
        List<Integer> wpmData = m.wpm();
        List<Integer> accData = m.acc();

// 左侧标签显示最新一条（当次）
        ResultsBridge.getLatest().ifPresent(latest -> {
            // 允许 latest[] 来自小数，统一四舍五入
            int latestWpm = (int) Math.round(latest[0]);
            int latestAcc = (int) Math.round(latest[1]);
            wpmLabel.setText("Words per minute: " + latestWpm);
            accLabel.setText("Accuracy: " + latestAcc + "%");
        });


// 创建图表组件并定位
        Node resultsChart = Table.create(TABLE_W, TABLE_H, wpmData, accData);
        resultsChart.setLayoutX(TABLE_X);
        resultsChart.setLayoutY(TABLE_Y);

// 添加到设计层
        design.getChildren().add(resultsChart);



// 在 build UI 的地方
        int latestAcc = accData.isEmpty() ? 0 : accData.get(accData.size() - 1);
        int accPercent = Math.max(0, Math.min(100, latestAcc)); // 夹紧到 0..100
        HBox stars = Stars.create(accPercent, STAR_HEIGHT, STAR_GAP);
        stars.setLayoutX(STARS_X);
        stars.setLayoutY(STARS_Y);
        design.getChildren().add(stars);


// 键盘
        {
            // 1) 先按 Keyboard 布局把所有键置零，避免缺键
            String[][] rows = new String[][]{
                    {"`","1","2","3","4","5","6","7","8","9","0","-","+","="},
                    {"Tab","Q","W","E","R","T","Y","U","I","O","P","[","]"},
                    {"Cap","A","S","D","F","G","H","J","K","L",";","'","Enter"},
                    {"Shift","Z","X","C","V","B","N","M",",",".","/","Shift"},
                    {"ctrl","fn","alt","Space","ctrl","alt"}
            };
            java.util.Map<String,Integer> heat = new java.util.LinkedHashMap<>();
            for (String[] row : rows) for (String k : row) heat.put(k, 0);

            // 2) 用本次逐键错误计数覆盖（来自 Session，控制器在跳转前已 setLatestTotals）
            java.util.Map<String,Integer> totals = typingNinja.auth.Session.getLatestTotals();
            if (totals != null) {
                for (java.util.Map.Entry<String,Integer> e : totals.entrySet()) {
                    String k = e.getKey();                 // 如 "A","1"
                    if (" ".equals(k)) k = "Space";        // 若以后统计空格，这里映射到“Space”
                    String Ku = k.toUpperCase();

                    if      (heat.containsKey(k))  heat.put(k,  e.getValue());
                    else if (heat.containsKey(Ku)) heat.put(Ku, e.getValue());
                    else {
                        String Kl = k.toLowerCase();
                        if (heat.containsKey(Kl)) heat.put(Kl, e.getValue());
                    }
                }
            }

            // 3) 渲染键盘（Keyboard.create 内部会按百分比着色）
            javafx.scene.Node keyboard = Keyboard.create(KEY_W, KEY_H, heat);
            keyboard.setLayoutX(KEY_X);
            keyboard.setLayoutY(KEY_Y);
            design.getChildren().add(keyboard);
        }




        design.getChildren().addAll(
                title, redHint, prev10,
                wpmBox, wpmLabel,
                accBox, accLabel,
                printBtn, backBtn
        );

        // 缩放容器：等比缩放 + 居中 + 背景
        Group scalable = new Group(design);
        StackPane viewport = new StackPane(scalable);
        viewport.setAlignment(Pos.CENTER);
        viewport.setStyle("-fx-background-color: " + BG + ";");

        Scene scene = new Scene(viewport, 1280, 720, Color.web(BG));

        // 等比缩放绑定
        scalable.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(scene.getWidth() / DESIGN_W, scene.getHeight() / DESIGN_H),
                scene.widthProperty(), scene.heightProperty()
        ));
        scalable.scaleYProperty().bind(scalable.scaleXProperty());

        return scene;
    }

    /**
     * Create the white rounded rectangle background used as the statistics box.
     *
     * @param x the x-coordinate of the box layout position
     * @param y the y-coordinate of the box layout position
     * @return the Rectangle node
     */
    private static Rectangle whiteBox(double x, double y) {
        Rectangle r = new Rectangle(BOX_W, BOX_H);
        r.setArcWidth(BOX_R * 2);
        r.setArcHeight(BOX_R * 2);
        r.setFill(Color.WHITE);
        r.setLayoutX(x);
        r.setLayoutY(y);
        return r;
    }

    /**
     * Create a styled green button positioned at the given coordinates.
     *
     * @param text the button label text
     * @param x the x-coordinate of the button layout position
     * @param y the y-coordinate of the button layout position
     * @return the Button node
     */
    private static Button greenButton(String text, double x, double y) {
        Button b = new Button(text);
        b.setLayoutX(x);
        b.setLayoutY(y);
        b.setPrefSize(BTN_W, BTN_H);
        b.setStyle("-fx-background-color: " + GREEN + ";" +
                "-fx-background-radius: " + BTN_R + ";" +
                "-fx-border-radius: " + BTN_R + ";");
        return b;
    }

    /**
     * Create a label with specified text, font, color, and position.
     *
     * @param text the label text content
     * @param font the Font applied to the label
     * @param color the text Color
     * @param x the x-coordinate of the label layout position
     * @param y the y-coordinate of the label layout position
     * @return the Label node
     */
    private static Label label(String text, Font font, Color color, double x, double y) {
        Label l = new Label(text);
        l.setFont(font);
        l.setTextFill(color);
        l.setLayoutX(x);
        l.setLayoutY(y);
        return l;
    }


    /**
     * Load a font resource from the classpath; return the fallback when unavailable.
     *
     * @param path the resource path of the font within the classpath
     * @param size the requested font size
     * @param fallback the Font to use if loading fails
     * @return the loaded Font or the fallback when the resource is missing
     */
    private static Font loadFont(String path, double size, Font fallback) {
        Font f = Font.loadFont(CongratulationsScene.class.getResourceAsStream(path), size);
        return f != null ? f : fallback;
    }
}
