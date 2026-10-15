package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    // Ang ngalan sa SQLite database file nga ma-generate sa imong project
    private static final String URL = "jdbc:sqlite:starlight_cinema_v2.db";

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            createTables(conn);
        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
        return conn;
    }

    private static void createTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {

            // 1. Users Table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS Users ("
                    + "UserID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Username TEXT UNIQUE NOT NULL, "
                    + "PasswordHash TEXT NOT NULL, "
                    + "Role TEXT NOT NULL, "
                    + "Status TEXT DEFAULT 'PENDING', "
                    + "LastLoginDate TIMESTAMP"
                    + ");";
            stmt.execute(createUsersTable);

            // 2. Movies Table
            String createMoviesTable = "CREATE TABLE IF NOT EXISTS Movies ("
                    + "MovieID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "Title TEXT NOT NULL, "
                    + "Genre TEXT, "
                    + "DurationMinutes INTEGER, "
                    + "BaseTicketPrice REAL NOT NULL, "
                    + "Rating TEXT"
                    + ");";
            stmt.execute(createMoviesTable);

            // 3. Theaters Table
            String createTheatersTable = "CREATE TABLE IF NOT EXISTS Theaters ("
                    + "TheaterID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "TheaterName TEXT NOT NULL, "
                    + "SeatingCapacity INTEGER, "
                    + "ScreenType TEXT"
                    + ");";
            stmt.execute(createTheatersTable);

            // 4. Bookings Table (Naay Foreign Keys)
            String createBookingsTable = "CREATE TABLE IF NOT EXISTS Bookings ("
                    + "BookingID INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "MovieID INTEGER NOT NULL, "
                    + "CashierID INTEGER NOT NULL, "
                    + "SeatNumber TEXT NOT NULL, "
                    + "BookingTimestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "TotalAmountPaid REAL NOT NULL, "
                    + "FOREIGN KEY (MovieID) REFERENCES Movies(MovieID), "
                    + "FOREIGN KEY (CashierID) REFERENCES Users(UserID)"
                    + ");";
            stmt.execute(createBookingsTable);

            System.out.println("Starlight Cinema database tables created successfully!");

        } catch (SQLException e) {
            System.out.println("Error creating tables: " + e.getMessage());
        }
    }
}
