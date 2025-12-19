package com.example.demo.user;

import java.util.Arrays;
import java.util.Objects;

public record UserDtoRequest(String username, String email, String password) {

    private static void requireNonNullAndNonBlank(String... params) {
        if (Arrays.stream(params).anyMatch(str -> Objects.isNull(str) || str.isBlank()))
            throw new IllegalArgumentException("All fields must be present");
    }
}
