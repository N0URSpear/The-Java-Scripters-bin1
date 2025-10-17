package typingNinja.controllers.lesson;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;

/**
 * Helper around JavaFX progress bars used to display lesson time remaining.
 */
public class ProgressBar {
    private final javafx.scene.control.ProgressBar bar;

    /**
     * @param bar JavaFX progress bar that should mirror lesson progress
     */
    public ProgressBar(javafx.scene.control.ProgressBar bar) {
        // Keep a reference so we can own the binding lifecycle from controller code.
        this.bar = bar;
    }

    /**
     * Binds the progress bar to the remaining time in seconds.
     *
     * @param timeRemaining observable seconds remaining
     * @param lessonSeconds total duration of the lesson in seconds
     */
    public void bindTo(IntegerProperty timeRemaining, int lessonSeconds) {
        // Bind the control to a capped ratio so the bar never creeps beyond 0..1.
        bar.progressProperty().bind(Bindings.createDoubleBinding(
            () -> Math.max(0.0, Math.min(1.0, timeRemaining.get() / (double) lessonSeconds)),
            timeRemaining
        ));
    }
}
