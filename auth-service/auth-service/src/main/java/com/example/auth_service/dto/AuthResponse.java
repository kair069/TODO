package com.example.auth_service.dto;

public class AuthResponse {
    private String token;
    private String username;
    private String message;

    // Constructor vacío
    public AuthResponse() {
    }

    // Constructor con token y username
    public AuthResponse(String token, String username) {
        this.token = token;
        this.username = username;
    }

    // Constructor completo
    public AuthResponse(String token, String username, String message) {
        this.token = token;
        this.username = username;
        this.message = message;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}