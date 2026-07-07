package panel;

import manager.BloodManager;
import model.*;
import ui.BasePanel;
import ui.FormDialog;
import util.AppTheme;
import util.Validator;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class BloodPanel extends BasePanel {
    private final BloodManager mgr = new BloodManager();
    private int view = 0; // 0=donors 1=requests

    @Override public String   getTitle()  { return "🩸  Blood Donation Management"; }
    @Override public String[] getColumns(){
        if(view==1) return new String[]{"ID","Requester","Blood Type","Units","Urgency","Date","Status"};
        return new String[]{"ID","Name","Blood Type","Phone","Address","Last Donation"};
    }

    @Override public void populate(String q){
        if(view==1){ for(BloodRequest r:q==null?mgr.requests.getAll():mgr.requests.search(q)) model.addRow(r.toTableRow()); }
        else { for(Donor d:q==null?mgr.donors.getAll():mgr.donors.search(q)) model.addRow(d.toTableRow()); }
    }

    @Override public void handleAdd(){
        if(view==0){ addDonor(); } else { submitRequest(); }
    }

    private void addDonor(){
        String[][] f={{"name","Full Name","TEXT"},{"blood","Blood Type","SELECT","A+|A-|B+|B-|O+|O-|AB+|AB-"},
                      {"phone","Phone","TEXT"},{"address","Address","TEXT"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Register Donor",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            Donor dn=new Donor(mgr.donors.nextId(),Validator.requireNonEmpty(d.get("name"),"Name"),
                d.get("blood"),d.get("phone"),d.get("address"),"Never");
            mgr.donors.add(dn); refresh(); info("Donor registered. ID: "+dn.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void submitRequest(){
        String[][] f={{"req","Requester Name","TEXT"},{"blood","Blood Type","SELECT","A+|A-|B+|B-|O+|O-|AB+|AB-"},
                      {"units","Units Needed","NUMBER"},{"urg","Urgency","SELECT","Low|Medium|High"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Submit Blood Request",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            BloodRequest r=new BloodRequest(mgr.requests.nextId(),Validator.requireNonEmpty(d.get("req"),"Requester"),
                d.get("blood"),Validator.requirePositiveInt(d.get("units"),"Units"),
                d.get("urg"),java.time.LocalDate.now().toString(),"Pending");
            mgr.requests.add(r); refresh(); info("Request submitted. ID: "+r.getId());
        }catch(Exception ex){error(ex.getMessage());}
    }

    @Override public void handleView(){
        String id=selectedId(); if(id==null) return;
        if(view==0){
            Donor dn=mgr.donors.findById(id); if(dn==null) return;
            FormDialog.showDetail((Frame)SwingUtilities.getWindowAncestor(this),"Donor Detail",new String[][]{
                {"ID",dn.getId()},{"Name",dn.getName()},{"Blood Type",dn.getBloodType()},
                {"Phone",dn.getPhone()},{"Address",dn.getAddress()},{"Last Donation",dn.getLastDonation()}});
        }
    }

    @Override public void handleEdit() {
        String id = selectedId(); if (id == null) return;
        if (view == 0) {
            // Edit donor details
            Donor dn = mgr.donors.findById(id); if (dn == null) return;
            String[][] f = {
                {"name",    "Full Name",  "TEXT"},
                {"blood",   "Blood Type", "SELECT", "A+|A-|B+|B-|O+|O-|AB+|AB-"},
                {"phone",   "Phone",      "TEXT"},
                {"address", "Address",    "TEXT"}
            };
            FormDialog d = new FormDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Donor", f);
            d.set("name",    dn.getName());
            d.set("blood",   dn.getBloodType());
            d.set("phone",   dn.getPhone());
            d.set("address", dn.getAddress());
            d.setVisible(true); if (!d.isConfirmed()) return;
            try {
                dn.setName(Validator.requireNonEmpty(d.get("name"), "Name"));
                dn.setPhone(d.get("phone"));
                dn.setAddress(d.get("address"));
                mgr.donors.update(dn); refresh(); info("Donor updated.");
            } catch (Exception ex) { error(ex.getMessage()); }
        } else {
            // Fulfil a pending blood request
            BloodRequest r = mgr.requests.findById(id);
            if (r == null || !"Pending".equals(r.getStatus())) {
                info("Only pending requests can be fulfilled."); return;
            }
            try { mgr.fulfilRequest(id); refresh(); info("Request fulfilled. Blood dispensed."); }
            catch (Exception ex) { error(ex.getMessage()); }
        }
    }

    @Override public void handleDelete(){
        String id=selectedId(); if(id==null) return;
        if(!confirmDelete()) return;
        if(view==0) mgr.donors.remove(id); else mgr.requests.remove(id);
        refresh();
    }

    @Override protected JPanel extraButtons(){
        JPanel p=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));
        p.setBackground(AppTheme.BG_MAIN);
        JButton d=AppTheme.btnInfo("Donors");   d.setPreferredSize(new Dimension(80,34));
        JButton r=AppTheme.btnInfo("Requests"); r.setPreferredSize(new Dimension(90,34));
        JButton dn=AppTheme.btnSuccess("Record Donation"); dn.setPreferredSize(new Dimension(140,34));
        JButton st=AppTheme.btnPrimary("Blood Stock"); st.setPreferredSize(new Dimension(110,34));
        d.addActionListener(e->{ view=0; rebuild(); refresh(); });
        r.addActionListener(e->{ view=1; rebuild(); refresh(); });
        dn.addActionListener(e->recordDonation());
        st.addActionListener(e->showStock());
        p.add(d); p.add(r); p.add(dn); p.add(st); return p;
    }

    private void recordDonation(){
        if(mgr.donors.count()==0){ info("No donors registered."); return; }
        String[] opts=mgr.donors.getAll().stream().map(dn->dn.getId()+" — "+dn.getName()+" ("+dn.getBloodType()+")").toArray(String[]::new);
        String[][] f={{"did","Donor","SELECT",String.join("|",opts)},{"units","Units (ml)","NUMBER"}};
        FormDialog d=new FormDialog((Frame)SwingUtilities.getWindowAncestor(this),"Record Donation",f);
        d.setVisible(true); if(!d.isConfirmed()) return;
        try{
            String did=d.get("did").split(" ")[0];
            int units=Validator.requirePositiveInt(d.get("units"),"Units");
            mgr.recordDonation(did,units);
            refresh(); info("Donation recorded. Blood bank updated.");
        }catch(Exception ex){error(ex.getMessage());}
    }

    private void showStock() {
        Map<String,Integer> stock = mgr.getAllStock();
        int totalUnits    = stock.values().stream().mapToInt(Integer::intValue).sum();
        int totalDonors   = mgr.donors.count();
        int pendingReq    = (int) mgr.requests.getAll().stream().filter(r->"Pending".equals(r.getStatus())).count();
        int fulfilledReq  = (int) mgr.requests.getAll().stream().filter(r->"Fulfilled".equals(r.getStatus())).count();

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Blood Bank Stock Report", true);
        dlg.setSize(620, 540);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(0, 0));

        // ── Header ────────────────────────────────────────────────────────────
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(new Color(192, 57, 43));
        hdr.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel titleLbl = new JLabel("Blood Bank Stock Report");
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(Color.WHITE);
        hdr.add(titleLbl, BorderLayout.WEST);
        dlg.add(hdr, BorderLayout.NORTH);

        // ── Summary cards row ─────────────────────────────────────────────────
        JPanel cards = new JPanel(new GridLayout(1, 4, 10, 0));
        cards.setBackground(new Color(245, 245, 245));
        cards.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        cards.add(makeSummaryCard("Total Units",     String.valueOf(totalUnits),  new Color(192, 57, 43)));
        cards.add(makeSummaryCard("Registered Donors", String.valueOf(totalDonors), new Color(41, 128, 185)));
        cards.add(makeSummaryCard("Pending Requests",  String.valueOf(pendingReq),  new Color(211,159, 28)));
        cards.add(makeSummaryCard("Fulfilled",         String.valueOf(fulfilledReq),new Color(39, 174, 96)));

        // ── Stock table ───────────────────────────────────────────────────────
        String[] cols = {"Blood Type", "Units Available", "Status"};
        javax.swing.table.DefaultTableModel tm = new javax.swing.table.DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Map.Entry<String,Integer> e : stock.entrySet()) {
            String status = e.getValue() == 0 ? "Out of Stock"
                          : e.getValue() <= 5  ? "Critical"
                          : e.getValue() <= 20  ? "Low"
                          : "Available";
            tm.addRow(new Object[]{e.getKey(), e.getValue(), status});
        }

        JTable tbl = new JTable(tm);
        tbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tbl.setRowHeight(32);
        tbl.setShowGrid(false);
        tbl.setIntercellSpacing(new Dimension(0, 0));
        tbl.setFillsViewportHeight(true);
        tbl.setBackground(Color.WHITE);

        // Colour-coded rows by status
        tbl.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public java.awt.Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                String status = tm.getValueAt(r, 2).toString();
                if (!sel) {
                    switch (status) {
                        case "Out of Stock": setBackground(new Color(253, 237, 237)); setForeground(new Color(170,0,0)); break;
                        case "Critical":     setBackground(new Color(255, 243, 205)); setForeground(new Color(150,80,0)); break;
                        case "Low":          setBackground(new Color(255, 251, 230)); setForeground(new Color(120,100,0)); break;
                        default:             setBackground(r%2==0?Color.WHITE:new Color(242,249,244)); setForeground(new Color(30,30,30));
                    }
                }
                setFont(c == 0 ? new Font("Segoe UI", Font.BOLD, 13) : new Font("Segoe UI", Font.PLAIN, 13));
                setHorizontalAlignment(c == 1 ? CENTER : c == 0 ? LEFT : CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        javax.swing.table.JTableHeader th = tbl.getTableHeader();
        th.setBackground(new Color(192, 57, 43));
        th.setForeground(Color.BLACK);
        th.setFont(new Font("Segoe UI", Font.BOLD, 13));
        th.setPreferredSize(new Dimension(0, 32));
        ((javax.swing.table.DefaultTableCellRenderer) th.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createEmptyBorder());

        // ── Legend ─────────────────────────────────────────────────────────────
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        legend.setBackground(new Color(250, 250, 250));
        legend.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220,220,220)));
        legend.add(legendDot(new Color(253,237,237), new Color(170,0,0),    "Out of Stock (0)"));
        legend.add(legendDot(new Color(255,243,205), new Color(150,80,0),   "Critical (1–5)"));
        legend.add(legendDot(new Color(255,251,230), new Color(120,100,0),  "Low (6–20)"));
        legend.add(legendDot(new Color(242,249,244), new Color(30,30,30),   "Available (>20)"));

        // ── Pending requests that can now be fulfilled ─────────────────────────
        java.util.List<model.BloodRequest> pending = mgr.requests.getAll().stream()
            .filter(r -> "Pending".equals(r.getStatus()))
            .collect(java.util.stream.Collectors.toList());

        java.util.List<model.BloodRequest> canFulfil = pending.stream()
            .filter(r -> mgr.getStock(r.getBloodType()) >= r.getUnits())
            .collect(java.util.stream.Collectors.toList());

        JPanel reconcilePanel = new JPanel(new BorderLayout(8, 0));
        reconcilePanel.setBackground(new Color(232, 245, 233));
        reconcilePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(39,174,96)),
            BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        String recMsg = canFulfil.isEmpty()
            ? "No pending requests can be fulfilled with current stock."
            : canFulfil.size() + " pending request(s) can be fulfilled now.";
        JLabel recLbl = new JLabel(recMsg);
        recLbl.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        recLbl.setForeground(canFulfil.isEmpty() ? new Color(120,120,120) : new Color(27,120,60));
        reconcilePanel.add(recLbl, BorderLayout.CENTER);

        if (!canFulfil.isEmpty()) {
            JButton reconcileBtn = AppTheme.btnSuccess("Reconcile Now (" + canFulfil.size() + ")");
            reconcileBtn.addActionListener(e -> {
                int done = 0;
                StringBuilder result = new StringBuilder("Reconciliation complete:\n\n");
                for (model.BloodRequest r : canFulfil) {
                    try {
                        mgr.fulfilRequest(r.getId());
                        result.append("✓  ").append(r.getId()).append("  —  ")
                              .append(r.getBloodType()).append("  x").append(r.getUnits())
                              .append("  for ").append(r.getRequester()).append("\n");
                        done++;
                    } catch (Exception ex) {
                        result.append("✗  ").append(r.getId()).append("  —  ").append(ex.getMessage()).append("\n");
                    }
                }
                dlg.dispose();
                refresh();
                JOptionPane.showMessageDialog(BloodPanel.this,
                    result.toString(), done + " Request(s) Fulfilled",
                    JOptionPane.INFORMATION_MESSAGE);
            });
            reconcilePanel.add(reconcileBtn, BorderLayout.EAST);
        }

        // ── Footer ─────────────────────────────────────────────────────────────
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        foot.setBackground(new Color(250,250,250));
        JButton close = AppTheme.btnDanger("Close");
        close.addActionListener(e -> dlg.dispose());
        foot.add(close);

        JPanel center = new JPanel(new BorderLayout());
        center.add(cards, BorderLayout.NORTH);
        center.add(sp,    BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.add(legend,        BorderLayout.NORTH);
        south.add(reconcilePanel,BorderLayout.CENTER);
        south.add(foot,          BorderLayout.SOUTH);

        dlg.add(center, BorderLayout.CENTER);
        dlg.add(south,  BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private JPanel makeSummaryCard(String label, String value, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220,220,220)),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel bar = new JPanel();
        bar.setBackground(accent);
        bar.setPreferredSize(new Dimension(0, 4));
        card.add(bar, BorderLayout.NORTH);

        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Segoe UI", Font.BOLD, 22));
        val.setForeground(accent);
        card.add(val, BorderLayout.CENTER);

        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(new Color(100,100,100));
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    private JLabel legendDot(Color bg, Color fg, String text) {
        JLabel l = new JLabel("  " + text + "  ");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        l.setForeground(fg);
        l.setBackground(bg);
        l.setOpaque(true);
        l.setBorder(BorderFactory.createLineBorder(new Color(200,200,200)));
        return l;
    }

    private void rebuild(){ model.setColumnIdentifiers(getColumns()); model.setRowCount(0); }
}
