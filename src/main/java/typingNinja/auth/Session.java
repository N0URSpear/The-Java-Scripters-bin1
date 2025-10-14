package typingNinja.auth;

import java.util.Map;
import java.util.LinkedHashMap;

public class Session {
    private static volatile int currentUserId = 1;

    public static int getCurrentUserId() {
        return currentUserId;
    }
    public static void setCurrentUserId(int id) {
        currentUserId = id;
    }

    //缓存本次 lesson 的逐键错误统计
    private static volatile Map<String, Integer> latestTotals = new LinkedHashMap<>();
    public static Map<String, Integer> getLatestTotals() {
        return latestTotals;
    }
    public static void setLatestTotals(Map<String, Integer> m) {
        latestTotals = (m == null) ? new LinkedHashMap<>() : m;
    }
}