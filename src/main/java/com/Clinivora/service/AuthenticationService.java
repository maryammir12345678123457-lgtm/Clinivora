package com.Clinivora.service;

import com.Clinivora.model.AppUser;
import com.Clinivora.repository.AppUserRepository;
import com.Clinivora.util.PasswordHasher;

import java.sql.SQLException;
import java.util.Optional;

public class AuthenticationService {
    private final AppUserRepository userRepository;
    private final UserSession session;
    private final AuditService auditService;
    private final ValidationService validationService;

    public AuthenticationService(AppUserRepository userRepository,
                                 UserSession session,
                                 AuditService auditService,
                                 ValidationService validationService) {
        this.userRepository = userRepository;
        this.session = session;
        this.auditService = auditService;
        this.validationService = validationService;
    }

    public AppUser login(String username, String password) {
        String normalized = validationService.requireUsername(username);
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        try {
            Optional<AppUser> found = userRepository.findByUsername(normalized);
            if (found.isEmpty()) {
                throw new SecurityException("Invalid username or password");
            }
            AppUser user = found.get();
            if (!user.isActive()) {
                throw new SecurityException("Account is inactive");
            }
            if (!PasswordHasher.verify(password, user.getSalt(), user.getPasswordHash())) {
                throw new SecurityException("Invalid username or password");
            }
            session.setCurrentUser(user);
            auditService.record("LOGIN", "app_user", user.getId(), "User logged in");
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Login failed", e);
        }
    }

    public void logout() {
        Long userId = session.getUserId();
        session.clear();
        if (userId != null) {
            auditService.record("LOGOUT", "app_user", userId, "User logged out");
        }
    }
}
