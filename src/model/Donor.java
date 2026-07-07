package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class Donor extends Entity implements Displayable {
    private String name, bloodType, phone, address, lastDonation;

    public Donor(String id,String name,String bt,String phone,String addr,String lastDon){
        super(id); this.name=name; this.bloodType=bt; this.phone=phone;
        this.address=addr; this.lastDonation=lastDon;
    }
    public String getName()         { return name; }
    public String getBloodType()    { return bloodType; }
    public String getPhone()        { return phone; }
    public String getAddress()      { return address; }
    public String getLastDonation() { return lastDonation; }
    public void   setName(String v)         { this.name=v; }
    public void   setPhone(String v)        { this.phone=v; }
    public void   setAddress(String v)      { this.address=v; }
    public void   setLastDonation(String d) { this.lastDonation=d; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Name");
        Validator.requireNonEmpty(bloodType,"Blood Type");
        Validator.requireNonEmpty(phone,"Phone");
    }
    @Override public String toFileString(){
        return id+"|"+name+"|"+bloodType+"|"+phone+"|"+address+"|"+lastDonation;
    }
    public static Donor fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Donor(p[0],p[1],p[2],p[3],p[4],p[5]);
    }
    @Override public String[] toTableRow(){ return new String[]{id,name,bloodType,phone,address,lastDonation}; }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return bloodType+" | Last: "+lastDonation; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Name","Blood Type","Phone","Address","Last Donation"}; }
}
