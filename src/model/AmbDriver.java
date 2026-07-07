package model;

import base.Entity;
import util.Validator;

public class AmbDriver extends Entity {
    private String name, phone, licenseNo, status; // Available|On Duty

    public AmbDriver(String id,String name,String phone,String lic,String status){
        super(id); this.name=name; this.phone=phone; this.licenseNo=lic; this.status=status;
    }
    public String getName()      { return name; }
    public String getPhone()     { return phone; }
    public String getLicenseNo() { return licenseNo; }
    public String getStatus()    { return status; }
    public void   setStatus(String s){ this.status=s; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Name");
        Validator.requireNonEmpty(licenseNo,"License No");
    }
    @Override public String toFileString(){ return id+"|"+name+"|"+phone+"|"+licenseNo+"|"+status; }
    public static AmbDriver fromFile(String l){
        String[] p=l.split("\\|",-1); return new AmbDriver(p[0],p[1],p[2],p[3],p[4]);
    }
    @Override public String[] toTableRow(){ return new String[]{id,name,phone,licenseNo,status}; }
}
