package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String URL = "jdbc:sqlite:library_database.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
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

        String createBooks = """
            CREATE TABLE IF NOT EXISTS books (
                book_id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                author TEXT NOT NULL,
                isbn TEXT UNIQUE,
                category TEXT,
                total_copies INTEGER NOT NULL DEFAULT 1 CHECK (total_copies >= 0),
                available_copies INTEGER NOT NULL DEFAULT 1
                    CHECK (available_copies >= 0 AND available_copies <= total_copies)
            );
        """;

        String createBorrows = """
            CREATE TABLE IF NOT EXISTS borrow_transactions (
                transaction_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                book_id INTEGER NOT NULL,
                issue_date TEXT NOT NULL,
                due_date TEXT NOT NULL,
                return_date TEXT,
                status TEXT NOT NULL DEFAULT 'BORROWED',
                fine_amount REAL NOT NULL DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id),
                FOREIGN KEY (book_id) REFERENCES books(book_id)
            );
        """;

        // Default admin so there is someone who can approve accounts. CHANGE THIS PASSWORD.
        String seedAdmin = """
            INSERT OR IGNORE INTO users (username, password, roles, status)
            VALUES ('admin', 'admin123', 'ADMIN', 'ACTIVE');
        """;

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createBooks);
            stmt.execute(createBorrows);
            stmt.execute(seedAdmin);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
