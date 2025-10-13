package typingNinja.lesson;

import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.function.Consumer;

/**
 * Handles free typing mode where there is no fixed prompt. Characters are appended
 * to the user flow and metrics are updated using simple character counts.
 */
public class FreeTypingInput {
    private final TextFlow userFlow;
    private final TextArea hiddenInput;
    private final KeyboardHands keyboard;
    private final Metrics metrics;
    private final Text cursor;
    private final Consumer<Text> cursorListener;
    private int index = 0;

    public FreeTypingInput(TextFlow userFlow,
                           TextArea hiddenInput,
                           KeyboardHands keyboard,
                           Metrics metrics,
                           Consumer<Text> cursorListener) {
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

    public void onKeyTyped(KeyEvent e) {
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

    public void onKeyPressed(KeyEvent e) {
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
        Text t = new Text(String.valueOf(c));
        t.getStyleClass().addAll("free-typed", "mono");
        userFlow.getChildren().add(index, t);
        index++;
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
}
