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
        this.lessonSeconds = lessonSeconds;
        this.timeRemaining.set(lessonSeconds);
        buildTimer();
    }

    private void buildTimer() {
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
        if (timeRemaining.get() <= 0) reset();
        ended = false;
        timer.playFromStart();
    }

    public void pause() {
        timer.pause();
    }

    public void resume() {
        timer.play();
    }

    public void reset() {
        timer.stop();
        ended = false;
        timeRemaining.set(lessonSeconds);
        charsTyped.set(0);
        errors.set(0);
        wpm.set(0);
        errorRatePercent.set(0);
    }

    public void incTyped(boolean correct) {
        charsTyped.set(charsTyped.get() + 1);
        if (!correct) errors.set(errors.get() + 1);
        int secondsElapsed = lessonSeconds - timeRemaining.get();
        recomputeDerived(Math.max(1, secondsElapsed));
    }

    public void decTypedIfBackspace(boolean wasError) {
        if (charsTyped.get() > 0) charsTyped.set(charsTyped.get() - 1);
        if (wasError && errors.get() > 0) errors.set(errors.get() - 1);
        int secondsElapsed = Math.max(1, lessonSeconds - timeRemaining.get());
        recomputeDerived(secondsElapsed);
    }

    private void recomputeDerived(int secondsElapsed) {
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
        timerLabel.textProperty().bind(timeRemaining.asString().concat(" Seconds Remaining"));
    }

    public void bindStats(Label wpmLabel, Label errorsLabel, Label accuracyLabel) {
        wpmLabel.textProperty().bind(wpm.asString());
        errorsLabel.textProperty().bind(errors.asString());
        accuracyLabel.textProperty().bind(errorRatePercent.asString().concat("%"));
    }

    public void onLessonEnd(Runnable r) {
        this.onEnd = (r != null) ? r : () -> {};
    }

    public void endLessonNow() {
        if (ended) return;
        ended = true;
        if (timer != null) {
            timer.stop();
        }
        timeRemaining.set(0);
        onEnd.run();
    }

    public int lessonSeconds() {
        return lessonSeconds;
    }

    public IntegerProperty timeRemainingProperty() {
        return timeRemaining;
    }

    public ReadOnlyIntegerProperty charsTypedProperty() {
        return charsTyped;
    }

    public ReadOnlyIntegerProperty errorsProperty() {
        return errors;
    }

    public int getWpm() { return wpm.get(); }

    public int getErrors() { return errors.get(); }

    public int getCharsTyped() { return charsTyped.get(); }

    /** Returns accuracy as 0..100 (%) */
    public double getAccuracyPercent() {
        int typed = getCharsTyped();
        if (typed <= 0) return 0.0;
        int correct = Math.max(typed - getErrors(), 0);
        return (correct * 100.0) / typed;
    }

    public void setCharsPerWord(double value) {
        if (value > 0) {
            this.charsPerWord = value;
            int secondsElapsed = Math.max(1, lessonSeconds - timeRemaining.get());
            recomputeDerived(secondsElapsed);
        }
    }
}
