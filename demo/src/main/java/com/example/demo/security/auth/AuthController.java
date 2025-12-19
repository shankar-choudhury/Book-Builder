package com.example.demo.security.auth;

import static com.example.demo.security.CookieUtils.*;
import static com.example.demo.security.CookieUtils.TokenType.*;

import com.example.demo.security.refresh.RefreshToken;
import com.example.demo.security.refresh.RefreshTokenService;
import com.example.demo.user.User;
import com.example.demo.user.UserDtoRequest;
import com.example.demo.user.UserDto;
import com.example.demo.security.jwt.JwtService;
import com.example.demo.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshService;

    @Autowired
    public AuthController(AuthService authService, JwtService jwtService, UserService userService, RefreshTokenService refreshService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userService = userService;
        this.refreshService = refreshService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register (@RequestBody UserDtoRequest dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse httpResponse
    ) {
        UserDetails details = authService.authenticate(request);
        User user = (User) details;

        String sessionId = UUID.randomUUID().toString();

        issueAuthCookies(
                httpResponse,
                () -> jwtService.generateToken(user, sessionId),
                () -> refreshService.issue(user, sessionId)
        );

        return ResponseEntity.ok(new LoginResponse("Successful login"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            HttpServletRequest request,
            HttpServletResponse httpResponse
    ) {
        String refreshRaw = extractCookie(request, REFRESH_COOKIE)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Missing refresh token"
                        )
                );

        RefreshToken current = refreshService.verify(refreshRaw);

        issueAuthCookies(
                httpResponse,
                () -> jwtService.generateToken(
                        current.getUser(),
                        current.getSessionId()
                ),
                () -> refreshService.rotate(current)
        );

        return ResponseEntity.ok().build();
    }

    private void issueAuthCookies(
            HttpServletResponse response,
            Supplier<String> accessJwtSupplier,
            Supplier<String> refreshTokenSupplier
    ) {
        ResponseCookie jwtCookie = createCookie(
                JWT_COOKIE,
                accessJwtSupplier.get(),
                true,
                true
        );

        ResponseCookie refreshCookie = createCookie(
                REFRESH_COOKIE,
                refreshTokenSupplier.get(),
                true,
                true
        );

        response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
    }

}
