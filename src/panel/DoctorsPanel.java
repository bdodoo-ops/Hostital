package panel;

import manager.RecordManager;
import model.RecDoctor;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;

/**
 * Central doctor registry.
 * Shares the same RecordManager singleton used by Medical Records, Appointments, and Lab.
 */
public class DoctorsPanel extends BasePanel {

    private final RecordManager mgr = RecordManager.get();

    @Override public String   getTitle()   { return "Doctors Registry"; }
    @Override public String[] getColumns() {
        return new String[]{"ID", "Full Name", "Specialization", "Phone"};
    }

    @Override public void populate(String q) {
        for (RecDoctor d : q == null ? mgr.doctors.getAll() : mgr.doctors.search(q))
            model.addRow(d.toTableRow());
    }

    @Override public void handleAdd() {
        String[][] f = {
            {"name", "Full Name",       "TEXT"},
            {"spec", "Specialization",  "SELECT",
             "General Practice|Cardiology|Neurology|Orthopedics|Pediatrics|Obstetrics|Gynecology|Dermatology|Oncology|Radiology|Surgery|Psychiatry|Emergency Medicine|Other"},
            {"phone","Phone",           "TEXT"}
        };
        FormDialog d = new FormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Register New Doctor", f);
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            String name = Validator.requireNonEmpty(d.get("name"), "Name");
            String spec = Validator.requireNonEmpty(d.get("spec"), "Specialization");
            RecDoctor dr = new RecDoctor(mgr.doctors.nextId(), name, spec, d.get("phone"));
            mgr.doctors.add(dr);
            refresh();
            info("Doctor registered successfully!\nID: " + dr.getId() + "  |  Name: " + dr.getName());
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleView() {
        String id = selectedId(); if (id == null) return;
        RecDoctor dr = mgr.doctors.findById(id); if (dr == null) return;
        FormDialog.showDetail((Frame) SwingUtilities.getWindowAncestor(this), "Doctor Details",
            new String[][]{
                {"Doctor ID",      dr.getId()},
                {"Full Name",      dr.getName()},
                {"Specialization", dr.getSpecialization()},
                {"Phone",          dr.getPhone()}
            });
    }

    @Override public void handleEdit() {
        String id = selectedId(); if (id == null) return;
        RecDoctor dr = mgr.doctors.findById(id); if (dr == null) return;
        String[][] f = {
            {"name", "Full Name",      "TEXT"},
            {"spec", "Specialization", "SELECT",
             "General Practice|Cardiology|Neurology|Orthopedics|Pediatrics|Obstetrics|Gynecology|Dermatology|Oncology|Radiology|Surgery|Psychiatry|Emergency Medicine|Other"},
            {"phone","Phone",          "TEXT"}
        };
        FormDialog d = new FormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Edit Doctor", f);
        d.set("name",  dr.getName());
        d.set("spec",  dr.getSpecialization());
        d.set("phone", dr.getPhone());
        d.setVisible(true);
        if (!d.isConfirmed()) return;
        try {
            RecDoctor updated = new RecDoctor(id,
                Validator.requireNonEmpty(d.get("name"), "Name"),
                Validator.requireNonEmpty(d.get("spec"), "Specialization"),
                d.get("phone"));
            mgr.doctors.update(updated);
            refresh();
            info("Doctor record updated.");
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleDelete() {
        String id = selectedId(); if (id == null) return;
        if (!confirmDelete()) return;
        mgr.doctors.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(AppTheme.BG_MAIN);
        JLabel tip = new JLabel("  Doctors registered here are available in all modules.");
        tip.setFont(new java.awt.Font("Segoe UI", java.awt.Font.ITALIC, 11));
        tip.setForeground(new java.awt.Color(100, 120, 160));
        p.add(tip);
        return p;
    }
}
