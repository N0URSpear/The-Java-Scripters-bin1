package com.example.addressbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

public class SqliteContactDAO implements INinjaContactDAO {
    private Connection connection;

    public SqliteContactDAO() {
        connection = SqliteConnection.getInstance();
        createTable();
    }

    private void createTable() {
        // Create table if not exists
        try {
            Statement statement = connection.createStatement();
            String query = "CREATE TABLE IF NOT EXISTS NinjaUsers ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "userName VARCHAR NOT NULL,"
                    + "passwordHash VARCHAR NOT NULL,"
                    + "SecretQuestion1 VARCHAR NOT NULL,"
                    + "SecretQuestion2 VARCHAR NOT NULL,"
                    + "SecretQuestion1Answer VARCHAR NOT NULL,"
                    + "SecretQuestion2Answer VARCHAR NOT NULL,"
                    + ")";
            statement.execute(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addNinjaUser(NinjaUser ninjaUser) {
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO NinjaUsers (userName, passwordHash, SecretQuestion1, SecretQuestion2, SecretQuestion1Answer, SecretQuestion2Answer) VALUES (?, ?, ?, ?, ?, ?)");
            statement.setString(1, ninjaUser.getUserName());
            statement.setString(2, ninjaUser.getPasswordHash());
            statement.setString(3, ninjaUser.getSecretQuestion1());
            statement.setString(4, ninjaUser.getSecretQuestion2());
            statement.setString(5, ninjaUser.getSecretQuestion1Answer());
            statement.setString(6, ninjaUser.getSecretQuestion2Answer());
            statement.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateNinjaUser(NinjaUser ninjaUser) {

    }

    @Override
    public void deleteNinjaUser(NinjaUser ninjaUser) {

    }

    @Override
    public NinjaUser getNinjaUser(String userName) {
        return null;
    }

    @Override
    public List<NinjaUser> getAllNinjas() {
        return List.of();
    }
}
