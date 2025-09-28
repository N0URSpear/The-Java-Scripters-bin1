package com.example.addressbook.controllers;

import com.example.addressbook.lesson.InputSection;
import com.example.addressbook.lesson.KeyboardHands;
import com.example.addressbook.lesson.Metrics;
import com.example.addressbook.lesson.PauseMenu;
import com.example.addressbook.lesson.ProgressBar;
import com.example.addressbook.lesson.CustomPrompts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;
import com.example.addressbook.auth.Session;
import com.example.addressbook.lesson.Lesson;
import com.example.addressbook.lesson.LessonDAO;
import com.example.addressbook.ai.AITextService;
import com.example.addressbook.ai.OpenAITextService;

import java.util.concurrent.CompletableFuture;


public class LessonActivePageController {
  @FXML private HBox buttonBar;
  @FXML private Button pauseButton;
  @FXML private ChoiceBox<String> modeChoice;
  @FXML private Label timerLabel;

  @FXML private StackPane readingStack;
  @FXML private TextFlow promptFlow;
  @FXML private TextFlow userFlow;
  @FXML private javafx.scene.control.ProgressBar timeProgress;

  @FXML private GridPane helpGrid;
  @FXML private VBox statsBox;
  @FXML private GridPane keyboardGrid;
  @FXML private Region handsRegion;
  @FXML private Label handsLabel;

  @FXML private TextArea hiddenInput;
  @FXML private Label wpmLabel;
  @FXML private Label errorsLabel;
  @FXML private Label accuracyLabel;
  @FXML private Label lessonTitleLabel;
  @FXML private Label lessonDurationLabel;

  private Metrics metrics;
  private CustomPrompts prompts;
  private ProgressBar progressFeature;
  private PauseMenu pauseMenu;
  private KeyboardHands keyboardHands;
  private InputSection inputSection;

    private void buildInputSectionAndStart(String passage) {
        inputSection = new InputSection(
                promptFlow, userFlow, hiddenInput, keyboardHands, metrics, passage
        );
        keyboardHands.highlightExpected(inputSection.peekExpected());

        // These filters already exist below your old InputSection line; keep only one set
        hiddenInput.addEventFilter(KeyEvent.KEY_TYPED, inputSection::onKeyTyped);
        hiddenInput.addEventFilter(KeyEvent.KEY_PRESSED, inputSection::onKeyPressed);

        readingStack.setOnMouseClicked(e -> hiddenInput.requestFocus());
        promptFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
        userFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
        keyboardGrid.setOnMouseClicked(e -> hiddenInput.requestFocus());

        boolean strict = modeChoice.getSelectionModel().getSelectedIndex() == 0;
        inputSection.setStrictMode(strict);
        modeChoice.getSelectionModel().selectedIndexProperty()
                .addListener((obs, o, n) -> inputSection.setStrictMode(n.intValue() == 0));


        Platform.runLater(() -> {
            hiddenInput.setText("");
            hiddenInput.requestFocus();
            metrics.start();
            keyboardHands.highlightExpected(inputSection.peekExpected());
        });
    }

    @FXML
    private void initialize() {
        prompts = new CustomPrompts();
        CustomPrompts.Prompt p = prompts.current();
        lessonTitleLabel.setText(p.title());
        lessonDurationLabel.setText(p.durationSeconds() + "s");

        metrics = new Metrics(p.durationSeconds());
        metrics.bindTimerLabel(timerLabel);
        metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);

