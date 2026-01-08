package com.shankar.book_builder.auth.users;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserService users;

    public AppUserDetailsService(UserService users) {
        this.users = users;
    }

    @Override
    @NullMarked
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            return users.getUser(username);
        } catch (ResponseStatusException e) {
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
}
