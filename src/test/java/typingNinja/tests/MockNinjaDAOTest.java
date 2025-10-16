package typingNinja.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typingNinja.model.MockNinjaDAO;
import typingNinja.model.NinjaUser;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MockNinjaDAOTest {

    MockNinjaDAO dao;

    private static String unique(String base) {
        return base + "_" + System.nanoTime();
    }

    private static NinjaUser make(String name, String hash) {
        return new NinjaUser(name, hash, "Q1", "Q2", "A1", "A2");
    }

    @BeforeEach
    void setUp() {
        try {
            Method m = MockNinjaDAO.class.getDeclaredMethod("clearAll");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception ignored) {
            // If the helper doesn't exist we rely on unique usernames.
        }
        dao = new MockNinjaDAO();
    }

    @Test
    void addShouldAssignIncrementalIdAndBeRetrievableByName() {
        String a = unique("alice");
        String b = unique("bob");

        NinjaUser u1 = make(a, "h1");
        NinjaUser u2 = make(b, "h2");

        dao.addNinjaUser(u1);
        dao.addNinjaUser(u2);

        assertTrue(u2.getId() == u1.getId() + 1, "id should be incremental");

        NinjaUser got = dao.getNinjaUser(a);
        assertNotNull(got);
        assertEquals(a, got.getUserName());
        assertEquals("h1", got.getPasswordHash());
    }

    @Test
    void updateShouldReplacePasswordHash() {
        String a = unique("alice");
        NinjaUser u = make(a, "oldHash");
        dao.addNinjaUser(u);

        u.setPasswordHash("newHash");
        dao.updateNinjaUser(u);

        NinjaUser got = dao.getNinjaUser(a);
        assertNotNull(got);
        assertEquals("newHash", got.getPasswordHash());
        assertEquals(u.getId(), got.getId(), "id should remain unchanged");
    }

    @Test
    void deleteShouldRemoveUser() {
        String a = unique("alice");
        String b = unique("bob");

        NinjaUser alice = make(a, "h1");
        NinjaUser bob = make(b, "h2");

        dao.addNinjaUser(alice);
        dao.addNinjaUser(bob);

        dao.deleteNinjaUser(alice);

        assertNull(dao.getNinjaUser(a), "Alice should be removed");
        assertNotNull(dao.getNinjaUser(b), "Bob should still exist");
    }

    @Test
    void getAllNinjasShouldReturnDefensiveCopy() {
        dao.addNinjaUser(make(unique("alice"), "h1"));
        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear();
        assertEquals(size, dao.getAllNinjas().size());
    }

    @Test
    void getNinjaUserUnknownNameShouldReturnNull() {
        assertNull(dao.getNinjaUser(unique("nobody")));
    }
}
