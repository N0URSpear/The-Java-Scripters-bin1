package typingNinja.controllers.lesson;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.function.Consumer;
import typingNinja.model.lesson.WeakKeyTracker;
import typingNinja.model.lesson.Metrics;
import typingNinja.controllers.lesson.KeyboardHands;

/**
 * Drives the structured typing workflow against a fixed lesson passage.
 */
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
    private final Runnable onComplete;
    private final boolean[] typedCorrect;

    // Expose the next expected character so keyboard hints can stay lockstep with the prompt.
    /**
     * Exposes the next expected character so external UI can highlight it.
     *
     * @return the next expected character or {@code '\0'} if the passage is complete
     */
    public char peekExpected() { return index < passage.length() ? passage.charAt(index) : '\0'; }

    /**
     * Builds a new typed-lesson section, wiring prompt display and input tracking together.
     *
     * @param promptFlow flow containing the reference passage
     * @param userFlow flow reflecting the user's key strokes
     * @param hiddenInput backing text area that receives focus
     * @param keyboard keyboard visualiser used for hints
     * @param metrics metrics aggregator shared by the controller
     * @param passage literal passage the student should type
     * @param weakKeys tracker capturing recurrent mistakes
     * @param cursorListener callback invoked when the caret moves
     * @param onComplete hook executed when the passage is finished
     */
    public InputSection(TextFlow promptFlow, TextFlow userFlow, TextArea hiddenInput,
                        KeyboardHands keyboard, Metrics metrics, String passage,
                        WeakKeyTracker weakKeys, Consumer<Text> cursorListener,
                        Runnable onComplete) {
        // Prebuild the prompt visuals so the hot path key handlers only juggle state deltas.
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
        this.onComplete = (onComplete != null) ? onComplete : () -> {};
        this.typedCorrect = new boolean[passage.length()];
        buildPrompt();
        userFlow.getChildren().clear();
        updateCursor();
    }

    /**
     * Enables or disables strict mode where incorrect characters block forward progress.
     *
     * @param strict {@code true} for strict mode, {@code false} for relaxed mode
     */
    public void setStrictMode(boolean strict) {
        // Toggle whether mismatched characters block progress or get logged as soft errors.
        this.strictMode = strict;
    }

    /**
     * Stops accepting input once the controller retires this section.
     */
    public void disable() {
        // Lock the hidden text area once a lesson winds down.
        hiddenInput.setDisable(true);
    }

    /**
     * Handles printable key events and updates the prompt/user flows accordingly.
     *
     * @param e key-typed event
     */
    public void onKeyTyped(KeyEvent e) {
        // Handle printable characters here because KeyTyped gives us already-localised chars.
        String s = e.getCharacter();
        if (s == null || s.isEmpty()) { e.consume(); return; }
        char c = s.charAt(0);
        if (c == '\r') c = '\n';
        if (c < 32 && c != '\n') { e.consume(); return; }
        if (index >= passage.length()) { e.consume(); return; }
        removeCursor();
        if (e.isShiftDown()) {
            c = applyShiftChar(c);
        }
        char expected = passage.charAt(index);
        boolean match = (c == expected);
        if (!match && index < passage.length() && weakKeys != null) {
            if (!errorCounted[index] && WeakKeyTracker.trackable(expected) && WeakKeyTracker.trackable(c)) {
                Character prev = (index > 0) ? passage.charAt(index - 1) : null;
                weakKeys.record(prev, expected, c);
                errorCounted[index] = true;
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

    /**
     * Handles backspace and enter key presses.
     *
     * @param e key-pressed event
     */
    public void onKeyPressed(KeyEvent e) {
        // Non-printables like backspace and enter live here so we can tweak prompt state manually.
        KeyCode code = e.getCode();
        if (code == KeyCode.BACK_SPACE) {
            if (index > 0) {
                removeCursor();
                index--;
                resetPromptCharColor(index);
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
        // Materialise each character node once so we can just recolor them during typing.
        promptFlow.getChildren().clear();
        for (int i = 0; i < passage.length(); i++) {
            Text t = new Text(String.valueOf(passage.charAt(i)));
            t.getStyleClass().addAll("prompt-char", "mono");
            typedCorrect[i] = false;
            t.setFill(Color.web("#000000"));
            promptFlow.getChildren().add(t);
        }
    }

    private void pushUserChar(char c, boolean correct) {
        // Insert the latest keystroke into the rendered flow and sync prompt colouring.
        Text t = new Text(String.valueOf(c));
        t.getStyleClass().addAll(correct ? "user-correct" : "user-wrong", "mono");
        if (index <= userFlow.getChildren().size()) userFlow.getChildren().add(index, t);
        else userFlow.getChildren().add(t);
        if (index < typedCorrect.length) {
            typedCorrect[index] = correct;
            applyPromptCharColor(index);
        }
    }

    private void resetPromptCharColor(int idx) {
        // When we step backwards we treat the character as unseen so accuracy recalcs are clean.
        if (idx >= 0 && idx < typedCorrect.length) {
            typedCorrect[idx] = false;
            applyPromptCharColor(idx);
        }
    }

    private void applyPromptCharColor(int idx) {
        // Prompt glyphs double as a correctness heatmap, so we flip the fill on demand.
        if (idx >= 0 && idx < promptFlow.getChildren().size()) {
            Text promptChar = (Text) promptFlow.getChildren().get(idx);
            promptChar.setFill(typedCorrect[idx] ? Color.web("#FFFFFF") : Color.web("#000000"));
        }
    }

    private char applyShiftChar(char c) {
        // Manual map keeps the behaviour predictable even when the keyboard layout shifts.
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
        // Pull the cursor placeholder out so insertions do not duplicate it.
        userFlow.getChildren().remove(cursor);
    }

    private void updateCursor() {
        // Reposition the cursor and let the containing view know it probably needs to scroll.
        removeCursor();
        int insertionIndex = Math.min(index, userFlow.getChildren().size());
        userFlow.getChildren().add(insertionIndex, cursor);
        notifyCursorListener();
    }

    private void notifyCursorListener() {
        // Controllers watch the cursor to keep the viewport centred on the active line.
        if (cursorListener != null) {
            cursorListener.accept(cursor);
        }
    }

    private void handleCompletionIfNeeded() {
        // Once the cursor clears the passage we let the owning controller decide the next state.
        if (index >= passage.length()) {
            try { onComplete.run(); } catch (Exception ignored) {}
        }
    }

    /**
     * Indicates whether the user has finished the passage.
     *
     * @return {@code true} when the passage has been fully traversed
     */
    public boolean isComplete() { return index >= passage.length(); }

    /**
     * @return the total passage length tracked by this section
     */
    public int getPassageLength() { return passage.length(); }

    /**
     * @return count of passage positions typed correctly so far
     */
    public int getCorrectPositions() {
        // Collapse the correctness array down into a simple count for post-lesson stats.
        int c = 0;
        for (int i = 0; i < typedCorrect.length; i++) if (typedCorrect[i]) c++;
        return c;
    }
}
