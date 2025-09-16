package typing_ninja;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class TypingApp extends Application {

    // 叠加弹窗容器（最外层）
    private StackPane root;
    // 全局外壳布局（固定 bottom，切换 center）
    private BorderPane rootLayout;
    // 全局唯一 Scene
    private Scene scene;

    // 底部菜单链接（全局字段，方便更新高亮）
    private Hyperlink mainMenuLink;
    private Hyperlink profileLink;
    private Hyperlink historyLink;
    private Hyperlink settingsLink;

    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Jaro-Regular.ttf"), 60);

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        // 外壳：固定底部菜单，切换中间页面
        rootLayout = new BorderPane();
        // 初始显示 Profile 页
        rootLayout.setCenter(buildProfilePage());
        // 底部菜单（一直在最底部）
        rootLayout.setBottom(buildFooterBar());

        // 初始化高亮为 Profile
        updateFooterHighlight("PROFILE");

        // 用 StackPane 包裹，承载弹窗
        root = new StackPane(rootLayout);

        // 全局唯一 Scene
        scene = new Scene(root, screenWidth, screenHeight);

        stage.setTitle("Typing Ninja");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    // ================= Profile 页面 =================
    private Node buildProfilePage() {
        BorderPane profileLayout = new BorderPane();
        profileLayout.setStyle("-fx-background-color: #0A002C;");

        // ---------------- 顶部 ----------------
        Label profileTitle = new Label("PROFILE");
        profileTitle.setTextFill(Color.WHITE);
        profileTitle.setFont(Font.font("Jaro", FontWeight.EXTRA_BOLD, 60));
        BorderPane.setAlignment(profileTitle, Pos.CENTER);

        // 右上角用户信息
        Label avatar = makeWhiteLabel("👤 Renee");
        avatar.setFont(Font.font("Jaro", FontWeight.BOLD, 32)); // 放大

        Image img = new Image(getClass().getResourceAsStream("/pencil-write-tool-icon.png"));
        ImageView pencilIcon = new ImageView(img);
        pencilIcon.setFitWidth(32);
        pencilIcon.setFitHeight(32);

        Button editUserBtn = new Button();
        editUserBtn.setGraphic(pencilIcon);
        editUserBtn.setStyle("-fx-background-color: transparent;");
        editUserBtn.setOnAction(e -> showEditUserPopup());

        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
                "-fx-background-color: #FF6F61;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;"
        );

        HBox userBox = new HBox(20, editUserBtn, avatar, logoutBtn);
        userBox.setAlignment(Pos.CENTER_RIGHT);
        userBox.setPadding(new Insets(20, 30, 0, 0));

        // 顶部容器：中间标题 + 右上角用户信息
        BorderPane topBar = new BorderPane();
        topBar.setCenter(profileTitle);
        topBar.setRight(userBox);

        profileLayout.setTop(topBar);

        // ---------------- 左边 Goals 区域 ----------------
        GridPane goalsPane = new GridPane();
        goalsPane.setHgap(40);
        goalsPane.setVgap(25);
        goalsPane.setPadding(new Insets(20));

        Label header1 = makeWhiteLabel("GOALS");
        Label header2 = makeWhiteLabel("Actual");
        goalsPane.addRow(0, new Label(""), header1, header2);

        Label g1v = makeWhiteLabel("3");
        Label a1 = makeGreenLabel("5.3");
        addGoalRow(goalsPane, 1, "Practice hours per week", g1v, a1, "");

        Label g2v = makeWhiteLabel("30 wpm");
        Label a2 = makeGreenLabel("27 wpm");
        addGoalRow(goalsPane, 2, "Target speed", g2v, a2, " wpm");

        Label g3v = makeWhiteLabel("97%");
        Label a3 = makeGreenLabel("98%");
        addGoalRow(goalsPane, 3, "Target accuracy", g3v, a3, "%");

        Button lessonHistoryBtn = new Button("Lesson History");
        styleGreenPill(lessonHistoryBtn);
        lessonHistoryBtn.setOnAction(e -> {
            rootLayout.setCenter(buildLessonHistoryPage());
            updateFooterHighlight("HISTORY");
        });

        Button viewCertificatesBtn = new Button("View Certificates");
        styleGreenPill(viewCertificatesBtn);

        VBox leftBox = new VBox(30, goalsPane, lessonHistoryBtn, viewCertificatesBtn);
        leftBox.setAlignment(Pos.TOP_LEFT);
        BorderPane.setMargin(leftBox, new Insets(80, 0, 0, 100));
        profileLayout.setLeft(leftBox);

        // ---------------- 右边统计信息 ----------------
        Label belt = makeWhiteLabel("Blue belt");
        belt.setFont(Font.font("Jaro", FontWeight.BOLD, 22));

        Label lessons = makeWhiteLabel("Lessons completed: 21");
        Label avg = makeWhiteLabel("Average WPM: 16");
        // Highest Rating
        HBox highest = new HBox(5,
                makeWhiteLabel("Highest Rating: "),
                makeShuriken(true),
                makeShuriken(true),
                makeShuriken(true),
                makeShuriken(true),
                makeShuriken(false) // 没获得的灰色
        );
        highest.setAlignment(Pos.CENTER_LEFT);

// Average Rating
        HBox avgRating = new HBox(5,
                makeWhiteLabel("Average Rating: "),
                makeShuriken(true),
                makeShuriken(true),
                makeShuriken(true),
                makeShuriken(false),
                makeShuriken(false)
        );
        avgRating.setAlignment(Pos.CENTER_LEFT);

        VBox rightContent = new VBox(30, belt, lessons, avg, highest, avgRating);

        rightContent.setAlignment(Pos.TOP_LEFT);
        rightContent.setPadding(new Insets(20));

        BorderPane.setMargin(rightContent, new Insets(120, 100, 0, 0));
        profileLayout.setRight(rightContent);

        return profileLayout;
    }


    // ================= Lesson History 页面 =================
    private Node buildLessonHistoryPage() {
        BorderPane page = new BorderPane();
        page.setStyle("-fx-background-color: #0A002C;");

        Label title = new Label("LESSON HISTORY");
        title.setFont(Font.font("Jaro", FontWeight.EXTRA_BOLD, 60));
        title.setTextFill(Color.WHITE);

        Region titleRule = new Region();
        titleRule.setPrefHeight(3);
        titleRule.setStyle("-fx-background-color: #35A8FF;");
        titleRule.prefWidthProperty().bind(title.widthProperty().add(40));

        VBox titleBox = new VBox(6, title, titleRule);
        titleBox.setAlignment(Pos.TOP_LEFT);

        Label best = new Label("Personal Best Full Keyboard Result: 72 WPM at 99% accuracy (achieved 24/08/2025)");
        best.setTextFill(Color.LIMEGREEN);
        best.setFont(Font.font("Jaro", FontWeight.BOLD, 24));

        Button back = new Button("Back");
        styleGreenPill(back);
        back.setOnAction(e -> {
            rootLayout.setCenter(buildProfilePage());
            updateFooterHighlight("PROFILE");
        });

        BorderPane bestAndBack = new BorderPane();
        bestAndBack.setLeft(best);
        bestAndBack.setRight(back);

        VBox topBox = new VBox(15, titleBox, bestAndBack);
        topBox.setPadding(new Insets(25, 25, 10, 25));
        page.setTop(topBox);

        VBox historyList = new VBox(12,
                makeHistoryRow("Lesson 1f", "84 WPM", "24/08/2025", 4),
                makeHistoryRow("Lesson 4",  "42 WPM", "24/08/2025", 3),
                makeHistoryRow("AI Gen",    "22 WPM", "24/08/2025", 2),
                makeHistoryRow("AI Gen",    "90 WPM", "24/08/2025", 5)
        );
        historyList.setPadding(new Insets(15));

        ScrollPane historyScroll = new ScrollPane(historyList);
        historyScroll.setFitToWidth(true);
        historyScroll.setPrefViewportHeight(420);
        historyScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox leftPanel = new VBox(15, historyScroll);
        leftPanel.setPadding(new Insets(15));
        leftPanel.setStyle("-fx-background-color: #D3D3D3; -fx-background-radius: 14;");
        leftPanel.setPrefWidth(560);

        Button exportPdf = new Button("Export to PDF");
        styleGreenPill(exportPdf);
        Button exportCsv = new Button("Export to CSV");
        styleGreenPill(exportCsv);

        HBox exportRow = new HBox(20, exportPdf, exportCsv);
        exportRow.setAlignment(Pos.CENTER_LEFT);
        VBox leftColumn = new VBox(15, leftPanel, exportRow);
        leftColumn.setAlignment(Pos.TOP_LEFT);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Date");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Value");
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setTitle("Typing Statistics Trend (Sample)");
        chart.setPrefSize(600, 450);

        XYChart.Series<String, Number> s1 = new XYChart.Series<>();
        s1.getData().add(new XYChart.Data<>("2025-08-16", 45));
        s1.getData().add(new XYChart.Data<>("2025-08-17", 65));
        s1.getData().add(new XYChart.Data<>("2025-08-18", 72));
        s1.getData().add(new XYChart.Data<>("2025-08-19", 74));
        s1.getData().add(new XYChart.Data<>("2025-08-20", 48));
        s1.getData().add(new XYChart.Data<>("2025-08-21", 60));
        s1.getData().add(new XYChart.Data<>("2025-08-22", 70));

        XYChart.Series<String, Number> s2 = new XYChart.Series<>();
        s2.getData().add(new XYChart.Data<>("2025-08-16", 95));
        s2.getData().add(new XYChart.Data<>("2025-08-17", 95));
        s2.getData().add(new XYChart.Data<>("2025-08-18", 89));
        s2.getData().add(new XYChart.Data<>("2025-08-19", 100));
        s2.getData().add(new XYChart.Data<>("2025-08-20", 87));
        s2.getData().add(new XYChart.Data<>("2025-08-21", 92));
        s2.getData().add(new XYChart.Data<>("2025-08-22", 85));

        chart.getData().addAll(s1, s2);

        HBox center = new HBox(40, leftColumn, chart);
        center.setPadding(new Insets(25));
        center.setAlignment(Pos.TOP_LEFT);

        page.setCenter(center);
        return page;
    }

    // ================= 固定在最底部的菜单栏 =================
    private Node buildFooterBar() {
        HBox footerBox = new HBox(20);
        footerBox.setAlignment(Pos.CENTER);
        footerBox.setPadding(new Insets(10, 0, 20, 0));
        footerBox.setStyle("-fx-background-color: #0A002C;");

        mainMenuLink = new Hyperlink("MAIN MENU");
        profileLink = new Hyperlink("PROFILE");
        historyLink = new Hyperlink("HISTORY");
        settingsLink = new Hyperlink("SETTINGS");

        for (Hyperlink link : new Hyperlink[]{mainMenuLink, profileLink, historyLink, settingsLink}) {
            link.setFont(Font.font("Jaro", FontWeight.BOLD, 18));
            link.setBorder(Border.EMPTY);
        }

        profileLink.setOnAction(e -> {
            rootLayout.setCenter(buildProfilePage());
            updateFooterHighlight("PROFILE");
        });
        historyLink.setOnAction(e -> {
            rootLayout.setCenter(buildLessonHistoryPage());
            updateFooterHighlight("HISTORY");
        });

        footerBox.getChildren().addAll(
                mainMenuLink, new Label("|"),
                profileLink, new Label("|"),
                historyLink, new Label("|"),
                settingsLink
        );
        return footerBox;
    }

    // ================= 更新底部菜单高亮 =================
    private void updateFooterHighlight(String active) {
        mainMenuLink.setTextFill(Color.WHITE);
        profileLink.setTextFill(Color.WHITE);
        historyLink.setTextFill(Color.WHITE);
        settingsLink.setTextFill(Color.WHITE);

        if ("PROFILE".equals(active)) {
            profileLink.setTextFill(Color.LIMEGREEN);
        } else if ("HISTORY".equals(active)) {
            historyLink.setTextFill(Color.LIMEGREEN);
        } else if ("SETTINGS".equals(active)) {
            settingsLink.setTextFill(Color.LIMEGREEN);
        }
    }

    // ================ 工具方法与弹窗逻辑（保留原样） ================

    private void addGoalRow(GridPane pane, int row, String title, Label goalLabel, Label actualLabel, String unit) {
        Label name = makeWhiteLabel(title);
        Image img = new Image(getClass().getResourceAsStream("/pencil-write-tool-icon.png"));
        ImageView pencilIcon = new ImageView(img);
        pencilIcon.setFitWidth(24);
        pencilIcon.setFitHeight(24);

        Button editBtn = new Button();
        editBtn.setGraphic(pencilIcon);
        editBtn.setStyle("-fx-background-color: transparent;");
        editBtn.setOnAction(e -> showEditPopup(goalLabel, unit));

        pane.addRow(row, name, new HBox(10, editBtn, goalLabel), actualLabel);
    }

    private void showEditPopup(Label goalLabel, String unit) {
        VBox popup = new VBox(15);
        popup.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20; -fx-background-radius: 10;");
        popup.setAlignment(Pos.CENTER);

        TextField input = new TextField();
        input.setPromptText("请输入新的数值 (数字)");

        Button ok = new Button("OK");
        ok.setOnAction(e -> {
            String value = input.getText().trim();
            if (!value.isEmpty()) {
                goalLabel.setText(value + unit);
            }
            root.getChildren().remove(popup);
        });

        popup.getChildren().addAll(new Label("修改目标数值:"), input, ok);
        root.getChildren().add(popup);
    }

    private void showEditUserPopup() {
        VBox popup = new VBox(20);
        popup.setStyle("-fx-background-color: #DDDDDD; -fx-padding: 20; -fx-background-radius: 15;");
        popup.setAlignment(Pos.TOP_CENTER);

        popup.setPrefWidth(600);
        popup.setPrefHeight(400);
        popup.setMaxWidth(Region.USE_PREF_SIZE);
        popup.setMaxHeight(Region.USE_PREF_SIZE);

        StackPane container = new StackPane(popup);
        container.setAlignment(Pos.CENTER);

        Label title = new Label("Edit User");
        title.setFont(Font.font("Jaro", FontWeight.BOLD, 24));

        Button closeBtn = new Button("Back");
        closeBtn.setStyle("-fx-background-color: #7CFC00; -fx-text-fill: black; -fx-font-weight: bold;");
        closeBtn.setOnAction(e -> root.getChildren().remove(container));

        HBox headerBox = new HBox(title, closeBtn);
        HBox.setHgrow(title, Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setSpacing(20);
        headerBox.setPadding(new Insets(0, 20, 0, 20));

        Label usernameLabel = new Label("Renee");
        TextField usernameField = new TextField("Renee");
        usernameField.setVisible(false);
        Button pencil1 = makePencil(() -> {
            if (!usernameField.isVisible()) {
                usernameField.setText(usernameLabel.getText());
                usernameLabel.setVisible(false);
                usernameField.setVisible(true);
                usernameField.requestFocus();
            } else {
                String value = usernameField.getText().trim();
                if (!value.isEmpty()) {
                    usernameLabel.setText(value);
                }
                usernameField.setVisible(false);
                usernameLabel.setVisible(true);
            }
        });
        HBox usernameBox = new HBox(10, new Label("Username:"), usernameLabel, usernameField, pencil1);

        Label passwordLabel = new Label("********");
        TextField passwordField = new TextField("mypassword");
        passwordField.setVisible(false);
        Button pencil2 = makePencil(() -> {
            if (!passwordField.isVisible()) {
                passwordField.setText(passwordField.getText());
                passwordLabel.setVisible(false);
                passwordField.setVisible(true);
                passwordField.requestFocus();
            } else {
                String value = passwordField.getText().trim();
                if (!value.isEmpty()) {
                    passwordLabel.setText("********");
                }
                passwordField.setVisible(false);
                passwordLabel.setVisible(true);
            }
        });
        HBox passwordBox = new HBox(10, new Label("Password:"), passwordLabel, passwordField, pencil2);

        ComboBox<String> q1 = new ComboBox<>();
        q1.getItems().addAll("City of Birth", "Mother's Maiden Name", "Favorite Teacher", "Pet's Name", "First School");
        q1.getSelectionModel().selectFirst();
        Label answer1Label = new Label("********");
        TextField answer1Field = new TextField("Brisbane");
        answer1Field.setVisible(false);
        Button pencil3 = makePencil(() -> {
            if (!answer1Field.isVisible()) {
                answer1Field.setText(answer1Field.getText());
                answer1Label.setVisible(false);
                answer1Field.setVisible(true);
                answer1Field.requestFocus();
            } else {
                String value = answer1Field.getText().trim();
                if (!value.isEmpty()) {
                    answer1Label.setText("********");
                }
                answer1Field.setVisible(false);
                answer1Label.setVisible(true);
            }
        });
        HBox q1Box = new HBox(10, q1, answer1Label, answer1Field, pencil3);

        ComboBox<String> q2 = new ComboBox<>();
        q2.getItems().addAll("Favorite Color", "Favorite Movie", "First Car", "Best Friend", "Dream Job");
        q2.getSelectionModel().selectFirst();
        Label answer2Label = new Label("********");
        TextField answer2Field = new TextField("Blue");
        answer2Field.setVisible(false);
        Button pencil4 = makePencil(() -> {
            if (!answer2Field.isVisible()) {
                answer2Field.setText(answer2Field.getText());
                answer2Label.setVisible(false);
                answer2Field.setVisible(true);
                answer2Field.requestFocus();
            } else {
                String value = answer2Field.getText().trim();
                if (!value.isEmpty()) {
                    answer2Label.setText("********");
                }
                answer2Field.setVisible(false);
                answer2Label.setVisible(true);
            }
        });
        HBox q2Box = new HBox(10, q2, answer2Label, answer2Field, pencil4);

        Button resetTypingBtn = new Button("Reset Typing Statistics");
        resetTypingBtn.setStyle("-fx-background-color: #FF6F61; -fx-text-fill: white; -fx-font-weight: bold;");

        Button resetLessonBtn = new Button("Reset Lesson Completion");
        resetLessonBtn.setStyle("-fx-background-color: #FF6F61; -fx-text-fill: white; -fx-font-weight: bold;");

        Button deleteAccountBtn = new Button("Delete Account");
        deleteAccountBtn.setStyle("-fx-background-color: #FF6F61; -fx-text-fill: white; -fx-font-weight: bold;");

        resetTypingBtn.setOnAction(e ->
                showWarningPopup(
                        "You are about to permanently delete all your typing statistics.\nLesson completion will remain intact.",
                        "Permanently delete typing statistics"
                )
        );

        resetLessonBtn.setOnAction(e ->
                showWarningPopup(
                        "You are about to permanently delete all your lesson completion and earned ninja stars.\nTyping statistics will remain intact.",
                        "Permanently delete lesson completion and ninja stars"
                )
        );

        deleteAccountBtn.setOnAction(e ->
                showWarningPopup(
                        "You are about to permanently delete your entire account.\nThis action cannot be reversed.",
                        "Permanently delete my account"
                )
        );


        HBox bottomButtons = new HBox(20, resetTypingBtn, resetLessonBtn, deleteAccountBtn);
        bottomButtons.setAlignment(Pos.CENTER);

        popup.getChildren().addAll(headerBox, usernameBox, passwordBox, q1Box, q2Box, bottomButtons);

        root.getChildren().add(container);
    }

    // 通用警告弹窗（使用 Jaro 字体）
    // 通用警告弹窗（和 Edit User 弹窗一样的格式）
    private void showWarningPopup(String message, String deleteButtonText) {
        VBox popup = new VBox(25);
        popup.setAlignment(Pos.CENTER);
        popup.setPadding(new Insets(30));
        popup.setStyle("-fx-background-color: lightgray; -fx-background-radius: 15;");

        // 固定大小（和 Edit User 一致）
        popup.setPrefWidth(600);
        popup.setPrefHeight(400);
        popup.setMaxWidth(Region.USE_PREF_SIZE);
        popup.setMaxHeight(Region.USE_PREF_SIZE);

        // ==WARNING== 标题
        Label warningLabel = new Label("==WARNING==");
        warningLabel.setFont(Font.font("Jaro", FontWeight.EXTRA_BOLD, 48));
        warningLabel.setTextFill(Color.RED);

        // 提示信息
        Label msgLabel = new Label(message);
        msgLabel.setFont(Font.font("Jaro", FontWeight.BOLD, 20));
        msgLabel.setWrapText(true);
        msgLabel.setAlignment(Pos.CENTER);
        msgLabel.setStyle("-fx-text-fill: black;");
        msgLabel.setMaxWidth(500);

        // 输入框部分
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        Label prompt = new Label("Type YES if you wish to proceed");
        prompt.setFont(Font.font("Jaro", FontWeight.BOLD, 18));
        TextField inputField = new TextField();
        inputField.setPrefWidth(220);
        inputBox.getChildren().addAll(prompt, inputField);

        // 底部两个按钮
        Button backBtn = new Button("Back to profile");
        backBtn.setStyle(
                "-fx-background-color: #7CFC00;" +
                        "-fx-text-fill: black;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 20;"
        );

        // ❗ 正确删除 overlay
        backBtn.setOnAction(e -> root.getChildren().remove(root.getChildren().size() - 1));

        Button deleteBtn = new Button(deleteButtonText);
        deleteBtn.setStyle(
                "-fx-background-color: #FF6F61;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 18px;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 20;"
        );
        deleteBtn.setOnAction(e -> {
            if ("YES".equalsIgnoreCase(inputField.getText().trim())) {
                System.out.println("Confirmed: " + deleteButtonText);
                root.getChildren().remove(root.getChildren().size() - 1);
            } else {
                inputField.setStyle("-fx-border-color: red; -fx-border-width: 2;");
            }
        });

        HBox buttons = new HBox(20, backBtn, deleteBtn);
        buttons.setAlignment(Pos.CENTER);

        popup.getChildren().addAll(warningLabel, msgLabel, inputBox, buttons);

        // ✅ 只保留一层 overlay
        StackPane overlay = new StackPane(popup);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.6);");

        root.getChildren().add(overlay);
    }


    // lesson = "Lesson 1f", wpm = "84 WPM", date = "24/08/2025", stars = "4"
