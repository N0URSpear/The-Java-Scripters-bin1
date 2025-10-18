package typingNinja.tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import typingNinja.model.INinjaContactDAO;
import typingNinja.model.MockNinjaDAO;
import typingNinja.model.NinjaUser;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class INinjaContactDAOTest {

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
            // No-op if the mock doesn't expose a clearAll helper.
        }

        dao = new MockNinjaDAO();
    }

    @Test
    void addAndGetByName_shouldFollowContract() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));
        NinjaUser got = dao.getNinjaUser(user);
        assertNotNull(got);
        assertEquals(user, got.getUserName());
        assertEquals("h1", got.getPasswordHash());
    }

    @Test
    void getUnknownName_shouldReturnNull() {
        assertNull(dao.getNinjaUser(unique("nobody")));
    }

    @Test
    void update_shouldPersistNewValues() {
        String user = unique("alice");

        dao.addNinjaUser(make(user, "h1"));

        NinjaUser current = dao.getNinjaUser(user);
        assertNotNull(current);

        current.setPasswordHash("new");
        dao.updateNinjaUser(current);

        NinjaUser after = dao.getNinjaUser(user);
        assertNotNull(after);
        assertEquals("new", after.getPasswordHash());
        assertEquals(current.getId(), after.getId());
    }

    @Test
    void delete_shouldRemoveUserAndShrinkList() {
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
    void getAllNinjas_shouldReturnDefensiveCopyIfDocumented() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear();
        assertEquals(size, dao.getAllNinjas().size());
    }
}
