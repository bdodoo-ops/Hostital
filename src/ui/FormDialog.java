package ui;

import util.AppTheme;
import javax.swing.*;
import java.awt.*;
import java.util.*;

/**
 * Reusable modal form dialog used by every system panel.
 * Field types: TEXT | NUMBER | DATE | SELECT | AREA | READONLY
 */
public class FormDialog extends JDialog {

    private final Map<String, JComponent> fields = new LinkedHashMap<>();
    private boolean confirmed = false;

    /**
     * @param parent    owner frame
     * @param title     dialog title
     * @param fieldDefs each row: { key, label, type, options/default }
     */
    public FormDialog(Frame parent, String title, String[][] fieldDefs) {
        super(parent, title, true);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // ── Form grid ──────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 22, 8, 22));

        GridBagConstraints g = new GridBagConstraints();
        g.fill    = GridBagConstraints.HORIZONTAL;
        g.insets  = new Insets(5, 4, 5, 4);

        for (int i = 0; i < fieldDefs.length; i++) {
            String key    = fieldDefs[i][0];
            String label  = fieldDefs[i][1];
            String type   = fieldDefs[i][2];
            String extra  = fieldDefs[i].length > 3 ? fieldDefs[i][3] : "";

            // Label
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            JLabel lbl = new JLabel(label + "  ");
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setPreferredSize(new Dimension(160, 28));
            form.add(lbl, g);

            // Field
            g.gridx = 1; g.weightx = 1;
            JComponent comp = buildField(type, extra);
            fields.put(key, comp);
            form.add(comp, g);
        }

        // ── Buttons ────────────────────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        btns.setBackground(Color.WHITE);
        btns.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));

        JButton cancel = AppTheme.btnDanger("Cancel");
        cancel.setPreferredSize(new Dimension(90, 32));
        cancel.addActionListener(e -> dispose());

        JButton save = AppTheme.btnSuccess("Save");
        save.setPreferredSize(new Dimension(90, 32));
        save.addActionListener(e -> { confirmed = true; dispose(); });

        btns.add(cancel);
        btns.add(save);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);
        add(btns,   BorderLayout.SOUTH);

        int h = Math.min(600, 100 + fieldDefs.length * 44);
        setSize(480, h);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    // ── Field builder ─────────────────────────────────────────────────────

    private JComponent buildField(String type, String extra) {
        switch (type) {
            case "SELECT": {
                String[] opts = extra.isEmpty() ? new String[]{""} : extra.split("\\|");
                JComboBox<String> cb = new JComboBox<>(opts);
                cb.setFont(AppTheme.FONT_BODY);
                return cb;
            }
            case "AREA": {
                JTextArea ta = new JTextArea(3, 20);
                ta.setFont(AppTheme.FONT_BODY);
                ta.setLineWrap(true);
                ta.setWrapStyleWord(true);
                JScrollPane sp = new JScrollPane(ta);
                sp.setPreferredSize(new Dimension(240, 70));
                return sp;
            }
            case "READONLY": {
                JTextField tf = new JTextField(extra);
                tf.setFont(AppTheme.FONT_BODY);
                tf.setEditable(false);
                tf.setBackground(new Color(242, 244, 246));
                return tf;
            }
            default: { // TEXT | NUMBER | DATE
                JTextField tf = new JTextField(extra);
                tf.setFont(AppTheme.FONT_BODY);
                return tf;
            }
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────

    public boolean isConfirmed() { return confirmed; }

    public String get(String key) {
        JComponent c = fields.get(key);
        if (c instanceof JComboBox) return (String) ((JComboBox<?>) c).getSelectedItem();
        if (c instanceof JScrollPane) {
            Component v = ((JScrollPane) c).getViewport().getView();
            if (v instanceof JTextArea) return ((JTextArea) v).getText().trim();
        }
        if (c instanceof JTextField) return ((JTextField) c).getText().trim();
        return "";
    }

    @SuppressWarnings("unchecked")
    public void set(String key, String value) {
        JComponent c = fields.get(key);
        if (c instanceof JComboBox) ((JComboBox<String>) c).setSelectedItem(value);
        else if (c instanceof JScrollPane) {
            Component v = ((JScrollPane) c).getViewport().getView();
            if (v instanceof JTextArea) ((JTextArea) v).setText(value);
        } else if (c instanceof JTextField) ((JTextField) c).setText(value);
    }

    /** Show a read-only detail dialog for any key→value map. */
    public static void showDetail(Frame parent, String title, String[][] rows) {
        JDialog dlg = new JDialog(parent, title, true);
        dlg.setLayout(new BorderLayout());
        dlg.getContentPane().setBackground(Color.WHITE);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 4, 4, 8);

        for (int i = 0; i < rows.length; i++) {
            g.gridx = 0; g.gridy = i; g.anchor = GridBagConstraints.WEST; g.weightx = 0;
            JLabel lbl = new JLabel(rows[i][0] + " :");
            lbl.setFont(AppTheme.FONT_LABEL);
            lbl.setPreferredSize(new Dimension(170, 24));
            content.add(lbl, g);

            g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
            JLabel val = new JLabel(rows[i].length > 1 ? rows[i][1] : "");
            val.setFont(AppTheme.FONT_BODY);
            content.add(val, g);
        }

        JButton close = AppTheme.btnPrimary("Close");
        close.addActionListener(e -> dlg.dispose());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(Color.WHITE);
        btns.add(close);

        dlg.add(new JScrollPane(content), BorderLayout.CENTER);
        dlg.add(btns, BorderLayout.SOUTH);
        dlg.setSize(460, Math.min(600, 120 + rows.length * 34));
        dlg.setLocationRelativeTo(parent);
        dlg.setVisible(true);
    }
}
