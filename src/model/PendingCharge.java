package model;

import base.Entity;
import util.Validator;

/** An unbilled charge raised by a source module (Lab, Pharmacy, Vaccination, Queue) awaiting manual billing. */
public class PendingCharge extends Entity {
    private String patientId, patientName, category, description, date;
    private double amount;

    public PendingCharge(String id,String patientId,String patientName,String category,
                         String description,double amount,String date){
        super(id); this.patientId=patientId; this.patientName=patientName; this.category=category;
        this.description=description; this.amount=amount; this.date=date;
    }
    public String getPatientId()   { return patientId; }
    public String getPatientName() { return patientName; }
    public String getCategory()    { return category; }
    public String getDescription() { return description; }
    public double getAmount()      { return amount; }
    public String getDate()        { return date; }

    @Override public void validate(){
        Validator.requireNonEmpty(patientId,"Patient ID");
        Validator.requireNonEmpty(category,"Category");
        if(amount<=0) throw new IllegalArgumentException("Charge amount must be greater than 0.");
    }
    @Override public String toFileString(){
        return id+"|"+patientId+"|"+patientName+"|"+category+"|"+description+"|"+amount+"|"+date;
    }
    public static PendingCharge fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new PendingCharge(p[0],p[1],p[2],p[3],p[4],Double.parseDouble(p[5]),p[6]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,patientName,category,description,String.format("%.2f",amount),date};
    }
}
