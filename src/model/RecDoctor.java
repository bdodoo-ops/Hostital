package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class RecDoctor extends Entity implements Displayable {
    private String name, specialization, phone;

    public RecDoctor(String id,String name,String spec,String phone){
        super(id); this.name=name; this.specialization=spec; this.phone=phone;
    }
    public String getName()           { return name; }
    public String getSpecialization() { return specialization; }
    public String getPhone()          { return phone; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Name");
        Validator.requireNonEmpty(specialization,"Specialization");
    }
    @Override public String toFileString(){ return id+"|"+name+"|"+specialization+"|"+phone; }
    public static RecDoctor fromFile(String l){
        String[] p=l.split("\\|",-1); return new RecDoctor(p[0],p[1],p[2],p[3]);
    }
    @Override public String[] toTableRow(){ return new String[]{id,name,specialization,phone}; }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return specialization; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Name","Specialization","Phone"}; }
}
