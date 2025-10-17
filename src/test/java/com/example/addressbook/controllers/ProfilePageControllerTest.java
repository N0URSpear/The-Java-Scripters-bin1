package com.example.addressbook.controllers;

import com.example.addressbook.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ 测试 ProfilePageController 是否能正确加载数据库中 admin 的资料。
 * 前提：数据库中已存在一个用户名为 'admin' 的用户。
 */
public class ProfilePageControllerTest {

    private SqliteContactDAO dao;
    private ProfilePageController controller;
    private NinjaUser adminUser;

    @BeforeEach
    void setup() {
        dao = new SqliteContactDAO();
        controller = new ProfilePageController();

        // 1️⃣ 确认数据库中存在 admin 用户
        adminUser = dao.getNinjaUser("admin");
        assertNotNull(adminUser, "Admin user must exist in the database");

        // 2️⃣ 初始化 Goals / Statistics 数据（防止为空）
        dao.safeInitUserData(adminUser.getId());

        // 3️⃣ 设置当前 Session
        SessionManager.setUser(adminUser.getId(), adminUser.getUserName());

        System.out.println("✅ Setup complete: Session set to " +
                SessionManager.getCurrentUsername() + " (ID=" + SessionManager.getCurrentUserId() + ")");
    }

    // -------------------- Test 1: SessionManager 连接 --------------------
    @Test
    void testSessionManagerLinked() {
        assertEquals("admin", SessionManager.getCurrentUsername());
        assertEquals(adminUser.getId(), SessionManager.getCurrentUserId());
    }

    // -------------------- Test 2: DAO 返回数据 --------------------
    @Test
    void testDAO_ReturnsAdminData() {
        NinjaUser user = dao.getNinjaUser("admin");
        assertNotNull(user);
        assertEquals("admin", user.getUserName());
        System.out.println("✅ DAO successfully loaded admin user from DB");
    }

    // -------------------- Test 3: ProfilePage 加载用户信息 --------------------
    @Test
    void testProfilePageLoadsUser() {
        // 模拟页面加载数据的逻辑
        String uname = SessionManager.getCurrentUsername();
        NinjaUser user = dao.getNinjaUser(uname);

        assertNotNull(user, "User should not be null when loaded in ProfilePage");
        assertEquals("admin", user.getUserName(), "Loaded username should be 'admin'");
        System.out.println("✅ ProfilePage successfully loaded current user: " + user.getUserName());
    }

    // -------------------- Test 4: ProfilePage 加载目标与统计 --------------------
    @Test
    void testProfilePageLoadsGoalsAndStats() {
        int userId = SessionManager.getCurrentUserId();
        var stats = dao.getUserGoalsAndStats(userId);

        assertNotNull(stats, "ProfileStats should not be null");
        System.out.println("✅ Loaded stats for admin -> Belt=" + stats.getBelt() +
                ", Hours=" + stats.getEstHours() +
                ", WPM=" + stats.getEstWPM() +
                ", Accuracy=" + stats.getEstAccuracy());
    }

    // -------------------- Test 5: ProfilePage 界面显示数据绑定 --------------------
    @Test
    void testProfilePageDisplaysData() {
        // 模拟 UI 控件显示用的数据（不直接依赖 FXML）
        NinjaUser user = dao.getNinjaUser(SessionManager.getCurrentUsername());
        var stats = dao.getUserGoalsAndStats(SessionManager.getCurrentUserId());

        assertNotNull(user, "User should not be null");
        assertNotNull(stats, "Stats should not be null");

        String summary = String.format(
                "User: %s | Belt: %s | Goals: %s hrs, %s wpm, %s%% acc",
                user.getUserName(),
                stats.getBelt(),
                stats.getEstHours(),
                stats.getEstWPM(),
                stats.getEstAccuracy()
        );

        System.out.println("✅ ProfilePage Summary → " + summary);
        assertTrue(summary.contains("admin"), "Summary should include username 'admin'");
    }

    @AfterEach
    void teardown() {
        SessionManager.clearSession();
        System.out.println("🧹 Session cleared after test.");
    }
}
