package model;

import base.Entity;
import util.Validator;

public class TestConstituent extends Entity {
    private String testId, testName, name, unit;
    private double low, high;

    public TestConstituent(String id,String testId,String testName,String name,String unit,double low,double high){
        super(id); this.testId=testId; this.testName=testName; this.name=name;
        this.unit=unit; this.low=low; this.high=high;
    }
    public String getTestId()   { return testId; }
    public String getTestName() { return testName; }
    public String getName()     { return name; }
    public String getUnit()     { return unit; }
    public double getLow()      { return low; }
    public double getHigh()     { return high; }

    /** Classify a numeric result against this constituent's normal range. */
    public String interpret(double value){
        if(value < low)  return "Low";
        if(value > high) return "High";
        return "Normal";
    }
    public String rangeText(){ return low+" - "+high+(unit==null||unit.isEmpty()?"":" "+unit); }

    @Override public void validate(){
        Validator.requireNonEmpty(testId,"Test");
        Validator.requireNonEmpty(name,"Constituent Name");
        if(high<low) throw new IllegalArgumentException("High range must be >= Low range.");
    }
    @Override public String toFileString(){
        return id+"|"+testId+"|"+testName+"|"+name+"|"+unit+"|"+low+"|"+high;
    }
    public static TestConstituent fromFile(String l){
        String[] p=l.split("\\|",-1);
        return new TestConstituent(p[0],p[1],p[2],p[3],p[4],Double.parseDouble(p[5]),Double.parseDouble(p[6]));
    }
    @Override public String[] toTableRow(){
        return new String[]{id,testName,name,unit,String.valueOf(low),String.valueOf(high)};
    }
}
