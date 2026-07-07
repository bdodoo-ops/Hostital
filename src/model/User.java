package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class User extends Entity implements Displayable {
    private String username, passwordHash, fullName, role; // role: Admin | Staff

    public User(String id, String username, String passwordHash, String fullName, String role) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
    }

    public String getUsername()          { return username; }
    public String getPasswordHash()      { return passwordHash; }
    public void   setPasswordHash(String h) { this.passwordHash = h; }
    public String getFullName()          { return fullName; }
    public String getRole()              { return role; }

    @Override public void validate() {
        Validator.requireNonEmpty(username, "Username");
        Validator.requireNonEmpty(passwordHash, "Password");
        Validator.requireNonEmpty(fullName, "Full name");
    }

    @Override public String toFileString() {
        return id + "|" + username + "|" + passwordHash + "|" + fullName + "|" + role;
    }

    public static User fromFile(String l) {
        String[] p = l.split("\\|", -1);
        return new User(p[0], p[1], p[2], p[3], p[4]);
    }

    @Override public String[] toTableRow()      { return new String[]{id, username, fullName, role}; }
    @Override public String   getDisplayName()  { return fullName + " (" + username + ")"; }
    @Override public String   getSummary()      { return role; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Username","Full Name","Role"}; }
}
