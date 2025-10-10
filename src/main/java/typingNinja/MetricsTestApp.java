package com.example.addressbook;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MetricsTestApp extends Application {

    @Override
    public void start(Stage stage) {
        TextField wpmField = new TextField("66");
        wpmField.setPromptText("Words per minute (整数)");

        TextField accField = new TextField("99");
        accField.setPromptText("Accuracy % (0–100)");

        Button saveAndOpen = new Button("Save & Open Congratulations");
        Label hint = new Label("输入 WPM 与 Accuracy，点击按钮或回车写入数据库并打开 Congratulations 场景。");

        VBox root = new VBox(10,
                hint,
                new Label("WPM:"), wpmField,
                new Label("Accuracy (%):"), accField,
                saveAndOpen
        );
        root.setPadding(new Insets(16));
        root.setAlignment(Pos.CENTER_LEFT);

        // 回车提交
        root.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) saveAndOpen.fire();
        });

        saveAndOpen.setOnAction(e -> {
            try {
                // 去掉命名参数写法
                int wpm = parseNonNegativeInt(wpmField.getText().trim(), "WPM");
                int acc = parseBoundedInt(accField.getText().trim(), "Accuracy", 0, 100);

                ResultsBridge.ensureTable();
                ResultsBridge.saveResult(wpm, acc);

                // 传入当前 stage，匹配 createScene(Stage) 的签名
                Scene congrats = CongratulationsScene.createScene(stage);
                stage.setScene(congrats);
                stage.centerOnScreen();

            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "输入或保存失败：\n" + ex.getMessage()).showAndWait();
                ex.printStackTrace();
            }
        });

        stage.setTitle("Metrics Test");
        stage.setScene(new Scene(root, 440, 300));
        stage.show();
    }

    //输入校验
    private static int parseNonNegativeInt(String text, String field) {
        int v = Integer.parseInt(text);
        if (v < 0) throw new IllegalArgumentException(field + " must be ≥ 0");
        return v;
    }

    private static int parseBoundedInt(String text, String field, int min, int max) {
        int v = Integer.parseInt(text);
        if (v < min || v > max) throw new IllegalArgumentException(field + " must be " + min + "–" + max);
        return v;
    }

    public static void main(String[] args) {
        launch();
    }
}
