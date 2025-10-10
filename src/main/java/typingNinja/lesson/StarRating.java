package typingNinja.lesson;

public final class StarRating {
    private StarRating() {}

    public static double compute(double wpm, double accuracyPercent, int errors) {
        double accScore = clamp01(accuracyPercent / 100.0);   // 0..1
        double speedScore = clamp01(wpm / 80.0);              // 0..1 (80 WPM ~ “full speed” for scoring)
        double raw = (accScore * 0.60 + speedScore * 0.40) * 5.0;

        // small penalty for many errors (max -0.5)
        double penalty = Math.min(errors / 50.0, 0.5);
        double stars = Math.max(0.0, raw - penalty);

        // round to 2 decimals for nicer storage
        return Math.round(stars * 100.0) / 100.0;
    }
    private static double clamp01(double x) {
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }
}
