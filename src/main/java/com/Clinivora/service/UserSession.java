package com.Clinivora.service;

import com.Clinivora.model.AppUser;
import com.Clinivora.model.enums.UserRole;

public class UserSession {
    private AppUser currentUser;

    public void setCurrentUser(AppUser user) {
        this.currentUser = user;
    }

    public AppUser getCurrentUser() {
        return currentUser;
    }

    public boolean isAuthenticated() {
        return currentUser != null;
    }

    public UserRole getRole() {
        return currentUser == null ? null : currentUser.getRole();
    }

    public Long getUserId() {
        return currentUser == null ? null : currentUser.getId();
    }

    public void clear() {
        currentUser = null;
    }
}
