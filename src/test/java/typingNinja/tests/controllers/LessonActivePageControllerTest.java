package typingNinja.tests.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import typingNinja.controllers.LessonActivePageController;
import typingNinja.model.MainLessonDAO;
import typingNinja.model.lesson.Lesson;
import typingNinja.model.lesson.LessonDAO;
import typingNinja.tests.support.DatabaseTestHarness;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class LessonActivePageControllerTest extends DatabaseTestHarness {

    @BeforeAll
    static void initToolkit() {
        if (Platform.isFxApplicationThread()) {
            return;
        }
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await(5, TimeUnit.SECONDS);
        } catch (IllegalStateException alreadyStarted) {
            // JavaFX runtime is already running
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while starting JavaFX platform", ie);
        }
    }

    @Test
    void cancelActiveLesson_removesPendingLessonFromDatabase() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "2a");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        assertNotNull(latest);

        LessonActivePageController controller = new LessonActivePageController();
        setField(controller, "currentLessonId", latest.getLessonId());
        setField(controller, "currentUserId", 1);

        invoke(controller, "cancelActiveLesson");

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Lesson")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void cancelActiveLesson_doesNotRemoveCompletedLesson() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertSelection(1, "2b");
        Lesson latest = new LessonDAO().fetchLatestForUser(1);
        LessonDAO dao = new LessonDAO();
        dao.markStarted(latest.getLessonId(), 1);
        dao.markCompleted(latest.getLessonId(), 1,
                4.0, 65.0, 95.0, 2, "Co");

        LessonActivePageController controller = new LessonActivePageController();
        setField(controller, "currentLessonId", latest.getLessonId());
        setField(controller, "currentUserId", 1);

        invoke(controller, "cancelActiveLesson");

        try (Connection conn = connection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Lesson")) {
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(1, rs.getInt(1), "Completed lesson should remain recorded");
            }
        }
    }

    @Test
    void initialize_withFreeTypeLesson_enablesFreeMode() throws Exception {
        MainLessonDAO mainDao = new MainLessonDAO();
        mainDao.insertFreeType(1, 5);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/typingNinja/LessonActivePage.fxml"));
        Parent root = loader.load();
        assertNotNull(root);
        LessonActivePageController controller = loader.getController();
        assertNotNull(controller);

        waitForFxEvents();

        boolean freeMode = getBooleanField(controller, "freeMode");
        assertTrue(freeMode, "FreeType lesson should put controller into free mode");

        typingNinja.model.lesson.Metrics metrics =
                (typingNinja.model.lesson.Metrics) getField(controller, "metrics");
        assertNotNull(metrics);
        assertEquals(300, metrics.lessonSeconds(), "FreeType duration minutes should convert to seconds");
    }

    private void invoke(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private Object getField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private boolean getBooleanField(Object target, String fieldName) throws Exception {
        return (boolean) getField(target, fieldName);
    }

    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        latch.await(2, TimeUnit.SECONDS);
    }
}
