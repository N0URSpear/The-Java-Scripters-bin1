package com.example.addressbook;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = null;
    private static String currentPassword = null;

    // ✅ 临时保存明文答案
    private static String currentSecretAnswer1 = null;
    private static String currentSecretAnswer2 = null;

    public static void setUser(int userId, String username) {
        currentUserId = userId;
        currentUsername = username;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void clearSession() {
        // ❌ 不再清除密码和答案
        currentUserId = -1;
        currentUsername = null;
        // 保留这些，让程序退出前都还在内存中
    }

    // ✅ 保存明文密码（仅当前会话）
    public static void setCurrentPassword(String password) {
        currentPassword = password;
    }

    public static String getCurrentPassword() {
        return currentPassword;
    }

    // ✅ 保存明文密保答案（仅当前会话）
    public static void setCurrentSecretAnswers(String a1, String a2) {
        currentSecretAnswer1 = a1;
        currentSecretAnswer2 = a2;
    }

    public static String getCurrentSecretAnswer1() {
        return currentSecretAnswer1;
    }

    public static String getCurrentSecretAnswer2() {
        return currentSecretAnswer2;
    }
}
