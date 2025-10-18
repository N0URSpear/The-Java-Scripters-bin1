package typingNinja.tests.model.lesson;

import org.junit.jupiter.api.Test;
import typingNinja.model.MainLessonDAO;
import typingNinja.model.lesson.Lesson;
import typingNinja.model.lesson.LessonDAO;
import typingNinja.tests.support.DatabaseTestHarness;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LessonDaoIntegrationTest extends DatabaseTestHarness {

    @Test
    void insertSelection_createsBasicLessonRow() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "1b");

        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest, "Expected a lesson row to be created");
        assertEquals("1b", latest.getLessonType());
        assertEquals(0, latest.getDurationMinutes(), "Default duration should be zero minutes");
        assertNull(latest.getPrompt(), "Plain selections should not set a prompt");
    }

    @Test
    void insertCustomTopic_persistsAllCustomFlags() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertCustomTopic(1, "Space travel", 3, true, false, true, false);

        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest);
        assertEquals("CustomTopic", latest.getLessonType());
        assertEquals("Space travel", latest.getPrompt());
        assertEquals(3, latest.getDurationMinutes());
        assertTrue(latest.isUpperCase());
        assertFalse(latest.isNumbers());
        assertTrue(latest.isPunctuation());
        assertFalse(latest.isSpecialChars());
    }

    @Test
    void insertFreeType_setsLessonTypeAndDuration() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertFreeType(1, 7);

        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest);
        assertEquals("FreeType", latest.getLessonType());
        assertEquals(7, latest.getDurationMinutes());
        assertFalse(latest.isUpperCase(), "Free mode defaults to relaxed character filters");
    }

    @Test
    void markStarted_setsTimestamp() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "1c");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest);

        new LessonDAO().markStarted(latest.getLessonId(), 1);

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT DateStarted FROM Lesson WHERE LessonID = ?")) {
            ps.setInt(1, latest.getLessonId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                String started = rs.getString(1);
                assertNotNull(started);
                assertFalse(started.isBlank(), "DateStarted should be populated");
            }
        }
    }

    @Test
    void markCompleted_updatesSummaryFields() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "1d");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest);

        LessonDAO lessonDAO = new LessonDAO();
        lessonDAO.markStarted(latest.getLessonId(), 1);
        lessonDAO.markCompleted(latest.getLessonId(), 1,
                4.5, 72.0, 96.4, 3, "Co As");

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT DateCompleted, StarRating, WPM, Accuracy, ErrorAmount, WeakKeys " +
                             "FROM Lesson WHERE LessonID = ?")) {
            ps.setInt(1, latest.getLessonId());
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertNotNull(rs.getString("DateCompleted"));
                assertEquals(4.5, rs.getDouble("StarRating"), 1e-6);
                assertEquals(72.0, rs.getDouble("WPM"), 1e-6);
                assertEquals(96.4, rs.getDouble("Accuracy"), 1e-6);
                assertEquals(3, rs.getInt("ErrorAmount"));
                assertEquals("Co As", rs.getString("WeakKeys"));
            }
        }
    }

    @Test
    void deleteIfNotCompleted_removesInProgressLesson() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "1e");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);

        new LessonDAO().deleteIfNotCompleted(latest.getLessonId(), 1);

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Lesson")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1), "Pending lesson should be removed");
            }
        }
    }

    @Test
    void deleteIfNotCompleted_leavesCompletedLessonIntact() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "1f");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        LessonDAO lessonDAO = new LessonDAO();
        lessonDAO.markStarted(latest.getLessonId(), 1);
        lessonDAO.markCompleted(latest.getLessonId(), 1,
                5.0, 80.0, 98.0, 1, "Ty");

        lessonDAO.deleteIfNotCompleted(latest.getLessonId(), 1);

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Lesson")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "Completed lessons must remain in history");
            }
        }
    }

    @Test
    void topWeakPairs_returnsMostFrequentCompletedPairs() throws Exception {
        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT INTO Lesson (UserID, LessonType, WeakKeys, DateCompleted) VALUES (1, 'CustomTopic', ?, datetime('now'))")) {
            ps.setString(1, "Co As Co");
            ps.executeUpdate();
            ps.setString(1, "As Co Ty");
            ps.executeUpdate();
            ps.setString(1, "Ty Co");
            ps.executeUpdate();
            ps.setString(1, "Invalid token");
            ps.executeUpdate();
        }

        LessonDAO dao = new LessonDAO();
        List<String> top = dao.topWeakPairsForUserFromCompletedCustomLessons(1, 3);
        assertEquals(List.of("Co", "As", "Ty"), top);
    }
}
