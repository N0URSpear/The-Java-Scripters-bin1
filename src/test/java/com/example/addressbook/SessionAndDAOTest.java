package com.example.addressbook;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SessionAndDAOTest {

    private SqliteContactDAO dao;

    @BeforeEach
    void setup() {
        dao = new SqliteContactDAO();
    }

    // -------------------- Test 1: SessionManager --------------------
    @Test
    void testSessionManagerStoresUser() {
        SessionManager.setUser(1, "admin");
        assertEquals(1, SessionManager.getCurrentUserId());
        assertEquals("admin", SessionManager.getCurrentUsername());

        SessionManager.clearSession();
        assertEquals(-1, SessionManager.getCurrentUserId());
        assertNull(SessionManager.getCurrentUsername());
    }

    // -------------------- Test 2: 读取数据库用户 --------------------
    @Test
    void testGetNinjaUser_Admin() {
        NinjaUser user = dao.getNinjaUser("admin");
        assertNotNull(user, "User 'admin' should exist in database");
        assertEquals("admin", user.getUserName());
        System.out.println("✅ Loaded user: " + user.getUserName() + " (ID: " + user.getId() + ")");
    }

    // -------------------- Test 3: 初始化 Goals / Statistics --------------------
    @Test
    void testSafeInitUserData_Admin() {
        NinjaUser admin = dao.getNinjaUser("admin");
        assertNotNull(admin, "Admin must exist");

        dao.safeInitUserData(admin.getId());
        var stats = dao.getUserGoalsAndStats(admin.getId());
        assertNotNull(stats, "ProfileStats should not be null after safeInitUserData");

        System.out.println("✅ Goals/Stats initialized for admin (UserID=" + admin.getId() + ")");
    }

    // -------------------- Test 4: 更新目标 --------------------
    @Test
    void testUpdateGoals_Admin() {
        NinjaUser admin = dao.getNinjaUser("admin");
        assertNotNull(admin);

        dao.safeInitUserData(admin.getId());
        dao.updateGoals(admin.getId(), 12, 90, 98);

        var stats = dao.getUserGoalsAndStats(admin.getId());
        assertNotNull(stats);

        System.out.println("✅ Updated goals for admin -> Hours=" + stats.getEstHours() +
                ", WPM=" + stats.getEstWPM() + ", Accuracy=" + stats.getEstAccuracy());
    }

    // -------------------- Test 5: 重新计算统计数据 --------------------
    @Test
    void testRecalcUserStatistics_Admin() {
        NinjaUser admin = dao.getNinjaUser("admin");
        assertNotNull(admin);

        dao.safeInitUserData(admin.getId());
        dao.recalcUserStatistics(admin.getId());

        var stats = dao.getUserGoalsAndStats(admin.getId());
        assertNotNull(stats);

        System.out.println("✅ Recalculated statistics for admin -> Lessons=" + stats.getTotalLessons() +
                ", AvgWPM=" + stats.getAvgWPM() + ", Belt=" + stats.getBelt());
    }
}
