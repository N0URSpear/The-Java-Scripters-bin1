package typingNinja;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteConnectionTest {

    @Test
    void getInstance_shouldReturnNonNullConnection() throws SQLException {
        Connection conn = SqliteConnection.getInstance();
        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");
    }

    @Test
    void getInstance_calledTwice_shouldReturnSameConnectionIfSingleton() throws SQLException {
        Connection c1 = SqliteConnection.getInstance();
        Connection c2 = SqliteConnection.getInstance();

        // 若你的实现是“单例连接”，保留这一句：
        assertSame(c1, c2, "Expecting the same Connection instance (singleton)");

        // 若你的实现每次新建连接，把上一句换成：
        // assertNotSame(c1, c2);
        // 并可增加一致性检查（同一 DB）：
        // assertEquals(c1.getMetaData().getURL(), c2.getMetaData().getURL());
    }
}
