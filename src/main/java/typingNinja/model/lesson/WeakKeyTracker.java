package typingNinja.model.lesson;

import java.util.*;

/**
 * Captures per-key and contextual mistake statistics during structured lessons.
 */
public class WeakKeyTracker {
    private final Map<Character, Integer> totals = new HashMap<>();
    private final Map<Character, Map<Character, Integer>> misinputs = new HashMap<>();
    private final Map<String, Map<Character, Integer>> ctx = new HashMap<>();
    public static final List<Character> ALL_KEYS;
    static {
        // Precompute a stable ordering of trackable characters.
        List<Character> keys = new ArrayList<>();
        keys.add(' ');
        for (char c = 33; c <= 126; c++) {
            if (trackable(c)) keys.add(c);
        }

        ALL_KEYS = Collections.unmodifiableList(keys);
    }

    /**
     * Records a mistake for the expected character under the supplied context.
     *
     * @param prevExpected character that preceded the mistake (nullable)
     * @param expected character that should have been typed
     * @param actual character that was actually typed
     */
    public void record(Character prevExpected, char expected, char actual) {
        // Count this mistake by key, by misinput, and by the two-character context.
        if (!trackable(expected) || !trackable(actual)) return;
        totals.merge(expected, 1, Integer::sum);
        misinputs.computeIfAbsent(expected, k -> new HashMap<>())
                .merge(actual, 1, Integer::sum);
        String key = key(prevExpected, expected);
        ctx.computeIfAbsent(key, k -> new HashMap<>())
                .merge(actual, 1, Integer::sum);
    }

    /**
     * Indicates whether the tracker cares about the supplied character.
     */
    public static boolean trackable(char c) {
        // Ignore control characters so we stay focused on printable keys.
        if (Character.isISOControl(c)) return false;
        return !(c == '\n' || c == '\r' || c == '\t');
    }

    private static String key(Character prev, char expected) {
        // Pack the previous character and expected key into a simple map key.
        return (prev == null ? "\u0000" : String.valueOf(prev)) + "|" + expected;
    }

    /**
     * Builds the list of most troublesome expected/misinput pairs.
     *
     * @param k maximum number of pairs to include
     * @return space-separated pairs, padded with {@code "--"}
     */
    public String topPairsString(int k) {
        // Produce the most troublesome expected/misinput pairs for quick summaries.
        List<String> pairs = new ArrayList<>(k);

        List<Map.Entry<Character,Integer>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort((a,b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            return cmp != 0 ? cmp : Character.compare(a.getKey(), b.getKey());
        });

        for (var e : sorted) {
            if (pairs.size() >= k) break;
            char expected = e.getKey();
            Character mis = bestMisinput(expected);
            if (mis != null) pairs.add("" + expected + mis);
        }
        while (pairs.size() < k) pairs.add("--");
        return String.join(" ", pairs);
    }

    /**
     * Builds the list of most troublesome previous/expected combinations.
     *
     * @param k maximum number of combinations to include
     * @return space-separated pairs, padded with {@code "--"}
     */
    public String topPrevExpectedPairsString(int k) {
        // Focus on two-character contexts so we can craft smarter free-typing prompts.
        java.util.List<String> out = new java.util.ArrayList<>(k);

        java.util.List<java.util.Map.Entry<Character,Integer>> sorted =
                new java.util.ArrayList<>(totals.entrySet());
        sorted.sort((a,b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            return (cmp != 0) ? cmp : Character.compare(a.getKey(), b.getKey());
        });

        for (var e : sorted) {
            if (out.size() >= k) break;
            char expected = e.getKey();
            Character bestPrev = bestPrevForExpected(expected);
            if (bestPrev != null) {
                out.add("" + bestPrev + expected);
            }
        }

        while (out.size() < k) out.add("--");
        return String.join(" ", out);
    }

