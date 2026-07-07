package ui;

import util.AppTheme;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Abstract base panel shared by all 10 system panels.
 * Demonstrates: Inheritance, Abstraction, Polymorphism, Event-driven programming
 */
public abstract class BasePanel extends JPanel {

    protected JTable             table;
    protected DefaultTableModel  model;
    protected JTextField         searchField;
    protected JLabel             countLabel;

    public BasePanel() {
        setLayout(new BorderLayout(0, 10));
        setBackground(AppTheme.BG_MAIN);
        setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        build();
        // Defer first refresh so subclass field initializers run first
        SwingUtilities.invokeLater(this::refresh);
    }

    // ── Layout ───────────────────────────────────────────────────────────────

    private void build() {
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildTable(),     BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(10, 0));
        bar.setBackground(AppTheme.BG_MAIN);

        // Title + count
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setBackground(AppTheme.BG_MAIN);
        JLabel title = new JLabel(getTitle());
        title.setFont(AppTheme.FONT_TITLE);
        title.setForeground(AppTheme.PRIMARY);
        left.add(title);
        countLabel = new JLabel("  —  0 records");
        countLabel.setFont(AppTheme.FONT_SMALL);
        countLabel.setForeground(AppTheme.TEXT_MUTED);
        left.add(countLabel);
        bar.add(left, BorderLayout.WEST);

        // Search
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setBackground(AppTheme.BG_MAIN);
        searchField = new JTextField(18);
        searchField.setFont(AppTheme.FONT_BODY);
        searchField.setToolTipText("Type to filter…");
        searchField.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filter(searchField.getText()); }
        });
        JLabel searchIcon = new JLabel("Search:");
        searchIcon.setFont(AppTheme.FONT_BODY);
        right.add(searchIcon);
        right.add(searchField);
        JButton refreshBtn = AppTheme.btnInfo("⟳  Refresh");
        refreshBtn.addActionListener(e -> refresh());
        right.add(refreshBtn);
        bar.add(right, BorderLayout.EAST);

        return bar;
    }

    private JScrollPane buildTable() {
        model = new DefaultTableModel(getColumns(), 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(AppTheme.FONT_TABLE);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(173, 216, 230));
        table.setSelectionForeground(AppTheme.TEXT_MAIN);

        // Alternating row renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                if (!sel) setBackground(r % 2 == 0 ? Color.WHITE : AppTheme.BG_ROW_ALT);
                setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        // Header — light Windows style
        JTableHeader th = table.getTableHeader();
        th.setBackground(new Color(0, 120, 215));
        th.setForeground(Color.BLACK);
        th.setFont(new Font("Segoe UI", Font.BOLD, 12));
        th.setPreferredSize(new Dimension(0, 30));
        th.setReorderingAllowed(false);
        ((DefaultTableCellRenderer) th.getDefaultRenderer())
            .setHorizontalAlignment(SwingConstants.LEFT);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(210, 215, 220)));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    private JPanel buildActionBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        bar.setBackground(AppTheme.BG_MAIN);

        Dimension d = new Dimension(118, 34);
        JButton addBtn  = AppTheme.btnSuccess("➕  Add");
        JButton viewBtn = AppTheme.btnPrimary("👁  View");
        JButton editBtn = AppTheme.btnWarning("✏  Edit");
        JButton delBtn  = AppTheme.btnDanger("🗑  Delete");
        for (JButton b : new JButton[]{addBtn,viewBtn,editBtn,delBtn}) b.setPreferredSize(d);

        addBtn .addActionListener(e -> handleAdd());
        viewBtn.addActionListener(e -> handleView());
        editBtn.addActionListener(e -> handleEdit());
        delBtn .addActionListener(e -> handleDelete());

        bar.add(addBtn); bar.add(viewBtn); bar.add(editBtn); bar.add(delBtn);
        JPanel extra = extraButtons();
        if (extra != null) bar.add(extra);
        return bar;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    protected void refresh() {
        model.setRowCount(0);
        populate(null);
        countLabel.setText("  —  " + model.getRowCount() + " records");
    }

    private void filter(String q) {
        model.setRowCount(0);
        populate(q.isBlank() ? null : q);
        countLabel.setText("  —  " + model.getRowCount() + " results");
    }

    protected String selectedId() {
        int r = table.getSelectedRow();
        if (r < 0) { warn("Please select a record first."); return null; }
        return model.getValueAt(r, 0).toString();
    }

    protected void info(String msg)  { JOptionPane.showMessageDialog(this, msg, "Info",    JOptionPane.INFORMATION_MESSAGE); }
    protected void warn(String msg)  { JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);     }
    protected void error(String msg) { JOptionPane.showMessageDialog(this, msg, "Error",   JOptionPane.ERROR_MESSAGE);       }

    protected boolean confirmDelete() {
        return JOptionPane.showConfirmDialog(this,
            "Delete this record? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    // ── Abstract interface ────────────────────────────────────────────────────

    /** Panel heading shown above the table. */
    public abstract String   getTitle();

    /** Column names for the JTable. */
    public abstract String[] getColumns();

    /**
     * Fill the table. If query is null load all; otherwise load filtered rows.
     * Subclasses call model.addRow(entity.toTableRow()) for each match.
     */
    public abstract void populate(String query);

    public abstract void handleAdd();
    public abstract void handleView();
    public abstract void handleEdit();
    public abstract void handleDelete();

    /** Override to add extra buttons beside the standard CRUD set. */
    protected JPanel extraButtons() { return null; }
}
