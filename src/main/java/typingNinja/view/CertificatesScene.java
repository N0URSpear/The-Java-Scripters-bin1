package typingNinja.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.io.IOException;


import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import java.io.File;

import typingNinja.util.SceneNavigator;
import javafx.scene.Parent;

public class CertificatesScene {

    private static final double DESIGN_W = 1920, DESIGN_H = 1080;
    private static final String BG = "#140B38", GREEN = "#2EFF04", JARO = "/fonts/Jaro-Regular.ttf";

    // title
    private static final double TITLE_X = 36, TITLE_Y = 0, TITLE_SIZE = 180;

    // ScrollPane
    private static final double SCROLL_X = 178, SCROLL_Y = 249;
    private static final double SCROLL_W = 1569, SCROLL_H = 724;

    private static final double CONTENT_W = 1569, CONTENT_H = 10000;

    // buttum
    private static final double BACK_X = 1734, BACK_Y = 52;
    private static final double NAV_Y = 1000, NAV_FONT = 40;
    private static final double NAV_MM_X = 600, NAV_PF_X = 900, NAV_ST_X = 1150;


    /**
     * Create a navigation label with active/inactive styling.
     *
     * @param text   the label text
     * @param active whether the label should be styled as the active (selected) item
     * @return the configured Label
     */
    // hightlight
    private static Label navLabel(String text, boolean active) {
        Label l = new Label(text);
        l.setFont(Font.font("Jaro", 40)); // 与 MainMenu 一致
        l.setTextFill(active ? Color.web("#2EFF04") : Color.WHITE); // 绿色高亮当前页
        return l;
    }


    /**
     * Build the bottom navigation bar node for the Certificates screen.
     *
     * @param stage the Stage used for scene switching
     * @param root  the root Pane to which the navigation may be attached/aligned
     * @return the navigation Node
     */

    private static Node buildBottomNav(Stage stage, Pane root) {
        Label mainMenu = navLabel("MAIN MENU", false);
        Label sep1     = navLabel("|", false);
        Label profile  = navLabel("PROFILE",   true);
        Label sep2     = navLabel("|", false);
        Label settings = navLabel("SETTINGS",  false);


        asButton(mainMenu, () -> new MainMenu().show(stage));

        asButton(profile, () -> navigate(stage,
                "/typingNinja/ProfilePage.fxml",
                "Profile - Typing Ninja"));
        asButton(settings, () -> navigate(stage,
                "/typingNinja/Settings.fxml",
                "Settings - Typing Ninja"));

        HBox box = new HBox(12, mainMenu, sep1, profile, sep2, settings);
        box.setAlignment(Pos.CENTER);
        box.prefWidthProperty().bind(root.widthProperty());
        box.layoutYProperty().bind(root.heightProperty().subtract(60)); // 距底约 60px，可微调
        return box;
    }


    /**
     * Make the given label behave like a button (hover/click handlers) and bind an action.
     *
     * @param l      the Label to decorate with button-like behavior
     * @param action the action to run when the label is activated
     */
    private static void asButton(Label l, Runnable action) {
        l.setOnMouseEntered(e -> l.setUnderline(true));
        l.setOnMouseExited(e -> l.setUnderline(false));
        l.setOnMouseClicked(e -> action.run());
        l.setCursor(Cursor.HAND);
    }


