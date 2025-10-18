package typingNinja.tests.controllers;

import typingNinja.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import typingNinja.controllers.ProfilePageController;

public class ProfilePageControllerTest {

    private SqliteContactDAO dao;
    private ProfilePageController controller;
    private NinjaUser adminUser;

    @BeforeEach
    void setup() {
        dao = new SqliteContactDAO();
        controller = new ProfilePageController();

        adminUser = dao.getNinjaUser("admin");
        assertNotNull(adminUser, "Admin user must exist in the database");

        dao.safeInitUserData(adminUser.getId());

        SessionManager.setUser(adminUser.getId(), adminUser.getUserName());

        System.out.println("Setup complete: Session set to " +
                SessionManager.getCurrentUsername() + " (ID=" + SessionManager.getCurrentUserId() + ")");
    }

    @Test
    void testSessionManagerLinked() {
        assertEquals("admin", SessionManager.getCurrentUsername());
        assertEquals(adminUser.getId(), SessionManager.getCurrentUserId());
    }

    @Test
    void testDAO_ReturnsAdminData() {
        NinjaUser user = dao.getNinjaUser("admin");
        assertNotNull(user);
        assertEquals("admin", user.getUserName());
        System.out.println("DAO successfully loaded admin user from DB");
    }

    @Test
    void testProfilePageLoadsUser() {
        String uname = SessionManager.getCurrentUsername();
        NinjaUser user = dao.getNinjaUser(uname);

        assertNotNull(user, "User should not be null when loaded in ProfilePage");
        assertEquals("admin", user.getUserName(), "Loaded username should be 'admin'");
        System.out.println("ProfilePage successfully loaded current user: " + user.getUserName());
    }

    @Test
    void testProfilePageLoadsGoalsAndStats() {
        int userId = SessionManager.getCurrentUserId();
        var stats = dao.getUserGoalsAndStats(userId);

        assertNotNull(stats, "ProfileStats should not be null");
        System.out.println("Loaded stats for admin -> Belt=" + stats.getBelt() +
                ", Hours=" + stats.getEstHours() +
                ", WPM=" + stats.getEstWPM() +
                ", Accuracy=" + stats.getEstAccuracy());
    }

    @Test
    void testProfilePageDisplaysData() {
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

        System.out.println("ProfilePage Summary → " + summary);
        assertTrue(summary.contains("admin"), "Summary should include username 'admin'");
    }

    @AfterEach
    void teardown() {
        SessionManager.clearSession();
        System.out.println("Session cleared after test.");
    }
}
