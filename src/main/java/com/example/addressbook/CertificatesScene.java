package com.example.addressbook;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.fxml.FXMLLoader;
import java.io.IOException;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CertificatesScene {

    // 设计基准
    private static final double DESIGN_W = 1920, DESIGN_H = 1080;
    // 颜色与字体
    private static final String BG = "#140B38", GREEN = "#2EFF04", JARO = "/fonts/Jaro-Regular.ttf";

    // 标题
    private static final double TITLE_X = 36, TITLE_Y = 0, TITLE_SIZE = 180;

    // ScrollPane
    private static final double SCROLL_X = 178, SCROLL_Y = 249;
    private static final double SCROLL_W = 1569, SCROLL_H = 724;

    // 内容占位，高于 ScrollPane 以产生滚动
    private static final double CONTENT_W = 1569, CONTENT_H = 1000;

    // 右上角返回按钮
    private static final double BACK_X = 1734, BACK_Y = 52;

    // 底部 3 个文字按钮
    private static final double NAV_Y = 1000, NAV_FONT = 40;
    private static final double NAV_MM_X = 600, NAV_PF_X = 900, NAV_ST_X = 1150;

    // 创建带高亮的文本标签
    private static Label navLabel(String text, boolean active) {
        Label l = new Label(text);
        l.setFont(Font.font("Jaro", 40)); // 与 MainMenu 一致
        l.setTextFill(active ? Color.web("#2EFF04") : Color.WHITE); // 绿色高亮当前页
        return l;
    }

    // 构建底部栏（贴底居中）
    private static Node buildBottomNav(Stage stage, Pane root) {
        // Certificates 页面常见是 PROFILE 高亮；如需 MAIN MENU 高亮，把 true 改到 mainMenu
        Label mainMenu = navLabel("MAIN MENU", false);
        Label sep1     = navLabel("|", false);
        Label profile  = navLabel("PROFILE",   true);
        Label sep2     = navLabel("|", false);
        Label settings = navLabel("SETTINGS",  false);

        // 点击行为（复制 MainMenu 的跳转方式）
        asButton(mainMenu, () -> stage.setScene(new MainMenu().buildScene(stage)));

        asButton(profile, () -> switchTo(stage,
                "/com/example/addressbook/Profile.fxml",
                "Profile - Typing Ninja"));
        asButton(settings, () -> switchTo(stage,
                "/com/example/addressbook/Settings.fxml",
                "Settings - Typing Ninja"));

        // 水平排布 + 居中 + 贴底
        HBox box = new HBox(12, mainMenu, sep1, profile, sep2, settings);
        box.setAlignment(Pos.CENTER);
        box.prefWidthProperty().bind(root.widthProperty());
        box.layoutYProperty().bind(root.heightProperty().subtract(60)); // 距底约 60px，可微调
        return box;
    }

    // 若项目里暂无 asButton，这里给一个本地最小实现（已有就删掉这段）
    private static void asButton(Label l, Runnable action) {
        l.setOnMouseEntered(e -> l.setUnderline(true));
        l.setOnMouseExited(e -> l.setUnderline(false));
        l.setOnMouseClicked(e -> action.run());
        l.setCursor(Cursor.HAND);
    }

    // 若项目里暂无 switchTo，这里给一个本地最小实现（已有就删掉这段）
    private static void switchTo(Stage stage, String fxml, String title) {
        try {
            Parent root = FXMLLoader.load(CertificatesScene.class.getResource(fxml));

            Scene sc = new Scene(root, stage.getScene() != null ? stage.getScene().getWidth() : 1280,
                    stage.getScene() != null ? stage.getScene().getHeight() : 720);
            stage.setTitle(title);
            stage.setScene(sc);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public static Scene createScene(Stage stage) {
        // 设计层
        Pane design = new Pane();
        design.setPrefSize(DESIGN_W, DESIGN_H);
        design.setMinSize(DESIGN_W, DESIGN_H);
        design.setMaxSize(DESIGN_W, DESIGN_H);

        Font jaro180 = loadFont(JARO, TITLE_SIZE, Font.font("System", TITLE_SIZE));
        Label title  = label("CERTIFICATES", jaro180, Color.WHITE, TITLE_X, TITLE_Y);

        // ScrollPane + 内容
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

        // 内容容器（保持你的 Pane 架构，但内部放一个 VBox 列表）
        Pane content = new Pane();
        content.setPrefSize(CONTENT_W, CONTENT_H);
        content.setMinHeight(CONTENT_H);
        content.setMaxHeight(CONTENT_H);
        content.setStyle("-fx-background-color: lightgray;");

        // 放置列表容器（在内容 Pane 内，控制具体偏移）
        VBox listBox = new VBox(16);
        listBox.setLayoutX(40);           // 你可以微调这个位置
        listBox.setLayoutY(40);
        listBox.setPrefWidth(CONTENT_W - 80); // 两侧各留 40px
        content.getChildren().add(listBox);

        // —— 渲染数据库数据到列表 —— //
        populateListFromDb(listBox);

        sp.setContent(content);

        //返回按钮
        Button backBtn = new Button("Back");
        backBtn.setLayoutX(BACK_X);
        backBtn.setLayoutY(BACK_Y);
        backBtn.setPrefWidth(140);
        backBtn.setPrefHeight(58);
        backBtn.setStyle("-fx-background-color: " + GREEN + "; -fx-background-radius: 10;");
        backBtn.setOnAction(e -> stage.setScene(CongratulationsScene.createScene(stage)));

        // 底部 3 个文字按钮
        design.getChildren().add(buildBottomNav(stage, design));

        design.getChildren().addAll(title, sp, backBtn);


        //缩放容器：等比缩放 + 居中 + 背景
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

    // 从数据库加载，并渲染每条 result 一行 + 按钮
    private static final class Row {
        final int index;
        final int wpm;
        final int acc; // 0..100
        Row(int index, int wpm, int acc) { this.index = index; this.wpm = wpm; this.acc = acc; }
    }

    private static void populateListFromDb(VBox listBox) {
        listBox.getChildren().clear();

        // 1) 取数（用 ResultsBridge；没有 getAll 时取最近 1000 条，旧→新）
        ResultsBridge.ensureTable();
        var m = ResultsBridge.loadLastN(1000);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < m.wpm().size(); i++) {
            rows.add(new Row(i + 1, m.wpm().get(i), m.acc().get(i)));
        }

        // 2) 没数据时给出提示
        if (rows.isEmpty()) {
            Label empty = new Label("No results yet.");
            empty.setTextFill(Color.BLACK);
            empty.setStyle("-fx-opacity: 0.6;");
            listBox.getChildren().add(empty);
            return;
        }

        // 3) 每条渲染为一行：左侧信息、右侧 Download 按钮
        for (Row r : rows) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPrefWidth(listBox.getPrefWidth());
            row.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 10;");
            row.setPadding(new Insets(10, 12, 10, 12));

            Label info = new Label(String.format("#%03d   WPM: %d   ACC: %d%%", r.index, r.wpm, r.acc));
            info.setTextFill(Color.BLACK);
            info.setStyle("-fx-font-size: 20;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button downloadBtn = new Button("Download PDF");
            downloadBtn.setPrefSize(180, 36);
            downloadBtn.setStyle("-fx-background-color: #2D9CDB; -fx-text-fill: white; -fx-background-radius: 10; -fx-font-size: 16;");

            downloadBtn.setOnAction(ev -> {
                try {
                    FileChooser chooser = new FileChooser();
                    chooser.setTitle("Save Certificate PDF");
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
                    chooser.setInitialFileName(String.format("certificate_%03d_%dwpm_%d%%.pdf", r.index, r.wpm, r.acc));
                    File file = chooser.showSaveDialog(listBox.getScene().getWindow());
                    if (file == null) return;

                    // 证书字段
                    String name = "Student Name";
                    int typingSpeedWpm = r.wpm;
                    double accuracyPercent = r.acc;
                    LocalDate dateCompleted = LocalDate.now();
                    String lesson = "Lesson 7 - Punctuation";

                    CertificatePdfUtil.saveSimpleCertificate(
                            file.toPath(),
                            name,
                            typingSpeedWpm,
                            accuracyPercent,
                            dateCompleted,
                            lesson
                    );

                    // 如需生成后自动打开系统 PDF 查看器：

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            row.getChildren().addAll(info, spacer, downloadBtn);
            listBox.getChildren().add(row);
        }

        // 4) 根据行数把内容高度拉高，保证可滚动
        double estimatedHeight = 40 + rows.size() * 64.0;
        Pane parent = (Pane) listBox.getParent();
        parent.setMinHeight(Math.max(CONTENT_H, estimatedHeight));
        parent.setPrefHeight(Math.max(CONTENT_H, estimatedHeight));
        parent.setMaxHeight(Math.max(CONTENT_H, estimatedHeight));
    }

    // 小工具
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
