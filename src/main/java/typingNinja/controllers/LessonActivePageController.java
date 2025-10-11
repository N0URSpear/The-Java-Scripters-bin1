package typingNinja.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;

import typingNinja.lesson.*;
import typingNinja.auth.Session;
import typingNinja.ai.AITextService;
import typingNinja.lesson.WeakKeyTracker;


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
    private final WeakKeyTracker weakKeyTracker = new WeakKeyTracker();
    private Integer currentLessonId;
    private int currentUserId;
    private final LessonDAO lessonDAO = new LessonDAO(); // reuse the same DAO

    private void buildInputSectionAndStart(String passage) {
        inputSection = new InputSection(
                promptFlow, userFlow, hiddenInput, keyboardHands, metrics, passage, weakKeyTracker
        );
        keyboardHands.highlightExpected(inputSection.peekExpected());

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

            if (currentLessonId != null) {
                try { lessonDAO.markStarted(currentLessonId, currentUserId); } catch (Exception ex) { ex.printStackTrace(); }
            }
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

        currentUserId = Session.getCurrentUserId();
        Lesson latest = null;

        try {
            latest = lessonDAO.fetchLatestForUser(currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (latest == null) {
            buildInputSectionAndStart(p.text());
        }
        else {
            currentLessonId = latest.getLessonId();
            lessonTitleLabel.setText(latest.getLessonType());
            int durationSeconds = Math.max(10, latest.getDurationMinutes() * 60);

            metrics = new Metrics(durationSeconds);
            metrics.bindTimerLabel(timerLabel);
            metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);
            progressFeature = new typingNinja.lesson.ProgressBar(timeProgress);
            progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());
            lessonDurationLabel.setText(durationSeconds + "s");

            String lt = latest.getLessonType();
            boolean isCustom = "CustomTopic".equalsIgnoreCase(lt);
            boolean isFree = (lt != null && lt.toLowerCase().startsWith("free")); // matches your FreeTypeSelectController ("FreeWeakKeys", "FreeAnything")

            if (isFree) {
                buildInputSectionAndStart("");
            }
            else if (isCustom) {
                typingNinja.ai.OllamaTextService ollama = new typingNinja.ai.OllamaTextService();
                typingNinja.ai.LocalSimpleTextService local = new typingNinja.ai.LocalSimpleTextService();
                int targetWords = Math.max(60, latest.getDurationMinutes() * 50); // ~50 wpm target

                String promptToSend = latest.getPrompt();
                if ("PracticeWeakKeyCombos".equalsIgnoreCase(promptToSend)) {
                    java.util.List<String> pairs = java.util.Collections.emptyList();
                    try {
                        // only from PAST completed CustomTopic lessons
                        pairs = lessonDAO.topWeakPairsForUserFromCompletedCustomLessons(currentUserId, 7);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    String joined = String.join(" ", pairs);
                    StringBuilder sb = new StringBuilder();
                    sb.append("PracticeWeakKeyCombos MODE.\n");
                    if (!pairs.isEmpty()) {
                        sb.append("You must incorporate these EXACT bigrams (case-sensitive, no spaces inside each bigram): ")
                                .append(joined).append(".\n");
                        sb.append("Write normal English sentences with clear grammar and readable vocabulary. ");
                        sb.append("Embed each bigram inside ordinary words or right next to punctuation where natural ");
                        sb.append("(e.g., Ab → Abbot/Abberfeld; c. → logic.). ");
                        sb.append("Avoid alphabet drills or sequences like 'Ab Cd Ef'.\n");
                        sb.append("Target density: include EACH listed bigram approximately ONCE every 10 words across the passage. ");
                        sb.append("Keep characters in each bigram contiguous and preserve their original case.\n");
                    } else {
                        sb.append("No prior bigrams found; write clear, grammatical English sentences with varied vocabulary.\n");
                    }
                    sb.append("Return ONLY the passage text (no headings, quotes, or labels).");
                    promptToSend = sb.toString();
                    System.out.println("[AI] PracticeWeakKeyCombos pairs for user " + currentUserId + ": " + joined);
                }
                final String promptToUse = promptToSend;  // final for lambdas
                Lesson finalLatest = latest;

                java.util.concurrent.CompletableFuture
                        .supplyAsync(() -> {
                            // 1) Ollama (free, local API)
                            try {
                                System.out.println("[AI] Trying Ollama (free/local). prompt='" + promptToUse + "' "
                                        + "flags: upper=" + finalLatest.isUpperCase()
                                        + " numbers=" + finalLatest.isNumbers()
                                        + " punct=" + finalLatest.isPunctuation()
                                        + " special=" + finalLatest.isSpecialChars());
                                String t = ollama.generatePassage(
                                        promptToUse,
                                        targetWords,
                                        finalLatest.isUpperCase(),
                                        finalLatest.isNumbers(),
                                        finalLatest.isPunctuation(),
                                        finalLatest.isSpecialChars()
                                );
                                System.out.println("[AI] OllamaTextService SUCCESS");
                                return t;
                            } catch (Exception ex) {
                                System.out.println("[AI] OllamaTextService FAILED → " + ex.getMessage());
                                ex.printStackTrace();
                                return null;
                            }
                        })
                        .thenApply(text -> {
                            if (text != null && !text.isBlank()) return text;
                            // 2) Local fallback (always free)
                            System.out.println("[AI] Falling back to LocalSimpleTextService");
                            try {
                                return local.generatePassage(
                                        promptToUse,
                                        targetWords,
                                        finalLatest.isUpperCase(),
                                        finalLatest.isNumbers(),
                                        finalLatest.isPunctuation(),
                                        finalLatest.isSpecialChars()
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                                return new typingNinja.lesson.CustomPrompts().current().text();
                            }
                        })
                        .thenAccept(text -> {
                            String finalPassage = (text == null || text.isBlank())
                                    ? new typingNinja.lesson.CustomPrompts().current().text()
                                    : text;
                            Platform.runLater(() -> buildInputSectionAndStart(finalPassage));
                        });

            }
            else {
                // 1a…4f fixed placeholders for now
                String fixed = typingNinja.lesson.FixedLessons.passageFor(lt);
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

            double wpmVal;
            double accuracyVal;
            int errorCount;

            wpmVal      = metrics.getWpm();
            accuracyVal = metrics.getAccuracyPercent(); // 0..100
            errorCount  = metrics.getErrors();
            double star = StarRating.compute(wpmVal, accuracyVal, errorCount); // 0.0 .. 5.0
            String weakPairs = weakKeyTracker.topPrevExpectedPairsString(5); // e.g. "aC d4 e2 a1 B#" or with "--" pads

            // --- DEBUG: print full weak-keys structure to terminal ---
            System.out.println("\n----- WEAK KEYS DEBUG DUMP -----");
            System.out.println(weakKeyTracker.debugDump());
            System.out.println("Top-5 pairs stored to DB: " + weakPairs);
            System.out.println("--------------------------------\n");

            if (currentLessonId != null) {
                try { lessonDAO.markCompleted(
                        currentLessonId,
                        currentUserId,
                        star,
                        wpmVal,
                        accuracyVal,
                        errorCount,
                        weakPairs
                );
                }
                catch (Exception ex) { ex.printStackTrace(); }
            }

            Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "You won! Lesson complete.");
                a.setHeaderText(null);
                a.setTitle("Lesson");
                a.showAndWait();
            });
        });
    }
}
