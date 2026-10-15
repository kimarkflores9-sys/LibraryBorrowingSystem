package com.scc;

public record BorrowRecord(int transactionId, String username, String title, String issueDate,
                           String dueDate, String returnDate, String status, double fine) {
}
