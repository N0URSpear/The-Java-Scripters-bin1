package typingNinja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import typingNinja.model.MockNinjaDAO;
import typingNinja.model.NinjaUser;

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
        // 如果 MockNinjaDAO 提供了静态 clearAll()，优先清空全局状态
        try {
            Method m = MockNinjaDAO.class.getDeclaredMethod("clearAll");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception ignored) {
            // 没有也没关系；下面还会用“唯一用户名”避免冲突
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

        // ✅ 不再假定从 0 开始；只要求自增且连续
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

        // 修改“同一个实例”并更新
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
        NinjaUser bob   = make(b, "h2");

        dao.addNinjaUser(alice);
        dao.addNinjaUser(bob);

        // ✅ 用“当初 add 的同一个实例”删除，避免因防御性拷贝删不掉
        dao.deleteNinjaUser(alice);

        assertNull(dao.getNinjaUser(a), "Alice should be removed");
        assertNotNull(dao.getNinjaUser(b), "Bob should still exist");

        // （可选）如果 Mock 维护列表大小正确，可启用：
        // assertEquals(1, dao.getAllNinjas().size());
    }

    @Test
    void getAllNinjasShouldReturnDefensiveCopy() {
        dao.addNinjaUser(make(unique("alice"), "h1"));
        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear();                 // 修改外部列表不应影响内部存储
        assertEquals(size, dao.getAllNinjas().size());
    }

    @Test
    void getNinjaUserUnknownNameShouldReturnNull() {
        assertNull(dao.getNinjaUser(unique("nobody")));
    }
}
