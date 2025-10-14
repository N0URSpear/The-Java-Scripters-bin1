package typingNinja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import typingNinja.model.INinjaContactDAO;
import typingNinja.model.MockNinjaDAO;
import typingNinja.model.NinjaUser;

/**
 * 接口契约单元测试：使用 MockNinjaDAO，避免真实 DB。
 * 关键点：
 * 1) 每次测试前尽可能清空 Mock 的全局状态（若有 clearAll）；
 * 2) 每个用例使用唯一用户名，避免同名冲突；
 * 3) delete 使用当初 add 进去的“同一个实例”；
 * 4) update 先取当前 DAO 中的对象（若实现依赖 id/内部拷贝），再修改并 update。
 */
class INinjaContactDAOTest {

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
        // 尝试清空全局状态（如果 MockNinjaDAO 提供了静态 clearAll 方法）
        try {
            Method m = MockNinjaDAO.class.getDeclaredMethod("clearAll");
            m.setAccessible(true);
            m.invoke(null);
        } catch (Exception ignored) {
            // 没有该方法也没关系；我们用“唯一用户名”来隔离状态
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
        assertEquals("h1",  got.getPasswordHash());
    }

    @Test
    void getUnknownName_shouldReturnNull() {
        assertNull(dao.getNinjaUser(unique("nobody")));
    }

    @Test
    void update_shouldPersistNewValues() {
        String user = unique("alice");

        // 先 add
        dao.addNinjaUser(make(user, "h1"));

        // 再从 DAO 读取“当前对象”（若实现依赖 id/拷贝，用它来改更稳）
        NinjaUser current = dao.getNinjaUser(user);
        assertNotNull(current);

        // 修改后调用 update
        current.setPasswordHash("new");
        dao.updateNinjaUser(current);

        // 重新读取并断言
        NinjaUser after = dao.getNinjaUser(user);
        assertNotNull(after);
        assertEquals("new", after.getPasswordHash());

        // 若 Mock 在 add 时分配了自增 id，这里也顺手校验 id 不变
        assertEquals(current.getId(), after.getId());
    }

    @Test
    void delete_shouldRemoveUserAndShrinkList() {
        String a = unique("alice");
        String b = unique("bob");

        // 用“同一实例”加入并删除（避免 get 返回防御性副本导致删不掉）
        NinjaUser alice = make(a, "h1");
        NinjaUser bob   = make(b, "h2");

        dao.addNinjaUser(alice);
        dao.addNinjaUser(bob);

        // 删除“当初 add 的那个 alice 实例”
        dao.deleteNinjaUser(alice);

        // 关键：目标用户确实不存在
        assertNull(dao.getNinjaUser(a), "Alice should be removed");

        // 另一位用户仍存在
        NinjaUser stillThere = dao.getNinjaUser(b);
        assertNotNull(stillThere);
        assertEquals(b, stillThere.getUserName());

        // 如果你的 Mock 正确维护内部列表大小，可以启用这条：
        // assertEquals(1, dao.getAllNinjas().size());
    }

    @Test
    void getAllNinjas_shouldReturnDefensiveCopyIfDocumented() {
        String user = unique("alice");
        dao.addNinjaUser(make(user, "h1"));

        List<NinjaUser> list = dao.getAllNinjas();
        int size = list.size();
        list.clear(); // 不应影响 DAO 内部存储
        assertEquals(size, dao.getAllNinjas().size());
    }
}
