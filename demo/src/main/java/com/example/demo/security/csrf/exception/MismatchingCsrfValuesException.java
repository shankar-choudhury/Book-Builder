package com.example.demo.security.csrf.exception;

public class MismatchingCsrfValuesException extends SecurityException {
    private final String firstValue;
    private final String secondValue;

    public MismatchingCsrfValuesException(String expectedValue, String secondValue, String message) {
        super(message);
        this.firstValue = expectedValue;
        this.secondValue = secondValue;
    }

    public String getFirstValue() {
        return firstValue;
    }

    public String getSecondValue() {
        return secondValue;
    }

    @Override
    public String toString() {
        return String.format("%s [first_value=%s, second_value=%s]",
                super.toString(), firstValue, secondValue);
    }
}