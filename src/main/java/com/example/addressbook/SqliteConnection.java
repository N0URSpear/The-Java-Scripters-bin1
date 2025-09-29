package com.example.addressbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Paths;

public class SqliteConnection {
    private static Connection instance = null;

    private SqliteConnection() {
        String dbFilePath = Paths.get(System.getProperty("user.dir"), "TypingNinjaSQL.db").toString();
        String url = "jdbc:sqlite:" + dbFilePath;
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException sqlEx) {
            System.err.println(sqlEx);
        }
    }

    public static Connection getInstance() {
        if (instance == null) {
            new SqliteConnection();
        }
        return instance;
    }

    public static Connection getConnection() {
        return getInstance();
    }
}
