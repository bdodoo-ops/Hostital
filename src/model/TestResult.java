package model;

import base.Entity;
import util.Validator;

public class TestResult extends Entity {
    private String orderId, patientId, testName, result, refRange, interpretation, date, technician;

    public TestResult(String id,String oid,String pid,String tname,String result,
                      String refRange,String interp,String date,String tech){
        super(id); this.orderId=oid; this.patientId=pid; this.testName=tname;
        this.result=result; this.refRange=refRange; this.interpretation=interp;
        this.date=date; this.technician=tech;
    }
    public String getOrderId()       { return orderId; }
    public String getPatientId()     { return patientId; }
    public String getTestName()      { return testName; }
    public String getResult()        { return result; }
    public String getRefRange()      { return refRange; }
    public String getInterpretation(){ return interpretation; }
    public String getDate()          { return date; }
    public String getTechnician()    { return technician; }

    @Override public void validate(){
        Validator.requireNonEmpty(orderId,"Order ID");
        Validator.requireNonEmpty(result,"Result");
    }
    @Override public String toFileString(){
        return id+"|"+orderId+"|"+patientId+"|"+testName+"|"+result+"|"+refRange+"|"+
               interpretation+"|"+date+"|"+technician;
    }
    public static TestResult fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new TestResult(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7],p[8]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,orderId,testName,result,interpretation,date,technician};
    }
}
