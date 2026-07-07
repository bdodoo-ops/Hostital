package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class BillService extends Entity implements Displayable {
    private String name, category;
    private double price;

    public BillService(String id,String name,String cat,double price){
        super(id); this.name=name; this.category=cat; this.price=price;
    }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Service Name");
        if(price<0) throw new IllegalArgumentException("Price cannot be negative.");
    }
    @Override public String toFileString(){ return id+"|"+name+"|"+category+"|"+price; }
    public static BillService fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new BillService(p[0],p[1],p[2],Double.parseDouble(p[3]));
    }
    @Override public String[] toTableRow(){ return new String[]{id,name,category,String.format("%.2f",price)}; }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return category+" — GHS "+price; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Service","Category","Price(GHS)"}; }
    @Override public String   toString(){ return id+" | "+name+" (GHS "+String.format("%.2f",price)+")"; }
}
