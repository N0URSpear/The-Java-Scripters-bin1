package com.example.addressbook.lesson;

import com.example.addressbook.SqliteConnection;
import java.sql.*;

public class LessonDAO {
    private final Connection conn = SqliteConnection.getInstance();

    /** Fetch latest Lesson for the CURRENT user only (highest LessonID for that user). */
    public Lesson fetchLatestForUser(int userId) throws SQLException {
        String sql = """
      SELECT LessonID, UserID, LessonType, Prompt, LessonDuration,
             UpperCase, Numbers, Punctuation, SpecialChars
      FROM Lesson
      WHERE UserID = ?
      ORDER BY LessonID DESC
      LIMIT 1
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Lesson(
                        rs.getInt("LessonID"),
                        rs.getInt("UserID"),
                        rs.getString("LessonType"),
                        rs.getString("Prompt"),
                        rs.getInt("LessonDuration"),
                        rs.getInt("UpperCase") == 1,
                        rs.getInt("Numbers") == 1,
                        rs.getInt("Punctuation") == 1,
                        rs.getInt("SpecialChars") == 1
                );
            }
        }
    }
}
