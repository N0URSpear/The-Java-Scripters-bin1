package typingninja.typing_ninja_1;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public final class SqliteConnection {
    private static Connection instance;

    private SqliteConnection() {}

    public static synchronized Connection getInstance() {
        if (instance == null) instance = create();
        return instance;
    }

    private static Connection create() {
        try {
            // 放到项目目录：<project>/TypingNinjaSQL.db
            Path dbPath = Path.of(System.getProperty("user.dir"), "TypingNinjaSQL.db").toAbsolutePath();
            Files.createDirectories(dbPath.getParent());
            String url = "jdbc:sqlite:" + dbPath;



            Connection conn = DriverManager.getConnection(url);
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA foreign_keys=ON");

            }

            // 进程退出时自动关闭（可选）
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { if (instance != null && !instance.isClosed()) instance.close(); } catch (Exception ignored) {}
            }));
            return conn;
        } catch (Exception e) {
            throw new RuntimeException("Failed to open SQLite connection", e);
        }
    }

}
