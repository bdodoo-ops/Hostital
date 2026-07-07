package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class Mother extends Entity implements Displayable {
    private String name, dob, phone, address, partner, bloodType, edd;
    private int    weeksPregnant;

    public Mother(String id,String name,String dob,String phone,String addr,
                  String partner,String bt,int weeks,String edd){
        super(id); this.name=name; this.dob=dob; this.phone=phone; this.address=addr;
        this.partner=partner; this.bloodType=bt; this.weeksPregnant=weeks; this.edd=edd;
    }
    public String getName()         { return name; }
    public String getDob()          { return dob; }
    public String getPhone()        { return phone; }
    public String getAddress()      { return address; }
    public String getPartner()      { return partner; }
    public String getBloodType()    { return bloodType; }
    public int    getWeeksPregnant(){ return weeksPregnant; }
    public String getEdd()          { return edd; }
    public void setPhone(String v)        { this.phone=v; }
    public void setAddress(String v)      { this.address=v; }
    public void setWeeksPregnant(int v)   { this.weeksPregnant=v; }
    public void setEdd(String v)          { this.edd=v; }

    @Override public void validate(){
        Validator.requireNonEmpty(name,"Name");
        Validator.requireNonEmpty(phone,"Phone");
        if(weeksPregnant<0||weeksPregnant>45) throw new IllegalArgumentException("Weeks pregnant must be 0–45.");
    }
    @Override public String toFileString(){
        return id+"|"+name+"|"+dob+"|"+phone+"|"+address+"|"+partner+"|"+bloodType+"|"+weeksPregnant+"|"+edd;
    }
    public static Mother fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Mother(p[0],p[1],p[2],p[3],p[4],p[5],p[6],Integer.parseInt(p[7]),p[8]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,name,dob,phone,bloodType,String.valueOf(weeksPregnant)+" wks",edd};
    }
    @Override public String   getDisplayName(){ return name; }
    @Override public String   getSummary()    { return weeksPregnant+" wks | EDD: "+edd; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Name","DOB","Phone","Blood","Weeks","EDD"}; }
}
