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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.fxml.FXMLLoader;

public class MainMenu {

    // Baseline design size (matches Figma at 1920x1080)
    private static final double BASE_WIDTH = 1920.0;
    private static final double BASE_HEIGHT = 1080.0;

    public Scene buildScene(Stage stage) {
        // Load custom fonts
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Jaro-Regular.ttf"), 10);
        Font.loadFont(getClass().getResourceAsStream("/com/example/addressbook/fonts/Inter-VariableFont.ttf"), 10);

        // Top bar
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

        // Lesson grid
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

        // Click handlers for lessons
        asButton(lesson1, () -> {
            openModal("/com/example/addressbook/SubLessonSelect.fxml", "Lesson 1");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });
        asButton(lesson2, () -> {
            openModal("/com/example/addressbook/SubLessonSelect.fxml", "Lesson 2");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });
        asButton(lesson3, () -> {
            openModal("/com/example/addressbook/SubLessonSelect.fxml", "Lesson 3");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });
        asButton(lesson4, () -> {
            openModal("/com/example/addressbook/SubLessonSelect.fxml", "Lesson 4");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });
        asButton(custom, () -> {
            openModal("/com/example/addressbook/CustomTopicSelect.fxml", "Custom Topic - AI Gen");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });
        asButton(freeType, () -> {
            openModal("/com/example/addressbook/FreeTypeSelect.fxml", "Free Type");
            switchTo(stage, "/com/example/addressbook/Typing.fxml", "Typing - Typing Ninja");
        });

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

        // Bottom nav click handlers
        asButton(profile, () -> switchTo(stage, "/com/example/addressbook/Profile.fxml", "Profile - Typing Ninja"));
        asButton(settings, () -> switchTo(stage, "/com/example/addressbook/Settings.fxml", "Settings - Typing Ninja"));

        HBox bottomMenu = new HBox(40, mainMenu, sep1, profile, sep2, settings);
        bottomMenu.setAlignment(Pos.CENTER);
        VBox.setMargin(bottomMenu, new Insets(40, 0, 20, 0));

        // Main content
        VBox content = new VBox(40, topBar, grid, bottomMenu);
        content.setPadding(new Insets(20));
        content.setPrefSize(BASE_WIDTH, BASE_HEIGHT);

        // Root and background
        Group contentGroup = new Group(content);
        StackPane outer = new StackPane(contentGroup);
        outer.setBackground(new Background(new BackgroundFill(Color.web("#140B38"),
                CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(outer, BASE_WIDTH, BASE_HEIGHT);

        // Reuse the same hover glow as Login
        scene.getStylesheets().add(
                getClass().getResource("/com/example/addressbook/NinjaStyles.css").toExternalForm()
        );

        // DPI-neutral scaling tied to window size
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

    private void openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage popup = new Stage();
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.initStyle(StageStyle.TRANSPARENT); // no OS chrome, true transparent window
            popup.setTitle(title);

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);        // let the FXML root render rounded corners
            popup.setScene(scene);
            popup.setResizable(false);
            popup.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Scales content based on window size and normalizes by screen DPI (96dpi = 100%)
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
