package typingNinja.lesson;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;

public class ProgressBar {
    private final javafx.scene.control.ProgressBar bar;

    public ProgressBar(javafx.scene.control.ProgressBar bar) {
    this.bar = bar;
    }

    public void bindTo(IntegerProperty timeRemaining, int lessonSeconds) {
        bar.progressProperty().bind(Bindings.createDoubleBinding(
            () -> Math.max(0.0, Math.min(1.0, timeRemaining.get() / (double) lessonSeconds)),
            timeRemaining
        ));
    }
}
