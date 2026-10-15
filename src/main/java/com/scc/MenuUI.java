package com.scc;

import com.scc.BookDao;
import com.scc.BorrowDao;
import com.scc.dao.UserDao;
import com.scc.Book;
import com.scc.BorrowRecord;
import com.scc.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class MenuUI {
    private final UserDao userDao = new UserDao();
    private final BookDao bookDao = new BookDao();
    private final BorrowDao borrowDao = new BorrowDao();
    private final Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n==================================");
            System.out.println("      LIBRARY / USER SYSTEM       ");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");

            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> registerUser();
                case 2 -> loginUser();
                case 3 -> {
                    running = false;
                    System.out.println("Nag-exit na sa sistema. Salamat!");
                }
                default -> System.out.println("Pilian nga wala sa lista. Sulayi pag-usab.");
            }
        }
    }

    private void registerUser() {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role (TEACHER / STUDENT): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return;
        }
        if (!role.equals("TEACHER") && !role.equals("STUDENT")) {
            System.out.println("Ang role kinahanglan TEACHER o STUDENT ra!");
            return;
        }

        User user = new User(username, password, role, "PENDING");
        if (userDao.registerUser(user)) {
            System.out.println("Registration successful! Account status: PENDING (wait for admin approval)");
        } else {
            System.out.println("Registration failed (Username might already exist).");
        }
    }

    private void loginUser() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        Optional<User> result = userDao.login(username, password);
        if (result.isEmpty()) {
            System.out.println("Login failed! Check your username/password.");
            return;
        }

        User user = result.get();
        if (!"ACTIVE".equals(user.status())) {
            System.out.println("Your account is " + user.status() + ". Please wait for the admin to approve it.");
            return;
        }

        System.out.println("Login successful! Welcome, " + user.username() + " (" + user.roles() + ")");
        if ("ADMIN".equals(user.roles())) {
            adminMenu();
        } else {
            userMenu(user);
        }
    }

    // =====================================================
    //                      ADMIN MENU
    // =====================================================
    private void adminMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========== ADMIN MENU ==========");
            System.out.println("1.  View pending accounts");
            System.out.println("2.  Approve account");
            System.out.println("3.  Deactivate account");
            System.out.println("4.  View all users");
            System.out.println("5.  Delete user");
            System.out.println("6.  Add book");
            System.out.println("7.  View all books");
            System.out.println("8.  Update book copies");
            System.out.println("9.  Delete book");
            System.out.println("10. View currently borrowed books");
            System.out.println("0.  Logout");

            switch (readInt("Choose an option: ")) {
                case 1 -> printUsers(userDao.getUsersByStatus("PENDING"));
                case 2 -> changeUserStatus("ACTIVE");
                case 3 -> changeUserStatus("INACTIVE");
                case 4 -> printUsers(userDao.getAllUsers());
                case 5 -> deleteUser();
                case 6 -> addBook();
                case 7 -> printBooks(bookDao.getAllBooks());
                case 8 -> updateCopies();
                case 9 -> deleteBook();
                case 10 -> printBorrows(borrowDao.getActiveBorrows());
                case 0 -> {
                    loggedIn = false;
                    System.out.println("Logged out.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    private void changeUserStatus(String newStatus) {
        printUsers(userDao.getAllUsers());
        int id = readInt("Enter user ID: ");
        if (id < 0) return;
        System.out.println(userDao.updateStatus(id, newStatus)
                ? "User status changed to " + newStatus + "."
                : "Update failed (user not found, or user is an admin).");
    }

    private void deleteUser() {
        printUsers(userDao.getAllUsers());
        int id = readInt("Enter user ID to delete: ");
        if (id < 0) return;
        System.out.println(userDao.deleteUser(id)
                ? "User deleted."
                : "Delete failed (user not found, is an admin, or has borrow records).");
    }

    private void addBook() {
        System.out.println("\n--- ADD BOOK ---");
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("ISBN (optional): ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        int copies = readInt("Number of copies: ");

        if (title.isEmpty() || author.isEmpty() || copies < 1) {
            System.out.println("Title, author and at least 1 copy are required.");
            return;
        }

        Book book = new Book(0, title, author, isbn, category, copies, copies);
    }

    private void updateCopies() {
        printBooks(bookDao.getAllBooks());
        int id = readInt("Enter book ID: ");
        int total = readInt("New total copies: ");
        if (id < 0 || total < 0) return;
        System.out.println(bookDao.updateTotalCopies(id, total) ? "Copies updated." : "Update failed.");
    }

    private void deleteBook() {
        printBooks(bookDao.getAllBooks());
        int id = readInt("Enter book ID to delete: ");
        if (id < 0) return;
        System.out.println(bookDao.deleteBook(id) ? "Book deleted." : "Delete failed (book not found).");
    }

    // =====================================================
    //                       USER MENU
    // =====================================================
    private void userMenu(User user) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n========== USER MENU (" + user.username() + ") ==========");
            System.out.println("1. View all books");
            System.out.println("2. Search books");
            System.out.println("3. Borrow a book");
            System.out.println("4. Return a book");
            System.out.println("5. My borrowing history");
            System.out.println("0. Logout");

            switch (readInt("Choose an option: ")) {
                case 1 -> printBooks(bookDao.getAllBooks());
                case 2 -> {
                    System.out.print("Search (title / author / ISBN / category): ");
                    printBooks(bookDao.searchBooks(scanner.nextLine().trim()));
                }
                case 3 -> {
                    printBooks(bookDao.getAllBooks());
                    int bookId = readInt("Enter book ID to borrow: ");
                    if (bookId >= 0) borrowDao.borrowBook(user.id(), bookId);
                }
                case 4 -> {
                    printBorrows(borrowDao.getBorrowsByUser(user.id()));
                    int txId = readInt("Enter transaction ID to return: ");
                    if (txId >= 0) borrowDao.returnBook(txId, user.id());
                }
                case 5 -> printBorrows(borrowDao.getBorrowsByUser(user.id()));
                case 0 -> {
                    loggedIn = false;
                    System.out.println("Logged out.");
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // =====================================================
    //                       HELPERS
    // =====================================================
    private int readInt(String prompt) {
        System.out.print(prompt);
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Palihog pagbutang og saktong numero.");
            return -1;
        }
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("(no users found)");
            return;
        }
        System.out.printf("%-5s %-20s %-10s %-10s%n", "ID", "USERNAME", "ROLE", "STATUS");
        for (User u : users) {
            System.out.printf("%-5d %-20s %-10s %-10s%n", u.id(), u.username(), u.roles(), u.status());
        }
    }

    private void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("(no books found)");
            return;
        }
        System.out.printf("%-5s %-30s %-20s %-14s %-12s %s%n",
                "ID", "TITLE", "AUTHOR", "ISBN", "CATEGORY", "AVAILABLE");
        for (Book b : books) {
            System.out.printf("%-5d %-30s %-20s %-14s %-12s %d/%d%n", b.id(), b.title(), b.author(),
                    b.isbn() == null ? "-" : b.isbn(), b.category() == null ? "-" : b.category(),
                    b.availableCopies(), b.totalCopies());
        }
    }

    private void printBorrows(List<BorrowRecord> records) {
        if (records.isEmpty()) {
            System.out.println("(no borrow records)");
            return;
        }
        System.out.printf("%-6s %-15s %-28s %-11s %-11s %-11s %-9s %s%n",
                "TX ID", "USER", "BOOK", "ISSUED", "DUE", "RETURNED", "STATUS", "FINE");
        for (BorrowRecord r : records) {
            System.out.printf("%-6d %-15s %-28s %-11s %-11s %-11s %-9s %.2f%n", r.transactionId(),
                    r.username(), r.title(), r.issueDate(), r.dueDate(),
                    r.returnDate() == null ? "-" : r.returnDate(), r.status(), r.fine());
        }
    }
}
