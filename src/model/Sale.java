package model;

import base.Entity;
import util.Validator;

public class Sale extends Entity {
    private String medicineId, medicineName, patientId, patientName, date;
    private int    quantity;
    private double unitPrice, total;

    public Sale(String id, String mid, String mname, String pid, String pname,
                int qty, double unit, double total, String date) {
        super(id);
        this.medicineId   = mid;   this.medicineName = mname;
        this.patientId    = pid;   this.patientName  = pname;
        this.quantity     = qty;   this.unitPrice    = unit;
        this.total        = total; this.date         = date;
    }

    public String getMedicineId()   { return medicineId; }
    public String getMedicineName() { return medicineName; }
    public String getPatientId()    { return patientId; }
    public String getPatientName()  { return patientName; }
    public int    getQuantity()     { return quantity; }
    public double getUnitPrice()    { return unitPrice; }
    public double getTotal()        { return total; }
    public String getDate()         { return date; }

    @Override public void validate() { Validator.requireNonEmpty(medicineId, "Medicine ID"); }

    @Override public String toFileString() {
        return id+"|"+medicineId+"|"+medicineName+"|"+patientId+"|"+patientName
             +"|"+quantity+"|"+unitPrice+"|"+total+"|"+date;
    }

    public static Sale fromFile(String l) {
        String[] p = l.split("\\|", -1);
        // Support old 7-field files (no patient) gracefully
        if (p.length < 9)
            return new Sale(p[0], p[1], p[2], "", "", Integer.parseInt(p[3]),
                            Double.parseDouble(p[4]), Double.parseDouble(p[5]), p[6]);
        return new Sale(p[0], p[1], p[2], p[3], p[4],
                        Integer.parseInt(p[5]), Double.parseDouble(p[6]),
                        Double.parseDouble(p[7]), p[8]);
    }

    @Override public String[] toTableRow() {
        return new String[]{id, patientName.isEmpty() ? "—" : patientName,
                            medicineName, String.valueOf(quantity),
                            String.format("%.2f", unitPrice),
                            String.format("%.2f", total), date};
    }
}
