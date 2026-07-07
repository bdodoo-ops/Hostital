package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class Vaccine extends Entity implements Displayable {
    private String name, disease, manufacturer;
    private int    doses, intervalDays;
    private double price;

    public Vaccine(String id,String name,String disease,String mfr,int doses,int interval,double price){
        super(id); this.name=name; this.disease=disease; this.manufacturer=mfr;
        this.doses=doses; this.intervalDays=interval; this.price=price;
    }
    public String getName()         { return name; }
    public String getDisease()      { return disease; }
    public String getManufacturer() { return manufacturer; }
    public int    getDoses()        { return doses; }
    public int    getIntervalDays() { return intervalDays; }
    public double getPrice()        { return price; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Vaccine Name");
        Validator.requireNonEmpty(disease,"Target Disease");
        if(doses<1) throw new IllegalArgumentException("Doses must be >= 1.");
        if(price<0) throw new IllegalArgumentException("Price cannot be negative.");
    }
    @Override public String toFileString(){
        return id+"|"+name+"|"+disease+"|"+manufacturer+"|"+doses+"|"+intervalDays+"|"+price;
    }
    public static Vaccine fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Vaccine(p[0],p[1],p[2],p[3],Integer.parseInt(p[4]),Integer.parseInt(p[5]),Double.parseDouble(p[6]));
    }
    @Override public String[] toTableRow(){
        return new String[]{id,name,disease,manufacturer,String.valueOf(doses),String.valueOf(intervalDays),String.format("%.2f",price)};
    }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return disease+" | "+doses+" dose(s)"; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Vaccine","Disease","Manufacturer","Doses","Interval(days)","Price(GHS)"}; }
}
