package typingNinja.model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Paths;

public class SqliteConnection {
    private static Connection instance = null;

    private SqliteConnection() {
        // Resolve an absolute path to ensure we are always using the intended DB file
        String dbFilePath = Paths.get(System.getProperty("user.dir"), "TypingNinjaSQL.db").toString();
        String url = "jdbc:sqlite:" + dbFilePath;
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException sqlEx) {
            System.err.println(sqlEx);
        }
    }

    public static synchronized Connection getInstance() {
        try {
            if (instance == null || instance.isClosed()) {
                new SqliteConnection();
            }
        } catch (SQLException e) {
            // If the existing handle is in a bad state, build a fresh one.
            new SqliteConnection();
        }
        return instance;
    }

    /**
     * Test-only hook that lets unit tests isolate themselves by closing and nulling the singleton.
     * Production code should never call this.
     */
    public static void resetForTests() {
        if (instance != null) {
            try {
                instance.close();
            } catch (SQLException ignored) {
            }
            instance = null;
        }
    }
}
