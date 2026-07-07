package model;

import base.Entity;
import util.Validator;

public class BloodRequest extends Entity {
    private String requester, bloodType, urgency, date, status;
    private int    units;

    public BloodRequest(String id,String req,String bt,int units,String urg,String date,String status){
        super(id); this.requester=req; this.bloodType=bt; this.units=units;
        this.urgency=urg; this.date=date; this.status=status;
    }
    public String getRequester() { return requester; }
    public String getBloodType() { return bloodType; }
    public int    getUnits()     { return units; }
    public String getUrgency()   { return urgency; }
    public String getDate()      { return date; }
    public String getStatus()    { return status; }
    public void   setStatus(String s){ this.status=s; }

    @Override public void validate(){
        Validator.requireNonEmpty(requester,"Requester Name");
        Validator.requireNonEmpty(bloodType,"Blood Type");
        if(units<=0) throw new IllegalArgumentException("Units must be > 0.");
    }
    @Override public String toFileString(){
        return id+"|"+requester+"|"+bloodType+"|"+units+"|"+urgency+"|"+date+"|"+status;
    }
    public static BloodRequest fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new BloodRequest(p[0],p[1],p[2],Integer.parseInt(p[3]),p[4],p[5],p[6]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,requester,bloodType,String.valueOf(units),urgency,date,status};
    }
}
