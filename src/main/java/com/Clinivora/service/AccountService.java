package com.Clinivora.service;

import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;
import com.Clinivora.repository.AppUserRepository;
import com.Clinivora.util.DateTimeUtil;
import com.Clinivora.util.PasswordHasher;

import java.sql.SQLException;
import java.util.List;

public class AccountService {
    private final AppUserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final ValidationService validationService;
    private final AuditService auditService;
    private final UserSession session;

    public AccountService(AppUserRepository userRepository,
                          AuthorizationService authorizationService,
                          ValidationService validationService,
                          AuditService auditService,
                          UserSession session) {
        this.userRepository = userRepository;
        this.authorizationService = authorizationService;
        this.validationService = validationService;
        this.auditService = auditService;
        this.session = session;
    }

    public List<AppUser> listUsers() {
        authorizationService.requireRole(UserRole.ADMIN);
        try {
            return userRepository.findAll();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to list users", e);
        }
    }

    public AppUser createUser(String username, String displayName, UserRole role, String password) {
        authorizationService.requireRole(UserRole.ADMIN);
        String normalized = validationService.requireUsername(username);
        validationService.requireStrongPassword(password);
        if (displayName == null || displayName.isBlank()) {
            throw new IllegalArgumentException("Display name is required");
        }
        if (role == null) {
            throw new IllegalArgumentException("Select a role");
        }
        try {
            if (userRepository.existsByUsername(normalized)) {
                throw new IllegalArgumentException("Username already exists");
            }
            String salt = PasswordHasher.generateSalt();
            AppUser user = new AppUser();
            user.setUsername(normalized);
            user.setSalt(salt);
            user.setPasswordHash(PasswordHasher.hash(password, salt));
            user.setRole(role);
            user.setDisplayName(displayName.trim());
            user.setActive(true);
            user.setCreatedAt(DateTimeUtil.now());
            user.setUpdatedAt(user.getCreatedAt());
            long id = userRepository.insert(user);
            user.setId(id);
            auditService.record("USER_CREATE", "app_user", id, normalized + ":" + role);
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create user", e);
        }
    }

    public void setActive(long userId, boolean active) {
        authorizationService.requireRole(UserRole.ADMIN);
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            if (session.getUserId() != null && session.getUserId() == userId && !active) {
                throw new IllegalStateException("Cannot deactivate your own account");
            }
            userRepository.setActive(userId, active);
            auditService.record(active ? "USER_ACTIVATE" : "USER_DEACTIVATE",
                    "app_user", userId, user.getUsername());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update user status", e);
        }
    }

    public void resetPassword(long userId, String newPassword) {
        authorizationService.requireRole(UserRole.ADMIN);
        validationService.requireStrongPassword(newPassword);
        try {
            AppUser user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
            String salt = PasswordHasher.generateSalt();
            userRepository.updatePassword(userId, PasswordHasher.hash(newPassword, salt), salt);
            auditService.record("USER_RESET_PASSWORD", "app_user", userId, user.getUsername());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to reset password", e);
        }
    }

    public void changePassword(String currentPassword, String newPassword, String confirmPassword) {
        if (!session.isAuthenticated()) {
            throw new SecurityException("Authentication required");
        }
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password confirmation does not match");
        }
        validationService.requireStrongPassword(newPassword);
        try {
            AppUser user = userRepository.findById(session.getUserId())
                    .orElseThrow(() -> new IllegalStateException("Current user not found"));
            if (!PasswordHasher.verify(currentPassword, user.getSalt(), user.getPasswordHash())) {
                throw new SecurityException("Current password is incorrect");
            }
            String salt = PasswordHasher.generateSalt();
            userRepository.updatePassword(user.getId(), PasswordHasher.hash(newPassword, salt), salt);
            auditService.record("USER_CHANGE_PASSWORD", "app_user", user.getId(), user.getUsername());
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to change password", e);
        }
    }
}
