package panel;

import manager.MaternalManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MaternalPanel extends BasePanel {
    private final MaternalManager mgr = new MaternalManager();
    private int view = 0; // 0=records 1=mothers

    @Override public String   getTitle()  { return "🤰  Maternal Health Monitoring"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"ID","Name","DOB","Phone","Blood","Weeks Pregnant","EDD"};
        return new String[]{"ID","Mother","Date","Weight(kg)","BP","FHR","Fundal Ht","Complications","Doctor"};
    }

    @Override public void populate(String q){
        if(view==1){ for(Mother m:q==null?mgr.mothers.getAll():mgr.mothers.search(q)) model.addRow(m.toTableRow()); }
        else { for(HealthRecord r:q==null?mgr.records.getAll():mgr.records.search(q)) model.addRow(r.toTableRow()); }
    }

    @Override public void handleAdd(){
        if(view==1) registerMother(); else recordCheckup();
    }

    private void registerMother(){
        String[] patItems = RecordManager.get().patients.getAll().stream()
            .filter(p -> "Female".equalsIgnoreCase(p.getGender()))
            .map(p -> p.getId()+" - "+p.getName())
            .toArray(String[]::new);
        if(patItems.length==0){ info("No female patients registered. Register one in the Patients module first."); return; }

        String[][] f={{"pid","Patient","SELECT",String.join("|",patItems)},
                      {"partner","Partner/Next of Kin","TEXT"},
                      {"weeks","Weeks Pregnant","NUMBER"},{"edd","Expected Delivery Date","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Register Mother",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String pid=d.get("pid").split(" ")[0];
            RecPatient rp=RecordManager.get().patients.findById(pid);
            Mother m=new Mother(mgr.mothers.nextId(),rp.getName(),rp.getDob(),rp.getPhone(),rp.getAddress(),
                d.get("partner"),rp.getBloodType(),
                Validator.requireNonNegativeInt(d.get("weeks"),"Weeks"),d.get("edd"));
            mgr.mothers.add(m); refresh(); info("Mother registered. ID: "+m.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void recordCheckup(){
        if(mgr.mothers.count()==0){ info("Register mothers first."); return; }
        String[] opts=mgr.mothers.getAll().stream().map(m->m.getId()+" - "+m.getName()).toArray(String[]::new);
        String[][] f={{"mid","Mother","SELECT",String.join("|",opts)},
                      {"weight","Weight (kg)","NUMBER"},{"bp","Blood Pressure (e.g. 120/80)","TEXT"},
                      {"fhr","Fetal Heart Rate (bpm)","TEXT"},{"fh","Fundal Height (cm)","TEXT"},
                      {"comp","Complications (if any)","TEXT"},{"notes","Notes","AREA"},
                      {"doc","Doctor/Midwife","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Record Checkup",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String mid=d.get("mid").split(" ")[0];
            Mother m=mgr.mothers.findById(mid);
            double fhVal=0; try{ fhVal=Double.parseDouble(d.get("fh").trim()); }catch(Exception ignored){}
            mgr.recordCheckup(mid,m.getName(),
                Validator.requirePositiveDouble(d.get("weight"),"Weight"),
                Validator.requireNonEmpty(d.get("bp"),"Blood Pressure"),
                d.get("fhr"),fhVal,d.get("comp"),d.get("notes"),d.get("doc"));
            refresh(); info("Checkup recorded.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null) return;
        if(view==1){
            Mother m=mgr.mothers.findById(id); if(m==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Mother Profile",new String[][]{
                {"ID",m.getId()},{"Name",m.getName()},{"DOB",m.getDob()},
                {"Phone",m.getPhone()},{"Address",m.getAddress()},
                {"Partner",m.getPartner()},{"Blood Type",m.getBloodType()},
                {"Weeks Pregnant",String.valueOf(m.getWeeksPregnant())},{"EDD",m.getEdd()}});
        } else {
            HealthRecord r=mgr.records.findById(id); if(r==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Checkup Record",new String[][]{
                {"ID",r.getId()},{"Mother",r.getMotherName()},{"Date",r.getDate()},
                {"Weight",r.getWeight()+" kg"},{"Blood Pressure",r.getBp()},
                {"Fetal HR",r.getFhr()},{"Fundal Height",String.valueOf(r.getFundalHeight())},
                {"Complications",r.getComplications()},{"Notes",r.getNotes()},
                {"Doctor",r.getDoctor()}});
        }
    }

    @Override public void handleEdit(){
        if(view!=1){ info("Checkup records are read-only."); return; }
        String id=selectedId(); if(id==null) return;
        Mother m=mgr.mothers.findById(id); if(m==null) return;
        String[][] f={{"weeks","Weeks Pregnant","NUMBER"},{"edd","Expected Delivery Date","TEXT"},
                      {"phone","Phone","TEXT"},{"address","Address","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Update Mother Info",f);
        d.set("weeks",String.valueOf(m.getWeeksPregnant()));
        d.set("edd",m.getEdd()); d.set("phone",m.getPhone()); d.set("address",m.getAddress());
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            m.setWeeksPregnant(Validator.requireNonNegativeInt(d.get("weeks"),"Weeks"));
            m.setEdd(d.get("edd")); m.setPhone(d.get("phone")); m.setAddress(d.get("address"));
            mgr.mothers.update(m); refresh(); info("Mother info updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if(view==1) mgr.mothers.remove(id); else mgr.records.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton r=AppTheme.btnInfo("Checkups"); r.setPreferredSize(new Dimension(90,34));
        JButton m=AppTheme.btnInfo("Mothers");  m.setPreferredSize(new Dimension(90,34));
        JButton hr=AppTheme.btnWarning("High Risk"); hr.setPreferredSize(new Dimension(100,34));
        r.addActionListener(e->{ view=0; rebuild(); refresh(); });
        m.addActionListener(e->{ view=1; rebuild(); refresh(); });
        hr.addActionListener(e->showHighRisk());
        p.add(r); p.add(m); p.add(hr); return p;
    }

    private void showHighRisk(){
        List<HealthRecord> list=mgr.getHighRisk();
        if(list.isEmpty()){ info("No high-risk cases on record."); return; }
        StringBuilder sb=new StringBuilder("HIGH-RISK CASES:\n\n");
        for(HealthRecord r:list)
            sb.append(r.getMotherName()).append(" — ").append(r.getDate())
              .append(" | ").append(r.getComplications()).append("\n");
        JOptionPane.showMessageDialog(this,sb.toString(),"High Risk Mothers",JOptionPane.WARNING_MESSAGE);
    }

    private void rebuild(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }
}
