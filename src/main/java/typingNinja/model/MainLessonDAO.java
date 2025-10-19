package typingNinja.model;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Locale;
import typingNinja.model.auth.Session;

public class MainLessonDAO {
    private final Connection connection;

    public MainLessonDAO() {
        connection = SqliteConnection.getInstance();
        createTableIfNeeded();
        ensureOptionalColumns();
    }

    private void createTableIfNeeded() {
        final String sql = """
            CREATE TABLE IF NOT EXISTS Lesson (
                LessonID      INTEGER PRIMARY KEY AUTOINCREMENT,
                UserID        INTEGER NOT NULL,
                LessonType    TEXT    NOT NULL,
                CreatedAt     TEXT    DEFAULT CURRENT_TIMESTAMP
            )
            """;
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureOptionalColumns() {
        addColumnIfMissing("Prompt", "TEXT");
        addColumnIfMissing("LessonDuration", "INTEGER");
        addColumnIfMissing("UpperCase", "INTEGER");
        addColumnIfMissing("Numbers", "INTEGER");
        addColumnIfMissing("Punctuation", "INTEGER");
        addColumnIfMissing("SpecialChars", "INTEGER");
        addColumnIfMissing("DateStarted", "TEXT");
        addColumnIfMissing("DateCompleted", "TEXT");
    }

    private void addColumnIfMissing(String col, String type) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM pragma_table_info('Lesson') WHERE name = ?")) {
            ps.setString(1, col);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    try (Statement alter = connection.createStatement()) {
                        alter.execute("ALTER TABLE Lesson ADD COLUMN " + col + " " + type);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertSelection(int userId, String lessonType) {
        final String sql = "INSERT INTO Lesson (UserID, LessonType, DateStarted) VALUES (?, ?, datetime('now'))";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, lessonType);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertCustomTopic(
            int userId,
            String prompt,
            int lessonDuration,
            boolean upperCase,
            boolean numbers,
            boolean punctuation,
            boolean specialChars
    ) {
        final String sql = """
            INSERT INTO Lesson
              (UserID, LessonType, Prompt, LessonDuration, UpperCase, Numbers, Punctuation, SpecialChars, DateStarted)
            VALUES
              (?, 'CustomTopic', ?, ?, ?, ?, ?, ?, datetime('now'))
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, prompt);
            ps.setInt(3, lessonDuration);
            ps.setInt(4, upperCase ? 1 : 0);
            ps.setInt(5, numbers ? 1 : 0);
            ps.setInt(6, punctuation ? 1 : 0);
            ps.setInt(7, specialChars ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertFreeType(int userId, int lessonDuration) {
        final String sql = """
            INSERT INTO Lesson (UserID, LessonType, LessonDuration, DateStarted)
            VALUES (?, 'FreeType', ?, datetime('now'))
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, lessonDuration);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns formatted best time and date for the current user, or "Not Yet Completed" if none valid. */
    public String getBestTimeAndDate(String lessonType) {
        int userId = Session.getCurrentUserId();
        final String sql = """
            SELECT 
                (strftime('%s', DateCompleted) - strftime('%s', DateStarted)) AS seconds,
                DateCompleted
            FROM Lesson
            WHERE UserID = ? 
              AND LessonType = ?
              AND DateCompleted IS NOT NULL
              AND DateStarted IS NOT NULL
              AND strftime('%s', DateCompleted) > strftime('%s', DateStarted)
            ORDER BY seconds ASC
            LIMIT 1
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, lessonType);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int seconds = rs.getInt("seconds");
                    String dateCompleted = rs.getString("DateCompleted");
                    if (dateCompleted != null && dateCompleted.length() >= 10) {
                        String formatted = formatDate(dateCompleted);
                        return "Best time: " + seconds + " seconds\nCompleted " + formatted;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Not Yet Completed";
    }

    private String formatDate(String dateTime) {
        try {
            var in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            var out = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
            return out.format(in.parse(dateTime));
        } catch (Exception e) {
            try {
                var in = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                var out = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
                return out.format(in.parse(dateTime));
            } catch (Exception ignored) {}
        }
        return dateTime;
    }
}
