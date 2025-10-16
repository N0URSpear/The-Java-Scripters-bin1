package typingNinja.tests;

import org.junit.jupiter.api.Test;
import typingNinja.model.SqliteConnection;

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
        assertSame(c1, c2, "Expecting the same Connection instance (singleton)");
    }
}
