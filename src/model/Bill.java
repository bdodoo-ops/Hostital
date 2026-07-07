package model;

import base.Entity;
import util.Validator;
import java.util.*;

public class Bill extends Entity {
    private String patientId, patientName, date, status; // Unpaid|Partial|Paid
    private List<String> lineItems; // "svcName:qty:unitPrice"
    private double total;

    public Bill(String id,String pid,String pname,String date,List<String> items,double total,String status){
        super(id); this.patientId=pid; this.patientName=pname; this.date=date;
        this.lineItems=items; this.total=total; this.status=status;
    }
    public String       getPatientId()   { return patientId; }
    public String       getPatientName() { return patientName; }
    public String       getDate()        { return date; }
    public List<String> getLineItems()   { return lineItems; }
    public double       getTotal()       { return total; }
    public String       getStatus()      { return status; }
    public void         setStatus(String s){ this.status=s; }

    @Override public void validate(){ Validator.requireNonEmpty(patientId,"Patient ID"); }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+date+"|"+
               String.join(";",lineItems)+"|"+total+"|"+status;
    }
    public static Bill fromFile(String l){
        String[] p=l.split("\\|",-1);
        List<String> items=new ArrayList<>();
        if(!p[4].isEmpty()) for(String s:p[4].split(";")) items.add(s);
        return new Bill(p[0],p[1],p[2],p[3],items,Double.parseDouble(p[5]),p[6]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,date,String.format("%.2f",total),status};
    }
}
