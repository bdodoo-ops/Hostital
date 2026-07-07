package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class MaternalManager {
    public final Manager<Mother> mothers = new Manager<Mother>("mat_mothers.txt"){
        @Override public Mother fromFileString(String l){ return Mother.fromFile(l); }
        @Override public String getIdPrefix(){ return "MOM"; }
    };
    public final Manager<HealthRecord> records = new Manager<HealthRecord>("mat_records.txt"){
        @Override public HealthRecord fromFileString(String l){ return HealthRecord.fromFile(l); }
        @Override public String getIdPrefix(){ return "CHK"; }
    };

    public void recordCheckup(String mid,String mname,double weight,String bp,String fhr,
                               double fh,String comp,String notes,String doctor){
        HealthRecord r=new HealthRecord(records.nextId(),mid,mname,LocalDate.now().toString(),
                                        weight,bp,fhr,fh,comp,notes,doctor);
        records.add(r);
    }

    public List<HealthRecord> getByMother(String mid){
        return records.getAll().stream()
            .filter(r->r.getMotherId().equalsIgnoreCase(mid)).collect(Collectors.toList());
    }

    public List<HealthRecord> getHighRisk(){
        return records.getAll().stream()
            .filter(r->!r.getComplications().equalsIgnoreCase("None")&&!r.getComplications().isBlank())
            .collect(Collectors.toList());
    }
}
