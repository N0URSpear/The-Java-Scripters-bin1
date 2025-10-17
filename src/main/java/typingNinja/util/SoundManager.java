package typingNinja.util;

import javafx.scene.media.AudioClip;

/**
 * Central sound manager that keeps audio clips warm across scene changes.
 */
public final class SoundManager {
    private static AudioClip lessonCompleteClip;

    static {
        // Load once so subsequent controllers can reuse the same clip.
        try {
            var url = SoundManager.class.getResource("/typingNinja/Sounds/lesson_complete.mp3");
            if (url != null) {
                lessonCompleteClip = new AudioClip(url.toExternalForm());
            }
        } catch (Exception ignored) {}
    }

    private SoundManager() {}

    /**
     * Plays the lesson complete audio cue if the clip loaded successfully.
     */
    public static void playLessonComplete() {
        // Fire-and-forget helper used when a lesson wraps up.
        if (lessonCompleteClip != null) {
            lessonCompleteClip.play();
        }
    }
}
