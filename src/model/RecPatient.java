package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class RecPatient extends Entity implements Displayable {
    private String name, dob, gender, phone, address, bloodType;

    public RecPatient(String id,String name,String dob,String gender,String phone,String address,String bloodType){
        super(id); this.name=name; this.dob=dob; this.gender=gender;
        this.phone=phone; this.address=address; this.bloodType=bloodType;
    }
    public String getName()      { return name; }
    public String getDob()       { return dob; }
    public String getGender()    { return gender; }
    public String getPhone()     { return phone; }
    public String getAddress()   { return address; }
    public String getBloodType() { return bloodType; }

    @Override public void validate() {
        Validator.requireNonEmpty(name,"Name");
        Validator.requireNonEmpty(phone,"Phone");
        Validator.requireNonEmpty(bloodType,"Blood Type");
    }
    @Override public String toFileString(){
        return id+"|"+name+"|"+dob+"|"+gender+"|"+phone+"|"+address+"|"+bloodType;
    }
    public static RecPatient fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new RecPatient(p[0],p[1],p[2],p[3],p[4],p[5],p[6]);
    }
    @Override public String[] toTableRow(){ return new String[]{id,name,dob,gender,bloodType,phone,address}; }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return phone+" | "+bloodType; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Name","DOB","Gender","Blood","Phone","Address"}; }
}
