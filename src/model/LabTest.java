package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class LabTest extends Entity implements Displayable {
    private String name, category;
    private double price;
    private int    turnaroundHours;

    public LabTest(String id,String name,String cat,double price,int tat){
        super(id); this.name=name; this.category=cat; this.price=price; this.turnaroundHours=tat;
    }
    public String getName()           { return name; }
    public String getCategory()       { return category; }
    public double getPrice()          { return price; }
    public int    getTurnaroundHours(){ return turnaroundHours; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Test Name");
        if(price<0) throw new IllegalArgumentException("Price cannot be negative.");
    }
    @Override public String toFileString(){ return id+"|"+name+"|"+category+"|"+price+"|"+turnaroundHours; }
    public static LabTest fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new LabTest(p[0],p[1],p[2],Double.parseDouble(p[3]),Integer.parseInt(p[4]));
    }
    @Override public String[] toTableRow(){
        return new String[]{id,name,category,String.format("%.2f",price),String.valueOf(turnaroundHours)+" hrs"};
    }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return "GHS "+price+" | TAT: "+turnaroundHours+"h"; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Test Name","Category","Price(GHS)","Turnaround"}; }
}
