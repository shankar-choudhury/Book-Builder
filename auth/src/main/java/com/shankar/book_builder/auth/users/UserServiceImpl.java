package com.shankar.book_builder.auth.users;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder enc;

    @Override
    public UserDTO createUser(UserDTORequest dto) {
        User user = User.from(dto);
        user.setPassword(enc.encode(dto.password()));
        return UserDTO.from(repo.save(user));
    }

    @Override
    public User getUser(String username) {
        return repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with username: " + username + " not found"
                ));
    }

    @Override
    public UserDTO findUser(String username) {
        return repo.findByUsername(username)
                .map(UserDTO::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with username: " + username + " not found"
                ));
    }

    @Override
    public UserDTO updateUser(UserDTORequest dto) {
        return repo.findByUsername(dto.username())
                .map(u -> {
                    u.setEmail(dto.email());
                    u.setPassword(enc.encode(dto.password()));
                    return repo.save(u);
                })
                .map(UserDTO::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found with username: " + dto.username()
                ));
    }

    @Override
    public void deleteUser(String username) {
        repo.findByUsername(username)
                .ifPresentOrElse(
                        repo::delete,
                        () -> { throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"); }
                );
    }
}
