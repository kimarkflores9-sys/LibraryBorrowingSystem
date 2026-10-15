package com.scc;

import com.scc.Book;
import com.scc.dao.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookDao {

    private static final String COLUMNS =
            "book_id, title, author, isbn, category, total_copies, available_copies";

    // ---------- ADMIN functions ----------

    public boolean addBook(Book book) {
        String sql = "INSERT INTO books (title, author, isbn, category, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, book.title());
            pstmt.setString(2, book.author());
            pstmt.setString(3, book.isbn().isBlank() ? null : book.isbn());
            pstmt.setString(4, book.category());
            pstmt.setInt(5, book.totalCopies());
            pstmt.setInt(6, book.totalCopies()); // all copies available when added
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
            return false;
        }
    }

    // Changes total copies and shifts available copies by the same amount
    public boolean updateTotalCopies(int bookId, int newTotal) {
        String sql = "UPDATE books SET total_copies = ?, "
                + "available_copies = available_copies + (? - total_copies) WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newTotal);
            pstmt.setInt(2, newTotal);
            pstmt.setInt(3, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Cannot update copies (can't go below the number currently borrowed).");
            return false;
        }
    }

    public boolean deleteBook(int bookId) {
        String sql = "DELETE FROM books WHERE book_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bookId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Cannot delete: this book has borrow records.");
            return false;
        }
    }

    // ---------- USER (and admin) functions ----------

    public List<com.scc.Book> getAllBooks() {
        return query("SELECT " + COLUMNS + " FROM books ORDER BY title", null);
    }

    public List<com.scc.Book> searchBooks(String keyword) {
        String sql = "SELECT " + COLUMNS + " FROM books "
                + "WHERE title LIKE ? OR author LIKE ? OR isbn LIKE ? OR category LIKE ? ORDER BY title";
        return query(sql, "%" + keyword + "%");
    }

    private List<Book> query(String sql, String keyword) {
        List<Book> books = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            if (keyword != null) {
                for (int i = 1; i <= 4; i++) pstmt.setString(i, keyword);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(rs.getInt("book_id"), rs.getString("title"),
                            rs.getString("author"), rs.getString("isbn"), rs.getString("category"),
                            rs.getInt("total_copies"), rs.getInt("available_copies")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
        return books;
    }
}
