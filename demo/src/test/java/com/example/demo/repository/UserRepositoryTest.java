package com.example.demo.repository;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository repo;

    @Test
    public void findByUsername_WhenUserExists_ReturnsUser() {
        // Given
        User u1 = new User("user1", "user1@gmail.com", "password");
        repo.save(u1);

        // When
        var found = repo.findByUsername("user1");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("user1");
    }

    @Test
    public void findByUsername_WhenNonExists_ReturnsEmpty() {
        //When
        var found = repo.findByUsername("nonexistent");

        //Then
        assertThat(found).isEmpty();
    }


}
