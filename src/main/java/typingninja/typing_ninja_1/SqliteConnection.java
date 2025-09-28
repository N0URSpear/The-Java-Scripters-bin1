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
            // 放到用户目录，避免权限问题；你也可以换成工作目录
            Path dbDir = Path.of(System.getProperty("user.home"), ".typing-ninja");
            Files.createDirectories(dbDir);
            String url = "jdbc:sqlite:" + dbDir.resolve("TypingNinjaSQL.db");

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
