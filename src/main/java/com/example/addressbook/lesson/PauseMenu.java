package com.example.addressbook.lesson;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class PauseMenu {
    private final Button pauseBtn;
    private final Metrics metrics;
    private final TextArea input;
    private boolean paused = false;

    public PauseMenu(Button pauseBtn, Metrics metrics, TextArea input) {
        this.pauseBtn = pauseBtn;
        this.metrics = metrics;
        this.input = input;
        pauseBtn.setOnAction(e -> togglePause());
        render();
    }

    private void togglePause() {
        if (!paused) {
            paused = true;
            metrics.pause();
            input.setDisable(true);
            render();
        }
        else {
            pauseBtn.setText("Resuming in 3…");
            Timeline t = new Timeline(
                new KeyFrame(Duration.seconds(1), e -> pauseBtn.setText("Resuming in 2…")),
                new KeyFrame(Duration.seconds(2), e -> pauseBtn.setText("Resuming in 1…")),
                new KeyFrame(Duration.seconds(3), e -> {
                    paused = false;
                    input.setDisable(false);
                    input.requestFocus();
                    metrics.resume();
                    render();
                })
            );
            t.setCycleCount(1);
            t.playFromStart();
        }
    }

    private void render() {
    pauseBtn.setText(paused ? "▶ Resume" : "⏸ Pause");
    }
}
