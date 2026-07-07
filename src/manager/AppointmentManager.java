package manager;

import base.Manager;
import model.*;
import java.util.List;
import java.util.stream.Collectors;

public class AppointmentManager {

    public final Manager<Appointment> appointments = new Manager<Appointment>("appt_appointments.txt") {
        @Override public Appointment fromFileString(String l) { return Appointment.fromFile(l); }
        @Override public String getIdPrefix() { return "APT"; }
    };

    public void book(String pid, String pname, String did, String dname,
                     String date, String time, String reason) {
        Appointment a = new Appointment(appointments.nextId(),
                                        pid, pname, did, dname, date, time, reason, "Scheduled");
        appointments.add(a);
    }

    public void updateStatus(String aptId, String status) {
        Appointment a = appointments.findById(aptId);
        if (a == null) throw new IllegalArgumentException("Appointment not found.");
        a.setStatus(status);
        appointments.update(a);
    }

    public List<Appointment> getByDoctor(String did) {
        return appointments.getAll().stream()
            .filter(a -> a.getDoctorId().equalsIgnoreCase(did))
            .collect(Collectors.toList());
    }

    public List<Appointment> getByPatient(String pid) {
        return appointments.getAll().stream()
            .filter(a -> a.getPatientId().equalsIgnoreCase(pid))
            .collect(Collectors.toList());
    }
}
