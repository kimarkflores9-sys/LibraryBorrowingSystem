package com.scc;

public record Book(int id, String title, String author, String isbn, String category,
                   int totalCopies, int availableCopies) {
}
