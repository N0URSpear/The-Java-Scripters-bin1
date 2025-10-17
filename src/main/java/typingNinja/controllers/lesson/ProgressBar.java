package typingNinja.controllers.lesson;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;

public class ProgressBar {
    private final javafx.scene.control.ProgressBar bar;

    public ProgressBar(javafx.scene.control.ProgressBar bar) {
        // Keep a reference so we can own the binding lifecycle from controller code.
        this.bar = bar;
    }

    public void bindTo(IntegerProperty timeRemaining, int lessonSeconds) {
        // Bind the control to a capped ratio so the bar never creeps beyond 0..1.
        bar.progressProperty().bind(Bindings.createDoubleBinding(
            () -> Math.max(0.0, Math.min(1.0, timeRemaining.get() / (double) lessonSeconds)),
            timeRemaining
        ));
    }
}
