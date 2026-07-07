package ui;

import panel.*;
import util.AppTheme;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainWindow extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel     content = new JPanel(cards);
    private JButton          activeNav = null;

    private static final Color NAV_BG       = new Color(240, 240, 240);
    private static final Color NAV_SEL      = new Color(0,  120, 215);   // Windows blue
    private static final Color NAV_HOV      = new Color(204, 228, 247);
    private static final Color HDR_BG       = new Color(0,  120, 215);
    private static final Color MAIN_BG      = new Color(248, 248, 248);

    private static final String[] IDS = {
        "HOME",
        "PATIENTS","DOCTORS",
        "QUEUE","RECORDS","PHARMACY","APPOINTMENTS",
        "BLOOD","VACCINATION","AMBULANCE","LAB","BILLING","MATERNAL"
    };
    private static final String[] LABELS = {
        "Home",
        "Patients",
        "Doctors",
        "1.  Queue Management",
        "2.  Medical Records",
        "3.  Pharmacy",
        "4.  Appointments",
        "5.  Blood Donation",
        "6.  Vaccination",
        "7.  Ambulance Dispatch",
        "8.  Laboratory",
        "9.  Billing & Payment",
        "10. Maternal Health"
    };

    public MainWindow() {
        super("Hospital Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 640));
        setLocationRelativeTo(null);
        setIconImage(new ImageIcon(new byte[0]).getImage());
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        setLayout(new BorderLayout(0, 0));
        add(buildHeader(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);

        content.setBackground(MAIN_BG);
        content.add(buildHome(),            "HOME");
        content.add(new PatientsPanel(),    "PATIENTS");
        content.add(new DoctorsPanel(),     "DOCTORS");
        content.add(new QueuePanel(),       "QUEUE");
        content.add(new RecordsPanel(),     "RECORDS");
        content.add(new PharmacyPanel(),    "PHARMACY");
        content.add(new AppointmentPanel(), "APPOINTMENTS");
        content.add(new BloodPanel(),       "BLOOD");
        content.add(new VaccinationPanel(), "VACCINATION");
        content.add(new AmbulancePanel(),   "AMBULANCE");
        content.add(new LabPanel(),         "LAB");
        content.add(new BillingPanel(),     "BILLING");
        content.add(new MaternalPanel(),    "MATERNAL");
        add(content, BorderLayout.CENTER);

        cards.show(content, "HOME");
    }

    // ── Header bar ────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(HDR_BG);
        h.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));

        JLabel title = new JLabel("  Hospital Management System");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        h.add(title, BorderLayout.WEST);

        JLabel clock = new JLabel();
        clock.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        clock.setForeground(new Color(210, 230, 255));
        h.add(clock, BorderLayout.EAST);

        Timer t = new Timer(1000, e ->
            clock.setText(new SimpleDateFormat("EEE, dd MMM yyyy   HH:mm:ss").format(new Date())));
        t.start();
        t.getActionListeners()[0].actionPerformed(null);
        return h;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(NAV_BG);
        sidebar.setPreferredSize(new Dimension(210, 600));
        sidebar.setBorder(new MatteBorder(0, 0, 0, 1, new Color(200, 200, 200)));

        // section label
        JLabel lbl = new JLabel("  MODULES");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(120, 120, 120));
        lbl.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);

        for (int i = 0; i < IDS.length; i++) {
            final String id  = IDS[i];
            final int    idx = i;
            JButton btn = makeNavBtn(LABELS[i]);
            btn.addActionListener(e -> {
                cards.show(content, id);
                setActiveBtn(btn);
            });
            sidebar.add(btn);

            // After "Home" → REGISTRY section
            if (i == 0) {
                addSectionDivider(sidebar, "REGISTRY");
            }
            // After "Doctors" (index 2) → SYSTEMS section
            if (i == 2) {
                addSectionDivider(sidebar, "SYSTEMS");
            }

            if (i == 0) { setActiveBtn(btn); }
        }
        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private void addSectionDivider(JPanel sidebar, String label) {
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(210, 1));
        sep.setForeground(new Color(200, 200, 200));
        sidebar.add(sep);
        JLabel lbl = new JLabel("  " + label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(120, 120, 120));
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 12, 4, 0));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);
    }

    private JButton makeNavBtn(String label) {
        JButton b = new JButton("  " + label);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(new Color(40, 40, 40));
        b.setBackground(NAV_BG);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(210, 36));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(true);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { if (b != activeNav) b.setBackground(NAV_HOV); }
            public void mouseExited(MouseEvent e)  { if (b != activeNav) b.setBackground(NAV_BG);  }
        });
        return b;
    }

    private void setActiveBtn(JButton btn) {
        if (activeNav != null) {
            activeNav.setBackground(NAV_BG);
            activeNav.setForeground(new Color(40, 40, 40));
            activeNav.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        }
        activeNav = btn;
        btn.setBackground(NAV_SEL);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    }

    // ── Home dashboard ────────────────────────────────────────────────────────
    private JPanel buildHome() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(MAIN_BG);
        p.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(MAIN_BG);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        JLabel t1 = new JLabel("Welcome to Hospital Management System");
        t1.setFont(new Font("Segoe UI", Font.BOLD, 22));
        t1.setForeground(new Color(30, 30, 30));
        JLabel t2 = new JLabel("Select a module from the sidebar or click a card below.");
        t2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t2.setForeground(new Color(100, 100, 100));
        titlePanel.add(t1);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(t2);
        titlePanel.add(Box.createVerticalStrut(20));
        p.add(titlePanel, BorderLayout.NORTH);

        // Module cards grid
        JPanel grid = new JPanel(new GridLayout(2, 5, 14, 14));
        grid.setBackground(MAIN_BG);

        String[][] modules = {
            {"Queue Management",    "Patient flow & triage"},
            {"Medical Records",     "Patients & diagnoses"},
            {"Pharmacy",            "Inventory & sales"},
            {"Appointments",        "Booking & scheduling"},
            {"Blood Donation",      "Donors & blood bank"},
            {"Vaccination",         "Immunisation records"},
            {"Ambulance Dispatch",  "Fleet & dispatch"},
            {"Laboratory",          "Tests & results"},
            {"Billing & Payment",   "Invoices & payments"},
            {"Maternal Health",     "Pregnancy monitoring"}
        };
        String[] targets = {"QUEUE","RECORDS","PHARMACY","APPOINTMENTS","BLOOD",
                            "VACCINATION","AMBULANCE","LAB","BILLING","MATERNAL"};

        for (int i = 0; i < modules.length; i++) {
            final String target = targets[i];
            final int idx = i;
            JPanel card = makeModuleCard(modules[i][0], modules[i][1]);
            card.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    cards.show(content, target);
                    // find and activate the matching sidebar button
                    Component[] comps = ((JPanel)((JScrollPane)getContentPane()
                        .getComponent(1)).getViewport().getView()).getComponents();
                    for (Component c : comps)
                        if (c instanceof JButton && ((JButton)c).getText().contains(LABELS[idx+1].trim()))
                            setActiveBtn((JButton)c);
                }
                public void mouseEntered(MouseEvent e) {
                    card.setBackground(new Color(229, 241, 251));
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(NAV_SEL, 1),
                        BorderFactory.createEmptyBorder(15, 14, 15, 14)));
                    card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
                public void mouseExited(MouseEvent e) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
                        BorderFactory.createEmptyBorder(16, 15, 16, 15)));
                }
            });
            grid.add(card);
        }
        p.add(grid, BorderLayout.CENTER);

        JLabel footer = new JLabel("Java Swing  |  OOP: Encapsulation · Inheritance · Polymorphism · Abstraction  |  10 Healthcare Systems", SwingConstants.CENTER);
        footer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        footer.setForeground(new Color(160, 160, 160));
        footer.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        p.add(footer, BorderLayout.SOUTH);
        return p;
    }

    private JPanel makeModuleCard(String name, String sub) {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(210, 210, 210), 1),
            BorderFactory.createEmptyBorder(16, 15, 16, 15)));

        JLabel n = new JLabel(name);
        n.setFont(new Font("Segoe UI", Font.BOLD, 13));
        n.setForeground(new Color(30, 30, 30));
        n.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel s = new JLabel(sub);
        s.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        s.setForeground(new Color(120, 120, 120));
        s.setAlignmentX(Component.LEFT_ALIGNMENT);

        // coloured top accent bar
        JPanel bar = new JPanel();
        bar.setBackground(HDR_BG);
        bar.setMaximumSize(new Dimension(300, 4));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(bar);
        card.add(Box.createVerticalStrut(10));
        card.add(n);
        card.add(Box.createVerticalStrut(4));
        card.add(s);
        return card;
    }
}