        progressFeature = new ProgressBar(timeProgress);
        progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());

        keyboardHands = new KeyboardHands(keyboardGrid, handsRegion, handsLabel);
        keyboardHands.buildQwerty();

        // Decide passage based on latest Lesson for the current user
        LessonDAO lessonDAO = new LessonDAO();
        int userId = Session.getCurrentUserId();
        Lesson latest = null;

        try {
            latest = lessonDAO.fetchLatestForUser(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }

// Rebind metrics for the real lesson if we have one; otherwise keep your fallback from CustomPrompts
        if (latest == null) {
            // Fallback to the existing p.* values you already set above
            buildInputSectionAndStart(p.text());
        } else {
            // Set labels based on DB
            lessonTitleLabel.setText(latest.getLessonType());
            int durationSeconds = Math.max(10, latest.getDurationMinutes() * 60);

            // Recreate metrics and rebind UI to it (overriding the earlier p.durationSeconds binding)
            metrics = new Metrics(durationSeconds);
            metrics.bindTimerLabel(timerLabel);
            metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);
            progressFeature = new com.example.addressbook.lesson.ProgressBar(timeProgress);
            progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());

            String lt = latest.getLessonType();
            boolean isCustom = "CustomTopic".equalsIgnoreCase(lt);
            boolean isFree = (lt != null && lt.toLowerCase().startsWith("free")); // matches your FreeTypeSelectController ("FreeWeakKeys", "FreeAnything")

            if (isFree) {
                // Free typing → no fixed passage
                buildInputSectionAndStart("");

            } else if (isCustom) {
                // CustomTopic → try OpenAI; on failure, fall back to local generator
                AITextService openai = new OpenAITextService();
                AITextService local = new com.example.addressbook.ai.LocalSimpleTextService();
                int targetWords = Math.max(60, latest.getDurationMinutes() * 50);

                Lesson finalLatest = latest;
                CompletableFuture
                        .supplyAsync(() -> {
                            try {
                                // 1) Try OpenAI
                                return openai.generatePassage(
                                        finalLatest.getPrompt(),
                                        targetWords,
                                        finalLatest.isUpperCase(),
                                        finalLatest.isNumbers(),
                                        finalLatest.isPunctuation(),
                                        finalLatest.isSpecialChars()
                                );
                            } catch (Exception ex) {
                                // 2) Any failure (e.g., 429 insufficient_quota) → local free generator
                                try {
                                    return local.generatePassage(
                                            finalLatest.getPrompt(),
                                            targetWords,
                                            finalLatest.isUpperCase(),
                                            finalLatest.isNumbers(),
                                            finalLatest.isPunctuation(),
                                            finalLatest.isSpecialChars()
                                    );
                                } catch (Exception inner) {
                                    inner.printStackTrace();
                                    return null;
                                }
                            }
                        })
                        .thenAccept(text -> {
                            String finalPassage = (text == null || text.isBlank())
                                    ? new com.example.addressbook.lesson.CustomPrompts().current().text()
                                    : text;
                            Platform.runLater(() -> buildInputSectionAndStart(finalPassage));
                        });

            } else {
                // 1a…4f → fixed placeholders for now
                String fixed = com.example.addressbook.lesson.FixedLessons.passageFor(lt);
                buildInputSectionAndStart(fixed);
            }
        }

    modeChoice.getItems().addAll("Strict (no mistakes allowed)", "Lenient (highlight mistakes)");
    modeChoice.getSelectionModel().select(0);
    pauseMenu = new PauseMenu(pauseButton, metrics, hiddenInput);

    StackPane.setAlignment(promptFlow, Pos.TOP_LEFT);
    StackPane.setAlignment(userFlow, Pos.TOP_LEFT);
    promptFlow.setLineSpacing(30);
    userFlow.setLineSpacing(30);
    promptFlow.prefWidthProperty().bind(readingStack.widthProperty());
    userFlow.prefWidthProperty().bind(readingStack.widthProperty());
    userFlow.setTranslateY(26);
    readingStack.setPadding(new Insets(16));

    metrics.onLessonEnd(() -> {
      inputSection.disable();
      keyboardHands.dim();
      Platform.runLater(() -> {
        Alert a = new Alert(Alert.AlertType.INFORMATION, "You won! Lesson complete.");
        a.setHeaderText(null);
        a.setTitle("Lesson");
        a.showAndWait();
      });
    });
  }
}
