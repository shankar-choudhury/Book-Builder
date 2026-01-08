package com.shankar.book_builder.auth.users;

public interface UserService {
    UserDTO createUser(UserDTORequest dto);
    User getUser(String username);
    UserDTO findUser(String username);
    UserDTO updateUser(UserDTORequest dto);
    void deleteUser(String username);
}
