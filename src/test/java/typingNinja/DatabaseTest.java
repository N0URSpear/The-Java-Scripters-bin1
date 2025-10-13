package typingNinja;

import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SqliteContactDAO against the connected SQLite database.
 *
 * Each test runs inside a DB transaction that is rolled back in @AfterEach
 * to keep the database clean and independent between tests.
 *
 * @author Nour Spear
 */
class SqliteContactDAOIntegrationTest {

    private Connection conn;
    private SqliteContactDAO dao;

    private static String unique(String base) {
        return base + "_" + System.nanoTime();
    }

    private static NinjaUser make(String name, String hash) {
        // Adjust arguments if your NinjaUser constructor differs
        return new NinjaUser(name, hash, "Q1", "Q2", "A1", "A2");
    }

    @BeforeEach
    void setUp() throws SQLException {
        // Get the real DB connection and start a transaction
        conn = SqliteConnection.getInstance();
        assertNotNull(conn, "DB connection should not be null");
        conn.setAutoCommit(false);

        // Use the real DAO (ensures table exists)
        dao = new SqliteContactDAO();
    }

    @AfterEach
    void tearDown() throws SQLException {
        // Roll back to keep the DB clean between tests
        if (conn != null) {
            conn.rollback();
            conn.setAutoCommit(true);
        }
    }

    /**
     * Author: Nour Spear
     * Verifies that a user can be added and read back by username.
     */
    @Test
    void addUser_thenFindByName() {
        String username = unique("testUser_add_get");
        NinjaUser user = make(username, "pwHash1");

        // Add
        dao.addNinjaUser(user);

        // After insert, the DAO should set the generated ID on the passed instance
        assertTrue(user.getId() > 0, "Inserted user should have a generated ID");

        // Find
        NinjaUser fetched = dao.getNinjaUser(username);
        assertNotNull(fetched, "Inserted user should be fetched by username");
        assertEquals(username, fetched.getUserName());
        assertEquals("pwHash1", fetched.getPasswordHash());
    }

    /**
     * Author: Nour Spear
     * Verifies that a user can be deleted and is no longer retrievable.
     */
    @Test
    void deleteUser_andVerifyGone() {
        String username = unique("testUser_delete");
        NinjaUser user = make(username, "pwHash2");

        // Seed a user to delete
        dao.addNinjaUser(user);
        assertTrue(user.getId() > 0, "Inserted user should have a generated ID");

        // Ensure present before deletion
        assertNotNull(dao.getNinjaUser(username), "User should exist before deletion");

        // Delete using the same instance (has the ID)
        dao.deleteNinjaUser(user);

        // Verify removed
        NinjaUser afterDelete = dao.getNinjaUser(username);
        assertNull(afterDelete, "User should be removed after delete");
    }
}