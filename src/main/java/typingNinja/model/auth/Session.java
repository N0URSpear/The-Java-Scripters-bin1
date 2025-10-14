package typingNinja.model.auth;

import java.util.Map;

public class Session {
    private static volatile int currentUserId = 1;

    public static int getCurrentUserId() { return currentUserId; }
    public static void setCurrentUserId(int id) { currentUserId = id; }

    // ✅ 新增：缓存本次 lesson 的逐键错误统计（键名用大写 String）
    private static volatile Map<String,Integer> latestTotals;
    public static Map<String,Integer> getLatestTotals() { return latestTotals; }
    public static void setLatestTotals(Map<String,Integer> m) { latestTotals = m; }
}
