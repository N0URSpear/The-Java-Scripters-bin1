package com.example.addressbook.lesson;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
        this.grid = grid;
        this.handsRegion = handsRegion;
        this.handsLabel = handsLabel;
    }

    /** neat, proportional keyboard that grows to fill its column */
    public void buildQwerty() {
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        shiftKeys.clear();
        keyMap.clear();

        final int COLS = 60;
        for (int i = 0; i < COLS; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / COLS);
            cc.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 5; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setVgrow(Priority.ALWAYS);
            grid.getRowConstraints().add(rc);
        }

        addRow(0, new String[][]{
                {"`","3"},{"1","3"},{"2","3"},{"3","3"},{"4","3"},{"5","3"},{"6","3"},{"7","3"},{"8","3"},{"9","3"},{"0","3"},{"-","3"},{"=","3"},{"Backspace","9"}
        });

        addRow(1, new String[][]{
                {"Tab","7"},{"Q","3"},{"W","3"},{"E","3"},{"R","3"},{"T","3"},{"Y","3"},{"U","3"},{"I","3"},{"O","3"},{"P","3"},{"[","3"},{"]","3"},{"\\\\","5"}
        });

        addRow(2, new String[][]{
                {"Caps","8"},{"A","3"},{"S","3"},{"D","3"},{"F","3"},{"G","3"},{"H","3"},{"J","3"},{"K","3"},{"L","3"},{";","3"},{"'","3"},{"Enter","8"}
        });

        addRow(3, new String[][]{
                {"Shift","10"},{"Z","3"},{"X","3"},{"C","3"},{"V","3"},{"B","3"},{"N","3"},{"M","3"},{",","3"},{".","3"},{"/","3"},{"Shift","11"}
        });

        addRow(4, new String[][]{
                {"Ctrl","6"},{"fn","4"},{"Alt","6"},{"Space","20"},{"Control","8"},{"Alt","8"}
        });
    }

    private void addRow(int row, String[][] keys) {
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
        char base = baseChar(ch);
        boolean needShift = requiresShift(ch);

        lightForChar(base);
        if (handsLabel != null) handsLabel.setText(fingerTextFor(ch));

        for (Button s : shiftKeys) s.getStyleClass().remove("keycap-lit");
        if (needShift) {
            for (Button s : shiftKeys) {
                if (!s.getStyleClass().contains("keycap-lit")) s.getStyleClass().add("keycap-lit");
            }
        }
    }

    public void lightForChar(char ch) {
        String key = mapCharToKey(ch).toLowerCase();
        Button b = keyMap.get(key);
        if (b == null) return;
        if (lit != null) lit.getStyleClass().remove("keycap-lit");
        lit = b;
        if (!lit.getStyleClass().contains("keycap-lit")) lit.getStyleClass().add("keycap-lit");
    }

    public void dim() {
        if (lit != null) lit.getStyleClass().remove("keycap-lit");
        lit = null;
        for (Button s : shiftKeys) s.getStyleClass().remove("keycap-lit");
    }

    private String mapCharToKey(char ch) {
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
        if (Character.isUpperCase(ch)) return true;
        switch (ch) {
            case '!': case '@': case '#': case '$': case '%': case '^': case '&': case '*':
            case '(': case ')': case '_': case '+': case '{': case '}': case '|':
            case ':': case '\"': case '<': case '>': case '?':
                return true;
            default: return false;
        }
    }

    private char baseChar(char ch) {
        if (Character.isUpperCase(ch)) return Character.toLowerCase(ch);
        switch (ch) {
            case '!': return '1'; case '@': return '2'; case '#': return '3'; case '$': return '4'; case '%': return '5';
            case '^': return '6'; case '&': return '7'; case '*': return '8'; case '(': return '9'; case ')': return '0';
            case '_': return '-'; case '+': return '='; case '{': return '['; case '}': return ']'; case '|': return '\\';
            case ':': return ';'; case '\"': return '\''; case '<': return ','; case '>': return '.'; case '?': return '/';
            default: return ch;
        }
    }

    private String fingerTextFor(char ch) {
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
}
