package com.example.demo.user;

public interface UserService {
    UserDto createUser(UserDtoRequest dto);
    User getUser(String username);
    UserDto findUser(String username);
    UserDto updateUser(UserDtoRequest dto);
    void deleteUser(String username);
}
