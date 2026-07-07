package panel;

import manager.AppointmentManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AppointmentPanel extends BasePanel {
    private final AppointmentManager mgr = new AppointmentManager();
    private final RecordManager       reg = RecordManager.get();

    @Override public String   getTitle()   { return "Appointment Booking"; }
    @Override public String[] getColumns() {
        return new String[]{"ID","Patient","Doctor","Date","Time","Reason","Status"};
    }

    @Override public void populate(String q) {
        List<Appointment> list = q == null ? mgr.appointments.getAll() : mgr.appointments.search(q);
        for (Appointment a : list) model.addRow(a.toTableRow());
    }

    @Override public void handleAdd() { bookAppointment(); }

    private void bookAppointment() {
        List<RecPatient> patients = reg.patients.getAll();
        List<RecDoctor>  doctors  = reg.doctors.getAll();

        if (patients.isEmpty()) { error("No patients registered. Please add patients in the Patients section first."); return; }
        if (doctors.isEmpty())  { error("No doctors registered. Please add doctors in the Doctors section first.");   return; }

        // Build dropdown items
        String[] patItems = patients.stream()
            .map(p -> p.getId() + "  |  " + p.getName())
            .toArray(String[]::new);
        String[] docItems = doctors.stream()
            .map(d -> d.getId() + "  |  " + d.getName() + "  (" + d.getSpecialization() + ")")
            .toArray(String[]::new);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Book Appointment", true);
        dlg.setSize(500, 340);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> patBox    = new JComboBox<>(patItems);
        JComboBox<String> docBox    = new JComboBox<>(docItems);
        JTextField        dateField = new JTextField();
        JTextField        timeField = new JTextField();
        JTextField        reasonField = new JTextField();

        addRow(form, gc, 0, "Patient:",        patBox);
        addRow(form, gc, 1, "Doctor:",         docBox);
        addRow(form, gc, 2, "Date (YYYY-MM-DD):", dateField);
        addRow(form, gc, 3, "Time (HH:MM):",   timeField);
        addRow(form, gc, 4, "Reason:",         reasonField);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Book Appointment");
        JButton cancel = AppTheme.btnDanger("Cancel");
        btns.add(cancel); btns.add(ok);
        dlg.add(btns, BorderLayout.SOUTH);

        boolean[] confirmed = {false};
        ok    .addActionListener(e -> { confirmed[0] = true; dlg.dispose(); });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.getRootPane().setDefaultButton(ok);
        dlg.setVisible(true);
        if (!confirmed[0]) return;

        try {
            RecPatient rp = patients.get(patBox.getSelectedIndex());
            RecDoctor  rd = doctors .get(docBox.getSelectedIndex());
            String date   = Validator.requireNonEmpty(dateField.getText().trim(), "Date");
            String time   = timeField.getText().trim();
            String reason = reasonField.getText().trim();
            mgr.book(rp.getId(), rp.getName(), rd.getId(), rd.getName(), date, time, reason);
            refresh();
            info("Appointment booked successfully!\n"
               + "Patient: " + rp.getName() + "\nDoctor: " + rd.getName()
               + "\nDate: " + date + (time.isEmpty() ? "" : "  Time: " + time));
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleView() {
        String id = selectedId(); if (id == null) return;
        Appointment a = mgr.appointments.findById(id); if (a == null) return;
        FormDialog.showDetail((Frame) SwingUtilities.getWindowAncestor(this), "Appointment Detail",
            new String[][]{
                {"Appointment ID", a.getId()},
                {"Patient ID",     a.getPatientId()},
                {"Patient",        a.getPatientName()},
                {"Doctor ID",      a.getDoctorId()},
                {"Doctor",         a.getDoctorName()},
                {"Date",           a.getDate()},
                {"Time",           a.getTime()},
                {"Reason",         a.getReason()},
                {"Status",         a.getStatus()}
            });
    }

    @Override public void handleEdit() {
        String id = selectedId(); if (id == null) return;
        Appointment a = mgr.appointments.findById(id); if (a == null) return;
        String newStatus = (String) JOptionPane.showInputDialog(this,
            "Update status for appointment " + id + ":", "Update Status",
            JOptionPane.QUESTION_MESSAGE, null,
            new String[]{"Scheduled","Completed","Cancelled"}, a.getStatus());
        if (newStatus == null) return;
        try { mgr.updateStatus(id, newStatus); refresh(); } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleDelete() {
        String id = selectedId(); if (id == null) return;
        if (!confirmDelete()) return;
        mgr.appointments.remove(id);
        refresh();
    }

    private void addRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(comp, gc);
    }
}
