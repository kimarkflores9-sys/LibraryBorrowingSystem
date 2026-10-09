package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import javax.swing.JOptionPane;

public class MenuUI {
    private final UserDao userDao = new UserDao();
    private User currentUser = null;

    public void start() {
        while (true) {
            if (currentUser == null) {
                showMainMenu();
            } else {
                showUserMenu();
            }
        }
    }

    private void showMainMenu() {
        String[] options = {"Register Account", "Login", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select an action:",
                "Student Library Borrowing System",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0 -> handleRegister();
            case 1 -> handleLogin();
            case 2, -1 -> System.exit(0);
        }
    }

    private void handleRegister() {
        String username = JOptionPane.showInputDialog(null, "Enter Username:");
        if (username == null || username.trim().isEmpty()) return;

        String password = JOptionPane.showInputDialog(null, "Enter Password:");
        if (password == null || password.trim().isEmpty()) return;

        String[] roles = {"TEACHER", "STUDENT"};
        int roleChoice = JOptionPane.showOptionDialog(
                null,
                "Select Role:",
                "Register Role",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                roles,
                roles[1]
        );

        if (roleChoice == -1) return;
        String role = roles[roleChoice];

        if (userDao.registerUser(username, password, role)) {
            JOptionPane.showMessageDialog(null, "Registered successfully! Status: PENDING", "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Registration failed. Username might exist.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogin() {
        String username = JOptionPane.showInputDialog(null, "Enter Username:");
        if (username == null || username.trim().isEmpty()) return;

        String password = JOptionPane.showInputDialog(null, "Enter Password:");
        if (password == null || password.trim().isEmpty()) return;

        User user = userDao.login(username, password);
        if (user != null) {
            currentUser = user;
            JOptionPane.showMessageDialog(null, "Login successful! Welcome " + currentUser.username() + " [" + currentUser.roles() + "]");
        } else {
            JOptionPane.showMessageDialog(null, "Invalid username or password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserMenu() {
        String[] options = {"Logout", "Exit"};
        int choice = JOptionPane.showOptionDialog(
                null,
                "Logged in as: " + currentUser.username() + "\nRole: " + currentUser.roles() + "\nStatus: " + currentUser.status(),
                "User Dashboard",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == 0) {
            currentUser = null;
            JOptionPane.showMessageDialog(null, "Logged out successfully.");
        } else {
            System.exit(0);
        }
    }
}