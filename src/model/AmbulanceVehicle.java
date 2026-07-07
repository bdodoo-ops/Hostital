package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class AmbulanceVehicle extends Entity implements Displayable {
    private String regNo, type, status; // Available|Dispatched|Maintenance

    public AmbulanceVehicle(String id,String regNo,String type,String status){
        super(id); this.regNo=regNo; this.type=type; this.status=status;
    }
    public String getRegNo()  { return regNo; }
    public String getType()   { return type; }
    public String getStatus() { return status; }
    public void   setStatus(String s){ this.status=s; }

    @Override public void validate(){
        Validator.requireNonEmpty(regNo,"Registration No");
        Validator.requireNonEmpty(type,"Type");
    }
    @Override public String toFileString(){ return id+"|"+regNo+"|"+type+"|"+status; }
    public static AmbulanceVehicle fromFile(String l){
        String[] p=l.split("\\|",-1); return new AmbulanceVehicle(p[0],p[1],p[2],p[3]);
    }
    @Override public String[] toTableRow(){ return new String[]{id,regNo,type,status}; }
    @Override public String   getDisplayName(){ return regNo; }
    @Override public String   getSummary()    { return type+" | "+status; }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Reg No","Type","Status"}; }
}
