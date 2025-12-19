package com.example.demo.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getAuthenticatedUser() {

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok( UserDto.from(user));
    }

    @GetMapping("/find-user")
    public ResponseEntity<UserDto> getAUser(@RequestParam String username) {
        return ResponseEntity.ok(service.findUser(username));
    }

    @PutMapping("/update")
    @PreAuthorize("#dto.username == authentication.principal.username")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDtoRequest dto) {
        return ResponseEntity.ok(service.updateUser(dto));
    }

    @DeleteMapping("{username}")
    @PreAuthorize("#username == authentication.principal.username or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable String username) {
        service.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

}
