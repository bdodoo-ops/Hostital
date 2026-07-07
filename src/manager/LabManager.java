package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class LabManager {
    public final Manager<LabTest> tests = new Manager<LabTest>("lab_tests.txt"){
        @Override public LabTest fromFileString(String l){ return LabTest.fromFile(l); }
        @Override public String getIdPrefix(){ return "TST"; }
    };
    public final Manager<TestOrder> orders = new Manager<TestOrder>("lab_orders.txt"){
        @Override public TestOrder fromFileString(String l){ return TestOrder.fromFile(l); }
        @Override public String getIdPrefix(){ return "ORD"; }
    };
    public final Manager<TestResult> results = new Manager<TestResult>("lab_results.txt"){
        @Override public TestResult fromFileString(String l){ return TestResult.fromFile(l); }
        @Override public String getIdPrefix(){ return "RES"; }
    };
    public final Manager<TestConstituent> constituents = new Manager<TestConstituent>("lab_constituents.txt"){
        @Override public TestConstituent fromFileString(String l){ return TestConstituent.fromFile(l); }
        @Override public String getIdPrefix(){ return "CST"; }
    };
    private final BillingManager billing = new BillingManager();

    public void placeOrder(String pid,String pname,String tid,String ref){
        LabTest t=tests.findById(tid);
        if(t==null) throw new IllegalArgumentException("Test not found.");
        TestOrder o=new TestOrder(orders.nextId(),pid,pname,tid,t.getName(),
                                  LocalDate.now().toString(),ref,"Pending");
        orders.add(o);
        billing.addPendingCharge(pid,pname,"Laboratory","Laboratory: "+t.getName(),t.getPrice());
    }

    public List<TestConstituent> getConstituents(String testId){
        return constituents.getAll().stream()
            .filter(c->c.getTestId().equalsIgnoreCase(testId)).collect(Collectors.toList());
    }

    public void enterResult(String orderId,String result,String refRange,String interp,String tech){
        TestOrder o=orders.findById(orderId);
        if(o==null) throw new IllegalArgumentException("Order not found.");
        TestResult r=new TestResult(results.nextId(),orderId,o.getPatientId(),o.getTestName(),
                                    result,refRange,interp,LocalDate.now().toString(),tech);
        results.add(r);
        o.setStatus("Ready"); orders.update(o);
    }

    /** Enter one result per sub-constituent; interpretation (Normal/Low/High) is derived from each constituent's range. */
    public void enterConstituentResults(String orderId,List<TestConstituent> cons,List<Double> values,String tech){
        TestOrder o=orders.findById(orderId);
        if(o==null) throw new IllegalArgumentException("Order not found.");
        for(int i=0;i<cons.size();i++){
            TestConstituent c=cons.get(i);
            double val=values.get(i);
            TestResult r=new TestResult(results.nextId(),orderId,o.getPatientId(),
                o.getTestName()+" - "+c.getName(),String.valueOf(val),c.rangeText(),
                c.interpret(val),LocalDate.now().toString(),tech);
            results.add(r);
        }
        o.setStatus("Ready"); orders.update(o);
    }

    public List<TestResult> getResultsByPatient(String pid){
        return results.getAll().stream()
            .filter(r->r.getPatientId().equalsIgnoreCase(pid)).collect(Collectors.toList());
    }
}
