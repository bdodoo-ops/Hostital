package panel;

import manager.AmbulanceManager;
import manager.BillingManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AmbulancePanel extends BasePanel {
    private final AmbulanceManager mgr     = new AmbulanceManager();
    private final BillingManager   billing = new BillingManager();
    private int view = 0; // 0=dispatches 1=vehicles 2=drivers

    @Override public String   getTitle()  { return "🚑  Ambulance Dispatch Management"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"ID","Reg No","Type","Status"};
        if(view==2) return new String[]{"ID","Name","Phone","License No","Status"};
        return new String[]{"ID","Ambulance","Driver","Location","Caller","Dispatched","Returned","Status"};
    }

    @Override public void populate(String q){
        if(view==1){ for(AmbulanceVehicle v:q==null?mgr.vehicles.getAll():mgr.vehicles.search(q)) model.addRow(v.toTableRow()); }
        else if(view==2){ for(AmbDriver d:q==null?mgr.drivers.getAll():mgr.drivers.search(q)) model.addRow(d.toTableRow()); }
        else { for(Dispatch d:q==null?mgr.dispatches.getAll():mgr.dispatches.search(q)) model.addRow(d.toTableRow()); }
    }

    @Override public void handleAdd(){
        if(view==1){ addVehicle(); } else if(view==2){ addDriver(); } else { newDispatch(); }
    }

    private void addVehicle(){
        String[][] f={{"reg","Registration No","TEXT"},{"type","Type","SELECT","Basic|Advanced|ICU"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Register Ambulance",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            AmbulanceVehicle v=new AmbulanceVehicle(mgr.vehicles.nextId(),
                Validator.requireNonEmpty(d.get("reg"),"Registration No"),d.get("type"),"Available");
            mgr.vehicles.add(v); refresh(); info("Ambulance registered. ID: "+v.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void addDriver(){
        String[][] f={{"name","Full Name","TEXT"},{"phone","Phone","TEXT"},{"lic","License No","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Register Driver",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            AmbDriver dr=new AmbDriver(mgr.drivers.nextId(),Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("phone"),d.get("lic"),"Available");
            mgr.drivers.add(dr); refresh(); info("Driver registered. ID: "+dr.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void newDispatch(){
        if(mgr.vehicles.count()==0||mgr.drivers.count()==0){ info("Register vehicles and drivers first."); return; }
        String[] vOpts=mgr.vehicles.getAll().stream().map(v->v.getId()+" - "+v.getRegNo()+" - "+v.getType()+" ["+v.getStatus()+"]").toArray(String[]::new);
        String[] dOpts=mgr.drivers.getAll().stream().map(d->d.getId()+" - "+d.getName()+" - "+d.getPhone()+" - "+d.getLicenseNo()+" ["+d.getStatus()+"]").toArray(String[]::new);
        String[][] f={{"vid","Ambulance","SELECT",String.join("|",vOpts)},
                      {"did","Driver","SELECT",String.join("|",dOpts)},
                      {"loc","Incident Location","TEXT"},{"caller","Caller Name","TEXT"},{"cphone","Caller Phone","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Dispatch Ambulance",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String vid=d.get("vid").split(" ")[0], did=d.get("did").split(" ")[0];
            mgr.dispatch(vid,did,Validator.requireNonEmpty(d.get("loc"),"Location"),d.get("caller"),d.get("cphone"));
            refresh(); info("Ambulance dispatched.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null||view!=0) return;
        Dispatch d=mgr.dispatches.findById(id); if(d==null) return;
        FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Dispatch Detail",new String[][]{
            {"Dispatch ID",d.getId()},{"Ambulance",d.getAmbReg()},{"Driver",d.getDriverName()},
            {"Location",d.getLocation()},{"Caller",d.getCaller()+" / "+d.getCallerPhone()},
            {"Dispatched",d.getDispatchTime()},{"Returned",d.getReturnTime()},{"Status",d.getStatus()}});
    }

    @Override public void handleEdit(){
        String id=selectedId(); if(id==null) return;
        if(view==1) editVehicle(id);
        else if(view==2) editDriver(id);
        else editDispatch(id);
    }

    private void editVehicle(String id){
        AmbulanceVehicle v=mgr.vehicles.findById(id); if(v==null) return;
        String[][] f={{"reg","Registration No","TEXT"},{"type","Type","SELECT","Basic|Advanced|ICU"},
                      {"status","Status","SELECT","Available|Dispatched|Maintenance"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Ambulance",f);
        d.set("reg",v.getRegNo()); d.set("type",v.getType()); d.set("status",v.getStatus());
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            AmbulanceVehicle updated=new AmbulanceVehicle(v.getId(),
                Validator.requireNonEmpty(d.get("reg"),"Registration No"),d.get("type"),d.get("status"));
            mgr.vehicles.update(updated); refresh(); info("Ambulance updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void editDriver(String id){
        AmbDriver dr=mgr.drivers.findById(id); if(dr==null) return;
        String[][] f={{"name","Full Name","TEXT"},{"phone","Phone","TEXT"},{"lic","License No","TEXT"},
                      {"status","Status","SELECT","Available|On Duty"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Driver",f);
        d.set("name",dr.getName()); d.set("phone",dr.getPhone()); d.set("lic",dr.getLicenseNo()); d.set("status",dr.getStatus());
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            AmbDriver updated=new AmbDriver(dr.getId(),Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("phone"),d.get("lic"),d.get("status"));
            mgr.drivers.update(updated); refresh(); info("Driver updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void editDispatch(String id){
        Dispatch dp=mgr.dispatches.findById(id); if(dp==null) return;

        List<RecPatient> regPatients=RecordManager.get().patients.getAll();
        String[] billOpts=new String[regPatients.size()+1];
        billOpts[0]="-- No Bill --";
        for(int i=0;i<regPatients.size();i++) billOpts[i+1]=regPatients.get(i).getId()+" - "+regPatients.get(i).getName();

        String[][] f={{"loc","Incident Location","TEXT"},{"caller","Caller Name","TEXT"},
                      {"cphone","Caller Phone","TEXT"},{"status","Status","SELECT","Active|Completed"},
                      {"billPid","Bill For","SELECT",String.join("|",billOpts)},
                      {"billAmt","Bill Amount (GHS)","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Edit Dispatch",f);
        d.set("loc",dp.getLocation()); d.set("caller",dp.getCaller()); d.set("cphone",dp.getCallerPhone());
        d.set("status",dp.getStatus()); d.set("billPid",billOpts[0]);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            dp.setLocation(Validator.requireNonEmpty(d.get("loc"),"Location"));
            dp.setCaller(d.get("caller"));
            dp.setCallerPhone(d.get("cphone"));
            String oldStatus=dp.getStatus(), newStatus=d.get("status");
            if("Active".equals(oldStatus) && "Completed".equals(newStatus)){
                mgr.dispatches.update(dp);
                mgr.returnAmbulance(id);
            } else if("Completed".equals(oldStatus) && "Active".equals(newStatus)){
                AmbulanceVehicle amb=mgr.vehicles.findById(dp.getAmbulanceId());
                AmbDriver drv=mgr.drivers.findById(dp.getDriverId());
                if(amb!=null && !"Available".equals(amb.getStatus())) throw new IllegalArgumentException("Ambulance is not available to reactivate.");
                if(drv!=null && !"Available".equals(drv.getStatus())) throw new IllegalArgumentException("Driver is not available to reactivate.");
                dp.setStatus("Active"); dp.setReturnTime("—");
                mgr.dispatches.update(dp);
                if(amb!=null){ amb.setStatus("Dispatched"); mgr.vehicles.update(amb); }
                if(drv!=null){ drv.setStatus("On Duty"); mgr.drivers.update(drv); }
            } else {
                mgr.dispatches.update(dp);
            }

            String amountText=d.get("billAmt").trim();
            if(!amountText.isEmpty()){
                String billSel=d.get("billPid");
                if(billSel.startsWith("-- No Bill")) throw new IllegalArgumentException("Select a patient to bill.");
                String billPid=billSel.split(" ")[0];
                RecPatient billPatient=RecordManager.get().patients.findById(billPid);
                double amount=Validator.requirePositiveDouble(amountText,"Bill Amount");
                Bill b=billing.createSimpleBill(billPatient.getId(),billPatient.getName(),amount,"Ambulance: Dispatch "+dp.getAmbReg());
                info(String.format("Dispatch updated.\nBill created.\nID: %s\nPatient: %s\nAmount: GHS %.2f",
                    b.getId(),billPatient.getName(),b.getTotal()));
                refresh();
                return;
            }
            refresh(); info("Dispatch updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if(view==0){
            Dispatch dp=mgr.dispatches.findById(id);
            if(dp!=null && "Active".equals(dp.getStatus())){
                AmbulanceVehicle amb=mgr.vehicles.findById(dp.getAmbulanceId());
                if(amb!=null){ amb.setStatus("Available"); mgr.vehicles.update(amb); }
                AmbDriver drv=mgr.drivers.findById(dp.getDriverId());
                if(drv!=null){ drv.setStatus("Available"); mgr.drivers.update(drv); }
            }
            mgr.dispatches.remove(id);
        } else if(view==1){
            AmbulanceVehicle v=mgr.vehicles.findById(id);
            if(v!=null && "Dispatched".equals(v.getStatus())){ warn("Cannot delete: ambulance is on an active dispatch."); return; }
            mgr.vehicles.remove(id);
        } else {
            AmbDriver dr=mgr.drivers.findById(id);
            if(dr!=null && "On Duty".equals(dr.getStatus())){ warn("Cannot delete: driver is on an active dispatch."); return; }
            mgr.drivers.remove(id);
        }
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton dis=AppTheme.btnInfo("Dispatches"); dis.setPreferredSize(new Dimension(100,34));
        JButton veh=AppTheme.btnInfo("Vehicles");   veh.setPreferredSize(new Dimension(90,34));
        JButton drv=AppTheme.btnInfo("Drivers");    drv.setPreferredSize(new Dimension(80,34));
        dis.addActionListener(e->{ view=0; rebuild(); refresh(); });
        veh.addActionListener(e->{ view=1; rebuild(); refresh(); });
        drv.addActionListener(e->{ view=2; rebuild(); refresh(); });
        p.add(dis); p.add(veh); p.add(drv); return p;
    }
    private void rebuild(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }
}
