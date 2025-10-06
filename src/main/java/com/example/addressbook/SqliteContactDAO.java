package com.example.addressbook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteContactDAO implements INinjaContactDAO {
    private final Connection connection;

    public SqliteContactDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
        ensureUsersPlainColumns(); // 迁移：为旧库补齐明文字段
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            // Users 表（含明文字段，用于回显）
            statement.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Username TEXT NOT NULL UNIQUE, " +
                    "PasswordHash TEXT NOT NULL, " +
                    "PasswordPlain TEXT, " + // ✅ 新增
                    "SecretQuestion1 TEXT NOT NULL, " +
                    "SecretQuestion2 TEXT NOT NULL, " +
                    "SecretQuestion1Answer TEXT NOT NULL, " +
                    "SecretQuestion2Answer TEXT NOT NULL, " +
                    "SecretAnswer1Plain TEXT, " + // ✅ 新增
                    "SecretAnswer2Plain TEXT" +  // ✅ 新增
                    ")");

            // Goals 表
            statement.execute("CREATE TABLE IF NOT EXISTS Goals (" +
                    "UserID INTEGER PRIMARY KEY, " +
                    "EstHours INTEGER DEFAULT 0, " +
                    "EstWPM INTEGER DEFAULT 0, " +
                    "EstAccuracy INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(UserID) REFERENCES Users(UserID)" +
                    ")");

            // Statistics 表
            statement.execute("CREATE TABLE IF NOT EXISTS Statistics (" +
                    "UserID INTEGER PRIMARY KEY, " +
                    "TimeActiveWeek REAL DEFAULT 0, " +
                    "TotalWPMWeek INTEGER DEFAULT 0, " +
                    "TotalAccuracyWeek INTEGER DEFAULT 0, " +
                    "Belt TEXT DEFAULT 'White', " +
                    "TotalLessons INTEGER DEFAULT 0, " +
                    "AvgWPM REAL DEFAULT 0, " +
                    "HighestRating REAL DEFAULT 0, " +
                    "AvgRating REAL DEFAULT 0, " +
                    "TotalStars INTEGER DEFAULT 0, " +
                    "FOREIGN KEY(UserID) REFERENCES Users(UserID)" +
                    ")");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 为旧版本 Users 表补齐明文字段（忽略重复列错误）
    private void ensureUsersPlainColumns() {
        try (Statement s = connection.createStatement()) {
            try { s.execute("ALTER TABLE Users ADD COLUMN PasswordPlain TEXT"); } catch (SQLException ignored) {}
            try { s.execute("ALTER TABLE Users ADD COLUMN SecretAnswer1Plain TEXT"); } catch (SQLException ignored) {}
            try { s.execute("ALTER TABLE Users ADD COLUMN SecretAnswer2Plain TEXT"); } catch (SQLException ignored) {}
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === Users CRUD ===
    @Override
    public void addNinjaUser(NinjaUser ninjaUser) {
        String sql = "INSERT INTO Users (Username, PasswordHash, PasswordPlain, " +
                "SecretQuestion1, SecretQuestion2, " +
                "SecretQuestion1Answer, SecretQuestion2Answer, " +
                "SecretAnswer1Plain, SecretAnswer2Plain) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ninjaUser.getUserName());
            ps.setString(2, ninjaUser.getPasswordHash());
            ps.setString(3, ninjaUser.getPasswordPlain()); // ✅ 保存明文
            ps.setString(4, ninjaUser.getSecretQuestion1());
            ps.setString(5, ninjaUser.getSecretQuestion2());
            ps.setString(6, ninjaUser.getSecretQuestion1Answer());
            ps.setString(7, ninjaUser.getSecretQuestion2Answer());
            ps.setString(8, ninjaUser.getSecretAnswer1Plain()); // ✅ 保存明文
            ps.setString(9, ninjaUser.getSecretAnswer2Plain()); // ✅ 保存明文
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    ninjaUser.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NinjaUser getNinjaUser(String userName) {
        String sql = "SELECT UserID, Username, PasswordHash, PasswordPlain, " +
                "SecretQuestion1, SecretQuestion2, " +
                "SecretQuestion1Answer, SecretQuestion2Answer, " +
                "SecretAnswer1Plain, SecretAnswer2Plain " +
                "FROM Users WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NinjaUser user = new NinjaUser(
                            rs.getString("Username"),
                            rs.getString("PasswordHash"),
                            rs.getString("PasswordPlain"), // ✅ 返回明文
                            rs.getString("SecretQuestion1"),
                            rs.getString("SecretQuestion2"),
                            rs.getString("SecretQuestion1Answer"),
                            rs.getString("SecretQuestion2Answer"),
                            rs.getString("SecretAnswer1Plain"), // ✅ 返回明文
                            rs.getString("SecretAnswer2Plain")  // ✅ 返回明文
                    );
                    user.setId(rs.getInt("UserID"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<NinjaUser> getAllNinjas() {
        List<NinjaUser> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, PasswordHash, PasswordPlain, " +
                "SecretQuestion1, SecretQuestion2, " +
                "SecretQuestion1Answer, SecretQuestion2Answer, " +
                "SecretAnswer1Plain, SecretAnswer2Plain FROM Users";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NinjaUser user = new NinjaUser(
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("PasswordPlain"), // ✅ 返回明文
                        rs.getString("SecretQuestion1"),
                        rs.getString("SecretQuestion2"),
                        rs.getString("SecretQuestion1Answer"),
                        rs.getString("SecretQuestion2Answer"),
                        rs.getString("SecretAnswer1Plain"), // ✅ 返回明文
                        rs.getString("SecretAnswer2Plain")  // ✅ 返回明文
                );
                user.setId(rs.getInt("UserID"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void updateNinjaUser(NinjaUser ninjaUser) {
        String sql = "UPDATE Users SET Username=?, PasswordHash=?, PasswordPlain=?, " +
                "SecretQuestion1=?, SecretQuestion2=?, " +
                "SecretQuestion1Answer=?, SecretQuestion2Answer=?, " +
                "SecretAnswer1Plain=?, SecretAnswer2Plain=? " +
                "WHERE UserID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ninjaUser.getUserName());
            ps.setString(2, ninjaUser.getPasswordHash());
            ps.setString(3, ninjaUser.getPasswordPlain()); // ✅ 保存明文
            ps.setString(4, ninjaUser.getSecretQuestion1());
            ps.setString(5, ninjaUser.getSecretQuestion2());
            ps.setString(6, ninjaUser.getSecretQuestion1Answer());
            ps.setString(7, ninjaUser.getSecretQuestion2Answer());
            ps.setString(8, ninjaUser.getSecretAnswer1Plain()); // ✅ 保存明文
            ps.setString(9, ninjaUser.getSecretAnswer2Plain()); // ✅ 保存明文
            ps.setInt(10, ninjaUser.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNinjaUser(NinjaUser ninjaUser) {
        String sql = "DELETE FROM Users WHERE UserID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ninjaUser.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // === 内部类：ProfileStats ===
    public static class ProfileStats {
        private final String estHours, estWPM, estAccuracy;
        private final String timeActiveWeek, totalWPMWeek, totalAccuracyWeek;
        private final String belt, totalLessons, avgWPM, highestRating, avgRating, totalStars;

        public ProfileStats(String estHours, String estWPM, String estAccuracy,
                            String timeActiveWeek, String totalWPMWeek, String totalAccuracyWeek,
                            String belt, String totalLessons, String avgWPM,
                            String highestRating, String avgRating, String totalStars) {
            this.estHours = estHours;
            this.estWPM = estWPM;
            this.estAccuracy = estAccuracy;
            this.timeActiveWeek = timeActiveWeek;
            this.totalWPMWeek = totalWPMWeek;
            this.totalAccuracyWeek = totalAccuracyWeek;
            this.belt = belt;
            this.totalLessons = totalLessons;
            this.avgWPM = avgWPM;
            this.highestRating = highestRating;
            this.avgRating = avgRating;
            this.totalStars = totalStars;
        }

        public String getEstHours() { return estHours; }
        public String getEstWPM() { return estWPM; }
        public String getEstAccuracy() { return estAccuracy; }
        public String getTimeActiveWeek() { return timeActiveWeek; }
        public String getTotalWPMWeek() { return totalWPMWeek; }
        public String getTotalAccuracyWeek() { return totalAccuracyWeek; }
        public String getBelt() { return belt; }
        public String getTotalLessons() { return totalLessons; }
        public String getAvgWPM() { return avgWPM; }
        public String getHighestRating() { return highestRating; }
        public String getAvgRating() { return avgRating; }
        public String getTotalStars() { return totalStars; }
    }

    // === 查询 Goals + Statistics ===
    public ProfileStats getUserGoalsAndStats(int userId) {
        String sql = """
        SELECT g.EstHours, g.EstWPM, g.EstAccuracy,
               s.TimeActiveWeek, s.TotalWPMWeek, s.TotalAccuracyWeek,
               s.Belt, s.TotalLessons, s.AvgWPM, s.HighestRating,
               s.AvgRating, s.TotalStars
        FROM Users u
        LEFT JOIN Goals g ON u.UserID = g.UserID
        LEFT JOIN Statistics s ON u.UserID = s.UserID
        WHERE u.UserID = ?;
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new ProfileStats(
                        rs.getString("EstHours"),
                        rs.getString("EstWPM"),
                        rs.getString("EstAccuracy"),
                        rs.getString("TimeActiveWeek"),
                        rs.getString("TotalWPMWeek"),
                        rs.getString("TotalAccuracyWeek"),
                        rs.getString("Belt"),
                        rs.getString("TotalLessons"),
                        rs.getString("AvgWPM"),
                        rs.getString("HighestRating"),
                        rs.getString("AvgRating"),
                        rs.getString("TotalStars")
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 通过用户名获取 UserID
    public int getUserIdByUsername(String username) {
        String sql = "SELECT UserID FROM Users WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("UserID");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 初始化 Goals 和 Statistics 表的数据
    public void initUserData(int userId) {
        try {
            String insertGoals = """
            INSERT OR IGNORE INTO Goals (UserID, EstHours, EstWPM, EstAccuracy)
            VALUES (?, 0, 0, 0)
            """;
            try (PreparedStatement ps = connection.prepareStatement(insertGoals)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            String insertStats = """
            INSERT OR IGNORE INTO Statistics (UserID, TimeActiveWeek, TotalWPMWeek, TotalAccuracyWeek,
                                              Belt, TotalLessons, AvgWPM, HighestRating, AvgRating, TotalStars)
            VALUES (?, 0, 0, 0, 'White', 0, 0, 0, 0, 0)
            """;
            try (PreparedStatement ps = connection.prepareStatement(insertStats)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 安全初始化（存在即忽略）
    public void safeInitUserData(int userId) {
        try {
            String checkGoals = "SELECT COUNT(*) FROM Goals WHERE UserID = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkGoals)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    String goalsSql = "INSERT INTO Goals (UserID, EstHours, EstWPM, EstAccuracy) VALUES (?, 0, 0, 0)";
                    try (PreparedStatement insert = connection.prepareStatement(goalsSql)) {
                        insert.setInt(1, userId);
                        insert.executeUpdate();
                    }
                }
            }

            String checkStats = "SELECT COUNT(*) FROM Statistics WHERE UserID = ?";
            try (PreparedStatement ps = connection.prepareStatement(checkStats)) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    String statsSql = """
                    INSERT INTO Statistics (UserID, TimeActiveWeek, TotalWPMWeek, TotalAccuracyWeek,
                                            Belt, TotalLessons, AvgWPM, HighestRating, AvgRating, TotalStars)
                    VALUES (?, 0, 0, 0, 'White', 0, 0, 0, 0, 0)
                    """;
                    try (PreparedStatement insert = connection.prepareStatement(statsSql)) {
                        insert.setInt(1, userId);
                        insert.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 只更新传入的字段，null 表示保持原值
    public void updateGoals(int userId, Integer estHours, Integer estWPM, Integer estAccuracy) {
        String sql = """
        UPDATE Goals
        SET EstHours   = COALESCE(?, EstHours),
            EstWPM     = COALESCE(?, EstWPM),
            EstAccuracy= COALESCE(?, EstAccuracy)
        WHERE UserID = ?
        """;
        try (java.sql.PreparedStatement ps = connection.prepareStatement(sql)) {
            if (estHours == null) ps.setObject(1, null); else ps.setInt(1, estHours);
            if (estWPM   == null) ps.setObject(2, null); else ps.setInt(2, estWPM);
            if (estAccuracy == null) ps.setObject(3, null); else ps.setInt(3, estAccuracy);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }


    // ⚡ 重新计算并更新 Statistics 表数据
    public void recalcUserStatistics(int userId) {
        try {
            int totalLessons = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT COUNT(*) FROM Lesson WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalLessons = rs.getInt(1);
            }

            double avgWPM = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT AVG(WPM) FROM Lesson WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) avgWPM = rs.getDouble(1);
            }

            double highestRating = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT MAX(StarRating) FROM Lesson WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) highestRating = rs.getDouble(1);
            }

            double avgRating = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT AVG(StarRating) FROM Lesson WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) avgRating = rs.getDouble(1);
            }

            int totalStars = 0;
            try (PreparedStatement ps = connection.prepareStatement("SELECT SUM(CAST(StarRating AS INT)) FROM Lesson WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) totalStars = rs.getInt(1);
            }

            String update = """
            UPDATE Statistics
            SET TotalLessons = ?, AvgWPM = ?, HighestRating = ?, AvgRating = ?, TotalStars = ?
            WHERE UserID = ?
            """;
            try (PreparedStatement ps = connection.prepareStatement(update)) {
                ps.setInt(1, totalLessons);
                ps.setDouble(2, avgWPM);
                ps.setDouble(3, highestRating);
                ps.setDouble(4, avgRating);
                ps.setInt(5, totalStars);
                ps.setInt(6, userId);
                ps.executeUpdate();
            }

            System.out.println("DEBUG: Recalculated statistics for userId=" + userId +
                    " Lessons=" + totalLessons +
                    " AvgWPM=" + avgWPM +
                    " HighestRating=" + highestRating +
                    " AvgRating=" + avgRating +
                    " TotalStars=" + totalStars);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
