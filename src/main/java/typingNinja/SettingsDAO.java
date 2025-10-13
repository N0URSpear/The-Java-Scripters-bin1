package typingNinja;

import java.sql.*;
import java.util.Set;

public class SettingsDAO {

    private static final String DB_URL = "jdbc:sqlite:TypingNinjaSQL.db";
    private static final Set<String> ALLOWED =
            Set.of("DisplayLanguage","Theme","FontSize",
                    "KeyboardSounds","TypingErrors","TypingErrorSounds","LessonCompleteSound");

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void ensureRow(int userId) {
        String sql = "INSERT INTO Settings (UserID, DisplayLanguage, Theme, FontSize, " +
                "KeyboardSounds, TypingErrors, TypingErrorSounds, LessonCompleteSound) " +
                "SELECT ?, 'English', 'Dark Blue', 'Medium', 1, 1, 1, 1 " +
                "WHERE NOT EXISTS (SELECT 1 FROM Settings WHERE UserID = ?)";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public SettingsRecord fetch(int userId) {
        ensureRow(userId);
        String sql = "SELECT DisplayLanguage, Theme, FontSize, " +
                "KeyboardSounds, TypingErrors, TypingErrorSounds, LessonCompleteSound " +
                "FROM Settings WHERE UserID = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new SettingsRecord(
                            rs.getString("DisplayLanguage"),
                            rs.getString("Theme"),
                            rs.getString("FontSize"),
                            rs.getInt("KeyboardSounds") == 1,
                            rs.getInt("TypingErrors") == 1,
                            rs.getInt("TypingErrorSounds") == 1,
                            rs.getInt("LessonCompleteSound") == 1
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new SettingsRecord("English","Dark Blue","Medium", true,true,true,true);
    }

    public void update(int userId, String column, Object value) {
        if (!ALLOWED.contains(column)) return;

        String sql = "UPDATE Settings SET " + column + " = ? WHERE UserID = ?";
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (value instanceof Boolean) {
                ps.setInt(1, ((Boolean) value) ? 1 : 0);
            } else {
                ps.setString(1, String.valueOf(value));
            }
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Simple DTO
    public static class SettingsRecord {
        public final String displayLanguage, theme, fontSize;
        public final boolean keyboardSounds, typingErrors, typingErrorSounds, lessonCompleteSound;
        public SettingsRecord(String displayLanguage, String theme, String fontSize,
                              boolean keyboardSounds, boolean typingErrors,
                              boolean typingErrorSounds, boolean lessonCompleteSound) {
            this.displayLanguage = displayLanguage;
            this.theme = theme;
            this.fontSize = fontSize;
            this.keyboardSounds = keyboardSounds;
            this.typingErrors = typingErrors;
            this.typingErrorSounds = typingErrorSounds;
            this.lessonCompleteSound = lessonCompleteSound;
        }
    }
}
