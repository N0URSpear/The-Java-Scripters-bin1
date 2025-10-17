package typingNinja;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import typingNinja.auth.Session;



/** Results DAO that reuses the main Lesson table; NEVER creates/changes schema. */
public class SqliteResultsDAO implements IResultsDAO {

    // Table / Columns in main DB
    private static final String TBL       = "Lesson";
    private static final String COL_ID    = "LessonID";
    private static final String COL_UID   = "UserID";
    private static final String COL_WPM   = "WPM";          // REAL
    private static final String COL_ACC   = "Accuracy";     // REAL
    private static final String COL_TYPE  = "LessonType";   // TEXT
    private static final String COL_DONE  = "DateCompleted";// TEXT (ISO datetime)

    private final Connection connection;

    public SqliteResultsDAO(Connection connection) {
        if (connection == null) throw new IllegalStateException("SQLite connection is null.");
        this.connection = connection;
    }

    /** Team rule: do NOT touch schema at runtime. */
    @Override public void ensureTable() { /* no-op */ }

    @Override
    public long addResult(int wpm, int acc) throws Exception {
        int userId = resolveCurrentUserId(connection);

        // 1) Try to UPDATE the latest "in-progress" lesson (no DateCompleted yet)
        long updatedId = updateLatestInProgress(userId, wpm, acc);
        if (updatedId > 0) return updatedId;

        // 2) Otherwise INSERT a fresh completed lesson, inheriting recent LessonType (or default '1a')
        String lessonType = fetchLatestLessonTypeOrDefault(userId, "1a");
        final String insert = "INSERT INTO " + TBL +
                " (" + COL_UID + "," + COL_WPM + "," + COL_ACC + "," + COL_TYPE + "," + COL_DONE + ") " +
                "VALUES (?, ?, ?, ?, datetime('now'))";
        try (PreparedStatement ps = connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, userId);
            ps.setDouble(2, wpm);
            ps.setDouble(3, acc);
            ps.setString(4, lessonType);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1L;
            }
        }
    }

    @Override
    public List<Result> getLastN(int n) throws Exception {
        Integer userId = resolveCurrentUserId(connection);
        if (userId == null) return java.util.Collections.emptyList();

        final String sql =
                "SELECT " + COL_ID + " AS id, " + COL_WPM + " AS wpm, " + COL_ACC + " AS acc, " + COL_DONE + " AS createdAt " +
                        "FROM " + TBL + " " +
                        "WHERE " + COL_UID + "=? AND " + COL_DONE + " IS NOT NULL " +
                        "ORDER BY datetime(" + COL_DONE + ") DESC, " + COL_ID + " DESC LIMIT ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, n);
            try (ResultSet rs = ps.executeQuery()) {
                List<Result> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Result(
                            rs.getInt("id"),
                            (int)Math.round(rs.getDouble("wpm")),
                            (int)Math.round(rs.getDouble("acc")),
                            rs.getString("createdAt")
                    ));
                }
                return out;
            }
        }
    }

    @Override
    public List<Result> getAll() throws Exception {
        Integer userId = resolveCurrentUserId(connection);
        if (userId == null) return java.util.Collections.emptyList();

        final String sql =
                "SELECT " + COL_ID + " AS id, " + COL_WPM + " AS wpm, " + COL_ACC + " AS acc, " + COL_DONE + " AS createdAt " +
                        "FROM " + TBL + " " +
                        "WHERE " + COL_UID + "=? AND " + COL_DONE + " IS NOT NULL " +
                        "ORDER BY datetime(" + COL_DONE + ") ASC, " + COL_ID + " ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Result> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(new Result(
                            rs.getInt("id"),
                            (int)Math.round(rs.getDouble("wpm")),
                            (int)Math.round(rs.getDouble("acc")),
                            rs.getString("createdAt")
                    ));
                }
                return out;
            }
        }
    }

    @Override
    public int count() throws Exception {
        Integer userId = resolveCurrentUserId(connection);
        if (userId == null) return 0;

        final String sql = "SELECT COUNT(*) FROM " + TBL + " WHERE " + COL_UID + "=? AND " + COL_DONE + " IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public void deleteAll() throws Exception {
        int userId = resolveCurrentUserId(connection);
        final String sql = "DELETE FROM " + TBL + " WHERE " + COL_UID + "=? AND " + COL_DONE + " IS NOT NULL";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    // ---------- helpers ----------

    /** Return the latest (max UserID) user; adjust if you have a proper current-user context. */
    private Integer resolveCurrentUserId(Connection c) throws SQLException {
        Integer sid = typingNinja.auth.Session.getCurrentUserId();
        if (sid != null) return sid;

        try (PreparedStatement ps = c.prepareStatement(
                "SELECT UserID FROM Users ORDER BY UserID DESC LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : null;
        }
    }


    /** Try to update the most recent in-progress lesson; return its id if updated, or 0 if none. */
    private long updateLatestInProgress(int userId, int wpm, int acc) throws SQLException {
        // Find latest lesson for this user that has NO DateCompleted yet
        final String findSql = "SELECT " + COL_ID + " FROM " + TBL +
                " WHERE " + COL_UID + "=? AND " + COL_DONE + " IS NULL ORDER BY " + COL_ID + " DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(findSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return 0L;
                long lessonId = rs.getLong(1);

                final String updSql = "UPDATE " + TBL +
                        " SET " + COL_WPM + "=?, " + COL_ACC + "=?, " + COL_DONE + "=datetime('now')" +
                        " WHERE " + COL_ID + "=?";
                try (PreparedStatement up = connection.prepareStatement(updSql)) {
                    up.setDouble(1, wpm);
                    up.setDouble(2, acc);
                    up.setLong(3, lessonId);
                    if (up.executeUpdate() > 0) return lessonId;
                    return 0L;
                }
            }
        }
    }

    /** Fetch latest LessonType for this user, or fallback to default. */
    private String fetchLatestLessonTypeOrDefault(int userId, String def) throws SQLException {
        final String sql = "SELECT " + COL_TYPE + " FROM " + TBL +
                " WHERE " + COL_UID + "=? AND " + COL_TYPE + " IS NOT NULL" +
                " ORDER BY " + COL_ID + " DESC LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : def;
            }
        }
    }
}
