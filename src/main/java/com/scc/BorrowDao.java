package com.scc;

import com.scc.BorrowRecord;
import com.scc.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BorrowDao {

    private static final int LOAN_DAYS = 7;
    private static final double FINE_PER_DAY = 5.0;

    private static final String BASE_QUERY = """
        SELECT t.transaction_id, u.username, b.title, t.issue_date, t.due_date,
               t.return_date, t.status, t.fine_amount
        FROM borrow_transactions t
        JOIN users u ON u.id = t.user_id
        JOIN books b ON b.book_id = t.book_id
    """;

    // ---------- USER functions ----------

    public boolean borrowBook(int userId, int bookId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int available;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT available_copies FROM books WHERE book_id = ?")) {
                    ps.setInt(1, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("Book not found.");
                            conn.rollback();
                            return false;
                        }
                        available = rs.getInt(1);
                    }
                }
                if (available <= 0) {
                    System.out.println("Sorry, no copies available right now.");
                    conn.rollback();
                    return false;
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT 1 FROM borrow_transactions WHERE user_id = ? AND book_id = ? AND status = 'BORROWED'")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            System.out.println("You already borrowed this book.");
                            conn.rollback();
                            return false;
                        }
                    }
                }

                LocalDate today = LocalDate.now();
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO borrow_transactions (user_id, book_id, issue_date, due_date) VALUES (?, ?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, bookId);
                    ps.setString(3, today.toString());
                    ps.setString(4, today.plusDays(LOAN_DAYS).toString());
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE books SET available_copies = available_copies - 1 WHERE book_id = ?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println("Borrowed! Due date: " + today.plusDays(LOAN_DAYS));
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    public boolean returnBook(int transactionId, int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int bookId;
                LocalDate dueDate;
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT book_id, due_date FROM borrow_transactions "
                                + "WHERE transaction_id = ? AND user_id = ? AND status = 'BORROWED'")) {
                    ps.setInt(1, transactionId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println("No active borrow found with that ID.");
                            conn.rollback();
                            return false;
                        }
                        bookId = rs.getInt("book_id");
                        dueDate = LocalDate.parse(rs.getString("due_date"));
                    }
                }

                LocalDate today = LocalDate.now();
                long lateDays = Math.max(0, ChronoUnit.DAYS.between(dueDate, today));
                double fine = lateDays * FINE_PER_DAY;

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE borrow_transactions SET return_date = ?, status = 'RETURNED', fine_amount = ? "
                                + "WHERE transaction_id = ?")) {
                    ps.setString(1, today.toString());
                    ps.setDouble(2, fine);
                    ps.setInt(3, transactionId);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "UPDATE books SET available_copies = available_copies + 1 WHERE book_id = ?")) {
                    ps.setInt(1, bookId);
                    ps.executeUpdate();
                }

                conn.commit();
                System.out.println(fine > 0
                        ? "Returned " + lateDays + " day(s) late. Fine: " + fine
                        : "Returned on time. No fine.");
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    public List<BorrowRecord> getBorrowsByUser(int userId) {
        return query(BASE_QUERY + " WHERE t.user_id = ? ORDER BY t.transaction_id DESC", userId);
    }

    // ---------- ADMIN functions ----------

    public List<BorrowRecord> getActiveBorrows() {
        return query(BASE_QUERY + " WHERE t.status = 'BORROWED' ORDER BY t.due_date", null);
    }

    private List<BorrowRecord> query(String sql, Integer userId) {
        List<BorrowRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (userId != null) pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new BorrowRecord(rs.getInt("transaction_id"), rs.getString("username"),
                            rs.getString("title"), rs.getString("issue_date"), rs.getString("due_date"),
                            rs.getString("return_date"), rs.getString("status"), rs.getDouble("fine_amount")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
        return list;
    }
}
