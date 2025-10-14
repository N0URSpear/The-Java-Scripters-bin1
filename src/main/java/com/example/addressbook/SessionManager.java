package com.example.addressbook;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = null;

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
        currentUserId = -1;
        currentUsername = null;
    }
}