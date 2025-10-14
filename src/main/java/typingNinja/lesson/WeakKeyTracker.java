package typingNinja.lesson;

import java.util.*;

/** Tracks per-key mistakes (case-sensitive), with tie-break by (prev,expected) context. */
public class WeakKeyTracker {

    /** total mistakes per expected key */
    private final Map<Character, Integer> totals = new HashMap<>();

    /** for each expected key -> (misinput key -> count) */
    private final Map<Character, Map<Character, Integer>> misinputs = new HashMap<>();

    /** context counts: (prevExpected, expected) -> (misinput -> count) */
    private final Map<String, Map<Character, Integer>> ctx = new HashMap<>();

    /** All printable ASCII keys except whitespace (space/newline/tab). Stable order. */
    public static final List<Character> ALL_KEYS;
    static {
        List<Character> keys = new ArrayList<>();
        keys.add(' ');                     // 先放一个空格
        for (char c = 33; c <= 126; c++) {
            if (trackable(c)) keys.add(c);
        }

        ALL_KEYS = Collections.unmodifiableList(keys);
    }

    /** Record a mistake. prevExpected may be null. */
    public void record(Character prevExpected, char expected, char actual) {
        if (!trackable(expected) || !trackable(actual)) return;
        totals.merge(expected, 1, Integer::sum);
        misinputs.computeIfAbsent(expected, k -> new HashMap<>())
                .merge(actual, 1, Integer::sum);
        String key = key(prevExpected, expected);
        ctx.computeIfAbsent(key, k -> new HashMap<>())
                .merge(actual, 1, Integer::sum);
    }

    public static boolean trackable(char c) {
        if (Character.isISOControl(c)) return false;
        return !(c == '\n' || c == '\r' || c == '\t');
    }

    private static String key(Character prev, char expected) {
        return (prev == null ? "\u0000" : String.valueOf(prev)) + "|" + expected;
    }

    public String topPairsString(int k) {
        List<String> pairs = new ArrayList<>(k);

        List<Map.Entry<Character,Integer>> sorted = new ArrayList<>(totals.entrySet());
        sorted.sort((a,b) -> {
            int cmp = Integer.compare(b.getValue(), a.getValue()); // highest error count first
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

    public String topPrevExpectedPairsString(int k) {
        java.util.List<String> out = new java.util.ArrayList<>(k);

        // Expected keys sorted by total mistakes (desc), then by key
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
        int bestCount = -1;
        Character bestPrev = null;
        String suffix = "|" + expected; // our ctx key is prev + "|" + expected

        for (var entry : ctx.entrySet()) {
            String key = entry.getKey();
            if (!key.endsWith(suffix)) continue;

            // prev is the first character of the key (we stored exactly one char for prev)
            char prev = key.charAt(0);
            if (!trackable(prev)) continue; // skip space/newline/control and our NULL sentinel

            // sum all misinputs at this context
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

    //expose maps for local inspection
    public Map<Character, Integer> totals() { return Collections.unmodifiableMap(totals); }
    public Map<Character, Map<Character, Integer>> misinputs() { return Collections.unmodifiableMap(misinputs); }
    /** Pretty debug dump of totals, misinputs, and context, sorted by counts. */
    public String debugDump() {
        StringBuilder sb = new StringBuilder();

        // --- Totals (expected -> count) ---
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

        // --- Misinputs per expected (expected -> misinput=count, ...), sorted desc by count
        sb.append("\n=== Misinputs per Expected (expected -> misinput=count, ...) ===\n");
        for (var e : totalList) { // iterate expected keys in the same order as totals
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

        // --- Context (prev|expected -> misinput=count, ...), sorted by key then count desc
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

    public java.util.Map<Character, Integer> totalsAllKeys() {
        java.util.LinkedHashMap<Character, Integer> out = new java.util.LinkedHashMap<>(ALL_KEYS.size());
        for (char c : ALL_KEYS) {
            out.put(c, totals.getOrDefault(c, 0));
        }
        return java.util.Collections.unmodifiableMap(out);
    }


}
