package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class VaccinationManager {
    public final Manager<Vaccine> vaccines = new Manager<Vaccine>("vc_vaccines.txt") {
        @Override public Vaccine fromFileString(String l) { return Vaccine.fromFile(l); }
        @Override public String getIdPrefix() { return "VAC"; }
    };
    public final Manager<VaccRecord> records = new Manager<VaccRecord>("vc_records.txt") {
        @Override public VaccRecord fromFileString(String l) { return VaccRecord.fromFile(l); }
        @Override public String getIdPrefix() { return "VR"; }
    };
    private final BillingManager billing = new BillingManager();

    public void recordVaccination(String pid, String pname, String vid, int dose, String nurse) {
        Vaccine v = vaccines.findById(vid);
        if (v == null) throw new IllegalArgumentException("Vaccine not found.");
        String today   = LocalDate.now().toString();
        String nextDue = dose < v.getDoses()
            ? LocalDate.now().plusDays(v.getIntervalDays()).toString() : "Complete";
        VaccRecord r = new VaccRecord(records.nextId(), pid, pname, vid, v.getName(), dose, today, nextDue, nurse);
        records.add(r);
        billing.addPendingCharge(pid, pname, "Vaccination", "Vaccination: "+v.getName(), v.getPrice());
    }

    public List<VaccRecord> getOverdue() {
        String today = LocalDate.now().toString();
        return records.getAll().stream()
            .filter(r -> !r.getNextDue().equals("Complete") && r.getNextDue().compareTo(today) < 0)
            .collect(Collectors.toList());
    }
}
