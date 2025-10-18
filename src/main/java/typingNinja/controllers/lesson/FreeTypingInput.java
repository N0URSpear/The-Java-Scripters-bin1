package typingNinja.controllers.lesson;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import typingNinja.model.lesson.Metrics;

import java.util.function.Consumer;

/**
 * Manages the lightweight input experience used by free typing lessons.
 */
public class FreeTypingInput {
    private final TextFlow userFlow;
    private final TextArea hiddenInput;
    private final KeyboardHands keyboard;
    private final Metrics metrics;
    private final Text cursor;
    private final Consumer<Text> cursorListener;
    private int index = 0;

    /**
     * Builds a new free typing handler around the shared UI nodes.
     *
     * @param userFlow visual container that mirrors the user's input
     * @param hiddenInput backing text area that actually receives key events
     * @param keyboard keyboard overlay used for highlighting
     * @param metrics metrics recorder shared with the controller
     * @param cursorListener callback invoked whenever the caret shifts
     */
    public FreeTypingInput(TextFlow userFlow,
                           TextArea hiddenInput,
                           KeyboardHands keyboard,
                           Metrics metrics,
                           Consumer<Text> cursorListener) {
        // Wire up the shared UI nodes and keep a cursor sentinel we can reposition by hand.
        this.userFlow = userFlow;
        this.hiddenInput = hiddenInput;
        this.keyboard = keyboard;
        this.metrics = metrics;
        this.cursorListener = cursorListener;
        this.cursor = new Text("_");
        this.cursor.getStyleClass().addAll("cursor", "mono");
        userFlow.getChildren().clear();
        updateCursor();
    }

    /**
     * Responds to printable characters and commits them to the free typing flow.
     *
     * @param e key-typed event straight from the hidden text area
     */
    public void onKeyTyped(KeyEvent e) {
        // Let every printable character update the display immediately so free typing feels live.
        String s = e.getCharacter();
        if (s == null || s.isEmpty()) { e.consume(); return; }
        char c = s.charAt(0);
        if (c == '\r') c = '\n';
        if (c < 32 && c != '\n') { e.consume(); return; }

        removeCursor();
        char toHighlight = (c == '\n') ? '\n' : c;
        keyboard.lightForChar(toHighlight);
        pushChar(c);
        metrics.incTyped(true);
        updateCursor();
        e.consume();
    }

    /**
     * Handles backspace so the user can correct mistakes.
     *
     * @param e physical key press event
     */
    public void onKeyPressed(KeyEvent e) {
        // Backspace is the only control key we care about in this lightweight mode.
        if (e.getCode() == KeyCode.BACK_SPACE) {
            if (index > 0) {
                removeCursor();
                index--;
                if (index < userFlow.getChildren().size()) {
                    userFlow.getChildren().remove(index);
                }
                metrics.decTypedIfBackspace(false);
                keyboard.dim();
                updateCursor();
            }
            e.consume();
        }
    }

    private void pushChar(char c) {
        // Mirror the typed character into the flow at the cursor so metrics match what is shown.
        Text t = new Text(String.valueOf(c));
        t.getStyleClass().addAll("free-typed", "mono");
        userFlow.getChildren().add(index, t);
        index++;
    }

    private void removeCursor() {
        // Temporarily pull the cursor node so we can drop it back at the right position.
        userFlow.getChildren().remove(cursor);
    }

    private void updateCursor() {
        // Slide the cursor forward and let the caller adjust scroll position if needed.
        removeCursor();
        int insertionIndex = Math.min(index, userFlow.getChildren().size());
        userFlow.getChildren().add(insertionIndex, cursor);
        notifyCursorListener();
    }

    private void notifyCursorListener() {
        // The host controller cares about the cursor's bounds, so surface the update here.
        if (cursorListener != null) {
            cursorListener.accept(cursor);
        }
    }
}
