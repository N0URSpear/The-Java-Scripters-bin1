package com.example.addressbook.auth;

public class Session {
    private static volatile int currentUserId = 1;

    public static int getCurrentUserId() { return currentUserId; }
    public static void setCurrentUserId(int id) { currentUserId = id; }
}
