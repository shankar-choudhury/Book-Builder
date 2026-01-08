package com.shankar.book_builder.auth.security.refresh;

import com.shankar.book_builder.auth.users.User;

public interface RefreshTokenService {
    String issue(User user, String sessionId);
    RefreshToken verify(String rawToken);
    String rotate(RefreshToken current);
    void revoke(RefreshToken token);
}
