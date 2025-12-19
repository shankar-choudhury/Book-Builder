package com.example.demo.security.csrf.exception;

public class InvalidCsrfTokenException extends SecurityException {
    private final String tokenValue;

    public InvalidCsrfTokenException(String tokenValue, String message) {
        super(message);
        this.tokenValue = tokenValue;
    }

    public InvalidCsrfTokenException(String tokenValue, String message, Throwable cause) {
        super(message, cause);
        this.tokenValue = tokenValue;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    @Override
    public String toString() {
        return String.format("%s [token=%s]", super.toString(), tokenValue);
    }
}
