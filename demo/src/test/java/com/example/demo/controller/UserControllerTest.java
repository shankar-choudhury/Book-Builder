package com.example.demo.controller;

import com.example.demo.security.config.SecurityConfiguration;
import com.example.demo.security.csrf.CsrfAuthFilter;
import com.example.demo.security.csrf.CsrfService;
import com.example.demo.security.csrf.CsrfValidatorFilter;
import com.example.demo.security.jwt.JwtAuthFilter;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserDtoRequest;
import com.example.demo.user.UserService;
import com.example.demo.user.UserController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
@Import({
        SecurityConfiguration.class,
        JwtAuthFilter.class,
        CsrfAuthFilter.class,
        CsrfValidatorFilter.class
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private JwtService jwtService; // Required for JwtAuthFilter
    @MockitoBean
    private CsrfService csrfService; // Required for CsrfAuthFilter
    @MockitoBean
    private AuthenticationProvider authenticationProvider;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final UserDto testUserDto = new UserDto("testuser", "test@example.com");

    // ==================== GET /users/me (Authenticated User) ====================
    @Test
    @WithMockUser(username = "testuser") // Simulates authenticated user
    void getAuthenticatedUser_WhenAuthenticated_ReturnsUserDto() throws Exception {
        when(userService.findUser("testuser")).thenReturn(testUserDto);

        mockMvc.perform(get("/users/me")
                        .with(csrf())) // Include CSRF token
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getAuthenticatedUser_WhenUnauthenticated_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isForbidden()); // Blocked by JwtAuthFilter
    }

    // ==================== GET /users/find-user (Public Endpoint) ====================
    @Test
    void getAUser_WhenUserExists_ReturnsUserDto() throws Exception {
        when(userService.findUser("testuser")).thenReturn(testUserDto);

        mockMvc.perform(get("/users/find-user")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void getAUser_WhenUserNotExists_ReturnsNotFound() throws Exception {
        when(userService.findUser("unknown")).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        mockMvc.perform(get("/users/find-user")
                        .param("username", "unknown"))
                .andExpect(status().isNotFound());
    }

    // ==================== PUT /users/update (Secured Update) ====================
    @Test
    @WithMockUser(username = "testuser")
    void updateUser_WhenAuthorized_UpdatesUser() throws Exception {
        UserDtoRequest updateRequest = new UserDtoRequest("testuser", "new@example.com", "newpassword");
        when(userService.updateUser(updateRequest)).thenReturn(testUserDto);

        mockMvc.perform(put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "username": "testuser",
                        "email": "new@example.com",
                        "password": "newpassword"
                    }
                    """)
                        .with(csrf())) // CSRF token required for PUT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser(username = "otheruser") // Different user attempts update
    void updateUser_WhenUnauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(put("/users/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "username": "testuser", // Attempt to update another user
                        "email": "hacked@example.com",
                        "password": "hacked"
                    }
                    """)
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Blocked by @PreAuthorize
    }

    // ==================== DELETE /users/{username} (Secured Deletion) ====================
    @Test
    @WithMockUser(username = "testuser")
    void deleteUser_WhenOwner_DeletesUser() throws Exception {
        mockMvc.perform(delete("/users/testuser")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("testuser");
    }

    @Test
    @WithMockUser(username = "adminuser", roles = "ADMIN") // Admin can delete any user
    void deleteUser_WhenAdmin_DeletesUser() throws Exception {
        mockMvc.perform(delete("/users/testuser")
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser("testuser");
    }

    @Test
    @WithMockUser(username = "otheruser") // Non-admin, non-owner
    void deleteUser_WhenUnauthorized_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/users/testuser")
                        .with(csrf()))
                .andExpect(status().isForbidden()); // Blocked by @PreAuthorize
    }
}
