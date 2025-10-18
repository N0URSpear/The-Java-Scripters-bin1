package typingNinja.model;

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
            String query = "CREATE TABLE IF NOT EXISTS Users (" +
                    "UserID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "Username TEXT NOT NULL UNIQUE, " +
                    "PasswordHash TEXT NOT NULL, " +
                    "SecretQuestion1 TEXT NOT NULL, " +
                    "SecretQuestion2 TEXT NOT NULL, " +
                    "SecretQuestion1Answer TEXT NOT NULL, " +
                    "SecretQuestion2Answer TEXT NOT NULL" +
                    ")";
            statement.execute(query);

            // Ensure no legacy table remains to repopulate Users on startup
            try (Statement drop = connection.createStatement()) {
                drop.execute("DROP TABLE IF EXISTS NinjaUsers");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
    public void updateNinjaUser(NinjaUser ninjaUser) {
        String sql = "UPDATE Users SET Username = ?, PasswordHash = ?, SecretQuestion1 = ?, SecretQuestion2 = ?, SecretQuestion1Answer = ?, SecretQuestion2Answer = ? WHERE UserID = ?";
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
        String sql = "DELETE FROM Users WHERE UserID = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, ninjaUser.getId());
            ps.executeUpdate();
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
        String sql = "SELECT UserID, Username, PasswordHash, SecretQuestion1, SecretQuestion2, SecretQuestion1Answer, SecretQuestion2Answer FROM Users";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
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

            String belt = "White";
            if (totalStars >= 15 && totalStars <= 39) belt = "Yellow";
            else if (totalStars >= 40 && totalStars <= 79) belt = "Blue";
            else if (totalStars >= 80 && totalStars <= 139) belt = "Green";
            else if (totalStars >= 140 && totalStars <= 239) belt = "Orange";
            else if (totalStars >= 240 && totalStars <= 379) belt = "Red";
            else if (totalStars >= 380 && totalStars <= 539) belt = "Brown";
            else if (totalStars >= 800) belt = "Black";

            String update = """
            UPDATE Statistics
            SET TotalLessons = ?, AvgWPM = ?, HighestRating = ?, AvgRating = ?, TotalStars = ?, Belt = ?
            WHERE UserID = ?
            """;
            try (PreparedStatement ps = connection.prepareStatement(update)) {
                ps.setInt(1, totalLessons);
                ps.setDouble(2, avgWPM);
                ps.setDouble(3, highestRating);
                ps.setDouble(4, avgRating);
                ps.setInt(5, totalStars);
                ps.setString(6, belt);
                ps.setInt(7, userId);
                ps.executeUpdate();
            }

            System.out.println("Recalculated statistics for userId=" + userId +
                    " Lessons=" + totalLessons +
                    " AvgWPM=" + avgWPM +
                    " HighestRating=" + highestRating +
                    " AvgRating=" + avgRating +
                    " TotalStars=" + totalStars +
                    " Belt=" + belt);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void updateGoals(int userId, Integer estHours, Integer estWPM, Integer estAccuracy) {
        String sql = """
    UPDATE Goals
    SET EstHours   = COALESCE(?, EstHours),
        EstWPM     = COALESCE(?, EstWPM),
        EstAccuracy= COALESCE(?, EstAccuracy)
    WHERE UserID = ?
    """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            if (estHours == null) ps.setObject(1, null); else ps.setInt(1, estHours);
            if (estWPM   == null) ps.setObject(2, null); else ps.setInt(2, estWPM);
            if (estAccuracy == null) ps.setObject(3, null); else ps.setInt(3, estAccuracy);
            ps.setInt(4, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
}
