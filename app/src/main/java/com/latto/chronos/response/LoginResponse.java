package com.latto.chronos.response;

import com.latto.chronos.models.User;

public class LoginResponse {
    public boolean success;
    public String message;
    public String token; // JWT
    public int token_expires_in; // en secondes
    public User user;
}

