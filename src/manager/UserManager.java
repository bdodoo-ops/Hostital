package manager;

import base.Manager;
import model.User;
import util.PasswordUtil;

public class UserManager {

    public final Manager<User> users = new Manager<User>("sec_users.txt") {
        @Override public User fromFileString(String l) { return User.fromFile(l); }
        @Override public String getIdPrefix() { return "U"; }
    };

    public UserManager() {
        if (users.count() == 0) {
            User admin = new User(users.nextId(), "admin", PasswordUtil.hash("1234"), "Administrator", "Admin", true);
            users.add(admin);
        }
    }

    public User authenticate(String username, String password) {
        if (username == null || username.isBlank()) return null;
        String hash = PasswordUtil.hash(password == null ? "" : password);
        for (User u : users.getAll()) {
            if (u.getUsername().equalsIgnoreCase(username.trim())
                    && u.getPasswordHash().equals(hash)
                    && u.isActive()) {
                return u;
            }
        }
        return null;
    }

    public User addUser(String username, String password, String fullName, String role) {
        boolean exists = users.getAll().stream()
            .anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        if (exists) throw new IllegalArgumentException("Username already exists.");
        User u = new User(users.nextId(), username.trim(), PasswordUtil.hash(password), fullName, role, true);
        users.add(u);
        return u;
    }

    public void updateProfile(String userId, String fullName, String role) {
        User u = users.findById(userId);
        if (u == null) throw new IllegalArgumentException("User not found.");
        if ("Admin".equalsIgnoreCase(u.getRole()) && !"Admin".equalsIgnoreCase(role)
                && u.isActive() && !hasOtherActiveAdmin(userId)) {
            throw new IllegalArgumentException("Cannot demote the only active administrator.");
        }
        u.setFullName(fullName);
        u.setRole(role);
        users.update(u);
    }

    public void setActive(String userId, boolean active) {
        User u = users.findById(userId);
        if (u == null) throw new IllegalArgumentException("User not found.");
        if (!active && "Admin".equalsIgnoreCase(u.getRole()) && !hasOtherActiveAdmin(userId)) {
            throw new IllegalArgumentException("Cannot deactivate the only active administrator.");
        }
        u.setActive(active);
        users.update(u);
    }

    public void resetPassword(String userId, String newPassword) {
        User u = users.findById(userId);
        if (u == null) throw new IllegalArgumentException("User not found.");
        u.setPasswordHash(PasswordUtil.hash(newPassword));
        users.update(u);
    }

    /** Self-service password change: requires the caller to know the current password. */
    public void changePassword(String userId, String currentPassword, String newPassword) {
        User u = users.findById(userId);
        if (u == null) throw new IllegalArgumentException("User not found.");
        if (!u.getPasswordHash().equals(PasswordUtil.hash(currentPassword))) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        u.setPasswordHash(PasswordUtil.hash(newPassword));
        users.update(u);
    }

    private boolean hasOtherActiveAdmin(String excludeUserId) {
        return users.getAll().stream()
            .anyMatch(u -> u.isActive() && "Admin".equalsIgnoreCase(u.getRole()) && !u.getId().equals(excludeUserId));
    }
}