// stars 参数就是星数（int）
    private HBox makeHistoryRow(String lesson, String wpm, String date, int stars) {
        Label lLesson = new Label(lesson);
        Label lWpm   = new Label(wpm);
        Label lDate  = new Label(date);

        for (Label l : new Label[]{lLesson, lWpm, lDate}) {
            l.setFont(Font.font("Jaro", FontWeight.BOLD, 18));
            l.setStyle("-fx-text-fill: black;");
        }

        // 数字部分
        Label countLabel = new Label(stars + "×");
        countLabel.setFont(Font.font("Jaro", FontWeight.BOLD, 18));
        countLabel.setStyle("-fx-text-fill: black;");

        // 手里剑图标（用金色）
        Image img = new Image(getClass().getResourceAsStream("/gold_shuriken.png"));
        ImageView shuriken = new ImageView(img);
        shuriken.setFitWidth(22);
        shuriken.setFitHeight(22);

        HBox starBox = new HBox(5, countLabel, shuriken);
        starBox.setAlignment(Pos.CENTER_LEFT);

        HBox row = new HBox(30, lLesson, lWpm, lDate, starBox);
        row.setPadding(new Insets(12));
        row.setStyle(
                "-fx-background-color: #F0F0F0;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: black;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 10;"
        );
        return row;
    }

    private void styleGreenPill(Button b) {
        b.setStyle(
                "-fx-background-color: #7CFC00;" +
                        "-fx-text-fill: black;" +
                        "-fx-font-size: 20px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 22;" +
                        "-fx-padding: 10 26 10 26;"
        );
        b.setPrefWidth(300);
    }

    private Button makePencil(Runnable action) {
        Image img = new Image(getClass().getResourceAsStream("/pencil-write-tool-icon.png"));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(20);
        iv.setFitHeight(20);
        Button b = new Button();
        b.setGraphic(iv);
        b.setStyle("-fx-background-color: transparent;");
        b.setOnAction(e -> action.run());
        return b;
    }

    private Label makeWhiteLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.WHITE);
        l.setFont(Font.font("Jaro", FontWeight.BOLD, 40));
        return l;
    }

    // 工具方法：生成手里剑
    private ImageView makeShuriken(boolean earned) {
        String file = earned ? "/gold_shuriken.png" : "/gray_shuriken.png";
        Image img = new Image(getClass().getResourceAsStream(file));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(40);   // 控制大小
        iv.setFitHeight(40);
        return iv;
    }

    private Label makeGreenLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.LIMEGREEN);
        l.setFont(Font.font("Jaro", FontWeight.BOLD, 22));
        return l;
    }

    private Label makeGoldLabel(String text) {
        Label l = new Label(text);
        l.setTextFill(Color.GOLD);
        l.setFont(Font.font("Jaro", FontWeight.BOLD, 22));
        return l;
    }

    public static void main(String[] args) {
        launch();
    }
}
