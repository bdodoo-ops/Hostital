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
            User admin = new User(users.nextId(), "admin", PasswordUtil.hash("admin123"), "Administrator", "Admin");
            users.add(admin);
        }
    }

    public User authenticate(String username, String password) {
        if (username == null || username.isBlank()) return null;
        String hash = PasswordUtil.hash(password == null ? "" : password);
        for (User u : users.getAll()) {
            if (u.getUsername().equalsIgnoreCase(username.trim()) && u.getPasswordHash().equals(hash)) {
                return u;
            }
        }
        return null;
    }

    public User register(String username, String password, String fullName, String role) {
        boolean exists = users.getAll().stream()
            .anyMatch(u -> u.getUsername().equalsIgnoreCase(username.trim()));
        if (exists) throw new IllegalArgumentException("Username already exists.");
        User u = new User(users.nextId(), username.trim(), PasswordUtil.hash(password), fullName, role);
        users.add(u);
        return u;
    }
}
