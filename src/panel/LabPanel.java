package panel;

import manager.LabManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;

public class LabPanel extends BasePanel {
    private final LabManager    mgr = new LabManager();
    private final RecordManager reg = RecordManager.get();
    private int view = 0; // 0=orders 1=results 2=tests 3=constituents

    @Override public String   getTitle()  { return "🔬  Medical Laboratory Test Processing"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"ID","Order ID","Test","Result","Interpretation","Date","Technician"};
        if(view==2) return new String[]{"ID","Test Name","Category","Price(GHS)","Turnaround"};
        if(view==3) return new String[]{"ID","Test","Constituent","Unit","Low","High"};
        return new String[]{"ID","Patient","Test","Order Date","Referred By","Status"};
    }

    @Override public void populate(String q){
        if(view==1){ for(TestResult r:q==null?mgr.results.getAll():mgr.results.search(q)) model.addRow(r.toTableRow()); }
        else if(view==2){ for(LabTest t:q==null?mgr.tests.getAll():mgr.tests.search(q)) model.addRow(t.toTableRow()); }
        else if(view==3){ for(TestConstituent c:q==null?mgr.constituents.getAll():mgr.constituents.search(q)) model.addRow(c.toTableRow()); }
        else { for(TestOrder o:q==null?mgr.orders.getAll():mgr.orders.search(q)) model.addRow(o.toTableRow()); }
    }

    @Override public void handleAdd(){
        switch(view){
            case 2: addTest(); break;
            case 1: enterResult(); break;
            case 3: addConstituent(); break;
            default: placeOrder(); break;
        }
    }

    private void addConstituent(){
        if(mgr.tests.count()==0){ info("Add a lab test first."); return; }
        String[] tOpts=mgr.tests.getAll().stream().map(t->t.getId()+" - "+t.getName()).toArray(String[]::new);
        String[][] f={{"tid","Test","SELECT",String.join("|",tOpts)},
                      {"name","Constituent Name","TEXT"},{"unit","Unit","TEXT"},
                      {"low","Low Range","NUMBER"},{"high","High Range","NUMBER"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Test Constituent",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String tid=d.get("tid").split(" ")[0];
            LabTest t=mgr.tests.findById(tid);
            double low, high;
            try{ low=Double.parseDouble(d.get("low").trim()); high=Double.parseDouble(d.get("high").trim()); }
            catch(NumberFormatException nfe){ throw new IllegalArgumentException("Low/High range must be valid numbers."); }
            TestConstituent c=new TestConstituent(mgr.constituents.nextId(),tid,t.getName(),
                Validator.requireNonEmpty(d.get("name"),"Constituent Name"),d.get("unit"),low,high);
            mgr.constituents.add(c); refresh(); info("Constituent added.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void addTest(){
        String[][] f={{"name","Test Name","TEXT"},
                      {"cat","Category","SELECT","Hematology|Biochemistry|Microbiology|Immunology|Serology|Parasitology|Histopathology|Endocrinology|Molecular|Urinalysis"},
                      {"price","Price (GHS)","NUMBER"},{"tat","Turnaround (hours)","NUMBER"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Lab Test",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            LabTest t=new LabTest(mgr.tests.nextId(),Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("cat"),Validator.requirePositiveDouble(d.get("price"),"Price"),
                Validator.requirePositiveInt(d.get("tat"),"Turnaround"));
            mgr.tests.add(t); refresh(); info("Test added. ID: "+t.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void placeOrder(){
        if(reg.patients.count()==0||mgr.tests.count()==0){ info("Register a patient (Patients module) and add a test first."); return; }
        String[] pOpts=reg.patients.getAll().stream().map(p->p.getId()+" - "+p.getName()).toArray(String[]::new);
        String[] tOpts=mgr.tests.getAll().stream().map(t->t.getId()+" - "+t.getName()+" (GHS "+t.getPrice()+")").toArray(String[]::new);
        String[][] f={{"pid","Patient","SELECT",String.join("|",pOpts)},
                      {"tid","Test","SELECT",String.join("|",tOpts)},
                      {"ref","Referred By","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Place Test Order",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String pid=d.get("pid").split(" ")[0];
            String tid=d.get("tid").split(" ")[0];
            RecPatient rp=reg.patients.findById(pid);
            LabTest t=mgr.tests.findById(tid);
            mgr.placeOrder(pid,rp.getName(),tid,d.get("ref"));
            refresh(); info(String.format("Order placed.\nGHS %.2f added to pending charges for %s.",t.getPrice(),t.getName()));
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void enterResult(){
        java.util.List<TestOrder> pending=mgr.orders.getAll().stream()
            .filter(o->"Pending".equals(o.getStatus())).collect(java.util.stream.Collectors.toList());
        if(pending.isEmpty()){ info("No pending orders awaiting results."); return; }
        String[] oOpts=pending.stream().map(o->o.getId()+" - "+o.getPatientName()+" — "+o.getTestName()).toArray(String[]::new);
        String[][] pickF={{"oid","Order","SELECT",String.join("|",oOpts)}};
        FormDialog pick=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Select Order",pickF);
        pick.setSize(700,pick.getHeight());
        pick.setLocationRelativeTo(SwingUtilities.getWindowAncestor(this));
        pick.setVisible(true); if(!pick.isConfirmed()) return;
        String oid=pick.get("oid").split(" ")[0];
        TestOrder order=mgr.orders.findById(oid); if(order==null) return;

        java.util.List<TestConstituent> cons=mgr.getConstituents(order.getTestId());
        if(cons.isEmpty()){
            String[][] f={{"result","Result Value","TEXT"},{"ref","Reference Range","TEXT"},
                          {"interp","Interpretation","SELECT","Normal|Abnormal|Critical"},
                          {"tech","Technician","TEXT"}};
            FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Enter Result — "+order.getTestName(),f);
            d.setVisible(true); if(!d.isConfirmed()) return;
            try{
                mgr.enterResult(oid,d.get("result"),d.get("ref"),d.get("interp"),d.get("tech"));
                refresh(); info("Result entered.");
            }catch(Exception ex){error(ex.getMessage());}
        } else {
            String[][] f=new String[cons.size()+1][];
            for(int i=0;i<cons.size();i++){
                TestConstituent c=cons.get(i);
                f[i]=new String[]{"v"+i,c.getName()+" ("+c.rangeText()+")","TEXT"};
            }
            f[cons.size()]=new String[]{"tech","Technician","TEXT"};
            FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Enter Result — "+order.getTestName(),f);
            d.setVisible(true); if(!d.isConfirmed()) return;
            try{
                java.util.List<Double> values=new java.util.ArrayList<>();
                for(int i=0;i<cons.size();i++){
                    String raw=d.get("v"+i);
                    try{ values.add(Double.parseDouble(raw.trim())); }
                    catch(NumberFormatException nfe){ throw new IllegalArgumentException(cons.get(i).getName()+" must be a valid number."); }
                }
                mgr.enterConstituentResults(oid,cons,values,d.get("tech"));
                refresh(); info("Result entered.");
            }catch(Exception ex){error(ex.getMessage());}
        }
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null) return;
        if(view==0){
            TestOrder o=mgr.orders.findById(id); if(o==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Order Detail",new String[][]{
                {"ID",o.getId()},{"Patient",o.getPatientName()},{"Test",o.getTestName()},
                {"Order Date",o.getOrderDate()},{"Referred By",o.getReferredBy()},{"Status",o.getStatus()}});
        }
    }
    @Override public void handleEdit(){ info("Use 'Enter Result' button to update an order."); }
    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if(view==0) mgr.orders.remove(id);
        else if(view==1) mgr.results.remove(id);
        else if(view==2) mgr.tests.remove(id);
        else mgr.constituents.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton o=AppTheme.btnInfo("Orders"); o.setPreferredSize(new Dimension(80,34));
        JButton r=AppTheme.btnInfo("Results"); r.setPreferredSize(new Dimension(80,34));
        JButton t=AppTheme.btnInfo("Tests");  t.setPreferredSize(new Dimension(70,34));
        JButton c=AppTheme.btnInfo("Constituents");c.setPreferredSize(new Dimension(110,34));
        o.addActionListener(e->{ view=0; rebuild(); refresh(); });
        r.addActionListener(e->{ view=1; rebuild(); refresh(); });
        t.addActionListener(e->{ view=2; rebuild(); refresh(); });
        c.addActionListener(e->{ view=3; rebuild(); refresh(); });
        p.add(o); p.add(r); p.add(t); p.add(c); return p;
    }
    private void rebuild(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }
}
