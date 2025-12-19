package com.example.demo.security.csrf.exception;

public class MissingCsrfTokenException extends SecurityException {
    public MissingCsrfTokenException(String message) {
        super(message);
    }

    public MissingCsrfTokenException(String message, Throwable cause) {
        super(message, cause);
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
