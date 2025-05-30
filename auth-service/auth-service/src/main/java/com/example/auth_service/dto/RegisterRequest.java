package com.example.auth_service.dto;

public class RegisterRequest {
    private String username;
    private String password;

    // Constructor vacío
    public RegisterRequest() {
    }

    // Constructor con todos los campos
    public RegisterRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters y Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}