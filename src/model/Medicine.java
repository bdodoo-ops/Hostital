package model;

import base.Entity;
import base.Displayable;
import util.Validator;

public class Medicine extends Entity implements Displayable {
    private String name, category, manufacturer, expiryDate;
    private double price;
    private int    quantity, initialQuantity;

    public Medicine(String id, String name, String cat, String mfr,
                    double price, int qty, String exp) {
        super(id);
        this.name = name; this.category = cat; this.manufacturer = mfr;
        this.price = price; this.expiryDate = exp;
        this.quantity = qty; this.initialQuantity = qty; // set on first add
    }

    // Used internally when loading from file (initial qty already stored)
    public Medicine(String id, String name, String cat, String mfr,
                    double price, int qty, int initialQty, String exp) {
        super(id);
        this.name = name; this.category = cat; this.manufacturer = mfr;
        this.price = price; this.expiryDate = exp;
        this.quantity = qty; this.initialQuantity = initialQty;
    }

    public String getName()            { return name; }
    public String getCategory()        { return category; }
    public String getManufacturer()    { return manufacturer; }
    public double getPrice()           { return price; }
    public int    getQuantity()        { return quantity; }
    public int    getInitialQuantity() { return initialQuantity; }
    public String getExpiryDate()      { return expiryDate; }
    public void   setQuantity(int q)   { this.quantity = q; }
    public void   setPrice(double p)   { this.price = p; }

    @Override public void validate() {
        Validator.requireNonEmpty(name, "Medicine Name");
        if (price    < 0) throw new IllegalArgumentException("Price cannot be negative.");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
    }

    @Override public String toFileString() {
        return id+"|"+name+"|"+category+"|"+manufacturer+"|"+price
             +"|"+quantity+"|"+initialQuantity+"|"+expiryDate;
    }

    public static Medicine fromFile(String l) {
        String[] p = l.split("\\|", -1);
        // Support old 7-field files (no initialQuantity)
        if (p.length < 8)
            return new Medicine(p[0], p[1], p[2], p[3],
                                Double.parseDouble(p[4]), Integer.parseInt(p[5]),
                                Integer.parseInt(p[5]), p[6]);
        return new Medicine(p[0], p[1], p[2], p[3],
                            Double.parseDouble(p[4]), Integer.parseInt(p[5]),
                            Integer.parseInt(p[6]), p[7]);
    }

    @Override public String[] toTableRow() {
        return new String[]{id, name, category, manufacturer,
                            String.format("%.2f", price),
                            String.valueOf(initialQuantity),
                            String.valueOf(quantity),
                            expiryDate};
    }
    @Override public String   getDisplayName() { return name; }
    @Override public String   getSummary()     { return "GHS "+price+" | Qty: "+quantity+"/"+initialQuantity; }
    @Override public String[] getColumnHeaders(){
        return new String[]{"ID","Name","Category","Manufacturer","Price(GHS)","Initial Qty","Qty Left","Expiry"};
    }
}
