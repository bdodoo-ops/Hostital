package model;

import base.Entity;
import util.Validator;

public class MedRecord extends Entity {
    private String patientId, patientName, doctorId, doctorName;
    private String date, diagnosis, treatment, prescription, notes;

    public MedRecord(String id,String pid,String pname,String did,String dname,
                     String date,String diag,String treat,String presc,String notes){
        super(id); this.patientId=pid; this.patientName=pname; this.doctorId=did;
        this.doctorName=dname; this.date=date; this.diagnosis=diag;
        this.treatment=treat; this.prescription=presc; this.notes=notes;
    }
    public String getPatientId()   { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorId()    { return doctorId; }
    public String getDoctorName()  { return doctorName; }
    public String getDate()        { return date; }
    public String getDiagnosis()   { return diagnosis; }
    public String getTreatment()   { return treatment; }
    public String getPrescription(){ return prescription; }
    public String getNotes()       { return notes; }

    @Override public void validate(){
        Validator.requireNonEmpty(patientId,"Patient ID");
        Validator.requireNonEmpty(diagnosis,"Diagnosis");
    }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+doctorId+"|"+doctorName+"|"+
               date+"|"+diagnosis+"|"+treatment+"|"+prescription+"|"+notes;
    }
    public static MedRecord fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new MedRecord(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7],p[8],p[9]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,doctorName,date,diagnosis,treatment};
    }
}
