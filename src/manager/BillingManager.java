package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class BillingManager {
    public final Manager<BillService> services = new Manager<BillService>("bill_services.txt"){
        @Override public BillService fromFileString(String l){ return BillService.fromFile(l); }
        @Override public String getIdPrefix(){ return "SVC"; }
    };
    public final Manager<Bill> bills = new Manager<Bill>("bill_bills.txt"){
        @Override public Bill fromFileString(String l){ return Bill.fromFile(l); }
        @Override public String getIdPrefix(){ return "BILL"; }
    };
    public final Manager<Payment> payments = new Manager<Payment>("bill_payments.txt"){
        @Override public Payment fromFileString(String l){ return Payment.fromFile(l); }
        @Override public String getIdPrefix(){ return "PAY"; }
    };
    public final Manager<PendingCharge> pending = new Manager<PendingCharge>("bill_pending.txt"){
        @Override public PendingCharge fromFileString(String l){ return PendingCharge.fromFile(l); }
        @Override public String getIdPrefix(){ return "PC"; }
    };

    /** Creates a bill from a list of "svcId:qty" entries. */
    public Bill createBill(String pid, String pname, List<String> svcQtyPairs){
        if(svcQtyPairs.isEmpty()) throw new IllegalArgumentException("No services selected.");
        List<String> lines=new ArrayList<>();
        double total=0;
        for(String pair:svcQtyPairs){
            String[] parts=pair.split(":");
            BillService svc=services.findById(parts[0]);
            if(svc==null) throw new IllegalArgumentException("Service not found: "+parts[0]);
            int qty=Integer.parseInt(parts[1]);
            lines.add(svc.getName()+":"+qty+":"+svc.getPrice());
            total += qty * svc.getPrice();
        }
        Bill b=new Bill(bills.nextId(),pid,pname,LocalDate.now().toString(),lines,total,"Unpaid");
        bills.add(b);
        return b;
    }

    /** Creates a bill immediately for a flat amount with a single labeled line item (e.g. "Ambulance: Dispatch GS1234"). */
    public Bill createSimpleBill(String pid, String pname, double amount, String description){
        if(amount<=0) throw new IllegalArgumentException("Bill amount must be greater than 0.");
        List<String> lines=new ArrayList<>();
        lines.add(description+":1:"+amount);
        Bill b=new Bill(bills.nextId(),pid,pname,LocalDate.now().toString(),lines,amount,"Unpaid");
        bills.add(b);
        return b;
    }

    /** Records an unbilled charge raised by a source module (Lab, Pharmacy, Vaccination, Queue). Zero/negative amounts are ignored. */
    public void addPendingCharge(String pid, String pname, String category, String description, double amount){
        if(amount<=0) return;
        pending.add(new PendingCharge(pending.nextId(),pid,pname,category,description,amount,LocalDate.now().toString()));
    }

    public List<PendingCharge> getPendingCharges(String pid, String category){
        return pending.getAll().stream()
            .filter(c->c.getPatientId().equalsIgnoreCase(pid) && c.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    public double getPendingTotal(String pid, String category){
        return getPendingCharges(pid,category).stream().mapToDouble(PendingCharge::getAmount).sum();
    }

    /** Rolls up all of a patient's pending charges in one category into a single Unpaid bill, clearing them from pending. */
    public Bill billPendingCharges(String pid, String pname, String category){
        List<PendingCharge> charges=getPendingCharges(pid,category);
        if(charges.isEmpty()) throw new IllegalArgumentException("No pending "+category+" charges for this patient.");
        List<String> lines=new ArrayList<>();
        double total=0;
        for(PendingCharge c:charges){ lines.add(c.getDescription()+":1:"+c.getAmount()); total+=c.getAmount(); }
        Bill b=new Bill(bills.nextId(),pid,pname,LocalDate.now().toString(),lines,total,"Unpaid");
        bills.add(b);
        for(PendingCharge c:charges) pending.remove(c.getId());
        return b;
    }

    public List<Bill> getBillsByPatient(String pid){
        return bills.getAll().stream()
            .filter(b->b.getPatientId().equalsIgnoreCase(pid)).collect(Collectors.toList());
    }

    public Payment recordPayment(String billId, double amount, String method){
        Bill b=bills.findById(billId);
        if(b==null) throw new IllegalArgumentException("Bill not found.");
        if("Paid".equals(b.getStatus())) throw new IllegalArgumentException("Bill already fully paid.");
        double paid=getTotalPaid(billId);
        // Round to the cent so floating-point noise (e.g. 0.000000000002) doesn't block a bill from clearing to Paid.
        double balance=Math.round((b.getTotal()-paid-amount)*100.0)/100.0;
        Payment p=new Payment(payments.nextId(),billId,b.getPatientName(),amount,Math.max(0,balance),
                              method,LocalDate.now().toString());
        payments.add(p);
        b.setStatus(balance<=0?"Paid":"Partial"); bills.update(b);
        return p;
    }

    public double getTotalPaid(String billId){
        return payments.getAll().stream()
            .filter(p->p.getBillId().equalsIgnoreCase(billId))
            .mapToDouble(Payment::getAmountPaid).sum();
    }
}
