package manager;

import base.Manager;
import model.*;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class PharmacyManager {
    public final Manager<Medicine> medicines = new Manager<Medicine>("ph_medicines.txt"){
        @Override public Medicine fromFileString(String l){ return Medicine.fromFile(l); }
        @Override public String getIdPrefix(){ return "MED"; }
    };
    public final Manager<Sale> sales = new Manager<Sale>("ph_sales.txt"){
        @Override public Sale fromFileString(String l){ return Sale.fromFile(l); }
        @Override public String getIdPrefix(){ return "SL"; }
    };
    private final BillingManager billing = new BillingManager();

    /** Sell qty units of medicine to a patient; throws if stock insufficient. */
    public Sale sell(String medicineId, String patientId, String patientName, int qty) {
        Medicine m = medicines.findById(medicineId);
        if (m == null) throw new IllegalArgumentException("Medicine not found: " + medicineId);
        if (m.getQuantity() < qty) throw new IllegalArgumentException("Insufficient stock. Available: " + m.getQuantity());
        m.setQuantity(m.getQuantity() - qty);
        medicines.update(m);
        double total = qty * m.getPrice();
        Sale s = new Sale(sales.nextId(), m.getId(), m.getName(),
                          patientId, patientName, qty, m.getPrice(), total,
                          LocalDate.now().toString());
        sales.add(s);
        if (patientId != null && !patientId.isEmpty())
            billing.addPendingCharge(patientId, patientName, "Pharmacy", "Pharmacy: "+m.getName()+" x"+qty, total);
        return s;
    }

    public List<Medicine> getLowStock(int threshold){
        return medicines.getAll().stream()
            .filter(m -> m.getQuantity()<threshold)
            .collect(Collectors.toList());
    }

    public double totalRevenue(){
        return sales.getAll().stream().mapToDouble(Sale::getTotal).sum();
    }
}
