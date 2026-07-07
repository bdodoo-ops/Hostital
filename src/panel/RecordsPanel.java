package panel;

import manager.LabManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RecordsPanel extends BasePanel {
    private final RecordManager mgr = RecordManager.get();
    private final LabManager    lab = new LabManager();
    private int view = 0; // 0=records 1=patients 2=doctors

    @Override public String   getTitle()  { return "📋  Patient Medical Records"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"ID","Name","DOB","Gender","Blood","Phone","Address"};
        if(view==2) return new String[]{"ID","Name","Specialization","Phone"};
        return new String[]{"ID","Patient","Doctor","Date","Diagnosis","Treatment"};
    }

    @Override public void populate(String q){
        if(view==1){ List<RecPatient> l=q==null?mgr.patients.getAll():mgr.patients.search(q); for(RecPatient p:l) model.addRow(p.toTableRow()); }
        else if(view==2){ List<RecDoctor> l=q==null?mgr.doctors.getAll():mgr.doctors.search(q); for(RecDoctor d:l) model.addRow(d.toTableRow()); }
        else { List<MedRecord> l=q==null?mgr.records.getAll():mgr.records.search(q); for(MedRecord r:l) model.addRow(r.toTableRow()); }
    }

    @Override public void handleAdd(){
        if(view==1){ addPatient(); } else if(view==2){ addDoctor(); } else { addRecord(); }
    }

    private void addPatient(){
        String[][] f={{"name","Full Name","TEXT"},{"dob","Date of Birth","TEXT","YYYY-MM-DD"},
                      {"gender","Gender","SELECT","Male|Female|Other"},{"phone","Phone","TEXT"},
                      {"address","Address","TEXT"},{"blood","Blood Type","SELECT","A+|A-|B+|B-|O+|O-|AB+|AB-"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Register Patient",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            RecPatient p=new RecPatient(mgr.patients.nextId(),
                Validator.requireNonEmpty(d.get("name"),"Name"),d.get("dob"),d.get("gender"),
                d.get("phone"),d.get("address"),d.get("blood"));
            mgr.patients.add(p); refresh(); info("Patient registered. ID: "+p.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void addDoctor(){
        String[][] f={{"name","Full Name","TEXT"},{"spec","Specialization","TEXT"},{"phone","Phone","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Doctor",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            RecDoctor dr=new RecDoctor(mgr.doctors.nextId(),
                Validator.requireNonEmpty(d.get("name"),"Name"),d.get("spec"),d.get("phone"));
            mgr.doctors.add(dr); refresh(); info("Doctor added. ID: "+dr.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void addRecord() {
        List<RecPatient> patients = mgr.patients.getAll();
        List<RecDoctor>  doctors  = mgr.doctors.getAll();

        if (patients.isEmpty()) { error("No patients registered. Please add patients first via the Patients section."); return; }
        if (doctors.isEmpty())  { error("No doctors registered. Please add doctors first via the Doctors section.");   return; }

        // Build patient items: "P0001  |  John Doe"
        String[] patientItems = new String[patients.size()];
        for (int i = 0; i < patients.size(); i++)
            patientItems[i] = patients.get(i).getId() + "  |  " + patients.get(i).getName();

        // Build doctor items: "D0001  |  Dr. Smith  (Cardiology)"
        String[] doctorItems = new String[doctors.size()];
        for (int i = 0; i < doctors.size(); i++)
            doctorItems[i] = doctors.get(i).getId() + "  |  " + doctors.get(i).getName()
                           + "  (" + doctors.get(i).getSpecialization() + ")";

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Medical Record", true);
        dlg.setSize(520, 460);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> patBox  = new JComboBox<>(patientItems);
        JComboBox<String> docBox  = new JComboBox<>(doctorItems);
        JTextField diagField  = new JTextField();
        JTextField treatField = new JTextField();
        JTextField prescField = new JTextField();
        JTextArea  notesArea  = new JTextArea(3, 20);
        notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true);

        int row = 0;
        addFormRow(form, gc, row++, "Patient:",      patBox);
        addFormRow(form, gc, row++, "Doctor:",       docBox);
        addFormRow(form, gc, row++, "Diagnosis:",    diagField);
        addFormRow(form, gc, row++, "Treatment:",    treatField);
        addFormRow(form, gc, row++, "Prescription:", prescField);

        // Notes row with scroll pane
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel notesLbl = new JLabel("Notes:");
        notesLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(notesLbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        JScrollPane notesSp = new JScrollPane(notesArea);
        notesSp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(notesSp, gc);

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Save Record");
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
            String diag = Validator.requireNonEmpty(diagField.getText().trim(), "Diagnosis");
            mgr.addRecord(rp.getId(), rp.getName(), rd.getId(), rd.getName(),
                          diag, treatField.getText().trim(),
                          prescField.getText().trim(), notesArea.getText().trim());
            refresh();
            info("Medical record saved.");
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    private void addFormRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(comp, gc);
    }

    private void showPatientHistory(){
        if(mgr.patients.count()==0){ info("No patients registered."); return; }
        String[] pOpts=mgr.patients.getAll().stream().map(p->p.getId()+" - "+p.getName()).toArray(String[]::new);
        String[][] pickF={{"pid","Patient","SELECT",String.join("|",pOpts)}};
        FormDialog pick=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Select Patient",pickF);
        pick.setSize(500,pick.getHeight());
        pick.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        pick.setVisible(true); if(!pick.isConfirmed()) return;
        String pid=pick.get("pid").split(" ")[0];
        RecPatient patient=mgr.patients.findById(pid); if(patient==null) return;

        JDialog dlg=new JDialog((Frame)SwingUtilities.getWindowAncestor(this),"Patient History — "+patient.getName(),true);
        dlg.setSize(860,660);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10,10));
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel header=new JPanel(new GridLayout(2,3,10,4));
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(14,16,10,16));
        header.add(historyLabel("Name: "+patient.getName()));
        header.add(historyLabel("DOB: "+patient.getDob()));
        header.add(historyLabel("Gender: "+patient.getGender()));
        header.add(historyLabel("Blood Type: "+patient.getBloodType()));
        header.add(historyLabel("Phone: "+patient.getPhone()));
        header.add(historyLabel("Address: "+patient.getAddress()));
        dlg.add(header, BorderLayout.NORTH);

        JPanel body=new JPanel(new GridLayout(2,1,0,10));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(0,16,10,16));

        List<MedRecord> records=mgr.getRecordsByPatient(pid);
        DefaultTableModel recModel=new DefaultTableModel(
            new String[]{"Date","Doctor","Diagnosis","Treatment","Prescription","Notes"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        for(MedRecord r:records) recModel.addRow(new String[]{
            r.getDate(),r.getDoctorName(),r.getDiagnosis(),r.getTreatment(),r.getPrescription(),r.getNotes()});
        JTable recTable=new JTable(recModel);
        recTable.setRowHeight(24);
        body.add(historySection("Medical Records ("+records.size()+")",recTable));

        List<TestResult> results=lab.getResultsByPatient(pid);
        DefaultTableModel resModel=new DefaultTableModel(
            new String[]{"Date","Test","Result","Ref Range","Interpretation","Technician"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        for(TestResult r:results) resModel.addRow(new String[]{
            r.getDate(),r.getTestName(),r.getResult(),r.getRefRange(),r.getInterpretation(),r.getTechnician()});
        JTable resTable=new JTable(resModel);
        resTable.setRowHeight(24);
        body.add(historySection("Lab Results ("+results.size()+")",resTable));

        dlg.add(body, BorderLayout.CENTER);

        JPanel btns=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,8));
        btns.setBackground(Color.WHITE);
        JButton close=AppTheme.btnPrimary("Close");
        close.addActionListener(e->dlg.dispose());
        btns.add(close);
        dlg.add(btns, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private JLabel historyLabel(String text){
        JLabel l=new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    private JPanel historySection(String title, JTable table){
        JPanel section=new JPanel(new BorderLayout(0,4));
        section.setBackground(Color.WHITE);
        JLabel lbl=new JLabel(title);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(AppTheme.PRIMARY);
        section.add(lbl, BorderLayout.NORTH);
        JScrollPane sp=new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210,215,220)));
        section.add(sp, BorderLayout.CENTER);
        return section;
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null) return;
        if(view==0){
            MedRecord r=mgr.records.findById(id);
            if(r==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Medical Record",new String[][]{
                {"Record ID",r.getId()},{"Patient",r.getPatientName()},{"Doctor",r.getDoctorName()},
                {"Date",r.getDate()},{"Diagnosis",r.getDiagnosis()},{"Treatment",r.getTreatment()},
                {"Prescription",r.getPrescription()},{"Notes",r.getNotes()}});
        }
    }

    @Override public void handleEdit(){
        String id=selectedId(); if(id==null) return;
        if(view==1) editPatient(id);
        else if(view==2) editDoctor(id);
        else editRecord(id);
    }

    private void editPatient(String id){
        RecPatient p=mgr.patients.findById(id); if(p==null) return;
        String[][] f={{"name","Name","TEXT",p.getName()},{"dob","DOB","TEXT",p.getDob()},
                      {"gender","Gender","SELECT","Male|Female|Other"},{"phone","Phone","TEXT",p.getPhone()},
                      {"address","Address","TEXT",p.getAddress()},{"blood","Blood","SELECT","A+|A-|B+|B-|O+|O-|AB+|AB-"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Patient",f);
        d.set("gender",p.getGender()); d.set("blood",p.getBloodType());
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            RecPatient updated=new RecPatient(id,Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("dob"),d.get("gender"),d.get("phone"),d.get("address"),d.get("blood"));
            mgr.patients.update(updated); refresh();
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void editDoctor(String id){
        RecDoctor dr=mgr.doctors.findById(id); if(dr==null) return;
        String[][] f={{"name","Full Name","TEXT",dr.getName()},{"spec","Specialization","TEXT",dr.getSpecialization()},
                      {"phone","Phone","TEXT",dr.getPhone()}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Doctor",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            RecDoctor updated=new RecDoctor(id,Validator.requireNonEmpty(d.get("name"),"Name"),
                Validator.requireNonEmpty(d.get("spec"),"Specialization"),d.get("phone"));
            mgr.doctors.update(updated); refresh(); info("Doctor updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void editRecord(String id){
        MedRecord r=mgr.records.findById(id); if(r==null) return;
        String[][] f={{"diag","Diagnosis","TEXT",r.getDiagnosis()},{"treat","Treatment","TEXT",r.getTreatment()},
                      {"presc","Prescription","TEXT",r.getPrescription()},{"notes","Notes","AREA",r.getNotes()}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Medical Record",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            MedRecord updated=new MedRecord(id,r.getPatientId(),r.getPatientName(),r.getDoctorId(),r.getDoctorName(),
                r.getDate(),Validator.requireNonEmpty(d.get("diag"),"Diagnosis"),d.get("treat"),d.get("presc"),d.get("notes"));
            mgr.records.update(updated); refresh(); info("Record updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if(view==0) mgr.records.remove(id);
        else if(view==1) mgr.patients.remove(id);
        else mgr.doctors.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        String[] labels={"Records","Patients","Doctors"};
        for(int i=0;i<3;i++){
            final int vi=i;
            JButton b=AppTheme.btnInfo(labels[i]);
            b.setPreferredSize(new Dimension(90,34));
            b.addActionListener(e->{ view=vi; rebuild(); refresh(); });
            p.add(b);
        }
        JButton history=AppTheme.btnPrimary("🩺 Patient History");
        history.setPreferredSize(new Dimension(150,34));
        history.addActionListener(e->showPatientHistory());
        p.add(history);
        return p;
    }

    /** Rebuild column headers after view switch. */
    private void rebuild(){
        model.setColumnIdentifiers(getColumns());
        model.setRowCount(0);
    }

}
