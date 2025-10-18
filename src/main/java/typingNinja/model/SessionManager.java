package typingNinja.model;

public class SessionManager {
    private static int currentUserId = -1;
    private static String currentUsername = null;
    private static String currentPassword = null;

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
        currentUserId = -1;
        currentUsername = null;
    }

    public static void setCurrentPassword(String password) {
        currentPassword = password;
    }

    public static String getCurrentPassword() {
        return currentPassword;
    }

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
