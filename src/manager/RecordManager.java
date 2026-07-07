package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class RecordManager {

    // ── Singleton ─────────────────────────────────────────────────────────────
    private static RecordManager instance;
    public  static RecordManager get() {
        if (instance == null) instance = new RecordManager();
        return instance;
    }

    // ── Shared stores (used by PatientsPanel + DoctorsPanel + RecordsPanel) ──
    public final Manager<RecPatient> patients = new Manager<RecPatient>("rec_patients.txt") {
        @Override public RecPatient fromFileString(String l) { return RecPatient.fromFile(l); }
        @Override public String getIdPrefix() { return "P"; }
    };
    public final Manager<RecDoctor> doctors   = new Manager<RecDoctor>("rec_doctors.txt") {
        @Override public RecDoctor fromFileString(String l) { return RecDoctor.fromFile(l); }
        @Override public String getIdPrefix() { return "D"; }
    };
    public final Manager<MedRecord> records   = new Manager<MedRecord>("rec_records.txt") {
        @Override public MedRecord fromFileString(String l) { return MedRecord.fromFile(l); }
        @Override public String getIdPrefix() { return "MR"; }
    };

    private RecordManager() {}   // Use get()

    public void addRecord(String pid, String pname, String did, String dname,
                          String diag, String treat, String presc, String notes) {
        MedRecord r = new MedRecord(records.nextId(), pid, pname, did, dname,
                                    LocalDate.now().toString(), diag, treat, presc, notes);
        records.add(r);
    }

    public List<MedRecord> getRecordsByPatient(String pid) {
        return records.getAll().stream()
            .filter(r -> r.getPatientId().equalsIgnoreCase(pid))
            .collect(Collectors.toList());
    }
}
