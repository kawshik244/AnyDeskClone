package com.remotedesktop.server.model;

public class User {
    private String username;
    private String password; // stored as bcrypt hash, never plain text

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
