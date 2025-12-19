package com.example.demo.controller;

import com.example.demo.user.UserDtoRequest;
import com.example.demo.security.auth.LoginRequest;
import com.example.demo.user.UserDto;
import com.example.demo.security.auth.AuthController;
import com.example.demo.security.auth.AuthService;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {
    @Mock
    private AuthService authService;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserService userService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @InjectMocks
    private AuthController controller;

    // register
    @Test
    public void register_ValidRequest_ReturnsOkAndResponse() {
        // given
        var createDto = new UserDtoRequest("user1", "user1@gmail.com", "password");
        var expectedDto = new UserDto("user1", "user1@gmail.com");

        when(userService.createUser(createDto)).thenReturn(expectedDto);

        // when
        var response = controller.register(createDto);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedDto);
        verify(userService, times(1)).createUser(createDto);
    }

    // authenticate
    @Test
    public void login_ValidCredentials_ReturnsTokenAndSetsCookies() {
        // given
        var loginRequest = new LoginRequest("testuser", "password");
        UserDetails userDetails = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .authorities(Collections.emptyList())
                .build();

        when(authService.authenticate(loginRequest)).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("generated.jwt.token");

        CsrfToken mockCsrfToken = mock(CsrfToken.class);
        when(mockCsrfToken.getToken()).thenReturn("generated-csrf-token");
        when(request.getAttribute("_csrf")).thenReturn(mockCsrfToken);

        // when
        var controllerResponse = controller.login(
                loginRequest,
                response
        );

        // then
        assertThat(controllerResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<String> cookieCaptor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(eq(HttpHeaders.SET_COOKIE), cookieCaptor.capture());

        String jwtCookie = cookieCaptor.getValue();
        assertThat(jwtCookie).contains("JWTCookie=generated.jwt.token");
        assertThat(jwtCookie).contains("HttpOnly");
        assertThat(jwtCookie).contains("Secure");
        assertThat(jwtCookie).contains("SameSite=None");
        assertThat(jwtCookie).contains("Max-Age=86400");

    }

    // logout

}
