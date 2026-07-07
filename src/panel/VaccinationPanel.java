package panel;

import manager.RecordManager;
import manager.VaccinationManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class VaccinationPanel extends BasePanel {
    private final VaccinationManager mgr = new VaccinationManager();
    private final RecordManager       reg = RecordManager.get();
    private int view = 0; // 0=records 1=vaccines

    @Override public String   getTitle()   { return "Vaccination Tracking System"; }
    @Override public String[] getColumns() {
        if (view == 1) return new String[]{"ID","Vaccine","Disease","Manufacturer","Doses","Interval(days)"};
        return new String[]{"ID","Patient","Vaccine","Dose","Date Given","Next Due","Nurse"};
    }

    @Override public void populate(String q) {
        if (view == 1) {
            for (Vaccine v : q == null ? mgr.vaccines.getAll() : mgr.vaccines.search(q))
                model.addRow(v.toTableRow());
        } else {
            for (VaccRecord r : q == null ? mgr.records.getAll() : mgr.records.search(q))
                model.addRow(r.toTableRow());
        }
    }

    @Override public void handleAdd() {
        if (view == 1) addVaccine(); else recordVaccination();
    }

    private void addVaccine() {
        String[][] f = {
            {"name",     "Vaccine Name",          "TEXT"},
            {"dis",      "Target Disease",         "TEXT"},
            {"mfr",      "Manufacturer",           "TEXT"},
            {"doses",    "No. of Doses",           "NUMBER"},
            {"interval", "Days Between Doses",     "NUMBER"},
            {"price",    "Price (GHS)",            "NUMBER"}
        };
        FormDialog d = new FormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Vaccine", f);
        d.setVisible(true); if (!d.isConfirmed()) return;
        try {
            Vaccine v = new Vaccine(mgr.vaccines.nextId(),
                Validator.requireNonEmpty(d.get("name"), "Name"),
                d.get("dis"), d.get("mfr"),
                Validator.requirePositiveInt(d.get("doses"), "Doses"),
                Validator.requirePositiveInt(d.get("interval"), "Interval"),
                Validator.requirePositiveDouble(d.get("price"), "Price"));
            mgr.vaccines.add(v); refresh(); info("Vaccine added. ID: " + v.getId());
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    private void recordVaccination() {
        List<RecPatient> patients = reg.patients.getAll();
        List<Vaccine>    vaccines = mgr.vaccines.getAll();

        if (patients.isEmpty()) { error("No patients registered. Please add patients in the Patients section first."); return; }
        if (vaccines.isEmpty())  { error("No vaccines added yet. Please add vaccines first."); return; }

        String[] patItems = patients.stream()
            .map(p -> p.getId() + " — " + p.getName())
            .toArray(String[]::new);
        String[] vacItems = vaccines.stream()
            .map(v -> v.getId() + " — " + v.getName() + " (" + v.getDoses() + " doses)")
            .toArray(String[]::new);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Record Vaccination", true);
        dlg.setSize(480, 300);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> patBox   = new JComboBox<>(patItems);
        JComboBox<String> vacBox   = new JComboBox<>(vacItems);
        JTextField        doseField  = new JTextField("1");
        JTextField        nurseField = new JTextField();

        addRow(form, gc, 0, "Patient:",          patBox);
        addRow(form, gc, 1, "Vaccine:",          vacBox);
        addRow(form, gc, 2, "Dose Number:",      doseField);
        addRow(form, gc, 3, "Administered By:",  nurseField);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Record");
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
            RecPatient rp   = patients.get(patBox.getSelectedIndex());
            Vaccine    vac  = vaccines.get(vacBox.getSelectedIndex());
            int dose        = Validator.requirePositiveInt(doseField.getText().trim(), "Dose Number");
            String nurse    = nurseField.getText().trim();
            mgr.recordVaccination(rp.getId(), rp.getName(), vac.getId(), dose, nurse);
            refresh();
            String billNote = vac.getPrice() > 0 ? String.format("\nGHS %.2f added to pending charges.", vac.getPrice()) : "";
            info("Vaccination recorded.\nPatient: " + rp.getName() + "\nVaccine: " + vac.getName() + "  Dose: " + dose + billNote);
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    @Override public void handleView() {
        String id = selectedId(); if (id == null || view != 0) return;
        VaccRecord r = mgr.records.findById(id); if (r == null) return;
        FormDialog.showDetail((Frame) SwingUtilities.getWindowAncestor(this), "Vaccination Record",
            new String[][]{
                {"ID",             r.getId()},
                {"Patient",        r.getPatientName()},
                {"Vaccine",        r.getVaccineName()},
                {"Dose",           String.valueOf(r.getDose())},
                {"Date Given",     r.getDateGiven()},
                {"Next Due",       r.getNextDue()},
                {"Administered By",r.getNurse()}
            });
    }

    @Override public void handleEdit() { info("Vaccination records cannot be edited after entry."); }

    @Override public void handleDelete() {
        String id = selectedId(); if (id == null) return;
        if (!confirmDelete()) return;
        if (view == 0) mgr.records.remove(id); else mgr.vaccines.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton r  = AppTheme.btnInfo("Records");  r .setPreferredSize(new Dimension(90, 34));
        JButton v  = AppTheme.btnInfo("Vaccines"); v .setPreferredSize(new Dimension(90, 34));
        JButton od = AppTheme.btnWarning("Overdue"); od.setPreferredSize(new Dimension(90, 34));
        r .addActionListener(e -> { view = 0; rebuild(); refresh(); });
        v .addActionListener(e -> { view = 1; rebuild(); refresh(); });
        od.addActionListener(e -> showOverdue());
        p.add(r); p.add(v); p.add(od);
        return p;
    }

    private void showOverdue() {
        List<VaccRecord> list = mgr.getOverdue();
        if (list.isEmpty()) { info("No overdue vaccinations."); return; }
        StringBuilder sb = new StringBuilder("OVERDUE VACCINATIONS:\n\n");
        for (VaccRecord r : list)
            sb.append(r.getPatientName()).append(" — ").append(r.getVaccineName())
              .append(" Dose ").append(r.getDose()).append(" — due ").append(r.getNextDue()).append("\n");
        JOptionPane.showMessageDialog(this, sb.toString(), "Overdue", JOptionPane.WARNING_MESSAGE);
    }

    private void rebuild() { model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }

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
