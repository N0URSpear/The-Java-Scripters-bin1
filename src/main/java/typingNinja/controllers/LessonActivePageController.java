package typingNinja.controllers;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.TextAlignment;

import typingNinja.controllers.lesson.KeyboardHands;
import typingNinja.controllers.lesson.InputSection;
import typingNinja.controllers.lesson.FreeTypingInput;
import typingNinja.controllers.lesson.ProgressBar;
import typingNinja.controllers.lesson.PauseMenu;
import typingNinja.model.lesson.Lesson;
import typingNinja.model.lesson.CustomPrompts;
import typingNinja.model.lesson.LessonDAO;
import typingNinja.model.lesson.WeakKeyTracker;
import typingNinja.model.lesson.StarRating;
import typingNinja.model.auth.Session;
import typingNinja.model.SettingsDAO;
import typingNinja.model.SettingsDAO.SettingsRecord;
import typingNinja.view.CongratulationsScene;
import typingNinja.view.MainMenu;

import javafx.stage.Stage;
import typingNinja.util.SceneNavigator;

public class LessonActivePageController {
    @FXML private HBox buttonBar;
    @FXML private Button pauseButton;
    @FXML private Label timerLabel;

    @FXML private ScrollPane readingScroll;
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
    @FXML private Label errorsTitleLabel;
    @FXML private Label accuracyTitleLabel;
    @FXML private Label promptTitleLabel;
    @FXML private Label promptDisplayLabel;

    private typingNinja.model.lesson.Metrics metrics;
    private CustomPrompts prompts;
    private ProgressBar progressFeature;
    private PauseMenu pauseMenu;
    private KeyboardHands keyboardHands;
    private InputSection inputSection;
    private FreeTypingInput freeTypingInput;
    private final WeakKeyTracker weakKeyTracker = new WeakKeyTracker();
    private Integer currentLessonId;
    private int currentUserId;
    private final LessonDAO lessonDAO = new LessonDAO(); // reuse the same DAO
    private final SettingsDAO settingsDAO = new SettingsDAO();
    private boolean strictModePreferred = true;
    private boolean freeMode = false;
    private EventHandler<KeyEvent> keyTypedHandler;
    private EventHandler<KeyEvent> keyPressedHandler;
    private static final double DEFAULT_CHARS_PER_WORD = 5.0;
    private static final double FREE_MODE_CHARS_PER_WORD = 6.5;

    private boolean finishedByTyping = false;
    private boolean lessonCompleteSoundEnabled = false;

    // Sound toggles and clips (with throttling + pooling)
    private boolean keyboardSoundsEnabled = false;
    private boolean typingErrorSoundsEnabled = false;
    private javafx.scene.media.AudioClip typingClip; // legacy single clip (fallback)
    private javafx.scene.media.AudioClip errorClip;  // legacy single clip (fallback)
    private javafx.scene.media.AudioClip[] typingPool;
    private javafx.scene.media.AudioClip[] errorPool;
    private int typingPoolIdx = 0, errorPoolIdx = 0;
    private long lastTypingMs = 0, lastErrorMs = 0;
    private static final long TYPING_GAP_MS = 25;  // ~40 clicks/sec max
    private static final long ERROR_GAP_MS  = 200; // avoid buzzer spam
    private int lastErrorCount = 0;

    

    private void onReachedEndOfText() {
        // User reached end of passage before timer ended
        finishedByTyping = true;
        if (inputSection == null) return;
        int passageLen = inputSection.getPassageLength();
        int correctPos = inputSection.getCorrectPositions();
        double percentCorrectOfPassage = passageLen > 0 ? (correctPos * 100.0 / passageLen) : 0.0;
        int err = metrics.getErrors();
        boolean perfect = (err == 0 && correctPos == passageLen);
        double accuracyVal = metrics.getAccuracyPercent();
        boolean passWithErrors = (percentCorrectOfPassage > 40.0 && accuracyVal >= 40.0);

        if (perfect || passWithErrors) {
            // End timer and trigger onLessonEnd handler
            Platform.runLater(metrics::endLessonNow);
        } else {
            // Too many errors; keep lesson running and show a fading prompt
            Platform.runLater(() -> showFadingErrorBanner("TOO MANY ERRORS — CORRECT AND CONTINUE"));
        }
    }

