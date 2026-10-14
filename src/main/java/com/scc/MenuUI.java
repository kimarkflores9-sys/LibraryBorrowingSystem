package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class MenuUI {
    private UserDao userDao = new UserDao();
    private Scanner scanner = new Scanner(System.in);

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println("\n==================================");
            System.out.println("      LIBRARY / USER SYSTEM       ");
            System.out.println("==================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = -1;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Palihog pagbutang og saktong numero.");
                continue;
            }

            switch (choice) {
                case 1:
                    registerUser();
                    break;
                case 2:
                    loginUser();
                    break;
                case 3:
                    running = false;
                    System.out.println("Nag-exit na sa sistema. Salamat!");
                    break;
                default:
                    System.out.println("Pilian nga wala sa lista. Sulayi pag-usab.");
            }
        }
    }

    private void registerUser() {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter role (TEACHER / STUDENT): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (!role.equals("TEACHER") && !role.equals("STUDENT")) {
            System.out.println("Ang role kinahanglan TEACHER o STUDENT ra!");
            return;
        }

        User user = new User(username, password, role, "PENDING");
        boolean success = userDao.registerUser(user);

        if (success) {
            System.out.println("Registration successful! Account status: PENDING");
        } else {
            System.out.println("Registration failed (Username might already exist).");
        }
    }

    private void loginUser() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        boolean success = userDao.login(username, password);
        if (success) {
            System.out.println("Login successful!");
        } else {
            System.out.println("Login failed! Check your username/password.");
        }
    }
}