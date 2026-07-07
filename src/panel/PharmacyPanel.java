package panel;

import manager.PharmacyManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PharmacyPanel extends BasePanel {
    private final PharmacyManager mgr = new PharmacyManager();
    private int view = 0; // 0=medicines 1=sales

    @Override public String   getTitle()  { return "💊  Pharmacy Inventory Management"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"Sale ID","Patient","Medicine","Qty","Unit Price","Total","Date"};
        return new String[]{"ID","Name","Category","Manufacturer","Price(GHS)","Initial Qty","Qty Left","Expiry"};
    }

    @Override public void populate(String q){
        if(view==1){ List<Sale> l=q==null?mgr.sales.getAll():mgr.sales.search(q); for(Sale s:l) model.addRow(s.toTableRow()); }
        else { List<Medicine> l=q==null?mgr.medicines.getAll():mgr.medicines.search(q); for(Medicine m:l) model.addRow(m.toTableRow()); }
    }

    @Override public void handleAdd(){
        if(view==1){ sellMedicine(); return; }
        String[][] f={{"name","Medicine Name","TEXT"},{"cat","Category","TEXT"},
                      {"mfr","Manufacturer","TEXT"},{"price","Unit Price (GHS)","NUMBER"},
                      {"qty","Quantity","NUMBER"},{"exp","Expiry Date","TEXT","YYYY-MM-DD"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Medicine",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            Medicine m=new Medicine(mgr.medicines.nextId(),
                Validator.requireNonEmpty(d.get("name"),"Name"), d.get("cat"), d.get("mfr"),
                Validator.requirePositiveDouble(d.get("price"),"Price"),
                Validator.requirePositiveInt(d.get("qty"),"Quantity"), d.get("exp"));
            mgr.medicines.add(m); refresh(); info("Medicine added. ID: "+m.getId());
        }catch(Exception ex){ error(ex.getMessage()); }
    }

    private void sellMedicine() {
        List<Medicine>  meds     = mgr.medicines.getAll();
        List<RecPatient> patients = RecordManager.get().patients.getAll();
        if (meds.isEmpty()) { error("No medicines in inventory."); return; }

        // Medicine items
        String[] medItems = meds.stream()
            .map(m -> m.getId() + "  |  " + m.getName() + "  (Stock: " + m.getQuantity() + "  |  GHS " + m.getPrice() + ")")
            .toArray(String[]::new);

        // Patient items — walk-in option first
        String[] patientItems = new String[patients.size() + 1];
        patientItems[0] = "-- Walk-in / No patient --";
        for (int i = 0; i < patients.size(); i++)
            patientItems[i + 1] = patients.get(i).getId() + "  |  " + patients.get(i).getName();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sell Medicine", true);
        dlg.setSize(480, 280);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(14, 16, 6, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JComboBox<String> patBox = new JComboBox<>(patientItems);
        JComboBox<String> medBox = new JComboBox<>(medItems);
        JTextField qtyField = new JTextField("1");

        addSaleRow(form, gc, 0, "Patient:",  patBox);
        addSaleRow(form, gc, 1, "Medicine:", medBox);
        addSaleRow(form, gc, 2, "Quantity:", qtyField);

        dlg.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Color.WHITE);
        JButton ok     = AppTheme.btnSuccess("Sell");
        JButton cancel = AppTheme.btnDanger("Cancel");
        btns.add(cancel); btns.add(ok);
        dlg.add(btns, BorderLayout.SOUTH);

        boolean[] confirmed = {false};
        ok    .addActionListener(e -> { confirmed[0] = true; dlg.dispose(); });
        cancel.addActionListener(e -> dlg.dispose());
        dlg.getRootPane().setDefaultButton(ok);
        dlg.setVisible(true);
        if (!confirmed[0]) return;

        try {
            Medicine   med  = meds.get(medBox.getSelectedIndex());
            int patSel      = patBox.getSelectedIndex();
            String patId    = patSel == 0 ? "" : patients.get(patSel - 1).getId();
            String patName  = patSel == 0 ? "Walk-in" : patients.get(patSel - 1).getName();
            int qty = Validator.requirePositiveInt(qtyField.getText().trim(), "Quantity");
            Sale s  = mgr.sell(med.getId(), patId, patName, qty);
            refresh();
            String billNote = patId.isEmpty() ? "" : "\nAdded to patient's pending charges.";
            info(String.format("Sale complete!\nReceipt: %s\nPatient: %s\nMedicine: %s\nTotal: GHS %.2f%s",
                               s.getId(), patName, med.getName(), s.getTotal(), billNote));
        } catch (Exception ex) { error(ex.getMessage()); }
    }

    private void addSaleRow(JPanel form, GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(lbl, gc);
        gc.gridx = 1; gc.weightx = 1;
        comp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(comp, gc);
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null) return;
        if(view==0){
            Medicine m=mgr.medicines.findById(id); if(m==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Medicine Detail",new String[][]{
                {"ID",m.getId()},{"Name",m.getName()},{"Category",m.getCategory()},
                {"Manufacturer",m.getManufacturer()},{"Price","GHS "+String.format("%.2f",m.getPrice())},
                {"Initial Quantity",String.valueOf(m.getInitialQuantity())},
                {"Quantity Left",String.valueOf(m.getQuantity())},
                {"Expiry",m.getExpiryDate()}});
        } else {
            Sale s=mgr.sales.findById(id); if(s==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Sale Detail",new String[][]{
                {"Sale ID",s.getId()},
                {"Patient ID",  s.getPatientId().isEmpty()  ? "—" : s.getPatientId()},
                {"Patient Name",s.getPatientName().isEmpty() ? "Walk-in" : s.getPatientName()},
                {"Medicine",s.getMedicineName()},
                {"Quantity",String.valueOf(s.getQuantity())},
                {"Unit Price","GHS "+String.format("%.2f",s.getUnitPrice())},
                {"Total","GHS "+String.format("%.2f",s.getTotal())},
                {"Date",s.getDate()}});
        }
    }

    @Override public void handleEdit(){
        String id=selectedId(); if(id==null||view==1) return;
        Medicine m=mgr.medicines.findById(id); if(m==null) return;
        String[][] f={{"price","Unit Price (GHS)","NUMBER",String.valueOf(m.getPrice())},
                      {"qty","Quantity","NUMBER",String.valueOf(m.getQuantity())},
                      {"exp","Expiry Date","TEXT",m.getExpiryDate()}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Update Medicine",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            m.setPrice(Validator.requirePositiveDouble(d.get("price"),"Price"));
            m.setQuantity(Validator.requirePositiveInt(d.get("qty"),"Quantity"));
            mgr.medicines.update(m); refresh();
        }catch(Exception ex){ error(ex.getMessage()); }
    }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if (view==1) {
            Sale s = mgr.sales.findById(id);
            if (s != null) {
                Medicine m = mgr.medicines.findById(s.getMedicineId());
                if (m != null) { m.setQuantity(m.getQuantity() + s.getQuantity()); mgr.medicines.update(m); }
            }
            mgr.sales.remove(id);
        } else {
            mgr.medicines.remove(id);
        }
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton mBtn=AppTheme.btnInfo("Medicines"); mBtn.setPreferredSize(new Dimension(100,34));
        JButton sBtn=AppTheme.btnInfo("Sales");     sBtn.setPreferredSize(new Dimension(80,34));
        JButton lBtn=AppTheme.btnWarning("Low Stock"); lBtn.setPreferredSize(new Dimension(100,34));
        JButton rBtn=AppTheme.btnSuccess("Revenue"); rBtn.setPreferredSize(new Dimension(90,34));
        mBtn.addActionListener(e->{ view=0; rebuildCols(); refresh(); });
        sBtn.addActionListener(e->{ view=1; rebuildCols(); refresh(); });
        lBtn.addActionListener(e->showLowStock());
        rBtn.addActionListener(e->showRevenue());
        p.add(mBtn); p.add(sBtn); p.add(lBtn); p.add(rBtn);
        return p;
    }

    private void rebuildCols(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }

    private void showLowStock() {
        List<Medicine> low = mgr.getLowStock(10);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Low Stock Alert", true);
        dlg.setSize(680, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));

        // Header bar
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(192, 57, 43));
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel(low.isEmpty()
            ? "All medicines are adequately stocked"
            : "⚠  " + low.size() + " medicine(s) with quantity below 10");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        hdr.add(title, BorderLayout.WEST);
        dlg.add(hdr, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Medicine Name", "Category", "Initial Qty", "Qty Left", "Price (GHS)", "Expiry"};
        javax.swing.table.DefaultTableModel tm = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Medicine m : low)
            tm.addRow(new String[]{m.getId(), m.getName(), m.getCategory(),
                                   String.valueOf(m.getInitialQuantity()),
                                   String.valueOf(m.getQuantity()),
                                   String.format("%.2f", m.getPrice()),
                                   m.getExpiryDate()});

        JTable tbl = new JTable(tm);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(26);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);
        tbl.setBackground(Color.WHITE);
        tbl.setSelectionBackground(new Color(255, 200, 200));

        // Highlight rows where qty is critically low (≤5) in red
        tbl.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) {
                    int qty = Integer.parseInt(tm.getValueAt(r, 4).toString());
                    setBackground(qty <= 5 ? new Color(255, 235, 235) : (r % 2 == 0 ? Color.WHITE : new Color(255, 245, 245)));
                    setForeground(qty <= 5 ? new Color(180, 0, 0) : Color.BLACK);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        javax.swing.table.JTableHeader th = tbl.getTableHeader();
        th.setBackground(new Color(192, 57, 43));
        th.setForeground(Color.BLACK);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(0, 30));

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(sp, BorderLayout.CENTER);

        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        foot.setBackground(new Color(250, 250, 250));
        foot.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        JLabel legend = new JLabel("  Red rows = critically low (≤ 5 units)");
        legend.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        legend.setForeground(new Color(150, 0, 0));
        foot.add(legend);
        JButton close = AppTheme.btnDanger("Close");
        close.addActionListener(e -> dlg.dispose());
        foot.add(close);
        dlg.add(foot, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void showRevenue() {
        List<Sale> sales = mgr.sales.getAll();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sales Revenue Report", true);
        dlg.setSize(760, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));

        // Header
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(39, 174, 96));
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Sales Revenue Report  —  " + sales.size() + " transaction(s)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        hdr.add(title, BorderLayout.WEST);
        dlg.add(hdr, BorderLayout.NORTH);

        // Table
        String[] cols = {"Sale ID", "Date", "Patient", "Medicine", "Qty", "Unit Price (GHS)", "Total (GHS)"};
        javax.swing.table.DefaultTableModel tm = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        double grandTotal    = 0;
        int    totalUnits    = 0;
        for (Sale s : sales) {
            tm.addRow(new String[]{
                s.getId(), s.getDate(),
                s.getPatientName().isEmpty() ? "Walk-in" : s.getPatientName(),
                s.getMedicineName(),
                String.valueOf(s.getQuantity()),
                String.format("%.2f", s.getUnitPrice()),
                String.format("%.2f", s.getTotal())
            });
            grandTotal += s.getTotal();
            totalUnits += s.getQuantity();
        }

        JTable tbl = new JTable(tm);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(26);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);
        tbl.setBackground(Color.WHITE);
        tbl.setSelectionBackground(new Color(198, 239, 206));
        tbl.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(242, 249, 244));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                // Right-align numeric columns
                setHorizontalAlignment(c >= 4 ? RIGHT : LEFT);
                return this;
            }
        });

        javax.swing.table.JTableHeader th = tbl.getTableHeader();
        th.setBackground(new Color(39, 174, 96));
        th.setForeground(Color.BLACK);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(0, 30));

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(sp, BorderLayout.CENTER);

        // Summary footer
        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(new Color(232, 245, 233));
        foot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(39, 174, 96)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel summaryLeft = new JPanel(new GridLayout(2, 2, 20, 4));
        summaryLeft.setBackground(new Color(232, 245, 233));
        addSummaryItem(summaryLeft, "Total Transactions:", String.valueOf(sales.size()));
        addSummaryItem(summaryLeft, "Total Units Sold:",   String.valueOf(totalUnits));
        addSummaryItem(summaryLeft, "Average per Sale:",
            sales.isEmpty() ? "GHS 0.00" : String.format("GHS %.2f", grandTotal / sales.size()));
        addSummaryItem(summaryLeft, "", "");
        foot.add(summaryLeft, BorderLayout.WEST);

        JLabel totalLbl = new JLabel("Grand Total:  GHS " + String.format("%.2f", grandTotal));
        totalLbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLbl.setForeground(new Color(27, 120, 60));
        totalLbl.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(new Color(232, 245, 233));
        rightPanel.add(totalLbl, BorderLayout.CENTER);
        JButton close = AppTheme.btnSuccess("Close");
        close.addActionListener(e -> dlg.dispose());
        JPanel closeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        closeWrap.setBackground(new Color(232, 245, 233));
        closeWrap.add(close);
        rightPanel.add(closeWrap, BorderLayout.SOUTH);
        foot.add(rightPanel, BorderLayout.EAST);

        dlg.add(foot, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void addSummaryItem(JPanel p, String label, String value) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(50, 50, 50));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(new Color(30, 30, 30));
        p.add(l); p.add(v);
    }
}
