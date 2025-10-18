package typingNinja.model.auth;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Singleton-style holder for light-weight session state reused across controllers.
 */
public class Session {
    private static volatile int currentUserId = 1;

    /**
     * @return id of the authenticated user for the current session
     */
    public static int getCurrentUserId() {
        // UI code calls this constantly, so keep it lightweight.
        return currentUserId;
    }
    /**
     * Updates the current user id tracked in memory.
     */
    public static void setCurrentUserId(int id) {
        // Swap the active user id; tests reset this between scenarios.
        currentUserId = id;
    }

    private static volatile Map<String, Integer> latestTotals = new LinkedHashMap<>();
    /**
     * @return immutable view of the most recent per-key error counts
     */
    public static Map<String, Integer> getLatestTotals() {
        // Expose the per-key error tally captured at lesson completion.
        return latestTotals;
    }
    /**
     * Replaces the stored per-key error totals.
     */
    public static void setLatestTotals(Map<String, Integer> m) {
        // Defensive copy keeps downstream code from mutating our shared map.
        latestTotals = (m == null) ? new LinkedHashMap<>() : m;
    }
}
