package typingNinja.tests.support;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import typingNinja.model.SqliteConnection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;

/**
 * Provides an isolated temporary SQLite database for each test.
 * The production schema is applied so DAOs behave exactly as they do at runtime.
 */
public abstract class DatabaseTestHarness {

    private String originalUserDir;
    private Path tempDir;

    @BeforeEach
    void setUpDatabase() throws Exception {
        originalUserDir = System.getProperty("user.dir");
        tempDir = Files.createTempDirectory("typingninja-db-");
        System.setProperty("user.dir", tempDir.toString());
        SqliteConnection.resetForTests();
        try (Connection conn = SqliteConnection.getInstance()) {
            applySchema(conn);
        }
    }

    @AfterEach
    void tearDownDatabase() throws Exception {
        SqliteConnection.resetForTests();
        if (originalUserDir != null) {
            System.setProperty("user.dir", originalUserDir);
        }
        if (tempDir != null) {
            try (var paths = Files.walk(tempDir)) {
                paths.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.deleteIfExists(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
    }

    protected Connection connection() throws SQLException {
        return SqliteConnection.getInstance();
    }

    private void applySchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS Lesson (
                        LessonID INTEGER PRIMARY KEY AUTOINCREMENT,
                        UserID INTEGER NOT NULL,
                        LessonType TEXT NOT NULL,
                        Prompt TEXT,
                        LessonDuration INTEGER,
                        UpperCase INTEGER,
                        Numbers INTEGER,
                        Punctuation INTEGER,
                        SpecialChars INTEGER,
                        DateStarted TEXT,
                        DateCompleted TEXT,
                        StarRating REAL,
                        WPM REAL,
                        Accuracy REAL,
                        ErrorAmount INTEGER,
                        WeakKeys TEXT,
                        CreatedAt TEXT DEFAULT CURRENT_TIMESTAMP
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS Settings (
                        UserID INTEGER PRIMARY KEY,
                        DisplayLanguage TEXT,
                        Theme TEXT,
                        FontSize TEXT,
                        KeyboardSounds INTEGER,
                        TypingErrors INTEGER,
                        TypingErrorSounds INTEGER,
                        LessonCompleteSound INTEGER
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS Users (
                        UserID INTEGER PRIMARY KEY AUTOINCREMENT,
                        Username TEXT,
                        PasswordHash TEXT
                    )
                    """);
            st.execute("""
                    INSERT OR IGNORE INTO Users (UserID, Username, PasswordHash)
                    VALUES (1, 'test-user', 'hash')
                    """);
        }
    }
}
