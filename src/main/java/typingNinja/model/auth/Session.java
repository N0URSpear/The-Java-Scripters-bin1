package typingNinja.model.auth;

import java.util.Map;
import java.util.LinkedHashMap;

public class Session {
    private static volatile int currentUserId = 1;

    public static int getCurrentUserId() {
        // UI code calls this constantly, so keep it lightweight.
        return currentUserId;
    }
    public static void setCurrentUserId(int id) {
        // Swap the active user id; tests reset this between scenarios.
        currentUserId = id;
    }

    private static volatile Map<String, Integer> latestTotals = new LinkedHashMap<>();
    public static Map<String, Integer> getLatestTotals() {
        // Expose the per-key error tally captured at lesson completion.
        return latestTotals;
    }
    public static void setLatestTotals(Map<String, Integer> m) {
        // Defensive copy keeps downstream code from mutating our shared map.
        latestTotals = (m == null) ? new LinkedHashMap<>() : m;
    }
}
