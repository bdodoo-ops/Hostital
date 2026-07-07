package model;

import base.Entity;
import util.Validator;

public class TestOrder extends Entity {
    private String patientId, patientName, testId, testName, orderDate, referredBy, status;

    public TestOrder(String id,String pid,String pname,String tid,String tname,
                     String date,String ref,String status){
        super(id); this.patientId=pid; this.patientName=pname; this.testId=tid;
        this.testName=tname; this.orderDate=date; this.referredBy=ref; this.status=status;
    }
    public String getPatientId()   { return patientId; }
    public String getPatientName() { return patientName; }
    public String getTestId()      { return testId; }
    public String getTestName()    { return testName; }
    public String getOrderDate()   { return orderDate; }
    public String getReferredBy()  { return referredBy; }
    public String getStatus()      { return status; }
    public void   setStatus(String s){ this.status=s; }

    @Override public void validate(){
        Validator.requireNonEmpty(patientId,"Patient ID");
        Validator.requireNonEmpty(testId,"Test ID");
    }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+testId+"|"+testName+"|"+
               orderDate+"|"+referredBy+"|"+status;
    }
    public static TestOrder fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new TestOrder(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,testName,orderDate,referredBy,status};
    }
}
