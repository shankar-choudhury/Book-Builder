package com.shankar.book_builder.auth.security.auth;

import com.shankar.book_builder.auth.security.config.AppSecurityProperties;
import com.shankar.book_builder.auth.security.jwt.JwtService;
import com.shankar.book_builder.auth.security.refresh.RefreshToken;
import com.shankar.book_builder.auth.security.refresh.RefreshTokenService;
import com.shankar.book_builder.auth.users.User;
import com.shankar.book_builder.auth.users.UserDTO;
import com.shankar.book_builder.auth.users.UserDTORequest;
import com.shankar.book_builder.auth.users.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static com.shankar.book_builder.auth.security.CookieUtils.TokenType.JWT;
import static com.shankar.book_builder.auth.security.CookieUtils.TokenType.REFRESH;
import static com.shankar.book_builder.auth.security.CookieUtils.add;
import static com.shankar.book_builder.auth.security.CookieUtils.extract;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserService userService;
    private final RefreshTokenService refreshService;
    private final AppSecurityProperties props;

    /**
     * Call this first from SPA to ensure XSRF-TOKEN cookie is issued.
     * Spring Security will generate and persist the token via CookieCsrfTokenRepository.
     */
    @GetMapping("/csrf")
    public ResponseEntity<@NonNull Map<String,String>> csrf(CsrfToken token) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(Map.of(
                        "headerName", token.getHeaderName(),
                        "parameterName", token.getParameterName(),
                        "token", token.getToken()
                ));
    }

    @PostMapping("/register")
    public ResponseEntity<@NonNull UserDTO> register(@RequestBody UserDTORequest dto) {
        return ResponseEntity.ok(userService.createUser(dto));
    }

    /**
     * ✅ CSRF REQUIRED here (POST). Frontend must send X-XSRF-TOKEN header.
     */
    @PostMapping("/login")
    public ResponseEntity<@NonNull String> login(
            @RequestBody LoginRequest request,
            HttpServletResponse res
    ) {
        UserDetails details = authService.authenticate(request);
        User user = (User) details;

        String sessionId = UUID.randomUUID().toString();

        issueAuthCookies(
                res,
                () -> jwtService.issue(user, sessionId),
                () -> refreshService.issue(user, sessionId)
        );

        return ResponseEntity.ok("successful login");
    }

    @PostMapping("/refresh")
    public ResponseEntity<@NonNull Void> refresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshRaw = extract(req, REFRESH)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Missing refresh token"
                        )
                );

        RefreshToken current = refreshService.verify(refreshRaw);

        issueAuthCookies(
                res,
                () -> jwtService.issue(
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
        add(
                response,
                JWT,
                accessJwtSupplier.get(),
                true,
                props
        );

        add(
                response,
                REFRESH,
                refreshTokenSupplier.get(),
                true,
                props
        );
    }
}
