package com.example.demo.user;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDto createUser(UserDtoRequest dto) {
        User user = User.from((dto));
        user.setPassword(passwordEncoder.encode(dto.password()));
        return UserDto.from(repository.save(user));
    }

    @Override
    public User getUser(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User with username: " + username + " not found")
                );
    }

    @Override
    public UserDto findUser(String username) {
        return repository.findByUsername(username)
                .map(UserDto::from)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User with username: " + username + " not found")
                );
    }

    @Override
    public UserDto updateUser(UserDtoRequest dto) {
        return repository.findByUsername(dto.username())
                .map(user -> {
                    user.setEmail(dto.email());
                    user.setPassword(passwordEncoder.encode(dto.password()));

                    return repository.save(user);
                })
                .map(UserDto::from)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with username: " + dto.username())
                );
    }

    @Override
    public void deleteUser(String username) {
        repository.findByUsername(username)
                .ifPresentOrElse(
                        repository::delete,
                        () -> {
                            throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "User not found with username: " + username
                            );
                        });
    }
}
