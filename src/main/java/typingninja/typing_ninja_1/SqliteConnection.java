package com.example.addressbook;

import java.sql.Connection;


public final class SqliteConnection {
    private SqliteConnection() {}

    public static synchronized Connection getInstance() {
        return com.example.addressbook.SqliteConnection.getInstance();
    }
}
