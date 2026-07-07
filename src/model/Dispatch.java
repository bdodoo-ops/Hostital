package model;

import base.Entity;
import util.Validator;

public class Dispatch extends Entity {
    private String ambulanceId, ambReg, driverId, driverName;
    private String location, caller, callerPhone, dispatchTime, returnTime, status;

    public Dispatch(String id,String aid,String areg,String did,String dname,
                    String loc,String caller,String cphone,String dtime,String rtime,String status){
        super(id); this.ambulanceId=aid; this.ambReg=areg; this.driverId=did;
        this.driverName=dname; this.location=loc; this.caller=caller;
        this.callerPhone=cphone; this.dispatchTime=dtime; this.returnTime=rtime; this.status=status;
    }
    public String getAmbulanceId()  { return ambulanceId; }
    public String getAmbReg()       { return ambReg; }
    public String getDriverId()     { return driverId; }
    public String getDriverName()   { return driverName; }
    public String getLocation()     { return location; }
    public String getCaller()       { return caller; }
    public String getCallerPhone()  { return callerPhone; }
    public String getDispatchTime() { return dispatchTime; }
    public String getReturnTime()   { return returnTime; }
    public String getStatus()       { return status; }
    public void   setStatus(String s)     { this.status=s; }
    public void   setReturnTime(String t) { this.returnTime=t; }
    public void   setLocation(String l)     { this.location=l; }
    public void   setCaller(String c)       { this.caller=c; }
    public void   setCallerPhone(String p)  { this.callerPhone=p; }

    @Override public void validate(){ Validator.requireNonEmpty(location,"Location"); }
    @Override public String toFileString(){
        return id+"|"+ambulanceId+"|"+ambReg+"|"+driverId+"|"+driverName+"|"+
               location+"|"+caller+"|"+callerPhone+"|"+dispatchTime+"|"+returnTime+"|"+status;
    }
    public static Dispatch fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new Dispatch(p[0],p[1],p[2],p[3],p[4],p[5],p[6],p[7],p[8],p[9],p[10]);
    }
    @Override public String[] toTableRow(){
        return new String[]{id,ambReg,driverName,location,caller,dispatchTime,returnTime,status};
    }
}
