package typingNinja.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typingNinja.model.INinjaContactDAO;
import typingNinja.model.MockNinjaDAO;
import typingNinja.model.NinjaUser;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SqliteContactDAOTest {

    INinjaContactDAO dao;

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
            // Best effort; unique usernames still keep tests isolated.
        }
        dao = new MockNinjaDAO();
    }

    @Test
    void addAndGetShouldWork() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        NinjaUser got = dao.getNinjaUser(user);
        assertNotNull(got);
        assertEquals(user, got.getUserName());
        assertEquals("h1", got.getPasswordHash());
    }

    @Test
    void getUnknownNameShouldReturnNull() {
        assertNull(dao.getNinjaUser(unique("nobody")));
    }

    @Test
    void updateShouldPersistChanges() {
        String user = unique("alice");
        NinjaUser u = make(user, "oldHash");
        dao.addNinjaUser(u);

        u.setPasswordHash("newHash");
        dao.updateNinjaUser(u);

        NinjaUser got = dao.getNinjaUser(user);
        assertNotNull(got);
        assertEquals("newHash", got.getPasswordHash());
        assertEquals(u.getId(), got.getId());
    }

    @Test
    void deleteShouldRemoveRow() {
        String a = unique("alice");
        String b = unique("bob");

        NinjaUser alice = make(a, "h1");
        NinjaUser bob = make(b, "h2");

        dao.addNinjaUser(alice);
        dao.addNinjaUser(bob);

        dao.deleteNinjaUser(alice);

        assertNull(dao.getNinjaUser(a), "Alice should be removed");
        NinjaUser stillThere = dao.getNinjaUser(b);
        assertNotNull(stillThere);
        assertEquals(b, stillThere.getUserName());
    }

    @Test
    void getAllNinjasShouldReturnDefensiveCopyIfDocumented() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear();
        assertEquals(size, dao.getAllNinjas().size());
    }
}
