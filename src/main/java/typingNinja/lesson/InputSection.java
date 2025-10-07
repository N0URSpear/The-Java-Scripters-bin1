package typingNinja.lesson;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.function.Consumer;

public class InputSection {
    private final TextFlow promptFlow;
    private final TextFlow userFlow;
    private final TextArea hiddenInput;
    private final KeyboardHands keyboard;
    private final Metrics metrics;
    private final String passage;
    private int index = 0;
    private boolean strictMode = true;
    private final Text cursor;
    private final WeakKeyTracker weakKeys;
    private final boolean[] errorCounted;
    private final Consumer<Text> cursorListener;

    public char peekExpected() { return index < passage.length() ? passage.charAt(index) : '\0'; }

    public InputSection(TextFlow promptFlow, TextFlow userFlow, TextArea hiddenInput,
                        KeyboardHands keyboard, Metrics metrics, String passage,
                        WeakKeyTracker weakKeys, Consumer<Text> cursorListener) {
        this.promptFlow = promptFlow;
        this.userFlow = userFlow;
        this.hiddenInput = hiddenInput;
        this.keyboard = keyboard;
        this.metrics = metrics;
        this.passage = passage;
        this.weakKeys = weakKeys;
        this.errorCounted = new boolean[passage.length()];
        this.cursor = new Text("_");
        this.cursor.getStyleClass().addAll("cursor", "mono");
        this.cursorListener = cursorListener;
        buildPrompt();
        userFlow.getChildren().clear();
        updateCursor();
    }

    public void setStrictMode(boolean strict) {
        this.strictMode = strict;
    }

    public void disable() {
        hiddenInput.setDisable(true);
    }

    public void onKeyTyped(KeyEvent e) {
        String s = e.getCharacter();
        if (s == null || s.isEmpty()) { e.consume(); return; }
        char c = s.charAt(0);
        if (c == '\r') c = '\n';
        if (c < 32 && c != '\n') { e.consume(); return; }
        // keyboard highlight handled by highlightExpected
        if (index >= passage.length()) { e.consume(); return; }
        removeCursor();
        if (e.isShiftDown()) {
            c = applyShiftChar(c);
        }
        char expected = passage.charAt(index);
        boolean match = (c == expected);
        // --- WEAK KEYS: record only the first mistake for this expected position ---
        if (!match && index < passage.length() && weakKeys != null) {
            if (!errorCounted[index] && WeakKeyTracker.trackable(expected) && WeakKeyTracker.trackable(c)) {
                Character prev = (index > 0) ? passage.charAt(index - 1) : null;
                weakKeys.record(prev, expected, c);
                errorCounted[index] = true; // only once until we advance past this position
            }
        }

        if (strictMode) {
            if (match) {
                pushUserChar(c, true);
                index++;
                metrics.incTyped(true);
                        keyboard.highlightExpected(peekExpected());
                        keyboard.highlightExpected(peekExpected());
            }
            else {
                pushUserChar(c, false);
                metrics.incTyped(false);
                        keyboard.highlightExpected(peekExpected());
                userFlow.getChildren().remove(index);
                        keyboard.highlightExpected(peekExpected());
                        keyboard.highlightExpected(peekExpected());
            }
        }
        else {
            pushUserChar(c, match);
            index++;
            metrics.incTyped(match);
            keyboard.highlightExpected(peekExpected());
        }
        handleCompletionIfNeeded();
        updateCursor();
        e.consume();
    }

    public void onKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        if (code == KeyCode.BACK_SPACE) {
            if (index > 0) {
                removeCursor();
                index--;
                if (userFlow.getChildren().size() > index) {
                    javafx.scene.Node removed = userFlow.getChildren().remove(index);
                    boolean wasError = removed.getStyleClass().contains("user-wrong");
                    metrics.decTypedIfBackspace(wasError);
                }
                else {
                    metrics.decTypedIfBackspace(false);
                }
                keyboard.highlightExpected(peekExpected());
                keyboard.highlightExpected(peekExpected());
            }
            updateCursor();
            keyboard.dim();
            e.consume();
        }
        else if (code == KeyCode.ENTER) {
            removeCursor();
            if (index < passage.length() && passage.charAt(index) == '\n') {
                keyboard.lightForChar('\n');
                pushUserChar('\n', true);
                index++;
                metrics.incTyped(true);
                        keyboard.highlightExpected(peekExpected());
                        keyboard.highlightExpected(peekExpected());
                handleCompletionIfNeeded();
            }
            else if (strictMode) {
                keyboard.lightForChar('\n');
                pushUserChar('\n', false);
                metrics.incTyped(false);
                keyboard.highlightExpected(peekExpected());
                userFlow.getChildren().remove(index);
                keyboard.highlightExpected(peekExpected());
            }
            else {
                keyboard.lightForChar('\n');
                pushUserChar('\n', false);
                index++;
                metrics.incTyped(false);
                        keyboard.highlightExpected(peekExpected());
                handleCompletionIfNeeded();
            }
            updateCursor();
            e.consume();
        }
    }

    private void buildPrompt() {
        promptFlow.getChildren().clear();
        for (int i = 0; i < passage.length(); i++) {
            Text t = new Text(String.valueOf(passage.charAt(i)));
            t.getStyleClass().addAll("prompt-char", "mono");
            promptFlow.getChildren().add(t);
        }
    }

    private void pushUserChar(char c, boolean correct) {
        Text t = new Text(String.valueOf(c));
        t.getStyleClass().addAll(correct ? "user-correct" : "user-wrong", "mono");
        if (index <= userFlow.getChildren().size()) userFlow.getChildren().add(index, t);
        else userFlow.getChildren().add(t);
    }

    private char applyShiftChar(char c) {
        if (Character.isLetter(c)) return Character.toUpperCase(c);
        switch (c) {
            case '1': return '!';
            case '2': return '@';
            case '3': return '#';
            case '4': return '$';
            case '5': return '%';
            case '6': return '^';
            case '7': return '&';
            case '8': return '*';
            case '9': return '(';
            case '0': return ')';
            case '-': return '_';
            case '=': return '+';
            case '[': return '{';
            case ']': return '}';
            case '\\': return '|';
            case ';': return ':';
            case '\'': return '"';
            case ',': return '<';
            case '.': return '>';
            case '/': return '?';
            case '`': return '~';
            default: return c;
        }
    }

    private void removeCursor() {
        userFlow.getChildren().remove(cursor);
    }

    private void updateCursor() {
        removeCursor();
        int insertionIndex = Math.min(index, userFlow.getChildren().size());
        userFlow.getChildren().add(insertionIndex, cursor);
        notifyCursorListener();
    }

    private void notifyCursorListener() {
        if (cursorListener != null) {
            cursorListener.accept(cursor);
        }
    }

    private void handleCompletionIfNeeded() {
        if (index >= passage.length()) {
            hiddenInput.setDisable(true);
            metrics.endLessonNow();
        }
    }
}
