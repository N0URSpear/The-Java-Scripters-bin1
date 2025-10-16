package typingNinja.tests;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typingNinja.model.NinjaUser;
import typingNinja.model.SqliteConnection;
import typingNinja.model.SqliteContactDAO;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class SqliteContactDAOIntegrationTest {

    private Connection conn;
    private SqliteContactDAO dao;

    private static String unique(String base) {
        return base + "_" + System.nanoTime();
    }

    private static NinjaUser make(String name, String hash) {
        return new NinjaUser(name, hash, "Q1", "Q2", "A1", "A2");
    }

    @BeforeEach
    void setUp() throws SQLException {
        conn = SqliteConnection.getInstance();
        assertNotNull(conn, "DB connection should not be null");
        conn.setAutoCommit(false);
        dao = new SqliteContactDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (conn != null) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }

    @Test
    void addUser_thenFindByName() {
        String username = unique("testUser_add_get");
        NinjaUser user = make(username, "pwHash1");

        dao.addNinjaUser(user);

        assertTrue(user.getId() > 0, "Inserted user should have a generated ID");

        NinjaUser fetched = dao.getNinjaUser(username);
        assertNotNull(fetched, "Inserted user should be fetched by username");
        assertEquals(username, fetched.getUserName());
        assertEquals("pwHash1", fetched.getPasswordHash());
    }

    @Test
    void deleteUser_andVerifyGone() {
        String username = unique("testUser_delete");
        NinjaUser user = make(username, "pwHash2");

        dao.addNinjaUser(user);
        assertTrue(user.getId() > 0, "Inserted user should have a generated ID");

        assertNotNull(dao.getNinjaUser(username), "User should exist before deletion");

        dao.deleteNinjaUser(user);

        NinjaUser afterDelete = dao.getNinjaUser(username);
        assertNull(afterDelete, "User should be removed after delete");
    }
}
