package com.migo.backend.exception;

public enum ErrorCode {
    INVALID_KEY(1001, "Invalid message key"),
    USERNAME_EXISTED(1002, "Username already exists"),
    EMAIL_EXISTED(1003, "Email already exists"),
    USERNAME_INVALID(1004, "Username must be at least 3 characters"),
    INVALID_PASSWORD(1005, "Password must be at least 8 characters");


    private int code;
    private String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    
}
