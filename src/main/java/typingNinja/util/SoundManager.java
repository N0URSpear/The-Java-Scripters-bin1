package typingNinja.util;

import javafx.scene.media.AudioClip;

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

    public static void playLessonComplete() {
        // Fire-and-forget helper used when a lesson wraps up.
        if (lessonCompleteClip != null) {
            lessonCompleteClip.play();
        }
    }
}
