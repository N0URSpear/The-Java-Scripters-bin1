package com.example.addressbook;

import com.example.addressbook.ActiveLesson.InputSection;
import com.example.addressbook.ActiveLesson.KeyboardHands;
import com.example.addressbook.ActiveLesson.Metrics;
import com.example.addressbook.ActiveLesson.PauseMenu;
import com.example.addressbook.ActiveLesson.ProgressFeature;
import com.example.addressbook.ActiveLesson.CustomPrompts;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class LessonActivePageController {
  @FXML private HBox buttonBar;
  @FXML private Button pauseButton;
  @FXML private ChoiceBox<String> modeChoice;
  @FXML private Label timerLabel;

  @FXML private StackPane readingStack;
  @FXML private TextFlow promptFlow;
  @FXML private TextFlow userFlow;
  @FXML private ProgressBar timeProgress;

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
  private ProgressFeature progressFeature;
  private PauseMenu pauseMenu;
  private KeyboardHands keyboardHands;
  private InputSection inputSection;

  @FXML
  private void initialize() {
    prompts = new CustomPrompts();
    CustomPrompts.Prompt p = prompts.current();
    lessonTitleLabel.setText(p.title());
    lessonDurationLabel.setText(p.durationSeconds() + "s");

    metrics = new Metrics(p.durationSeconds());
    metrics.bindTimerLabel(timerLabel);
    metrics.bindStats(wpmLabel, errorsLabel, accuracyLabel);

    progressFeature = new ProgressFeature(timeProgress);
    progressFeature.bindTo(metrics.timeRemainingProperty(), metrics.lessonSeconds());

    keyboardHands = new KeyboardHands(keyboardGrid, handsRegion, handsLabel);
    keyboardHands.buildQwerty();

    inputSection = new InputSection(promptFlow, userFlow, hiddenInput, keyboardHands, metrics, p.text());
    keyboardHands.highlightExpected(inputSection.peekExpected());
    hiddenInput.addEventFilter(KeyEvent.KEY_TYPED, inputSection::onKeyTyped);
    hiddenInput.addEventFilter(KeyEvent.KEY_PRESSED, inputSection::onKeyPressed);

    readingStack.setOnMouseClicked(e -> hiddenInput.requestFocus());
    promptFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
    userFlow.setOnMouseClicked(e -> hiddenInput.requestFocus());
    keyboardGrid.setOnMouseClicked(e -> hiddenInput.requestFocus());

    modeChoice.getItems().addAll("Strict (no mistakes allowed)", "Lenient (highlight mistakes)");
    modeChoice.getSelectionModel().select(0);
    inputSection.setStrictMode(true);
    modeChoice.getSelectionModel().selectedIndexProperty().addListener((obs, o, n) -> inputSection.setStrictMode(n.intValue() == 0));

    pauseMenu = new PauseMenu(pauseButton, metrics, hiddenInput);

    StackPane.setAlignment(promptFlow, Pos.TOP_LEFT);
    StackPane.setAlignment(userFlow, Pos.TOP_LEFT);
    promptFlow.setLineSpacing(30);
    userFlow.setLineSpacing(30);
    promptFlow.prefWidthProperty().bind(readingStack.widthProperty());
    userFlow.prefWidthProperty().bind(readingStack.widthProperty());
    userFlow.setTranslateY(26);
    readingStack.setPadding(new Insets(16));

    Platform.runLater(() -> {
      hiddenInput.setText("");
      hiddenInput.requestFocus();
      metrics.start();
      keyboardHands.highlightExpected(inputSection.peekExpected());
    });

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