    /**
     * Switch the current Stage to a new scene loaded from an FXML resource and set the window title.
     *
     * @param stage the Stage to switch
     * @param fxml  the classpath path of the FXML resource
     * @param title the window title for the new scene
     */
    private static void navigate(Stage stage, String fxml, String title) {
        try {
            SceneNavigator.load(stage, fxml, title);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Display the certificates screen on the primary stage while preserving the existing scene instance.
     *
     * @param stage the Stage used to size and host the view
     */
    public static void show(Stage stage) {
        SceneNavigator.show(stage, createView(stage), "Certificates - Typing Ninja");
    }

    /**
     * Build the Certificates view and return it as a scalable root node.
     * The UI is laid out at a fixed design size and uniformly scaled to fit
     * the available viewport while preserving aspect ratio. The view includes
     * a scrollable results list populated from the database and a Back button
     * that navigates to the Congratulations screen.
     * <p>Must be called on the JavaFX Application Thread.</p>
     *
     * @param stage the hosting {@link javafx.stage.Stage} used for navigation callbacks and sizing context; must not be {@code null}
     * @return the root {@link javafx.scene.Parent} for the Certificates view (never {@code null})
     */

    public static Parent createView(Stage stage) {
        //desigh
        Pane design = new Pane();
        design.setPrefSize(DESIGN_W, DESIGN_H);
        design.setMinSize(DESIGN_W, DESIGN_H);
        design.setMaxSize(DESIGN_W, DESIGN_H);

        Font jaro180 = loadFont(JARO, TITLE_SIZE, Font.font("Jaro", TITLE_SIZE));
        Label title  = label("CERTIFICATES", jaro180, Color.WHITE, TITLE_X, TITLE_Y);

        // ScrollPane
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

        Pane content = new Pane();
        content.setPrefSize(CONTENT_W, CONTENT_H);
        content.setMinHeight(CONTENT_H);
        content.setMaxHeight(CONTENT_H);
        content.setStyle("-fx-background-color: lightgray;");

        VBox listBox = new VBox(16);
        listBox.setLayoutX(40);
        listBox.setLayoutY(40);
        listBox.setPrefWidth(CONTENT_W - 80);
        content.getChildren().add(listBox);

        populateListFromDb(listBox);

        sp.setContent(content);

        //back buttum
        Button backBtn = new Button("Back");
        backBtn.setLayoutX(BACK_X);
        backBtn.setLayoutY(BACK_Y);
        backBtn.setPrefWidth(140);
        backBtn.setPrefHeight(58);
        backBtn.setStyle("-fx-background-color: " + GREEN + "; -fx-background-radius: 10;");
        backBtn.setFont(Font.font("Jaro", 40));
        backBtn.setOnAction(e -> CongratulationsScene.show(stage));

        design.getChildren().add(buildBottomNav(stage, design));

        design.getChildren().addAll(title, sp, backBtn);


        Group scalable = new Group(design);
        StackPane viewport = new StackPane(scalable);
        viewport.setAlignment(Pos.CENTER);
        viewport.setStyle("-fx-background-color: " + BG + ";");

        scalable.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> {
                    double w = viewport.getWidth();
                    double h = viewport.getHeight();
                    if (w <= 0 || h <= 0) {
                        return 1.0;
                    }
                    return Math.min(w / DESIGN_W, h / DESIGN_H);
                },
                viewport.widthProperty(),
                viewport.heightProperty()
        ));
        scalable.scaleYProperty().bind(scalable.scaleXProperty());

        return viewport;
    }

    /**
     * Immutable view-model for a single Certificates list entry.
     * Holds lesson metadata and performance metrics used to render one row.
     * Instances are read-only and safe to share across JavaFX nodes.
     */
    private static final class Row {
        final int index;
        final int lessonId;
        final int wpm;
        final int acc;
        final LocalDate date;
        final String lessonType;
        final String userName;

         /**
         * Create a new immutable row for the Certificates list.
         *
         * @param index      1-based ordinal position in the list (for UI display)
         * @param lessonId   unique lesson identifier from the data source
         * @param wpm        typing speed in words per minute
         * @param acc        typing accuracy percentage (0–100)
         * @param date       date the result was recorded; must not be {@code null}
         * @param lessonType human-readable lesson type/title; may be {@code null} or empty if unknown
         * @param userName   user display name; may be {@code null} or empty if anonymous
         */
        Row(int index, int lessonId, int wpm, int acc,
            LocalDate date, String lessonType, String userName) {
            this.index = index;
            this.lessonId = lessonId;
            this.wpm = wpm;
            this.acc = acc;
            this.date = date;
            this.lessonType = lessonType;
            this.userName = userName;
        }
    }

    /**
     * Parse the first 10 characters of the given string as a {@code yyyy-MM-dd} date.
     * Any time or timezone component after position 10 is ignored. If the input is
     * {@code null}, shorter than 10 characters, or not parseable, the current date is returned.
     *
     * @param dt a date/time string whose first 10 characters follow {@code yyyy-MM-dd} (e.g., {@code "2025-10-23T12:34:56Z"}); may be {@code null}
     * @return the parsed {@link java.time.LocalDate}, or {@link java.time.LocalDate#now()} if parsing fails
     */

    private static LocalDate toLocalDate(String dt) {
        try {
            return LocalDate.parse(dt.substring(0, 10));
        } catch (Exception ignore) {
            return LocalDate.now();
        }
    }

