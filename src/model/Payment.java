package model;

import base.Entity;
import util.Validator;

public class Payment extends Entity {
    private String billId, patientName, method, date;
    private double amountPaid, balance;

    public Payment(String id,String bid,String pname,double paid,double bal,String method,String date){
        super(id); this.billId=bid; this.patientName=pname; this.amountPaid=paid;
        this.balance=bal; this.method=method; this.date=date;
    }
    public String getBillId()      { return billId; }
    public String getPatientName() { return patientName; }
    public double getAmountPaid()  { return amountPaid; }
    public double getBalance()     { return balance; }
    public String getMethod()      { return method; }
    public String getDate()        { return date; }

    @Override public void validate(){ Validator.requireNonEmpty(billId,"Bill ID"); }
    @Override public String toFileString(){
        return id+"|"+billId+"|"+patientName+"|"+amountPaid+"|"+balance+"|"+method+"|"+date;
    }
    public static Payment fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Payment(p[0],p[1],p[2],Double.parseDouble(p[3]),Double.parseDouble(p[4]),p[5],p[6]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,billId,patientName,
                            String.format("%.2f",amountPaid),String.format("%.2f",balance),method,date};
    }
}
