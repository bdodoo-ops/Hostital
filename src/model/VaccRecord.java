package model;

import base.Entity;
import util.Validator;

public class VaccRecord extends Entity {
    private String patientId, patientName, vaccineId, vaccineName;
    private int    dose;
    private String dateGiven, nextDue, nurse;

    public VaccRecord(String id,String pid,String pname,String vid,String vname,
                      int dose,String dateGiven,String nextDue,String nurse){
        super(id); this.patientId=pid; this.patientName=pname; this.vaccineId=vid;
        this.vaccineName=vname; this.dose=dose; this.dateGiven=dateGiven;
        this.nextDue=nextDue; this.nurse=nurse;
    }
    public String getPatientId()   { return patientId; }
    public String getPatientName() { return patientName; }
    public String getVaccineId()   { return vaccineId; }
    public String getVaccineName() { return vaccineName; }
    public int    getDose()        { return dose; }
    public String getDateGiven()   { return dateGiven; }
    public String getNextDue()     { return nextDue; }
    public String getNurse()       { return nurse; }

    @Override public void validate(){
        Validator.requireNonEmpty(patientId,"Patient ID");
        Validator.requireNonEmpty(vaccineId,"Vaccine ID");
    }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+vaccineId+"|"+vaccineName+"|"+
               dose+"|"+dateGiven+"|"+nextDue+"|"+nurse;
    }
    public static VaccRecord fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new VaccRecord(p[0],p[1],p[2],p[3],p[4],Integer.parseInt(p[5]),p[6],p[7],p[8]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,vaccineName,String.valueOf(dose),dateGiven,nextDue,nurse};
    }
}
