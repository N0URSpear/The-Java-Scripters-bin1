package typingNinja.model.lesson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class Metrics {
    private final int lessonSeconds;
    private final IntegerProperty timeRemaining = new SimpleIntegerProperty();
    private final IntegerProperty charsTyped = new SimpleIntegerProperty(0);
    private final IntegerProperty errors = new SimpleIntegerProperty(0);
    private final IntegerProperty wpm = new SimpleIntegerProperty(0);
    private final IntegerProperty errorRatePercent = new SimpleIntegerProperty(0);
    private Timeline timer;
    private boolean ended = false;
    private double charsPerWord = 5.0;
    private Runnable onEnd = () -> {};

    public Metrics(int lessonSeconds) {
        // Core timer and counters shared across controllers.
        this.lessonSeconds = lessonSeconds;
        this.timeRemaining.set(lessonSeconds);
        buildTimer();
    }

    private void buildTimer() {
        // A one-second heartbeat drives the stats and signals when time runs out.
        timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            int t = timeRemaining.get() - 1;
            timeRemaining.set(t);
            recomputeDerived(lessonSeconds - t);
            if (t <= 0) {
                timer.stop();
                ended = true;
                onEnd.run();
            }
        }));
        timer.setCycleCount(lessonSeconds);
    }

    public void start() {
        // Play from the top, resetting if another run already burned through the timer.
        if (timeRemaining.get() <= 0) reset();
        ended = false;
        timer.playFromStart();
    }

    public void pause() {
        // Pause is cheap because Timeline handles the bookkeeping.
        timer.pause();
    }

    public void resume() {
        // Resume picks up where pause left off.
        timer.play();
    }

    public void reset() {
        // Restore counters and timer to their initial state.
        timer.stop();
        ended = false;
        timeRemaining.set(lessonSeconds);
        charsTyped.set(0);
        errors.set(0);
        wpm.set(0);
        errorRatePercent.set(0);
    }

    public void incTyped(boolean correct) {
        // Called for every keypress so we can keep WPM and error rate reactive.
        charsTyped.set(charsTyped.get() + 1);
        if (!correct) errors.set(errors.get() + 1);
        int secondsElapsed = lessonSeconds - timeRemaining.get();
        recomputeDerived(Math.max(1, secondsElapsed));
    }

    public void decTypedIfBackspace(boolean wasError) {
        // Rewind stats when the user backspaces, keeping error count honest.
        if (charsTyped.get() > 0) charsTyped.set(charsTyped.get() - 1);
        if (wasError && errors.get() > 0) errors.set(errors.get() - 1);
        int secondsElapsed = Math.max(1, lessonSeconds - timeRemaining.get());
        recomputeDerived(secondsElapsed);
    }

    private void recomputeDerived(int secondsElapsed) {
        // All the secondary metrics flow through here so updates stay consistent.
        double minutes = Math.max(1.0 / 60.0, secondsElapsed / 60.0);
        int correctChars = Math.max(0, charsTyped.get() - errors.get());
        double divisor = Math.max(1.0, charsPerWord);
        int computedWpm = (int) Math.round((correctChars / divisor) / minutes);
        wpm.set(Math.max(0, computedWpm));
        int typed = Math.max(1, charsTyped.get());
        int err = Math.max(0, errors.get());
        int rate = (int) Math.round((err * 100.0) / typed);
        errorRatePercent.set(rate);
    }

    public void bindTimerLabel(Label timerLabel) {
        // Simple binding so the UI auto-updates every tick.
        timerLabel.textProperty().bind(timeRemaining.asString().concat(" Seconds Remaining"));
    }

    public void bindStats(Label wpmLabel, Label errorsLabel, Label accuracyLabel) {
        // These bindings keep the dashboard tiles in sync with the numbers we track.
        wpmLabel.textProperty().bind(wpm.asString());
        errorsLabel.textProperty().bind(errors.asString());
        accuracyLabel.textProperty().bind(errorRatePercent.asString().concat("%"));
    }

    public void onLessonEnd(Runnable r) {
        // Consumers provide a callback to run once the timer hits zero.
        this.onEnd = (r != null) ? r : () -> {};
    }

    public void endLessonNow() {
        // Immediate stop used for early completion cases.
        if (ended) return;
        ended = true;
        if (timer != null) {
            timer.stop();
        }
        timeRemaining.set(0);
        onEnd.run();
    }

    public int lessonSeconds() {
        // Expose the configured duration for progress widgets.
        return lessonSeconds;
    }

    public IntegerProperty timeRemainingProperty() {
        // Allows progress bars and timers to bind directly.
        return timeRemaining;
    }

    public ReadOnlyIntegerProperty charsTypedProperty() {
        // Read-only view for UI components that chart typing volume.
        return charsTyped;
    }

    public ReadOnlyIntegerProperty errorsProperty() {
        // Read-only view for charts tracking mistakes over time.
        return errors;
    }

    // Convenience accessor for tests and summary views.
    public int getWpm() { return wpm.get(); }

    // Expose the current error count.
    public int getErrors() { return errors.get(); }

    // Raw typed character count including mistakes.
    public int getCharsTyped() { return charsTyped.get(); }

    public double getAccuracyPercent() {
        // Calculate accuracy on demand to avoid rounding artifacts in the bindings.
        int typed = getCharsTyped();
        if (typed <= 0) return 0.0;
        int correct = Math.max(typed - getErrors(), 0);
        return (correct * 100.0) / typed;
    }

    public void setCharsPerWord(double value) {
        // Free mode tweaks this so WPM reflects the looser pacing.
        if (value > 0) {
            this.charsPerWord = value;
            int secondsElapsed = Math.max(1, lessonSeconds - timeRemaining.get());
            recomputeDerived(secondsElapsed);
        }
    }
}
