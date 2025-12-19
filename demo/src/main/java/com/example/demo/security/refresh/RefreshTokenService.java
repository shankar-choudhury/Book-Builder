package com.example.demo.security.refresh;

import com.example.demo.user.User;

public interface RefreshTokenService {
    String issue(User user, String sessionId);
    RefreshToken verify(String rawToken);
    String rotate(RefreshToken current);
    void revoke(RefreshToken token);
}
