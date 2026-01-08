package com.shankar.book_builder.auth.users;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public ResponseEntity<@NonNull UserDTO> me() {
        User user = (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        return ResponseEntity.ok(UserDTO.from(user));
    }

    @GetMapping("/find-user")
    public ResponseEntity<@NonNull UserDTO> find(@RequestParam String username) {
        return ResponseEntity.ok(service.findUser(username));
    }

    @PutMapping("/update")
    @PreAuthorize("#dto.username == authentication.principal.username")
    public ResponseEntity<@NonNull UserDTO> update(@RequestBody UserDTORequest dto) {
        return ResponseEntity.ok(service.updateUser(dto));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<@NonNull Void> delete(@PathVariable String username) {
        service.deleteUser(username);
        return ResponseEntity.noContent().build();
    }
}
