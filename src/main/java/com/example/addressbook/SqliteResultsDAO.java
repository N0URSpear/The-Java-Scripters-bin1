package com.example.addressbook;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

// SQLite 实现的成绩 DAO
public class SqliteResultsDAO implements IResultsDAO {

    private final Connection connection;

    public SqliteResultsDAO(Connection connection) {
        if (connection == null) throw new IllegalStateException("SQLite connection is null.");
        this.connection = connection;
    }

    // --- SQL ---
    private static final String SQL_CREATE = """
        CREATE TABLE IF NOT EXISTS Results (
            id         INTEGER PRIMARY KEY AUTOINCREMENT,
            wpm        INTEGER NOT NULL,
            acc        INTEGER NOT NULL,
            created_at TEXT    NOT NULL DEFAULT (datetime('now'))
        );
        """;

    private static final String SQL_INSERT = "INSERT INTO Results (wpm, acc) VALUES (?, ?);";

    private static final String SQL_LAST_N = """
        SELECT id, wpm, acc, created_at
        FROM Results
        ORDER BY id DESC
        LIMIT ?;
        """;

    private static final String SQL_ALL = """
        SELECT id, wpm, acc, created_at
        FROM Results
        ORDER BY id ASC;
        """;

    private static final String SQL_COUNT = "SELECT COUNT(*) FROM Results;";
    private static final String SQL_DELETE_ALL = "DELETE FROM Results;";

    //IResultsDAO
    @Override
    public void ensureTable() throws Exception {
        try (Statement st = connection.createStatement()) {
            st.execute(SQL_CREATE);
        }
    }

    @Override
    public long addResult(int wpm, int acc) throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, wpm);
            ps.setInt(2, acc);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getLong(1) : -1L;
            }
        }
    }

    @Override
    public List<Result> getLastN(int n) throws Exception {
        List<Result> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SQL_LAST_N)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Result(
                            rs.getInt("id"),
                            rs.getInt("wpm"),
                            rs.getInt("acc"),
                            rs.getString("created_at")
                    ));
                }
            }
        }
        return list;
    }

    @Override
    public List<Result> getAll() throws Exception {
        List<Result> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SQL_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Result(
                        rs.getInt("id"),
                        rs.getInt("wpm"),
                        rs.getInt("acc"),
                        rs.getString("created_at")
                ));
            }
        }
        return list; // 旧→新
    }

    @Override
    public int count() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(SQL_COUNT);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    @Override
    public void deleteAll() throws Exception {
        try (PreparedStatement ps = connection.prepareStatement(SQL_DELETE_ALL)) {
            ps.executeUpdate();
        }
    }
}
