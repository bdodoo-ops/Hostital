package panel;

import manager.BillingManager;
import manager.RecordManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class BillingPanel extends BasePanel {
    private final BillingManager mgr = new BillingManager();
    private final RecordManager  reg = RecordManager.get();
    private int view = 0; // 0=bills 1=payments 2=services

    @Override public String   getTitle()  { return "💰  Clinic Billing and Payment"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"Receipt","Bill ID","Patient","Paid(GHS)","Balance(GHS)","Method","Date"};
        if(view==2) return new String[]{"ID","Service","Category","Price(GHS)"};
        return new String[]{"Bill ID","Patient","Date","Total(GHS)","Status"};
    }

    @Override public void populate(String q){
        if(view==1){ for(Payment p:q==null?mgr.payments.getAll():mgr.payments.search(q)) model.addRow(p.toTableRow()); }
        else if(view==2){ for(BillService s:q==null?mgr.services.getAll():mgr.services.search(q)) model.addRow(s.toTableRow()); }
        else { for(Bill b:q==null?mgr.bills.getAll():mgr.bills.search(q)) model.addRow(b.toTableRow()); }
    }

    @Override public void handleAdd(){
        switch(view){
            case 2: addService(); break;
            case 1: recordPayment(); break;
            default: createBill(); break;
        }
    }

    private void addService(){
        String[][] f={{"name","Service Name","TEXT"},
                      {"cat","Category","SELECT","Laboratory|Pharmacy|Queue Management|Ambulance"},
                      {"price","Price (GHS)","NUMBER"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Add Service",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            BillService s=new BillService(mgr.services.nextId(),Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("cat"),Validator.requirePositiveDouble(d.get("price"),"Price"));
            mgr.services.add(s); refresh(); info("Service added. ID: "+s.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private static final String[] CHARGE_CATEGORIES = {"Queue Management","Laboratory","Pharmacy","Vaccination"};

    private void createBill(){
        if(reg.patients.count()==0){ info("Register a patient in the Patients module first."); return; }
        List<RecPatient> patients = reg.patients.getAll();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Bill", true);
        dlg.setSize(520, 400);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(16, 18, 6, 18));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;
        gc.gridwidth = 2;

        String[] patItems = patients.stream().map(p -> p.getId()+" — "+p.getName()).toArray(String[]::new);
        JComboBox<String> patBox = new JComboBox<>(patItems);

        gc.gridx = 0; gc.gridy = 0;
        JLabel patLbl = new JLabel("Patient:");
        patLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        form.add(patLbl, gc);
        gc.gridy = 1;
        form.add(patBox, gc);

        JCheckBox[] boxes = new JCheckBox[CHARGE_CATEGORIES.length];
        for(int i=0; i<CHARGE_CATEGORIES.length; i++){
            boxes[i] = new JCheckBox();
            boxes[i].setBackground(Color.WHITE);
            boxes[i].setFont(new Font("Segoe UI", Font.PLAIN, 13));
            gc.gridy = 2+i;
            form.add(boxes[i], gc);
        }

        Runnable refreshLines = () -> {
            RecPatient rp = patients.get(patBox.getSelectedIndex());
            for(int i=0; i<CHARGE_CATEGORIES.length; i++){
                double amt = mgr.getPendingTotal(rp.getId(), CHARGE_CATEGORIES[i]);
                boxes[i].setText(String.format("%s — GHS %.2f", CHARGE_CATEGORIES[i], amt));
                boxes[i].setSelected(false);
                boxes[i].setEnabled(amt > 0);
            }
        };
        patBox.addActionListener(e -> refreshLines.run());
        refreshLines.run();

        dlg.add(new JScrollPane(form), BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btns.setBackground(Color.WHITE);
        JButton manual = AppTheme.btnInfo("Add Custom Service…");
        JButton add    = AppTheme.btnSuccess("Add Bill(s)");
        JButton close  = AppTheme.btnDanger("Close");
        manual.addActionListener(e -> {
            RecPatient rp = patients.get(patBox.getSelectedIndex());
            dlg.dispose();
            createManualBillFor(rp);
        });
        add.addActionListener(e -> {
            RecPatient rp = patients.get(patBox.getSelectedIndex());
            List<String> created = new ArrayList<>();
            for(int i=0; i<CHARGE_CATEGORIES.length; i++){
                if(boxes[i].isSelected()){
                    try{
                        Bill b = mgr.billPendingCharges(rp.getId(), rp.getName(), CHARGE_CATEGORIES[i]);
                        created.add(String.format("%s: %s — GHS %.2f [Unpaid]", b.getId(), CHARGE_CATEGORIES[i], b.getTotal()));
                    }catch(Exception ex){ error(ex.getMessage()); }
                }
            }
            if(created.isEmpty()){ warn("Check at least one line to bill."); return; }
            refresh();
            dlg.dispose();
            info("Bill(s) created:\n"+String.join("\n",created));
        });
        close.addActionListener(e -> dlg.dispose());
        btns.add(manual); btns.add(add); btns.add(close);
        dlg.add(btns, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    private void createManualBillFor(RecPatient rp){
        if(mgr.services.count()==0){ info("No billable services defined. Add a service in the Services tab first."); return; }

        // Build service selection
        List<String> pairs = new ArrayList<>();
        List<BillService> services = mgr.services.getAll();
        String[] sOpts = services.stream().map(BillService::toString).toArray(String[]::new);
        while(true){
            String sSel=(String)JOptionPane.showInputDialog(this,"Add service (Cancel to finish):","Services — "+rp.getName(),
                JOptionPane.QUESTION_MESSAGE,null,sOpts,sOpts[0]);
            if(sSel==null) break;
            String sid=sSel.split(" ")[0];
            String qtyStr=JOptionPane.showInputDialog(this,"Quantity:");
            if(qtyStr==null) break;
            try{ int qty=Integer.parseInt(qtyStr.trim()); pairs.add(sid+":"+qty); }
            catch(Exception e){ error("Invalid quantity."); }
        }
        if(pairs.isEmpty()){ info("No services selected."); return; }
        try{
            Bill b=mgr.createBill(rp.getId(),rp.getName(),pairs);
            refresh();
            info(String.format("Bill created.\nID: %s\nTotal: GHS %.2f",b.getId(),b.getTotal()));
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void recordPayment(){
        if(mgr.bills.count()==0){ info("No bills on record."); return; }
        String[] bOpts=mgr.bills.getAll().stream()
            .filter(b->!"Paid".equals(b.getStatus()))
            .map(b->b.getId()+" - "+b.getPatientName()+" — GHS "+String.format("%.2f",b.getTotal())+" ["+b.getStatus()+"]")
            .toArray(String[]::new);
        if(bOpts.length==0){ info("All bills are paid."); return; }
        String[][] f={{"bid","Bill","SELECT",String.join("|",bOpts)},
                      {"amount","Amount (GHS)","NUMBER"},
                      {"method","Payment Method","SELECT","Cash|Card|Mobile Money|Insurance"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Record Payment",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String bid=d.get("bid").split(" ")[0];
            double amount=Validator.requirePositiveDouble(d.get("amount"),"Amount");
            Payment pay=mgr.recordPayment(bid,amount,d.get("method"));
            refresh();
            info(String.format("Payment recorded.\nReceipt: %s\nBalance: GHS %.2f",pay.getId(),pay.getBalance()));
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null||view!=0) return;
        Bill b=mgr.bills.findById(id); if(b==null) return;
        StringBuilder sb=new StringBuilder();
        for(String item:b.getLineItems()){
            String[] p=item.split(":"); if(p.length==3)
            sb.append(String.format("  %-28s x%s = GHS %.2f%n",p[0],p[1],Double.parseDouble(p[1])*Double.parseDouble(p[2])));
        }
        FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Bill Detail",new String[][]{
            {"Bill ID",b.getId()},{"Patient",b.getPatientName()},{"Date",b.getDate()},
            {"Services",sb.toString()},{"Total","GHS "+String.format("%.2f",b.getTotal())},
            {"Already Paid","GHS "+String.format("%.2f",mgr.getTotalPaid(b.getId()))},
            {"Status",b.getStatus()}});
    }

    @Override public void handleEdit(){ if(view==0) recordPayment(); }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        switch(view){
            case 0: mgr.bills.remove(id); break;
            case 1: mgr.payments.remove(id); break;
            case 2: mgr.services.remove(id); break;
        }
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton b=AppTheme.btnInfo("Bills");    b.setPreferredSize(new Dimension(70,34));
        JButton py=AppTheme.btnInfo("Payments");py.setPreferredSize(new Dimension(90,34));
        JButton s=AppTheme.btnInfo("Services"); s.setPreferredSize(new Dimension(90,34));
        JButton rv=AppTheme.btnSuccess("Revenue"); rv.setPreferredSize(new Dimension(90,34));
        b.addActionListener(e->{ view=0; rebuild(); refresh(); });
        py.addActionListener(e->{ view=1; rebuild(); refresh(); });
        s.addActionListener(e->{ view=2; rebuild(); refresh(); });
        rv.addActionListener(e->showRevenueReport());
        p.add(b); p.add(py); p.add(s); p.add(rv); return p;
    }
    private void rebuild(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }

    private void showRevenueReport(){
        List<Payment> pays = mgr.payments.getAll();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Revenue Report", true);
        dlg.setSize(860, 560);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));

        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(39, 174, 96));
        hdr.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        JLabel title = new JLabel("Revenue Report  —  " + pays.size() + " payment(s)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        hdr.add(title, BorderLayout.WEST);
        dlg.add(hdr, BorderLayout.NORTH);

        String[] cols = {"Receipt","Bill ID","Patient","Amount Paid (GHS)","Method","Date"};
        javax.swing.table.DefaultTableModel tm = new javax.swing.table.DefaultTableModel(cols, 0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        Map<String,Double> byMethod = new LinkedHashMap<>();
        double grandTotal = 0;
        for(Payment p : pays){
            tm.addRow(new String[]{p.getId(), p.getBillId(), p.getPatientName(),
                String.format("%.2f", p.getAmountPaid()), p.getMethod(), p.getDate()});
            grandTotal += p.getAmountPaid();
            byMethod.merge(p.getMethod(), p.getAmountPaid(), Double::sum);
        }

        JTable tbl = new JTable(tm);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tbl.setRowHeight(26);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);
        tbl.setBackground(Color.WHITE);
        tbl.setSelectionBackground(new Color(198, 239, 206));
        tbl.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c){
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if(!sel) setBackground(r % 2 == 0 ? Color.WHITE : new Color(242, 249, 244));
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                setHorizontalAlignment(c == 3 ? RIGHT : LEFT);
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

        List<Bill> allBills = mgr.bills.getAll();
        long unpaidCount = allBills.stream().filter(b -> !"Paid".equals(b.getStatus())).count();
        double outstandingTotal = allBills.stream()
            .filter(b -> !"Paid".equals(b.getStatus()))
            .mapToDouble(b -> b.getTotal() - mgr.getTotalPaid(b.getId()))
            .sum();

        JPanel foot = new JPanel(new BorderLayout());
        foot.setBackground(new Color(232, 245, 233));
        foot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, new Color(39, 174, 96)),
            BorderFactory.createEmptyBorder(10, 16, 10, 16)));

        JPanel summaryLeft = new JPanel(new GridLayout(0, 2, 20, 4));
        summaryLeft.setBackground(new Color(232, 245, 233));
        addSummaryItem(summaryLeft, "Total Payments:", String.valueOf(pays.size()));
        addSummaryItem(summaryLeft, "Bills Outstanding:", unpaidCount + "  (GHS " + String.format("%.2f", outstandingTotal) + ")");
        for(Map.Entry<String,Double> e : byMethod.entrySet())
            addSummaryItem(summaryLeft, e.getKey()+":", String.format("GHS %.2f", e.getValue()));
        foot.add(summaryLeft, BorderLayout.WEST);

        JLabel totalLbl = new JLabel("Total Revenue:  GHS " + String.format("%.2f", grandTotal));
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

    private void addSummaryItem(JPanel p, String label, String value){
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(new Color(50, 50, 50));
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        v.setForeground(new Color(30, 30, 30));
        p.add(l); p.add(v);
    }
}
