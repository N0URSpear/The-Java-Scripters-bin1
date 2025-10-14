package typingNinja.model;

import java.util.List;
import java.util.Optional;

public final class ResultsBridge {
    /**
     * UI-facing metrics bundle holding parallel lists of WPM and accuracy.
     *
     * @param wpm list of typing speeds (words per minute), newest-first
     * @param acc list of accuracy percentages (0–100), aligned with {@code wpm}
     */
    public static record Metrics(List<Integer> wpm, List<Integer> acc) {}

    private ResultsBridge() {}

    public static void ensureTable() {
        ResultsRepository.ensureTable();
    }

    /**
     * Persist a single typing result to storage.
     *
     * @param wpm typing speed in words per minute
     * @param acc accuracy percentage (0–100)
     */
    public static void saveResult(int wpm, int acc) {
        ResultsRepository.saveResult(wpm, acc);
    }

    /**
     * Load the most recent {@code n} results.
     *
     * @param n maximum number of rows to load
     * @return metrics containing up to {@code n} items, newest-first
     */    public static Metrics loadLastN(int n) {
        var r = ResultsRepository.loadLastN(n);   // Repository 已处理四舍五入 / 用户过滤 / 排序
        return new Metrics(r.wpm(), r.acc());
    }

    /**
     * Convenience loader for all results (internally uses a very large LIMIT).
     *
     * @return metrics containing all available rows, newest-first
     */
    public static Metrics loadAll() {
        return loadLastN(Integer.MAX_VALUE);
    }

    /**
     * Get the latest single result for quick UI display.
     *
     * @return an Optional containing {@code new int[]{wpm, acc}} when present; empty otherwise
     */
    public static Optional<int[]> getLatest() {
        var r = ResultsRepository.loadLastN(1);
        if (r.wpm().isEmpty()) return Optional.empty();
        int w = r.wpm().get(0);
        int a = r.acc().get(0);
        return Optional.of(new int[]{ w, a });
    }
}
