package panel;

import manager.RecordManager;
import model.RecPatient;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;

/**
 * Central patient registry.
 * Shares the same RecordManager singleton used by Medical Records, Appointments, Lab, Billing, etc.
 */
public class PatientsPanel extends BasePanel {

    private final RecordManager mgr = RecordManager.get();

    @Override public String   getTitle()   { return "Patients Registry"; }
    @Override public String[] getColumns() {
        return new String[]{"ID", "Full Name", "Date of Birth", "Gender", "Blood Type", "Phone", "Address"};
    }

    @Override public void populate(String q) {
        for (RecPatient p : q == null ? mgr.patients.getAll() : mgr.patients.search(q))
            model.addRow(p.toTableRow());
    }

    @Override public void handleAdd() {
        String[][] f = {
            {"name",    "Full Name",              "TEXT"},
            {"dob",     "Date of Birth (YYYY-MM-DD)", "DATE"},
            {"gender",  "Gender",                 "SELECT", "Male|Female|Other"},
            {"phone",   "Phone",                  "TEXT"},
            {"address", "Address",                "TEXT"},
            {"blood",   "Blood Type",             "SELECT", "A+|A-|B+|B-|O+|O-|AB+|AB-"}
        };
        FormDialog d = new FormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Register New Patient", f);
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            String name = Validator.requireNonEmpty(d.get("name"), "Name");
            RecPatient p = new RecPatient(mgr.patients.nextId(), name,
                d.get("dob"), d.get("gender"), d.get("phone"), d.get("address"), d.get("blood"));
            mgr.patients.add(p);
            refresh();
            info("Patient registered successfully!\nID: " + p.getId() + "  |  Name: " + p.getName());
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleView() {
        String id = selectedId(); if (id == null) return;
        RecPatient p = mgr.patients.findById(id); if (p == null) return;
        FormDialog.showDetail((Frame) SwingUtilities.getWindowAncestor(this), "Patient Details",
            new String[][]{
                {"Patient ID",   p.getId()},
                {"Full Name",    p.getName()},
                {"Date of Birth",p.getDob()},
                {"Gender",       p.getGender()},
                {"Blood Type",   p.getBloodType()},
                {"Phone",        p.getPhone()},
                {"Address",      p.getAddress()}
            });
    }

    @Override public void handleEdit() {
        String id = selectedId(); if (id == null) return;
        RecPatient p = mgr.patients.findById(id); if (p == null) return;
        String[][] f = {
            {"name",    "Full Name",   "TEXT"},
            {"dob",     "Date of Birth","TEXT"},
            {"gender",  "Gender",      "SELECT", "Male|Female|Other"},
            {"phone",   "Phone",       "TEXT"},
            {"address", "Address",     "TEXT"},
            {"blood",   "Blood Type",  "SELECT", "A+|A-|B+|B-|O+|O-|AB+|AB-"}
        };
        FormDialog d = new FormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Edit Patient", f);
        d.set("name",    p.getName());
        d.set("dob",     p.getDob());
        d.set("gender",  p.getGender());
        d.set("phone",   p.getPhone());
        d.set("address", p.getAddress());
        d.set("blood",   p.getBloodType());
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            RecPatient updated = new RecPatient(id,
                Validator.requireNonEmpty(d.get("name"), "Name"),
                d.get("dob"), d.get("gender"), d.get("phone"), d.get("address"), d.get("blood"));
            mgr.patients.update(updated);
            refresh();
            info("Patient record updated.");
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleDelete() {
        String id = selectedId(); if (id == null) return;
        if (!confirmDelete()) return;
        mgr.patients.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(AppTheme.BG_MAIN);
        JLabel tip = new JLabel("  Patients registered here are available in all modules.");
        tip.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        tip.setForeground(new java.awt.Color(100, 120, 160));
        p.add(tip);
        return p;
    }
}