    /** Find the prev char that maximizes total mistakes at (prev, expected), summing across misinputs. */
    private Character bestPrevForExpected(char expected) {
        // Look for the preceding character that leads to the most mistakes on this key.
        int bestCount = -1;
        Character bestPrev = null;
        String suffix = "|" + expected;

        for (var entry : ctx.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith(suffix)) continue;

            char prev = key.charAt(0);
            if (!trackable(prev)) continue;

            int totalHere = 0;
            for (int v : entry.getValue().values()) totalHere += v;

            if (totalHere > bestCount || (totalHere == bestCount && (bestPrev == null || prev < bestPrev))) {
                bestCount = totalHere;
                bestPrev = prev;
            }
        }
        return bestPrev;
    }


    private Character bestMisinput(char expected) {
        // Tie-break multiple misinputs by checking which context sees them most often.
        Map<Character,Integer> counts = misinputs.get(expected);
        if (counts == null || counts.isEmpty()) return null;

        int max = counts.values().stream().max(Integer::compareTo).orElse(0);
        List<Character> tied = new ArrayList<>();
        for (var e : counts.entrySet()) if (e.getValue() == max) tied.add(e.getKey());
        if (tied.size() == 1) return tied.get(0);

        int bestScore = -1;
        Character best = null;
        String suffix = "|" + expected;
        for (char candidate : tied) {
            int candidateBest = 0;
            for (var ctxEntry : ctx.entrySet()) {
                if (ctxEntry.getKey().endsWith(suffix)) {
                    candidateBest = Math.max(candidateBest, ctxEntry.getValue().getOrDefault(candidate, 0));
                }
            }
            if (candidateBest > bestScore || (candidateBest == bestScore && (best == null || candidate < best))) {
                bestScore = candidateBest;
                best = candidate;
            }
        }
        return best;
    }
    /** @return immutable view of total mistakes per expected key */
    public Map<Character, Integer> totals() { return Collections.unmodifiableMap(totals); }
    /** @return immutable view of misinput breakdowns per expected key */
    public Map<Character, Map<Character, Integer>> misinputs() { return Collections.unmodifiableMap(misinputs); }
    /**
     * @return formatted diagnostic string containing totals, misinputs, and context data
     */
    public String debugDump() {
        // Handy textual report for console debugging and unit tests.
        StringBuilder sb = new StringBuilder();

        sb.append("=== Weak Key Totals (expected -> count) ===\n");
        var totalMap = totalsAllKeys();
        var totalList = new ArrayList<>(totalMap.entrySet());
        totalList.sort((a,b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue());
            return (cmp != 0) ? cmp : Character.compare(a.getKey(), b.getKey());
        });
        for (var e : totalList) {
            sb.append(e.getKey()).append(" : ").append(e.getValue()).append('\n');
        }

        sb.append("\n=== Misinputs per Expected (expected -> misinput=count, ...) ===\n");
        for (var e : totalList) {
            char expected = e.getKey();
            var mm = misinputs.get(expected);
            if (mm == null || mm.isEmpty()) continue;
            var list = new ArrayList<>(mm.entrySet());
            list.sort((x,y) -> {
                int cmp = Integer.compare(y.getValue(), x.getValue());
                return (cmp != 0) ? cmp : Character.compare(x.getKey(), y.getKey());
            });
            sb.append(expected).append(" -> ");
            boolean first = true;
            for (var m : list) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(m.getKey()).append('=').append(m.getValue());
            }
            sb.append('\n');
        }

        sb.append("\n=== Context (prev|expected -> misinput=count, ...) ===\n");
        var ctxKeys = new ArrayList<>(ctx.keySet());
        Collections.sort(ctxKeys);
        for (String k : ctxKeys) {
            var m = ctx.get(k);
            if (m == null || m.isEmpty()) continue;
            var list = new ArrayList<>(m.entrySet());
            list.sort((x,y) -> {
                int cmp = Integer.compare(y.getValue(), x.getValue());
                return (cmp != 0) ? cmp : Character.compare(x.getKey(), y.getKey());
            });
            sb.append(k).append(" -> ");
            boolean first = true;
            for (var e2 : list) {
                if (!first) sb.append(", ");
                first = false;
                sb.append(e2.getKey()).append('=').append(e2.getValue());
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Returns an ordered map containing every trackable key with a count.
     */
    public java.util.Map<Character, Integer> totalsAllKeys() {
        // Return a stable map covering every key so UI rendering is easy.
        java.util.LinkedHashMap<Character, Integer> out = new java.util.LinkedHashMap<>(ALL_KEYS.size());
        for (char c : ALL_KEYS) {
            out.put(c, totals.getOrDefault(c, 0));
        }
        return java.util.Collections.unmodifiableMap(out);
    }


}
