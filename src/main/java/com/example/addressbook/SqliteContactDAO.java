package com.example.addressbook;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SqliteContactDAO implements INinjaContactDAO {
    private final Connection connection;

    public SqliteContactDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            // Users 表
            statement.execute("CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Username TEXT NOT NULL UNIQUE, " +
                    "PasswordHash TEXT NOT NULL, " +
                    "SecretQuestion1 TEXT NOT NULL, " +
                    "SecretQuestion2 TEXT NOT NULL, " +
                    "SecretQuestion1Answer TEXT NOT NULL, " +
                    "SecretQuestion2Answer TEXT NOT NULL" +
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

    // === Users CRUD ===
    @Override
    public void addNinjaUser(NinjaUser ninjaUser) {
        String sql = "INSERT INTO Users (Username, PasswordHash, SecretQuestion1, SecretQuestion2, SecretQuestion1Answer, SecretQuestion2Answer) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ninjaUser.getUserName());
            ps.setString(2, ninjaUser.getPasswordHash());
            ps.setString(3, ninjaUser.getSecretQuestion1());
            ps.setString(4, ninjaUser.getSecretQuestion2());
            ps.setString(5, ninjaUser.getSecretQuestion1Answer());
            ps.setString(6, ninjaUser.getSecretQuestion2Answer());
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
        String sql = "SELECT UserID, Username, PasswordHash, SecretQuestion1, SecretQuestion2, SecretQuestion1Answer, SecretQuestion2Answer FROM Users WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NinjaUser user = new NinjaUser(
                            rs.getString("Username"),
                            rs.getString("PasswordHash"),
                            rs.getString("SecretQuestion1"),
                            rs.getString("SecretQuestion2"),
                            rs.getString("SecretQuestion1Answer"),
                            rs.getString("SecretQuestion2Answer")
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
        String sql = "SELECT * FROM Users";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                NinjaUser user = new NinjaUser(
                        rs.getString("Username"),
                        rs.getString("PasswordHash"),
                        rs.getString("SecretQuestion1"),
                        rs.getString("SecretQuestion2"),
                        rs.getString("SecretQuestion1Answer"),
                        rs.getString("SecretQuestion2Answer")
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
        String sql = "UPDATE Users SET Username=?, PasswordHash=?, SecretQuestion1=?, SecretQuestion2=?, SecretQuestion1Answer=?, SecretQuestion2Answer=? WHERE UserID=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ninjaUser.getUserName());
            ps.setString(2, ninjaUser.getPasswordHash());
            ps.setString(3, ninjaUser.getSecretQuestion1());
            ps.setString(4, ninjaUser.getSecretQuestion2());
            ps.setString(5, ninjaUser.getSecretQuestion1Answer());
            ps.setString(6, ninjaUser.getSecretQuestion2Answer());
            ps.setInt(7, ninjaUser.getId());
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

        // === Getter 方法 ===
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
        return -1; // 没找到返回 -1
    }

    // 初始化 Goals 和 Statistics 表的数据
    public void initUserData(int userId) {
        try {
            // Goals
            String insertGoals = """
            INSERT OR IGNORE INTO Goals (UserID, EstHours, EstWPM, EstAccuracy)
            VALUES (?, 0, 0, 0)
        """;
            try (PreparedStatement ps = connection.prepareStatement(insertGoals)) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // Statistics
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




    // === 初始化 Goals + Statistics ===
    public void safeInitUserData(int userId) {
        try {
            // Goals
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

            // Statistics
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

}
