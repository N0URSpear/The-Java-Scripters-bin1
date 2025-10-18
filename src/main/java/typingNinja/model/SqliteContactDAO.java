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
}
