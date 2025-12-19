package com.example.demo.security.jwt;

public class MissingJwtException extends SecurityException {
    public MissingJwtException(String message) {
        super(message);
    }

    public MissingJwtException(String message, Throwable cause) {
        super(message, cause);
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
