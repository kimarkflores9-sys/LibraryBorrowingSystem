package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:library_database.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        createTables(conn);
        return conn;
    }

    private static void createTables(Connection conn) {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                roles TEXT NOT NULL,
                status TEXT NOT NULL
            );
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}