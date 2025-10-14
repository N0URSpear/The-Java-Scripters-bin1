package typingNinja.util;

import javafx.scene.media.AudioClip;

/**
 * Central sound manager to keep audio clips alive across scene switches.
 */
public final class SoundManager {
    private static AudioClip lessonCompleteClip;

    static {
        try {
            var url = SoundManager.class.getResource("/typingNinja/Sounds/lesson_complete.mp3");
            if (url != null) {
                lessonCompleteClip = new AudioClip(url.toExternalForm());
            }
        } catch (Exception ignored) {}
    }

    private SoundManager() {}

    public static void playLessonComplete() {
        if (lessonCompleteClip != null) {
            lessonCompleteClip.play();
        }
    }
}