    private void showFadingErrorBanner(String message) {
        Label banner = new Label(message);
        banner.setStyle("-fx-background-color: rgba(205,25,25,0.95); -fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 20px; -fx-padding: 12 18; -fx-background-radius: 12; -fx-border-radius: 12;");
        banner.setMouseTransparent(true);
        StackPane.setAlignment(banner, Pos.TOP_CENTER);
        readingStack.getChildren().add(banner);
        javafx.animation.FadeTransition ft = new javafx.animation.FadeTransition(javafx.util.Duration.millis(2200), banner);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> readingStack.getChildren().remove(banner));
        ft.play();
    }

    private void playCompletionSoundIfEnabled() {
        if (!lessonCompleteSoundEnabled) return;
        try { typingNinja.util.SoundManager.playLessonComplete(); } catch (Exception ignored) {}
    }

    // --- Sound helpers ---
    private void initSoundClips() {
        try {
            // Prefer WAV for lower latency; fallback to legacy MP3 if missing
            java.net.URL typingUrl = getClass().getResource("/typingNinja/Sounds/keyboard_click.wav");
            if (typingUrl == null) typingUrl = getClass().getResource("/typingNinja/Sounds/typing.mp3");
            if (typingUrl != null) {
                typingClip = new javafx.scene.media.AudioClip(typingUrl.toExternalForm());
                // Build a small pool for polyphony
                int n = 4;
                typingPool = new javafx.scene.media.AudioClip[n];
                for (int i = 0; i < n; i++) typingPool[i] = new javafx.scene.media.AudioClip(typingUrl.toExternalForm());
            } else {
                typingClip = null;
                typingPool = null;
            }
        } catch (Exception ex) {
            typingClip = null; typingPool = null;
        }
        try {
            java.net.URL errorUrl = getClass().getResource("/typingNinja/Sounds/wrong_buzzer.mp3");
            if (errorUrl != null) {
                errorClip = new javafx.scene.media.AudioClip(errorUrl.toExternalForm());
                int m = 2;
                errorPool = new javafx.scene.media.AudioClip[m];
                for (int i = 0; i < m; i++) errorPool[i] = new javafx.scene.media.AudioClip(errorUrl.toExternalForm());
            } else {
                errorClip = null;
                errorPool = null;
            }
        } catch (Exception ex) {
            errorClip = null; errorPool = null;
        }
    }

    private void playTypingKeySound() {
        if (!keyboardSoundsEnabled) return;
        long now = System.currentTimeMillis();
        if (now - lastTypingMs < TYPING_GAP_MS) return; // throttle
        lastTypingMs = now;
        if (typingPool != null && typingPool.length > 0) {
            javafx.scene.media.AudioClip c = typingPool[typingPoolIdx];
            typingPoolIdx = (typingPoolIdx + 1) % typingPool.length;
            c.play();
        } else if (typingClip != null) {
            typingClip.play();
        }
    }

    private void attachMetricsErrorSoundListener() {
        if (metrics == null) return;
        lastErrorCount = metrics.getErrors();
        metrics.errorsProperty().addListener((o, oldVal, newVal) -> {
            int oldCount = (oldVal != null) ? oldVal.intValue() : lastErrorCount;
            int newCount = (newVal != null) ? newVal.intValue() : oldCount;
            if (typingErrorSoundsEnabled && newCount > oldCount) {
                playErrorBuzzer();
            }
            lastErrorCount = newCount;
        });
    }

    private void playErrorBuzzer() {
        long now = System.currentTimeMillis();
        if (now - lastErrorMs < ERROR_GAP_MS) return; // throttle
        lastErrorMs = now;
        if (errorPool != null && errorPool.length > 0) {
            javafx.scene.media.AudioClip c = errorPool[errorPoolIdx];
            errorPoolIdx = (errorPoolIdx + 1) % errorPool.length;
            c.play();
        } else if (errorClip != null) {
            errorClip.play();
        }
    }


    private void rebuildPauseMenu() {
        Platform.runLater(() ->
                pauseMenu = new PauseMenu(pauseButton, metrics, hiddenInput,
                        this::openSettingsView, this::returnToHome));
    }

    private void openSettingsView() {
        // Cancel active lesson by removing its DB row if not completed
        cancelActiveLesson();
        Stage stage = pauseButton != null && pauseButton.getScene() != null
                ? (Stage) pauseButton.getScene().getWindow() : null;
        if (stage == null) return;
        try {
            SceneNavigator.load(stage, "/typingNinja/Settings.fxml", "Settings - Typing Ninja");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void returnToHome() {
        // Cancel active lesson by removing its DB row if not completed
        cancelActiveLesson();
        Stage stage = pauseButton != null && pauseButton.getScene() != null
                ? (Stage) pauseButton.getScene().getWindow() : null;
        if (stage == null) return;
        try {
            MainMenu mainMenu = new MainMenu();
            mainMenu.show(stage);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openResultsView() {
        Stage stage = pauseButton != null && pauseButton.getScene() != null
                ? (Stage) pauseButton.getScene().getWindow()
                : (hiddenInput != null && hiddenInput.getScene() != null)
                    ? (Stage) hiddenInput.getScene().getWindow()
                    : null;
        if (stage == null) return;
        try {
            CongratulationsScene.show(stage);
        } catch (Exception ex) {
            ex.printStackTrace();
            returnToHome();
        }
    }

    private void cancelActiveLesson() {
        if (currentLessonId != null) {
            try {
                lessonDAO.deleteIfNotCompleted(currentLessonId, currentUserId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void buildInputSectionAndStart(String passage) {
        if (keyTypedHandler != null) {
            hiddenInput.removeEventFilter(KeyEvent.KEY_TYPED, keyTypedHandler);
            keyTypedHandler = null;
        }
        if (keyPressedHandler != null) {
            hiddenInput.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
            keyPressedHandler = null;
        }

        hiddenInput.clear();
        hiddenInput.setDisable(false);

        finishedByTyping = false;
        if (freeMode) {
            Text message = new Text("Free typing mode — type anything you like.");
            message.getStyleClass().addAll("prompt-char", "mono");
            promptFlow.getChildren().setAll(message);
            freeTypingInput = new FreeTypingInput(userFlow, hiddenInput, keyboardHands, metrics, this::ensureCursorVisible);
            keyTypedHandler = freeTypingInput::onKeyTyped;
            keyPressedHandler = freeTypingInput::onKeyPressed;
            promptFlow.setTextAlignment(TextAlignment.LEFT);
            userFlow.setTextAlignment(TextAlignment.LEFT);
        } else {
            inputSection = new InputSection(
                    promptFlow, userFlow, hiddenInput, keyboardHands, metrics, passage, weakKeyTracker,
                    this::ensureCursorVisible,
                    this::onReachedEndOfText
            );
            inputSection.setStrictMode(strictModePreferred);
            keyboardHands.highlightExpected(inputSection.peekExpected());
            keyTypedHandler = inputSection::onKeyTyped;
            keyPressedHandler = inputSection::onKeyPressed;
            promptFlow.setTextAlignment(TextAlignment.LEFT);
            userFlow.setTextAlignment(TextAlignment.LEFT);
        }

        hiddenInput.addEventFilter(KeyEvent.KEY_TYPED, keyTypedHandler);
        hiddenInput.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);

        readingScroll.setOnMouseClicked(e -> hiddenInput.requestFocus());
        readingStack.setOnMouseClicked(e -> hiddenInput.requestFocus());
        promptFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
        userFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
        keyboardGrid.setOnMouseClicked(e -> hiddenInput.requestFocus());

        Platform.runLater(() -> {
            hiddenInput.setText("");
            hiddenInput.requestFocus();
            metrics.start();

            if (currentLessonId != null) {
                try { lessonDAO.markStarted(currentLessonId, currentUserId); } catch (Exception ex) { ex.printStackTrace(); }
            }
            boolean timerExpired = metrics.timeRemainingProperty().get() <= 0;

            if (!freeMode && inputSection != null && !timerExpired) {
                keyboardHands.highlightExpected(inputSection.peekExpected());
            } else {
                keyboardHands.dim();
            }
        });
    }

    private void ensureCursorVisible(Text cursorNode) {
        if (cursorNode == null) return;
        Platform.runLater(() -> {
            Bounds viewportBounds = readingScroll.getViewportBounds();
            Bounds contentBounds = readingStack.getBoundsInLocal();
            Bounds cursorSceneBounds = cursorNode.localToScene(cursorNode.getBoundsInLocal());
            Bounds contentSceneBounds = readingStack.localToScene(contentBounds);

            double cursorY = cursorSceneBounds.getMinY() - contentSceneBounds.getMinY();
            double cursorHeight = cursorSceneBounds.getHeight();
            double viewportHeight = viewportBounds.getHeight();
            double contentHeight = contentBounds.getHeight();

            if (contentHeight <= viewportHeight) {
                readingScroll.setVvalue(0);
                return;
            }

            double minVisible = readingScroll.getVvalue() * (contentHeight - viewportHeight);
            double maxVisible = minVisible + viewportHeight;

            double desiredTop = Math.max(0, cursorY - viewportHeight * 0.25);
            double desiredBottom = cursorY + cursorHeight + viewportHeight * 0.25;

            double newMin = minVisible;

            if (cursorY < minVisible) {
                newMin = desiredTop;
            } else if (cursorY + cursorHeight > maxVisible) {
                newMin = desiredBottom - viewportHeight;
            }

            double vValue = newMin / (contentHeight - viewportHeight);
            readingScroll.setVvalue(Math.max(0, Math.min(1, vValue)));
        });
    }

    private void showLoadingPlaceholder() {
        Text promptPlaceholder = new Text("LOADING ...");
        promptPlaceholder.getStyleClass().addAll("loading-placeholder", "mono");
        promptFlow.setTextAlignment(TextAlignment.CENTER);
        promptFlow.getChildren().setAll(promptPlaceholder);

        userFlow.getChildren().clear();
        userFlow.setTextAlignment(TextAlignment.LEFT);
    }

    private void configureStatsForFreeMode() {
        if (errorsTitleLabel != null) {
            errorsTitleLabel.setVisible(false);
            errorsTitleLabel.setManaged(false);
        }
        if (errorsLabel != null) {
            errorsLabel.setVisible(false);
            errorsLabel.setManaged(false);
        }
        if (accuracyTitleLabel != null) {
            accuracyTitleLabel.setVisible(false);
            accuracyTitleLabel.setManaged(false);
        }
        if (accuracyLabel != null) {
            accuracyLabel.setVisible(false);
            accuracyLabel.setManaged(false);
        }
        if (promptTitleLabel != null) {
            promptTitleLabel.setVisible(false);
            promptTitleLabel.setManaged(false);
        }
        if (promptDisplayLabel != null) {
            promptDisplayLabel.setVisible(false);
            promptDisplayLabel.setManaged(false);
        }
    }

    private void restoreStatsForStandardMode() {
        if (errorsTitleLabel != null) {
            errorsTitleLabel.setVisible(true);
            errorsTitleLabel.setManaged(true);
        }
        if (errorsLabel != null) {
            errorsLabel.setVisible(true);
            errorsLabel.setManaged(true);
        }
        if (accuracyTitleLabel != null) {
            accuracyTitleLabel.setVisible(true);
            accuracyTitleLabel.setManaged(true);
        }
        if (accuracyLabel != null) {
            accuracyLabel.setVisible(true);
            accuracyLabel.setManaged(true);
        }
    }

    private void showCustomPrompt(String prompt) {
        if (promptTitleLabel == null || promptDisplayLabel == null) return;
        promptTitleLabel.setVisible(true);
        promptTitleLabel.setManaged(true);
        if (prompt == null || prompt.isBlank()) {
            promptDisplayLabel.setText("--");
        } else {
            promptDisplayLabel.setText(prompt.toUpperCase());
        }
        promptDisplayLabel.setVisible(true);
        promptDisplayLabel.setManaged(true);
    }

    private void hidePrompt() {
        if (promptTitleLabel != null) {
            promptTitleLabel.setVisible(true);
            promptTitleLabel.setManaged(true);
        }
        if (promptDisplayLabel != null) {
            promptDisplayLabel.setVisible(true);
            promptDisplayLabel.setManaged(true);
            promptDisplayLabel.setText("--");
        }
    }

    @FXML
    private void initialize() {
        promptFlow.setMinWidth(0);
        promptFlow.setMaxWidth(Double.MAX_VALUE);
        userFlow.setMinWidth(0);
        userFlow.setMaxWidth(Double.MAX_VALUE);
        readingStack.setMinSize(0, 0);
        readingStack.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        prompts = new CustomPrompts();
        CustomPrompts.Prompt p = prompts.current();
        lessonTitleLabel.setText(p.title());
        lessonDurationLabel.setText(p.durationSeconds() + "s");

        restoreStatsForStandardMode();
        hidePrompt();

        metrics = new typingNinja.model.lesson.Metrics(p.durationSeconds());
        metrics.setCharsPerWord(DEFAULT_CHARS_PER_WORD);
        metrics.bindTimerLabel(timerLabel);
        metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);
        attachMetricsErrorSoundListener();
        rebuildPauseMenu();

        progressFeature = new ProgressBar(timeProgress);
        progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());

        keyboardHands = new KeyboardHands(keyboardGrid, handsRegion, handsLabel);
        keyboardHands.buildQwerty();

        showLoadingPlaceholder();
        hidePrompt();

        currentUserId = Session.getCurrentUserId();
        SettingsRecord settings = settingsDAO.fetch(currentUserId);
        strictModePreferred = settings.typingErrors;
        lessonCompleteSoundEnabled = settings.lessonCompleteSound;
        keyboardSoundsEnabled = settings.keyboardSounds;
        typingErrorSoundsEnabled = settings.typingErrorSounds;
        initSoundClips();
        if (hiddenInput != null) {
            // Use KEY_TYPED so we only play on actual character input, not modifiers
            hiddenInput.addEventFilter(KeyEvent.KEY_TYPED, e -> playTypingKeySound());
        }

        readingScroll.setFitToWidth(true);
        readingScroll.setPannable(false);
        readingScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        readingScroll.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double width = Math.max(newBounds.getWidth() - 24, 0);
            promptFlow.setPrefWidth(width);
            userFlow.setPrefWidth(width);
            readingStack.setMinWidth(width);
            readingStack.setPrefWidth(width);
            readingStack.setMinHeight(newBounds.getHeight());
        });
        Platform.runLater(() -> {
            Bounds viewportBounds = readingScroll.getViewportBounds();
            double width = Math.max(viewportBounds.getWidth() - 24, 0);
            promptFlow.setPrefWidth(width);
            userFlow.setPrefWidth(width);
            readingStack.setMinWidth(width);
            readingStack.setPrefWidth(width);
            readingStack.setMinHeight(viewportBounds.getHeight());
        });

        Lesson latest = null;

        try {
            latest = lessonDAO.fetchLatestForUser(currentUserId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (latest == null) {
            freeMode = false;
            restoreStatsForStandardMode();
            hidePrompt();
            buildInputSectionAndStart(p.text());
        }
        else {
            currentLessonId = latest.getLessonId();
            lessonTitleLabel.setText(latest.getLessonType());
            int durationSeconds = Math.max(10, latest.getDurationMinutes() * 60);

            metrics = new typingNinja.model.lesson.Metrics(durationSeconds);
            metrics.setCharsPerWord(DEFAULT_CHARS_PER_WORD);
            metrics.bindTimerLabel(timerLabel);
            metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);
            attachMetricsErrorSoundListener();
            progressFeature = new typingNinja.controllers.lesson.ProgressBar(timeProgress);
            progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());
            lessonDurationLabel.setText(durationSeconds + "s");
            rebuildPauseMenu();

            String lt = latest.getLessonType();
            boolean isCustom = "CustomTopic".equalsIgnoreCase(lt);
            boolean isFree = (lt != null && lt.toLowerCase().startsWith("free")); // matches your FreeTypeSelectController ("FreeWeakKeys", "FreeAnything")

            if (isFree) {
                freeMode = true;
                metrics.setCharsPerWord(FREE_MODE_CHARS_PER_WORD);
                configureStatsForFreeMode();
                lessonTitleLabel.setText("Free Typing");
                buildInputSectionAndStart("");
            }
            else if (isCustom) {
                freeMode = false;
                metrics.setCharsPerWord(DEFAULT_CHARS_PER_WORD);
                restoreStatsForStandardMode();
                lessonTitleLabel.setText("Custom Topic");
                showCustomPrompt(latest.getPrompt());
                typingNinja.model.ai.OllamaTextService ollama = new typingNinja.model.ai.OllamaTextService();
                typingNinja.model.ai.LocalSimpleTextService local = new typingNinja.model.ai.LocalSimpleTextService();
                int wpmTarget = 50;
                try {
                    String envWpm = System.getenv("AI_TARGET_WPM");
                    if (envWpm != null && !envWpm.isBlank()) {
                        int v = Integer.parseInt(envWpm.trim());
                        if (v >= 20 && v <= 150) wpmTarget = v; // clamp sane range
                    }
                } catch (Exception ignored) {}
                int targetWords = Math.max(60, latest.getDurationMinutes() * wpmTarget); // configurable WPM target

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
                                return new typingNinja.model.lesson.CustomPrompts().current().text();
                            }
                        })
                        .thenAccept(text -> {
                            String finalPassage = (text == null || text.isBlank())
                                    ? new typingNinja.model.lesson.CustomPrompts().current().text()
                                    : text;
                            Platform.runLater(() -> buildInputSectionAndStart(finalPassage));
                });

            }
            else {
                freeMode = false;
                metrics.setCharsPerWord(DEFAULT_CHARS_PER_WORD);
                restoreStatsForStandardMode();
                hidePrompt();
                // 1a…4f fixed placeholders for now
                String fixed = typingNinja.model.lesson.FixedLessons.passageFor(lt);
                buildInputSectionAndStart(fixed);
            }
        }

        rebuildPauseMenu();
        if (pauseButton != null) {
            pauseButton.getStyleClass().add("pause-button");
        }

        StackPane.setAlignment(promptFlow, Pos.TOP_LEFT);
        StackPane.setAlignment(userFlow, Pos.TOP_LEFT);
        promptFlow.setLineSpacing(30);
        userFlow.setLineSpacing(30);
        userFlow.setTranslateY(30);
        readingStack.setPadding(new Insets(16, 16, 8, 16));

        metrics.onLessonEnd(() -> {
            if (freeMode) {
                hiddenInput.setDisable(true);
            } else if (inputSection != null) {
                inputSection.disable();
            }
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

            boolean completed = false;
            boolean showResults = false;
            boolean timerExpired = (!freeMode) && metrics.timeRemainingProperty().get() <= 0;

            if (!freeMode && inputSection != null) {
                int passageLen = inputSection.getPassageLength();
                int correctPos = inputSection.getCorrectPositions();
                double percentCorrectOfPassage = passageLen > 0 ? (correctPos * 100.0 / passageLen) : 0.0;

                if (timerExpired) {
                    completed = true;
                    showResults = true;
                } else if (finishedByTyping) {
                    boolean perfect = (errorCount == 0 && correctPos == passageLen);
                    boolean passWithErrors = (percentCorrectOfPassage > 40.0 && accuracyVal >= 40.0);

                    if (perfect) {
                        completed = true;
                        showResults = true;
                    } else if (passWithErrors) {
                        completed = true;
                        showResults = true;
                    } else {
                        completed = false;
                    }
                }
            } else {
                // Timer expired or free mode ended
                completed = true;
                showResults = true;
            }

            

            if (completed && currentLessonId != null) {
                try {
                    lessonDAO.markCompleted(
                            currentLessonId,
                            currentUserId,
                            star,
                            wpmVal,
                            accuracyVal,
                            errorCount,
                            weakPairs
                    );
                } catch (Exception ex) { ex.printStackTrace(); }
            }

            var totalsChar = weakKeyTracker.totalsAllKeys(); // Map<Character,Integer>
// 转成键名为大写 String，便于与 UI 键帽匹配
            java.util.Map<String,Integer> totalsStr = new java.util.LinkedHashMap<>(totalsChar.size());
            for (var e : totalsChar.entrySet()) {
                totalsStr.put(String.valueOf(Character.toUpperCase(e.getKey())), e.getValue());
            }
// 存入 Session
            typingNinja.model.auth.Session.setLatestTotals(totalsStr);

            boolean finalCompleted = completed;
            boolean finalTimedOut = timerExpired;
            boolean finalShowResults = showResults;
            Platform.runLater(() -> {
                if (finalShowResults) {
                    // Play the congratulations sound for all lesson completions
                    // (timer expiry, perfect, pass-with-errors, and free-mode end)
                    playCompletionSoundIfEnabled();
                    openResultsView();
                }
            });
        });
    }
}
