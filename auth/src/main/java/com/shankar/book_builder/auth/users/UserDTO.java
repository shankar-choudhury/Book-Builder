package com.shankar.book_builder.auth.users;

public record UserDTO(String username, String email) {
    public static UserDTO from(User u) {
        return new UserDTO(u.getUsername(), u.getEmail());
    }
}
