package com.example.addressbook;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainMenu {

    // Baseline design size (matches Figma at 1920x1080)
    private static final double BASE_WIDTH = 1920;
    private static final double BASE_HEIGHT = 1080;

    public Scene buildScene(Stage stage) {
        // 🔹 Load custom fonts (from src/main/resources/com/example/addressbook/fonts)
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Jaro-Regular.ttf"), 10);
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Inter-VariableFont.ttf"), 10);

        // ----- TOP BAR -----
        Label title = new Label("TYPING NINJA");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Jaro", 180));

        Region logoSpace = new Region(); // placeholder for logo
        logoSpace.setPrefSize(520, 180);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(title);
        topBar.setRight(logoSpace);
        BorderPane.setAlignment(title, Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 0, 20));

        // ----- LESSON GRID -----
        VBox lesson1 = createLessonBox("Lesson 1 - The Home Row",
                "Type words using just the home row\nwhere your fingers rest");
        VBox lesson2 = createLessonBox("Lesson 2 - The Next Step",
                "Introducing the upper and lower rows");
        VBox lesson3 = createLessonBox("Lesson 3 - Shift Up",
                "Introducing the shift key and numbers");
        VBox lesson4 = createLessonBox("Lesson 4 - Full Keyboard",
                "All letters, numbers, and special characters");
        VBox custom = createLessonBox("Custom Topic - AI Gen",
                "Choose any topic and get a custom\nlesson created for you");
        VBox freeType = createLessonBox("Free Type",
                "Type whatever you want or practice\nyour least accurate key combos");

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(80);
        grid.setAlignment(Pos.CENTER);
        grid.add(lesson1, 0, 0);
        grid.add(lesson2, 1, 0);
        grid.add(lesson3, 2, 0);
        grid.add(lesson4, 0, 1);
        grid.add(custom, 1, 1);
        grid.add(freeType, 2, 1);

        // ----- BOTTOM MENU -----
        Label mainMenu = new Label("MAIN MENU");
        Label sep1 = new Label("|");
        Label profile = new Label("PROFILE");
        Label sep2 = new Label("|");
        Label settings = new Label("SETTINGS");

        // Colors
        mainMenu.setTextFill(Color.web("#2EFF04"));
        profile.setTextFill(Color.WHITE);
        settings.setTextFill(Color.WHITE);
        sep1.setTextFill(Color.WHITE);
        sep2.setTextFill(Color.WHITE);

        // Fonts (Jaro 40 for all)
        mainMenu.setFont(Font.font("Jaro", 40));
        profile.setFont(Font.font("Jaro", 40));
        settings.setFont(Font.font("Jaro", 40));
        sep1.setFont(Font.font("Jaro", 40));
        sep2.setFont(Font.font("Jaro", 40));

        HBox bottomMenu = new HBox(40, mainMenu, sep1, profile, sep2, settings);
        bottomMenu.setAlignment(Pos.CENTER);
        VBox.setMargin(bottomMenu, new Insets(40, 0, 20, 0));

        // 🔹 点击跳转 Profile
        profile.setOnMouseClicked(e -> {
            int userId = SessionManager.getCurrentUserId(); // 从 session 获取当前用户

            // 创建 ProfilePage 实例（它本身就是 BorderPane）
            ProfilePage profilePage = new ProfilePage(userId);

            // 获取屏幕大小并设置 75%
            javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
            double WIDTH = screenBounds.getWidth() * 0.75;
            double HEIGHT = screenBounds.getHeight() * 0.75;

            // 创建场景
            Scene profileScene = new Scene(profilePage.getRoot(), WIDTH, HEIGHT);
            stage.setScene(profileScene);

            // 居中窗口
            stage.setX((screenBounds.getWidth() - WIDTH) / 2);
            stage.setY((screenBounds.getHeight() - HEIGHT) / 2);
        });



        // ----- MAIN CONTENT -----
        VBox content = new VBox(40, topBar, grid, bottomMenu);
        content.setPadding(new Insets(20));
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        // ----- SCALE UNIFORMLY -----
        Group contentGroup = new Group(content);
        StackPane outer = new StackPane(contentGroup);
        outer.setBackground(new Background(new BackgroundFill(Color.web("#140B38"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(outer, BASE_WIDTH, BASE_HEIGHT);

        // Scale factor = min(width/1920, height/1080)
        NumberBinding scale = Bindings.min(
                outer.widthProperty().divide(BASE_WIDTH),
                outer.heightProperty().divide(BASE_HEIGHT)
        );
        content.scaleXProperty().bind(scale);
        content.scaleYProperty().bind(scale);

        return scene;
    }

    private VBox createLessonBox(String headingText, String descriptionText) {
        Label heading = new Label(headingText);
        heading.setFont(Font.font("Jaro", 40));
        heading.setTextFill(Color.BLACK);
        heading.setWrapText(true);

        Label description = new Label(descriptionText);
        description.setFont(Font.font("Inter", 25));
        description.setTextFill(Color.BLACK);
        description.setWrapText(true);

        VBox box = new VBox(15, heading, description);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20, 20, 20, 20));
        box.setPrefSize(512, 250); // exact Figma size
        box.setStyle(
                "-fx-background-color: #D9D9D9;" +
                        "-fx-border-color: #2EFF04;" +
                        "-fx-border-width: 3;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        );
        return box;
    }
}
