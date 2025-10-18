package typingNinja.model.lesson;

/**
 * Utility for condensing performance metrics into a star rating.
 */
public final class StarRating {
    private StarRating() {}

    /**
     * Combines speed, accuracy, and error counts into a five-star rating.
     *
     * @param wpm words per minute scored
     * @param accuracyPercent accuracy percentage
     * @param errors total errors recorded
     * @return clamped star rating rounded to two decimals
     */
    public static double compute(double wpm, double accuracyPercent, int errors) {
        // Blend speed and accuracy, then shave a bit off if the student racked up mistakes.
        double accScore = clamp01(accuracyPercent / 100.0);
        double speedScore = clamp01(wpm / 80.0);
        double raw = (accScore * 0.60 + speedScore * 0.40) * 5.0;

        double penalty = Math.min(errors / 50.0, 0.5);
        double stars = Math.max(0.0, raw - penalty);

        return Math.round(stars * 100.0) / 100.0;
    }
    private static double clamp01(double x) {
        // Utility helper to keep weighting math within bounds.
        return x < 0 ? 0 : (x > 1 ? 1 : x);
    }
}
