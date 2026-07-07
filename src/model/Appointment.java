package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class Appointment extends Entity implements Displayable {
    private String patientId, patientName, doctorId, doctorName;
    private String date, time, reason, status; // Scheduled|Completed|Cancelled

    public Appointment(String id,String pid,String pname,String did,String dname,
                       String date,String time,String reason,String status){
        super(id); this.patientId=pid; this.patientName=pname; this.doctorId=did;
        this.doctorName=dname; this.date=date; this.time=time;
        this.reason=reason; this.status=status;
    }
    public String getPatientId()   { return patientId; }
    public String getPatientName() { return patientName; }
    public String getDoctorId()    { return doctorId; }
    public String getDoctorName()  { return doctorName; }
    public String getDate()        { return date; }
    public String getTime()        { return time; }
    public String getReason()      { return reason; }
    public String getStatus()      { return status; }
    public void   setStatus(String s){ this.status=s; }

    @Override public void validate(){
        Validator.requireNonEmpty(patientId,"Patient ID");
        Validator.requireNonEmpty(doctorId,"Doctor ID");
        Validator.requireNonEmpty(date,"Date");
    }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+doctorId+"|"+doctorName+"|"+
               date+"|"+time+"|"+reason+"|"+status;
    }
    public static Appointment fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Appointment(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7],p[8]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,doctorName,date,time,reason,status};
    }
    @Override public String   getDisplayName(){ return patientName+" → "+doctorName; }
    @Override public String   getSummary()    { return date+" at "+time+" | "+status; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Patient","Doctor","Date","Time","Reason","Status"}; }
}
