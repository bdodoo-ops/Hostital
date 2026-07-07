package panel;

import manager.UserManager;
import model.User;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class UsersPanel extends BasePanel {
    private final UserManager mgr = new UserManager();
    private final User        currentUser;

    public UsersPanel(User currentUser) { this.currentUser = currentUser; }

    @Override public String   getTitle()   { return "User Management"; }
    @Override public String[] getColumns() { return new String[]{"ID","Username","Full Name","Role","Status"}; }

    @Override public void populate(String q) {
        List<User> list = q == null ? mgr.users.getAll() : mgr.users.search(q);
        for (User u : list) model.addRow(u.toTableRow());
    }

    @Override public void handleAdd() {
        String[][] f = {
            {"username","Username","TEXT"},
            {"password","Initial Password","TEXT"},
            {"fullName","Full Name","TEXT"},
            {"role","Role","SELECT","Admin|Staff"}
        };
        FormDialog d = new FormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add User", f);
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            User u = mgr.addUser(
                Validator.requireNonEmpty(d.get("username"), "Username"),
                Validator.requireNonEmpty(d.get("password"), "Password"),
                Validator.requireNonEmpty(d.get("fullName"), "Full name"),
                d.get("role"));
            refresh();
            info("User added. ID: " + u.getId());
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleView() {
        String id = selectedId(); if (id == null) return;
        User u = mgr.users.findById(id); if (u == null) return;
        FormDialog.showDetail((Frame) SwingUtilities.getWindowAncestor(this), "User Detail", new String[][]{
            {"ID", u.getId()},
            {"Username", u.getUsername()},
            {"Full Name", u.getFullName()},
            {"Role", u.getRole()},
            {"Status", u.isActive() ? "Active" : "Inactive"}
        });
    }

    @Override public void handleEdit() {
        String id = selectedId(); if (id == null) return;
        User u = mgr.users.findById(id); if (u == null) return;
        String[][] f = {
            {"fullName","Full Name","TEXT",u.getFullName()},
            {"role","Role","SELECT","Admin|Staff"}
        };
        FormDialog d = new FormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit User", f);
        d.set("role", u.getRole());
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            mgr.updateProfile(id, Validator.requireNonEmpty(d.get("fullName"), "Full name"), d.get("role"));
            refresh();
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleDelete() {
        String id = selectedId(); if (id == null) return;
        if (!confirmDelete()) return;
        try { mgr.users.remove(id); refresh(); }
        catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override protected JPanel extraButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(AppTheme.BG_MAIN);

        JButton toggleBtn = AppTheme.btnWarning("Activate / Deactivate");
        toggleBtn.setPreferredSize(new Dimension(170, 34));
        toggleBtn.addActionListener(e -> toggleActive());
        p.add(toggleBtn);

        JButton resetBtn = AppTheme.btnInfo("Reset Password");
        resetBtn.setPreferredSize(new Dimension(140, 34));
        resetBtn.addActionListener(e -> resetPassword());
        p.add(resetBtn);

        return p;
    }

    private void toggleActive() {
        String id = selectedId(); if (id == null) return;
        User u = mgr.users.findById(id); if (u == null) return;
        if (u.getId().equals(currentUser.getId())) { warn("You cannot deactivate your own account."); return; }
        try {
            mgr.setActive(id, !u.isActive());
            refresh();
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    private void resetPassword() {
        String id = selectedId(); if (id == null) return;
        User u = mgr.users.findById(id); if (u == null) return;
        String[][] f = {{"password","New Password","TEXT"}};
        FormDialog d = new FormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Reset Password - " + u.getUsername(), f);
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            mgr.resetPassword(id, Validator.requireNonEmpty(d.get("password"), "Password"));
            info("Password reset for " + u.getUsername() + ".");
        } catch (Exception ex) { error(ex.getMessage()); }
    }
}
