package typingNinja.controllers.lesson;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.net.URL;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KeyboardHands {
    private final GridPane grid;
    private final Region handsRegion;
    private final Label handsLabel;
    private final Map<String, Button> keyMap = new HashMap<>();
    private final List<Button> shiftKeys = new ArrayList<>();
    private Button lit;

    public KeyboardHands(GridPane grid, Region handsRegion, Label handsLabel) {
        // Cache references so we can rebuild or highlight keys without recreating controls.
        this.grid = grid;
        this.handsRegion = handsRegion;
        this.handsLabel = handsLabel;
    }

    public void buildQwerty() {
        // Lay out a proportional keyboard so highlighting works regardless of window size.
        grid.setMinSize(0, 0);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setPrefWidth(Double.MAX_VALUE);
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        shiftKeys.clear();
        keyMap.clear();

        final int COLS = 64;
        for (int i = 0; i < COLS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLS);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 5; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            rc.setPercentHeight(100.0 / 5);
            grid.getRowConstraints().add(rc);
        }

        addRow(0, new String[][]{
                {"`","4"},{"1","4"},{"2","4"},{"3","4"},{"4","4"},{"5","4"},{"6","4"},{"7","4"},{"8","4"},{"9","4"},{"0","4"},{"-","4"},{"=","4"},{"Backspace","12"}
        });
        addRow(1, new String[][]{
                {"Tab","6"},{"Q","4"},{"W","4"},{"E","4"},{"R","4"},{"T","4"},{"Y","4"},{"U","4"},{"I","4"},{"O","4"},{"P","4"},{"[","4"},{"]","4"},{"\\","10"}
        });
        addRow(2, new String[][]{
                {"Caps","7"},{"A","4"},{"S","4"},{"D","4"},{"F","4"},{"G","4"},{"H","4"},{"J","4"},{"K","4"},{"L","4"},{";","4"},{"'","4"},{"Enter","13"}
        });
        addRow(3, new String[][]{
                {"Shift","12"},{"Z","4"},{"X","4"},{"C","4"},{"V","4"},{"B","4"},{"N","4"},{"M","4"},{",","4"},{".","4"},{"/","4"},{"Shift","12"}
        });
        addRow(4, new String[][]{
                {"Ctrl","7"},{"fn","6"},{"Alt","7"},{"Space","24"},{"Ctrl","10"},{"Alt","10"}
        });
    }

    private void addRow(int row, String[][] keys) {
        // Translate the simple span metadata into actual JavaFX buttons.
        int col = 0;
        for (String[] kv : keys) {
            String k = kv[0];
            int span = Integer.parseInt(kv[1]);
            Button b = new Button(k);
            b.getStyleClass().add("keycap");
            b.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            b.setMinSize(0, 0);
            grid.add(b, col, row, span, 1);
            col += span;
            keyMap.put(k.toLowerCase(), b);
            if ("shift".equalsIgnoreCase(k)) shiftKeys.add(b);
        }
    }

    public void highlightExpected(char ch) {
        // Figure out which finger should move next and light both the key and the supporting shift.
        char base = baseChar(ch);
        boolean needShift = requiresShift(ch);

        lightForChar(base);
        updateHandsVisual(fingerTextFor(ch));

        for (Button s : shiftKeys) s.getStyleClass().remove("keycap-lit");
        if (needShift) {
            for (Button s : shiftKeys) {
                if (!s.getStyleClass().contains("keycap-lit")) s.getStyleClass().add("keycap-lit");
            }
        }
    }

    public void lightForChar(char ch) {
        // Keep only one key lit so the visual cue is unambiguous.
        String key = mapCharToKey(ch).toLowerCase();
        Button b = keyMap.get(key);
        if (b == null) return;
        if (lit != null) lit.getStyleClass().remove("keycap-lit");
        lit = b;
        if (!lit.getStyleClass().contains("keycap-lit")) lit.getStyleClass().add("keycap-lit");
    }

    public void dim() {
        // Clear all highlights when the lesson pauses or completes.
        if (lit != null) lit.getStyleClass().remove("keycap-lit");
        lit = null;
        for (Button s : shiftKeys) s.getStyleClass().remove("keycap-lit");
        if (handsRegion != null) {
            handsRegion.setStyle("");
        }
        if (handsLabel != null) {
            handsLabel.setVisible(false);
            handsLabel.setText("");
        }
    }

    private String mapCharToKey(char ch) {
        // Normalise characters into the labels we used when constructing buttons.
        if (ch == ' ') return "space";
        if (ch == '\n' || ch == '\r') return "enter";
        if (Character.isLetter(ch)) return String.valueOf(Character.toUpperCase(ch));
        if (Character.isDigit(ch)) return String.valueOf(ch);
        switch (ch) {
            case '`': return "`";
            case '-': return "-";
            case '=': return "=";
            case '[': return "[";
            case ']': return "]";
            case '\\': return "\\";
            case ';': return ";";
            case '\'': return "'";
            case ',': return ",";
            case '.': return ".";
            case '/': return "/";
            default:  return String.valueOf(ch);
        }
    }

    private boolean requiresShift(char ch) {
        // Uppercase and symbol characters imply the shift key should glow too.
        if (Character.isUpperCase(ch)) return true;
        switch (ch) {
            case '!': case '@': case '#': case '$': case '%': case '^': case '&': case '*':
            case '(': case ')': case '_': case '+': case '{': case '}': case '|':
            case ':': case '\"': case '<': case '>': case '?':
            case '~':
                return true;
            default: return false;
        }
    }

    private char baseChar(char ch) {
        // Reduce shifted characters back to their physical key so lighting stays consistent.
        if (Character.isUpperCase(ch)) return Character.toLowerCase(ch);
        switch (ch) {
            case '!': return '1'; case '@': return '2'; case '#': return '3'; case '$': return '4'; case '%': return '5';
            case '^': return '6'; case '&': return '7'; case '*': return '8'; case '(': return '9'; case ')': return '0';
            case '_': return '-'; case '+': return '='; case '{': return '['; case '}': return ']'; case '|': return '\\';
            case ':': return ';'; case '\"': return '\''; case '<': return ','; case '>': return '.'; case '?': return '/';
            case '~': return '`';
            default: return ch;
        }
    }

    private String fingerTextFor(char ch) {
        // Quick lookup for which finger owns a key; mostly home-row grouped heuristics.
        char c = Character.toLowerCase(ch);
        if (c == ' ') return "left 1";
        if (c == '\n' || c == '\r') return "right 5";
        String s = String.valueOf(c);
        if ("`1qaz".contains(s)) return "left 5";
        if ("2wsx".contains(s)) return "left 4";
        if ("3edc".contains(s)) return "left 3";
        if ("45rfgtvb".contains(s)) return "left 2";
        if ("67yhnujm".contains(s)) return "right 2";
        if ("8ik,".contains(s)) return "right 3";
        if ("9ol.".contains(s)) return "right 4";
        if ("0p;/-=[]\\'".contains(s)) return "right 5";
        return "right 2";
    }

    private void updateHandsVisual(String descriptor) {
        // Swap in a reference image if we have one, otherwise fall back to plain text coaching.
        if (handsRegion == null) return;
        String base = "/typingNinja/Images/";
        String[] exts = { ".png", ".jpg", ".jpeg", ".gif" };
        URL found = null;
        for (String ext : exts) {
            String candidate = base + descriptor + ext;
            URL u = getClass().getResource(candidate);
            if (u != null) { found = u; break; }
        }
        if (found != null) {
            String url = found.toExternalForm();
            String style = "-fx-background-color: transparent;" +
                    "-fx-background-image: url('" + url + "');" +
                    "-fx-background-repeat: no-repeat;" +
                    "-fx-background-position: center;" +
                    "-fx-background-size: 90% 90%;" +
                    "-fx-background-radius: 16; -fx-border-radius: 16;";
            handsRegion.setStyle(style);
            if (handsLabel != null) {
                handsLabel.setVisible(false);
                handsLabel.setManaged(false);
                handsLabel.setText("");
            }
        } else {
            if (handsLabel != null) {
                handsLabel.setVisible(true);
                handsLabel.setManaged(true);
                handsLabel.setText(descriptor);
            }
        }
    }
}