    /**
     * Load up to {@code limit} recent results from the database, enrich each with
     * lesson/user metadata, and return rows in oldest→newest order.
     * If lookups or parsing fail, sensible defaults are used (e.g., "Unknown" lesson type,
     * "Student Name", and {@link java.time.LocalDate#now()}).
     * <p>This performs blocking DB I/O.</p>
     *
     * @param limit maximum number of results to fetch
     * @return a list of {@link Row} entries in chronological order (never {@code null})
     */
    private static java.util.List<Row> loadRowsFromDb(int limit) {
        java.util.List<Row> out = new java.util.ArrayList<>();

        try {
            Connection conn = typingNinja.model.SqliteConnection.getInstance();
            typingNinja.model.SqliteResultsDAO dao = new typingNinja.model.SqliteResultsDAO(conn);
            java.util.List<typingNinja.model.Result> base = dao.getLastN(limit);
            java.util.Collections.reverse(base);

            try (PreparedStatement psLesson = conn.prepareStatement(
                    "SELECT LessonType, UserID FROM Lesson WHERE LessonID=?");
                 PreparedStatement psUser = conn.prepareStatement(
                         "SELECT Username FROM Users WHERE UserID=?")) {

                int idx = 1;
                for (typingNinja.model.Result r : base) {
                    int lessonId = r.id();
                    String lessonType = "Unknown";
                    int userId = -1;

                    psLesson.setInt(1, lessonId);
                    try (ResultSet rs = psLesson.executeQuery()) {
                        if (rs.next()) {
                            lessonType = rs.getString(1);
                            userId = rs.getInt(2);
                        }
                    }

                    String userName = "Student Name";
                    if (userId > 0) {
                        psUser.setInt(1, userId);
                        try (ResultSet rs2 = psUser.executeQuery()) {
                            if (rs2.next()) userName = rs2.getString(1);
                        }
                    }

                    out.add(new Row(
                            idx++,
                            lessonId,
                            r.wpm(),
                            r.acc(),
                            toLocalDate(r.createdAt()),
                            lessonType,
                            userName
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }


    /**
     * Populate the certificates list UI from the database.
     *
     * @param listBox the VBox container to fill with certificate rows/items
     */
    private static void populateListFromDb(VBox listBox) {
        listBox.getChildren().clear();

        typingNinja.model.ResultsBridge.ensureTable();

        java.util.List<Row> rows = loadRowsFromDb(1000);

        if (rows.isEmpty()) {
            Label empty = new Label("No results yet.");
            empty.setTextFill(Color.BLACK);
            empty.setStyle("-fx-opacity: 0.6;");
            listBox.getChildren().add(empty);
            return;
        }

        for (Row r : rows) {
            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPrefWidth(listBox.getPrefWidth());
            row.setStyle("-fx-background-color: rgba(0,0,0,0.04); -fx-background-radius: 10;");
            row.setPadding(new Insets(10, 12, 10, 12));

            Label info = new Label(String.format(
                    "#%03d   WPM: %d   ACC: %d%%",
                    r.index, r.wpm, r.acc
            ));
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
                    chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf")); // PDF (Portable Document Format)
                    chooser.setInitialFileName(String.format(
                            "certificate_%03d_%dwpm_%d%%.pdf",
                            r.index, r.wpm, r.acc
                    ));
                    File file = chooser.showSaveDialog(listBox.getScene().getWindow());
                    if (file == null) return;

                    // —— 关键：全部换成数据库真实值 ——
                    String name = r.userName;
                    int typingSpeedWpm = r.wpm;
                    double accuracyPercent = r.acc;
                    LocalDate dateCompleted = r.date;
                    String lesson = r.lessonType;

                    typingNinja.view.pdf.CertificatePdfUtil.saveSimpleCertificate(
                            file.toPath(),
                            name,
                            typingSpeedWpm,
                            accuracyPercent,
                            dateCompleted,
                            lesson
                    );
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            row.getChildren().addAll(info, spacer, downloadBtn);
            listBox.getChildren().add(row);
        }

        double estimatedHeight = 40 + rows.size() * 64.0;
        Pane parent = (Pane) listBox.getParent();
        parent.setMinHeight(Math.max(CONTENT_H, estimatedHeight));
        parent.setPrefHeight(Math.max(CONTENT_H, estimatedHeight));
        parent.setMaxHeight(Math.max(CONTENT_H, estimatedHeight));
    }


    /**
     * Create a label with specified text, font, color, and position.
     *
     * @param text  the label text content
     * @param font  the Font to apply
     * @param color the text Color
     * @param x     the x-coordinate of the label layout position
     * @param y     the y-coordinate of the label layout position
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
     * Load a font resource from the classpath; return the fallback if loading fails.
     *
     * @param path     the font resource path within the classpath
     * @param size     the requested font size
     * @param fallback the Font to use when the resource is unavailable
     * @return the loaded Font or the fallback when loading fails
     */
    private static Font loadFont(String path, double size, Font fallback) {
        Font f = Font.loadFont(CertificatesScene.class.getResourceAsStream(path), size);
        return f != null ? f : fallback;
    }
}
