package com.example.addressbook;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;

import javafx.fxml.FXMLLoader;

public class MainMenu {

    private static final double BASE_WIDTH = 1920.0;
    private static final double BASE_HEIGHT = 1080.0;

    public Scene buildScene(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Jaro-Regular.ttf"), 10);
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Inter-VariableFont.ttf"), 10);

        // Top bar
        Label title = new Label("TYPING NINJA");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Jaro", 180));

        Region logoSpace = new Region();
        logoSpace.setPrefSize(520, 180);

        BorderPane topBar = new BorderPane();
        topBar.setLeft(title);
        topBar.setRight(logoSpace);
        BorderPane.setAlignment(title, Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 20, 0, 20));

        // Lessons
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

        // Click handlers – open modals (controllers handle DB + navigation)
        asButton(lesson1, () -> openSubLesson(
                stage,
                "Lesson 1 - The Home Row",
                "1",
                new String[]{
                        "Lesson 1a - f, j and space keys",
                        "Lesson 1b - g, and h keys",
                        "Lesson 1c - d and k keys",
                        "Lesson 1d - s and l keys",
                        "Lesson 1e - a and ; keys",
                        "Lesson 1f - The whole home row"
                }));

        asButton(lesson2, () -> openSubLesson(
                stage,
                "Lesson 2 - The Next Step",
                "2",
                new String[]{
                        "Lesson 2a - r, t, y, u keys",
                        "Lesson 2b - q, w, e, i, o, p keys",
                        "Lesson 2c - The whole upper row",
                        "Lesson 2d - c, v, b, n keys",
                        "Lesson 2e - z, x, m keys",
                        "Lesson 2f - The whole lower row"
                }));

        asButton(lesson3, () -> openSubLesson(
                stage,
                "Lesson 3 - Shift Up",
                "3",
                new String[]{
                        "Lesson 3a - 4, 5, 6, 7",
                        "Lesson 3b - 1, 2, 3, 8, 9, 0",
                        "Lesson 3c - The whole number row",
                        "Lesson 3d - Shift and home row",
                        "Lesson 3e - Shift and all letters",
                        "Lesson 3f - Special characters"
                }));

        asButton(lesson4, () -> openSubLesson(
                stage,
                "Lesson 4 - Full Keyboard",
                "4",
                new String[]{
                        "Lesson 4a - Full keyboard - Very Easy",
                        "Lesson 4b - Full keyboard - Easy",
                        "Lesson 4c - Full keyboard - Medium",
                        "Lesson 4d - Full keyboard - Hard",
                        "Lesson 4e - Full keyboard - Very Hard",
                        "Lesson 4f - Full keyboard - Expert"
                }));

        // Custom / FreeType
        asButton(custom,   () -> openCustomTopic(stage));
        asButton(freeType, () -> openFreeType(stage));

        // Grid
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

        // Bottom menu
        Label mainMenu = new Label("MAIN MENU");
        Label sep1 = new Label("|");
        Label profile = new Label("PROFILE");
        Label sep2 = new Label("|");
        Label settings = new Label("SETTINGS");

        mainMenu.setTextFill(Color.web("#2EFF04"));
        profile.setTextFill(Color.WHITE);
        settings.setTextFill(Color.WHITE);
        sep1.setTextFill(Color.WHITE);
        sep2.setTextFill(Color.WHITE);

        mainMenu.setFont(Font.font("Jaro", 40));
        profile.setFont(Font.font("Jaro", 40));
        settings.setFont(Font.font("Jaro", 40));
        sep1.setFont(Font.font("Jaro", 40));
        sep2.setFont(Font.font("Jaro", 40));

        asButton(profile, () -> switchTo(stage, "/com/example/addressbook/Profile.fxml", "Profile - Typing Ninja"));
        asButton(settings, () -> switchTo(stage, "/com/example/addressbook/Settings.fxml", "Settings - Typing Ninja"));

        HBox bottomMenu = new HBox(40, mainMenu, sep1, profile, sep2, settings);
        bottomMenu.setAlignment(Pos.CENTER);
        VBox.setMargin(bottomMenu, new Insets(40, 0, 20, 0));

        VBox content = new VBox(40, topBar, grid, bottomMenu);
        content.setPadding(new Insets(20));
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        Group contentGroup = new Group(content);
        StackPane outer = new StackPane(contentGroup);
        outer.setBackground(new Background(new BackgroundFill(Color.web("#140B38"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(outer, BASE_WIDTH, BASE_HEIGHT);

        try {
            scene.getStylesheets().add(
                    getClass().getResource("/com/example/addressbook/NinjaStyles.css").toExternalForm()
            );
        } catch (Exception ignored) {}

        bindDpiNeutralScale(outer, content);
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
        box.setPrefSize(512, 250);
        box.setStyle(
                "-fx-background-color: #D9D9D9;" +
                        "-fx-border-color: #2EFF04;" +
                        "-fx-border-width: 3;" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-radius: 15;"
        );
        return box;
    }

    private void asButton(javafx.scene.Node node, Runnable onClick) {
        node.getStyleClass().add("button");
        node.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> { if (onClick != null) onClick.run(); });
        node.addEventHandler(MouseEvent.MOUSE_PRESSED,  e -> { node.setScaleX(0.98); node.setScaleY(0.98); });
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> { node.setScaleX(1.0);  node.setScaleY(1.0);  });
    }

    private void switchTo(Stage stage, String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Scene current = stage.getScene();
            if (current == null) {
                stage.setScene(new Scene(root));
            } else {
                current.setRoot(root);
            }
            if (title != null && !title.isEmpty()) stage.setTitle(title);
            stage.centerOnScreen();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // ---------- Popups (controllers will handle DB + navigation) ----------

    private void openSubLesson(Stage owner, String title, String codePrefix, String[] leftTexts) {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/example/addressbook/SubLessonSelect.fxml"));
            Parent root = fxml.load();
            com.example.addressbook.controllers.SubLessonSelectController c = fxml.getController();
            c.configure(title, codePrefix, leftTexts);

            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openCustomTopic(Stage owner) {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/example/addressbook/CustomTopicSelect.fxml"));
            Parent root = fxml.load();

            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openFreeType(Stage owner) {
        try {
            FXMLLoader fxml = new FXMLLoader(getClass().getResource("/com/example/addressbook/FreeTypeSelect.fxml"));
            Parent root = fxml.load();

            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.UNDECORATED);
            popup.setScene(new Scene(root));
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // DPI-neutral scaling
    private void bindDpiNeutralScale(StackPane outer, Region content) {
        Runnable apply = () -> {
            double w = outer.getWidth();
            double h = outer.getHeight();
            if (w <= 0 || h <= 0) return;

            double dpiScale = javafx.stage.Screen.getPrimary().getDpi() / 96.0;
            double sx = w / (BASE_WIDTH  * dpiScale);
            double sy = h / (BASE_HEIGHT * dpiScale);
            double s = Math.min(sx, sy);

            content.setScaleX(s);
            content.setScaleY(s);
        };
        outer.widthProperty().addListener((o, ov, nv) -> apply.run());
        outer.heightProperty().addListener((o, ov, nv) -> apply.run());
        apply.run();
    }
}
