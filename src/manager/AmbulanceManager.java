package manager;

import base.Manager;
import model.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class AmbulanceManager {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public final Manager<AmbulanceVehicle> vehicles = new Manager<AmbulanceVehicle>("amb_vehicles.txt"){
        @Override public AmbulanceVehicle fromFileString(String l){ return AmbulanceVehicle.fromFile(l); }
        @Override public String getIdPrefix(){ return "AMB"; }
    };
    public final Manager<AmbDriver> drivers = new Manager<AmbDriver>("amb_drivers.txt"){
        @Override public AmbDriver fromFileString(String l){ return AmbDriver.fromFile(l); }
        @Override public String getIdPrefix(){ return "DRV"; }
    };
    public final Manager<Dispatch> dispatches = new Manager<Dispatch>("amb_dispatches.txt"){
        @Override public Dispatch fromFileString(String l){ return Dispatch.fromFile(l); }
        @Override public String getIdPrefix(){ return "DIS"; }
    };

    public void dispatch(String ambId,String drvId,String loc,String caller,String cphone){
        AmbulanceVehicle amb=vehicles.findById(ambId);
        AmbDriver drv=drivers.findById(drvId);
        if(amb==null) throw new IllegalArgumentException("Ambulance not found.");
        if(drv==null) throw new IllegalArgumentException("Driver not found.");
        if(!"Available".equals(amb.getStatus())) throw new IllegalArgumentException("Ambulance not available.");
        if(!"Available".equals(drv.getStatus())) throw new IllegalArgumentException("Driver not available.");

        String now=LocalDateTime.now().format(FMT);
        Dispatch d=new Dispatch(dispatches.nextId(),ambId,amb.getRegNo(),drvId,drv.getName(),
                                loc,caller,cphone,now,"—","Active");
        dispatches.add(d);
        amb.setStatus("Dispatched"); vehicles.update(amb);
        drv.setStatus("On Duty");    drivers.update(drv);
    }

    public void returnAmbulance(String dispatchId){
        Dispatch d=dispatches.findById(dispatchId);
        if(d==null||!"Active".equals(d.getStatus())) throw new IllegalArgumentException("Active dispatch not found.");
        d.setStatus("Completed"); d.setReturnTime(LocalDateTime.now().format(FMT));
        dispatches.update(d);
        AmbulanceVehicle amb=vehicles.findById(d.getAmbulanceId());
        if(amb!=null){ amb.setStatus("Available"); vehicles.update(amb); }
        AmbDriver drv=drivers.findById(d.getDriverId());
        if(drv!=null){ drv.setStatus("Available"); drivers.update(drv); }
    }

    public List<Dispatch> getActive(){
        return dispatches.getAll().stream()
            .filter(d->"Active".equals(d.getStatus())).collect(Collectors.toList());
    }
}
