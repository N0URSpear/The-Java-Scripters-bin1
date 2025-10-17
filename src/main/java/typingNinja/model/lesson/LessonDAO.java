package typingNinja.model.lesson;

import typingNinja.model.SqliteConnection;
import java.sql.*;

public class LessonDAO {
    private final Connection conn = SqliteConnection.getInstance();

    public Lesson fetchLatestForUser(int userId) throws SQLException {
        // Grab the most recent lesson selection so controllers know what to launch next.
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

    public void markStarted(int lessonId, int userId) throws SQLException {
        // Stamp the start time once the student actually begins typing.
        String sql = "UPDATE Lesson SET DateStarted = datetime('now','localtime') WHERE LessonID = ? AND UserID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void markCompleted(int lessonId, int userId,
                              double starRating, double wpm, double accuracy, int errors,
                              String weakKeys) throws SQLException {
        // Record the final stats whenever a lesson reaches a valid end state.
        String sql = """
      UPDATE Lesson
      SET DateCompleted = datetime('now','localtime'),
          StarRating = ?, WPM = ?, Accuracy = ?, ErrorAmount = ?, WeakKeys = ?
      WHERE LessonID = ? AND UserID = ?
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, starRating);
            ps.setDouble(2, wpm);
            ps.setDouble(3, accuracy);
            ps.setInt(4, errors);
            ps.setString(5, weakKeys);
            ps.setInt(6, lessonId);
            ps.setInt(7, userId);
            ps.executeUpdate();
        }
    }

    public void deleteIfNotCompleted(int lessonId, int userId) throws SQLException {
        // Cleanup helper for when a lesson is cancelled before it finishes.
        String sql = "DELETE FROM Lesson WHERE LessonID = ? AND UserID = ? AND DateCompleted IS NULL";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lessonId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public java.util.List<String> topWeakPairsForUserFromCompletedCustomLessons(int userId, int k) throws java.sql.SQLException {
        // Aggregate bigram mistakes from finished custom lessons to feed the AI prompts.
        String sql = """
        SELECT WeakKeys
        FROM Lesson
        WHERE UserID = ?
          AND LessonType = 'CustomTopic'
          AND WeakKeys IS NOT NULL AND LENGTH(TRIM(WeakKeys)) > 0
          AND DateCompleted IS NOT NULL
    """;
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String wk = rs.getString(1);
                    if (wk == null || wk.isBlank()) continue;
                    for (String token : wk.trim().split("\\s+")) {
                        if (token.equals("--")) continue;
                        String pair = token.replace("|", "");
                        if (pair.length() != 2) continue;
                        char a = pair.charAt(0), b = pair.charAt(1);
                        if (!WeakKeyTracker.trackable(a) || !WeakKeyTracker.trackable(b)) continue;
                        counts.merge(pair, 1, Integer::sum);
                    }
                }
            }
        }
        java.util.List<java.util.Map.Entry<String,Integer>> list = new java.util.ArrayList<>(counts.entrySet());
        list.sort((e1, e2) -> {
            int c = Integer.compare(e2.getValue(), e1.getValue());
            return (c != 0) ? c : e1.getKey().compareTo(e2.getKey());
        });
        java.util.List<String> out = new java.util.ArrayList<>(Math.min(k, list.size()));
        for (int i = 0; i < list.size() && out.size() < k; i++) {
            out.add(list.get(i).getKey());
        }
        return out;
    }

}
