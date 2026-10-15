package com.scc.model;

public record User(int id, String username, String password, String roles, String status) {

    // Keeps the old 4-argument constructor working (id is assigned by the database)
    public User(String username, String password, String roles, String status) {
        this(0, username, password, roles, status);
    }
}
