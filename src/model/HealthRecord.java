package model;

import base.Entity;
import util.Validator;

public class HealthRecord extends Entity {
    private String motherId, motherName, date, bp, fhr, complications, notes, doctor;
    private double weight, fundalHeight;

    public HealthRecord(String id,String mid,String mname,String date,double wt,
                        String bp,String fhr,double fh,String comp,String notes,String doc){
        super(id); this.motherId=mid; this.motherName=mname; this.date=date;
        this.weight=wt; this.bp=bp; this.fhr=fhr; this.fundalHeight=fh;
        this.complications=comp; this.notes=notes; this.doctor=doc;
    }
    public String getMotherId()      { return motherId; }
    public String getMotherName()    { return motherName; }
    public String getDate()          { return date; }
    public double getWeight()        { return weight; }
    public String getBp()            { return bp; }
    public String getFhr()           { return fhr; }
    public double getFundalHeight()  { return fundalHeight; }
    public String getComplications() { return complications; }
    public String getNotes()         { return notes; }
    public String getDoctor()        { return doctor; }

    @Override public void validate(){
        Validator.requireNonEmpty(motherId,"Mother ID");
        if(weight<=0) throw new IllegalArgumentException("Weight must be > 0.");
    }
    @Override public String toFileString(){
        return id+"|"+motherId+"|"+motherName+"|"+date+"|"+weight+"|"+bp+"|"+fhr+"|"+
               fundalHeight+"|"+complications+"|"+notes+"|"+doctor;
    }
    public static HealthRecord fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new HealthRecord(p[0],p[1],p[2],p[3],Double.parseDouble(p[4]),
                                p[5],p[6],Double.parseDouble(p[7]),p[8],p[9],p[10]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,motherName,date,String.valueOf(weight),bp,fhr,
                            String.valueOf(fundalHeight),complications,doctor};
    }
}
