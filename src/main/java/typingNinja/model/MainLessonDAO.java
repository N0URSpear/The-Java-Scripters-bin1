package typingNinja.model;

import java.sql.*;

/**
 * DAO used to record a user's chosen lesson (and options for custom/free type).
 * It keeps your existing behavior but adds optional columns if they don't exist.
 */
public class MainLessonDAO {
    private final Connection connection;

    public MainLessonDAO() {
        connection = SqliteConnection.getInstance();
        createTableIfNeeded();
        ensureOptionalColumns(); // add new columns if they weren't created earlier
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

    /** Adds new columns in a forward-compatible way (safe if they already exist). */
    private void ensureOptionalColumns() {
        // Each ADD COLUMN is idempotent because we guard with pragma table_info
        addColumnIfMissing("Prompt",        "TEXT");
        addColumnIfMissing("LessonDuration","INTEGER");
        addColumnIfMissing("UpperCase",     "INTEGER");  // 0/1
        addColumnIfMissing("Numbers",       "INTEGER");
        addColumnIfMissing("Punctuation",   "INTEGER");
        addColumnIfMissing("SpecialChars",  "INTEGER");
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
            // If for any reason this fails, we log and carry on (older DBs still work).
            e.printStackTrace();
        }
    }

    /** Original simple insert (used by Lesson 1–4). */
    public void insertSelection(int userId, String lessonType) {
        final String sql = "INSERT INTO Lesson (UserID, LessonType) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, lessonType);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Custom Topic insert with all requested fields. */
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
              (UserID, LessonType, Prompt, LessonDuration, UpperCase, Numbers, Punctuation, SpecialChars)
            VALUES
              (?,      'CustomTopic', ?,      ?,             ?,         ?,       ?,            ?)
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

    /** Free Type insert with duration only. */
    public void insertFreeType(int userId, int lessonDuration) {
        final String sql = """
            INSERT INTO Lesson (UserID, LessonType, LessonDuration)
            VALUES (?, 'FreeType', ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, lessonDuration);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
