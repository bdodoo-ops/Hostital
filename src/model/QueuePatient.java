package model;

import base.Entity;
import base.Displayable;
import util.Validator;

/** Queue patient entity. Demonstrates Encapsulation + Inheritance + Abstraction. */
public class QueuePatient extends Entity implements Displayable {
    private String name, gender, department, arrivalTime, status;
    private int age, priority; // 1=Emergency 2=Urgent 3=Normal

    // Triage vitals — blank until recorded
    private String temperature="", bloodPressure="", pulseRate="", respRate="", o2Level="", weight="", height="";

    public QueuePatient(String id, String name, int age, String gender,
                        String department, int priority, String arrivalTime, String status) {
        super(id);
        this.name = name; this.age = age; this.gender = gender;
        this.department = department; this.priority = priority;
        this.arrivalTime = arrivalTime; this.status = status;
    }

    // Getters / setters
    public String getName()       { return name; }
    public int    getAge()        { return age; }
    public String getGender()     { return gender; }
    public String getDepartment() { return department; }
    public int    getPriority()   { return priority; }
    public String getArrivalTime(){ return arrivalTime; }
    public String getStatus()     { return status; }
    public void   setStatus(String s) { this.status = s; }

    // Triage getters / setters
    public String getTemperature()   { return temperature; }
    public String getBloodPressure() { return bloodPressure; }
    public String getPulseRate()     { return pulseRate; }
    public String getRespRate()      { return respRate; }
    public String getO2Level()       { return o2Level; }
    public String getWeight()        { return weight; }
    public String getHeight()        { return height; }
    public void setTemperature(String v)   { this.temperature = v; }
    public void setBloodPressure(String v) { this.bloodPressure = v; }
    public void setPulseRate(String v)     { this.pulseRate = v; }
    public void setRespRate(String v)      { this.respRate = v; }
    public void setO2Level(String v)       { this.o2Level = v; }
    public void setWeight(String v)        { this.weight = v; }
    public void setHeight(String v)        { this.height = v; }

    public boolean hasTriage() {
        return !temperature.isEmpty() || !bloodPressure.isEmpty() || !pulseRate.isEmpty()
            || !respRate.isEmpty() || !o2Level.isEmpty() || !weight.isEmpty() || !height.isEmpty();
    }

    public String getPriorityLabel() {
        return priority == 1 ? "EMERGENCY" : priority == 2 ? "Urgent" : "Normal";
    }

    @Override
    public void validate() {
        Validator.requireNonEmpty(name, "Name");
        Validator.requireNonEmpty(department, "Department");
        if (age < 0 || age > 150) throw new IllegalArgumentException("Age must be 0–150.");
        if (priority < 1 || priority > 3) throw new IllegalArgumentException("Priority 1–3 required.");
    }

    @Override
    public String toFileString() {
        return id+"|"+name+"|"+age+"|"+gender+"|"+department+"|"+priority+"|"+arrivalTime+"|"+status+"|"+
               temperature+"|"+bloodPressure+"|"+pulseRate+"|"+respRate+"|"+o2Level+"|"+weight+"|"+height;
    }

    public static QueuePatient fromFile(String line) {
        String[] p = line.split("\\|", -1);
        QueuePatient qp = new QueuePatient(p[0],p[1],Integer.parseInt(p[2]),p[3],p[4],Integer.parseInt(p[5]),p[6],p[7]);
        if (p.length > 8) {
            qp.temperature   = p[8];
            qp.bloodPressure = p[9];
            qp.pulseRate     = p[10];
            qp.respRate      = p[11];
            qp.o2Level       = p[12];
            qp.weight        = p[13];
            qp.height        = p[14];
        }
        return qp;
    }

    @Override public String[] toTableRow() {
        return new String[]{id, name, String.valueOf(age), gender, department, getPriorityLabel(), arrivalTime, status};
    }
    @Override public String   getDisplayName() { return name; }
    @Override public String   getSummary()     { return department + " | " + getPriorityLabel(); }
    @Override public String[] getColumnHeaders(){ return new String[]{"ID","Name","Age","Gender","Department","Priority","Arrived","Status"}; }
}
