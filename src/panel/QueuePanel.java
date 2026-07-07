package panel;

import manager.BillingManager;
import manager.QueueManager;
import manager.RecordManager;
import model.Bill;
import model.QueuePatient;
import model.RecPatient;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.List;

public class QueuePanel extends BasePanel {
    private final QueueManager   mgr     = new QueueManager();
    private final BillingManager billing = new BillingManager();

    @Override public String   getTitle()  { return "🏥  Hospital Queue Management"; }
    @Override public String[] getColumns(){ return new String[]{"ID","Name","Age","Gender","Department","Priority","Arrived","Status"}; }

    @Override public void populate(String q){
        List<QueuePatient> list = q==null ? mgr.getAll() : mgr.search(q);
        for(QueuePatient p : list) model.addRow(p.toTableRow());
    }

    @Override public void handleAdd() {
        List<RecPatient> patients = RecordManager.get().patients.getAll();

        // Build dialog manually so we can wire up the patient-selection listener
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  "Add Patient to Queue", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(480, 360);
        dlg.setLocationRelativeTo(this);
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setBackground(Color.WHITE);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new java.awt.Insets(5, 6, 5, 6);
        gc.fill   = java.awt.GridBagConstraints.HORIZONTAL;

        // Patient dropdown — "-- Walk-in (manual) --" + registered patients
        String[] patientItems = new String[patients.size() + 1];
        patientItems[0] = "-- Walk-in (enter name manually) --";
        for (int i = 0; i < patients.size(); i++)
            patientItems[i + 1] = patients.get(i).getId() + "  |  " + patients.get(i).getName();

        JComboBox<String> patientBox = new JComboBox<>(patientItems);
        JTextField nameField   = new JTextField();
        JTextField ageField    = new JTextField();
        ageField.setEditable(false);
        ageField.setBackground(new Color(240, 240, 240));
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Male","Female","Other"});
        JComboBox<String> deptBox   = new JComboBox<>(QueueManager.DEPARTMENTS);
        JComboBox<String> prioBox   = new JComboBox<>(
            new String[]{"1 - Emergency","2 - Urgent","3 - Normal"});

        // When a registered patient is selected, fill name/age/gender
        patientBox.addActionListener(e -> {
            int sel = patientBox.getSelectedIndex();
            if (sel == 0) {
                nameField.setEditable(true);
                nameField.setBackground(Color.WHITE);
                nameField.setText("");
                ageField.setText("");
                return;
            }
            RecPatient rp = patients.get(sel - 1);
            nameField.setEditable(false);
            nameField.setBackground(new Color(240, 240, 240));
            nameField.setText(rp.getName());
            genderBox.setSelectedItem(rp.getGender());
            // Calculate age from DOB
            try {
                LocalDate dob = LocalDate.parse(rp.getDob());
                int years = Period.between(dob, LocalDate.now()).getYears();
                ageField.setText(String.valueOf(years));
            } catch (DateTimeParseException ignored) {
                ageField.setText("");
            }
        });

        // Layout rows
        int row = 0;
        addRow(form, gc, row++, "Select Patient:", patientBox);
        addRow(form, gc, row++, "Full Name:",      nameField);
        addRow(form, gc, row++, "Age (years):",    ageField);
        addRow(form, gc, row++, "Gender:",         genderBox);
        addRow(form, gc, row++, "Department:",     deptBox);
        addRow(form, gc, row++, "Priority:",       prioBox);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        // Buttons
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Add to Queue");
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
            String name = Validator.requireNonEmpty(nameField.getText().trim(), "Name");
            String ageText = ageField.getText().trim();
            if (ageText.isEmpty()) throw new IllegalArgumentException("Age could not be determined. Please select a registered patient.");
            int age = Integer.parseInt(ageText);
            String gender = (String) genderBox.getSelectedItem();
            String dept   = (String) deptBox.getSelectedItem();
            int priority  = Integer.parseInt(prioBox.getSelectedItem().toString().substring(0, 1));
            mgr.addPatient(name, age, gender, dept, priority);
            refresh();
            info("Patient added to queue.");
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    /** Helper: add a label+component row to a GridBagLayout form. */
    private void addRow(JPanel form, java.awt.GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        comp.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        form.add(comp, gc);
    }

    @Override public void handleView(){
        String id = selectedId(); if(id==null) return;
        QueuePatient p = mgr.findById(id);
        if(p==null){ error("Record not found."); return; }

        RecPatient matched = findMatchedPatient(p.getName());
        String billInfo;
        if(matched==null){
            billInfo = "No registered patient match.";
        } else {
            double pendingAmt = billing.getPendingTotal(matched.getId(), "Queue Management");
            List<Bill> patientBills = billing.getBillsByPatient(matched.getId());
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Pending (not yet billed): GHS %.2f%n", pendingAmt));
            if(patientBills.isEmpty()) sb.append("No bills on record.");
            else for(Bill b : patientBills)
                sb.append(String.format("%s: GHS %.2f [%s]%n", b.getId(), b.getTotal(), b.getStatus()));
            billInfo = sb.toString();
        }

        FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Patient Detail",new String[][]{
            {"Queue ID",p.getId()},{"Name",p.getName()},{"Age",String.valueOf(p.getAge())},
            {"Gender",p.getGender()},{"Department",p.getDepartment()},
            {"Priority",p.getPriorityLabel()},{"Arrival",p.getArrivalTime()},{"Status",p.getStatus()},
            {"Temperature",p.getTemperature().isEmpty()?"—":p.getTemperature()+" °C"},
            {"Blood Pressure",p.getBloodPressure().isEmpty()?"—":p.getBloodPressure()},
            {"Pulse Rate",p.getPulseRate().isEmpty()?"—":p.getPulseRate()+" bpm"},
            {"Respiratory Rate",p.getRespRate().isEmpty()?"—":p.getRespRate()+" br/min"},
            {"O2 Saturation",p.getO2Level().isEmpty()?"—":p.getO2Level()+" %"},
            {"Weight",p.getWeight().isEmpty()?"—":p.getWeight()+" kg"},
            {"Height",p.getHeight().isEmpty()?"—":p.getHeight()+" cm"},
            {"Billing",billInfo}
        });
    }

    private void recordTriage(){
        String id = selectedId(); if(id==null) return;
        QueuePatient p = mgr.findById(id);
        if(p==null){ error("Record not found."); return; }

        String[][] f = {
            {"temp","Temperature (°C)","TEXT",p.getTemperature()},
            {"bp","Blood Pressure (e.g. 120/80)","TEXT",p.getBloodPressure()},
            {"pulse","Pulse Rate (bpm)","TEXT",p.getPulseRate()},
            {"resp","Respiratory Rate (breaths/min)","TEXT",p.getRespRate()},
            {"o2","O2 Saturation (%)","TEXT",p.getO2Level()},
            {"weight","Weight (kg)","TEXT",p.getWeight()},
            {"height","Height (cm)","TEXT",p.getHeight()}
        };
        FormDialog d = new FormDialog((Frame)SwingUtilities.getWindowAncestor(this), "Triage — "+p.getName(), f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            p.setTemperature(d.get("temp"));
            p.setBloodPressure(d.get("bp"));
            p.setPulseRate(d.get("pulse"));
            p.setRespRate(d.get("resp"));
            p.setO2Level(d.get("o2"));
            p.setWeight(d.get("weight"));
            p.setHeight(d.get("height"));
            mgr.update(p);
            refresh(); info("Triage recorded for "+p.getName()+".");
        }catch(Exception ex){ error(ex.getMessage()); }
    }

    /** Finds the registered patient whose name matches this queue entry's name, if any. */
    private RecPatient findMatchedPatient(String name){
        return RecordManager.get().patients.getAll().stream()
            .filter(rp -> rp.getName().equalsIgnoreCase(name))
            .findFirst().orElse(null);
    }

    @Override public void handleEdit(){
        String id = selectedId(); if(id==null) return;
        QueuePatient p = mgr.findById(id);
        if(p==null){ error("Not found."); return; }

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Queue Entry", true);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.setSize(480, 340);
        dlg.setLocationRelativeTo(this);
        dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        JPanel form = new JPanel(new java.awt.GridBagLayout());
        form.setBackground(Color.WHITE);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.insets = new java.awt.Insets(5, 6, 5, 6);
        gc.fill   = java.awt.GridBagConstraints.HORIZONTAL;

        JComboBox<String> deptBox = new JComboBox<>(QueueManager.DEPARTMENTS);
        deptBox.setSelectedItem(p.getDepartment());
        JComboBox<String> statusBox = new JComboBox<>(new String[]{"Waiting","Served"});
        statusBox.setSelectedItem(p.getStatus());

        List<RecPatient> regPatients = RecordManager.get().patients.getAll();
        String[] billPatientItems = new String[regPatients.size() + 1];
        billPatientItems[0] = "-- Select Patient --";
        int matchedIndex = 0;
        for (int i = 0; i < regPatients.size(); i++) {
            billPatientItems[i + 1] = regPatients.get(i).getId() + "  |  " + regPatients.get(i).getName();
            if (regPatients.get(i).getName().equalsIgnoreCase(p.getName())) matchedIndex = i + 1;
        }
        JComboBox<String> billPatientBox = new JComboBox<>(billPatientItems);
        billPatientBox.setSelectedIndex(matchedIndex);
        JTextField billField = new JTextField();

        int row = 0;
        addRow(form, gc, row++, "Department:",          deptBox);
        addRow(form, gc, row++, "Status:",              statusBox);
        addRow(form, gc, row++, "Bill For:",             billPatientBox);
        addRow(form, gc, row++, "Add Charge (GHS):",    billField);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Save");
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
            p.setStatus((String) statusBox.getSelectedItem());
            mgr.update(p);
            String amountText = billField.getText().trim();
            if(!amountText.isEmpty()){
                int sel = billPatientBox.getSelectedIndex();
                if(sel == 0) throw new IllegalArgumentException("Select a patient to bill.");
                RecPatient billPatient = regPatients.get(sel - 1);
                double amount = Validator.requirePositiveDouble(amountText, "Bill Amount");
                billing.addPendingCharge(billPatient.getId(), billPatient.getName(), "Queue Management", "Queue Management: Consultation", amount);
                info(String.format("GHS %.2f added to pending charges for %s.\nUse Billing > Add to finalize into a bill.",
                    amount, billPatient.getName()));
            }
            refresh();
        } catch(Exception ex){ error(ex.getMessage()); }
    }

    @Override public void handleDelete(){
        String id = selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        mgr.remove(id); refresh(); info("Record deleted.");
    }

    @Override protected JPanel extraButtons(){
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton serve = AppTheme.btnSuccess("▶  Serve Next");
        serve.setPreferredSize(new Dimension(120,34));
        serve.addActionListener(e -> serveNextDialog());
        JButton triage = AppTheme.btnWarning("🩺  Triage");
        triage.setPreferredSize(new Dimension(100,34));
        triage.addActionListener(e -> recordTriage());
        p.add(serve); p.add(triage);
        return p;
    }

    private void serveNextDialog(){
        String dept = (String) JOptionPane.showInputDialog(this,"Select Department:","Serve Next",
            JOptionPane.QUESTION_MESSAGE, null, QueueManager.DEPARTMENTS, QueueManager.DEPARTMENTS[0]);
        if(dept==null) return;
        QueuePatient served = mgr.serveNext(dept);
        if(served==null) info("No waiting patients in "+dept+".");
        else { info("Now serving: "+served.getName()+" ["+served.getPriorityLabel()+"]"); refresh(); }
    }

}
