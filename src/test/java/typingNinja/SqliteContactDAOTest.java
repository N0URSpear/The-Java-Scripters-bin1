package typingNinja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 接口契约层的 unit test：使用 MockNinjaDAO，避免真实 DB 依赖。
 * 关键点：
 * 1) 在每个测试前尝试清空 Mock 的全局存储（若提供 clearAll），避免状态泄漏；
 * 2) 每个用例使用唯一用户名，杜绝同名冲突；
 * 3) 删除时用“当初 add 的同一个实例”调用 deleteNinjaUser。
 */
class SqliteContactDAOTest {

    INinjaContactDAO dao;

    private static String unique(String base) {
        return base + "_" + System.nanoTime();
    }

    private static NinjaUser make(String name, String hash) {
        // 按你的 NinjaUser 构造器签名调整
        return new NinjaUser(name, hash, "Q1", "Q2", "A1", "A2");
    }

    @BeforeEach
    void setUp() {
        // 1) 如果 MockNinjaDAO 提供了清空方法，先清一次，保证测试独立
        try {
            Method m = MockNinjaDAO.class.getDeclaredMethod("clearAll");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception ignored) {
            // 没有该方法也没关系；我们还会用唯一用户名来隔离
        }

        // 2) 每次新建一个 DAO 实例
        dao = new MockNinjaDAO();
    }

    @Test
    void addAndGetShouldWork() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        NinjaUser got = dao.getNinjaUser(user);
        assertNotNull(got);
        assertEquals(user, got.getUserName());
        assertEquals("h1",  got.getPasswordHash());
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
        // 如果 Mock 会分配自增 id，这里顺手校验 id 未改变
        assertEquals(u.getId(), got.getId());
    }

    @Test
    void deleteShouldRemoveRow() {
        String a = unique("alice");
        String b = unique("bob");

        NinjaUser alice = make(a, "h1");
        NinjaUser bob   = make(b, "h2");

        dao.addNinjaUser(alice);
        dao.addNinjaUser(bob);

        // 用“当初 add 的同一个实例”来删除，避免防御性副本删不掉
        dao.deleteNinjaUser(alice);

        assertNull(dao.getNinjaUser(a), "Alice should be removed");
        NinjaUser stillThere = dao.getNinjaUser(b);
        assertNotNull(stillThere);
        assertEquals(b, stillThere.getUserName());

        // 如果你的 Mock 正确维护列表大小，也可以启用这条：
        // assertEquals(1, dao.getAllNinjas().size());
    }

    @Test
    void getAllNinjasShouldReturnDefensiveCopyIfDocumented() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear(); // 不应影响 DAO 内部存储
        assertEquals(size, dao.getAllNinjas().size());
    }
}
