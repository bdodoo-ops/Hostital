package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class User extends Entity implements Displayable {
    private String  username, passwordHash, fullName, role; // role: Admin | Staff
    private boolean active;

    public User(String id, String username, String passwordHash, String fullName, String role, boolean active) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public String  getUsername()              { return username; }
    public String  getPasswordHash()           { return passwordHash; }
    public void    setPasswordHash(String h)   { this.passwordHash = h; }
    public String  getFullName()               { return fullName; }
    public void    setFullName(String f)       { this.fullName = f; }
    public String  getRole()                   { return role; }
    public void    setRole(String r)           { this.role = r; }
    public boolean isActive()                  { return active; }
    public void    setActive(boolean a)        { this.active = a; }

    @Override public void validate() {
        Validator.requireNonEmpty(username, "Username");
        Validator.requireNonEmpty(passwordHash, "Password");
        Validator.requireNonEmpty(fullName, "Full name");
    }

    @Override public String toFileString() {
        return id + "|" + username + "|" + passwordHash + "|" + fullName + "|" + role + "|" + (active ? "1" : "0");
    }

    public static User fromFile(String l) {
        String[] p = l.split("\\|", -1);
        boolean active = p.length <= 5 || "1".equals(p[5]);
        return new User(p[0], p[1], p[2], p[3], p[4], active);
    }

    @Override public String[] toTableRow()      { return new String[]{id, username, fullName, role, active ? "Active" : "Inactive"}; }
    @Override public String   getDisplayName()  { return fullName + " (" + username + ")"; }
    @Override public String   getSummary()      { return role + " - " + (active ? "Active" : "Inactive"); }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Username","Full Name","Role","Status"}; }
}
