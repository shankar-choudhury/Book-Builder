package com.example.demo.service;

import com.example.demo.user.UserDtoRequest;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import com.example.demo.user.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository repo;
    @InjectMocks
    private UserServiceImpl service;

    @Test
    public void createUser_ValidInput_ReturnsUserDto() {
        // Given
        var createDto = new UserDtoRequest("user1", "user1@gmail.com", "password");
        var saved = User.from(createDto);
        when(repo.save(any(User.class))).thenReturn(saved);

        // When
        var res = service.createUser(createDto);

        // Then
        assertThat(res.username()).isEqualTo("user1");
    }

    @Test
    public void findUser_WhenUserExists_ReturnsUserDto() {
        // Given
        var user = new User("user2", "user2@gmail.com", "password2");
        when(repo.findByUsername("user2")).thenReturn(Optional.of(user));

        // When
        var res = service.findUser("user2");

        //Then
        assertThat(res.username()).isEqualTo("user2");
    }

    @Test
    public void findUser_WhenNonExists_ThrowsException() {
        // Given
        when(repo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> service.findUser("nonexistent"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User with username: nonexistent not found");
    }
}
