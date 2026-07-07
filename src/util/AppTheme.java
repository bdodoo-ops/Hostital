package util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/** Centralised colour palette, fonts, and component factory. */
public class AppTheme {

    // ── Colours ──────────────────────────────────────────────────────────────
    public static final Color PRIMARY        = new Color(26,  82, 118);
    public static final Color PRIMARY_DARK   = new Color(21,  67,  96);
    public static final Color PRIMARY_LIGHT  = new Color(52, 152, 219);
    public static final Color ACCENT         = new Color(39, 174,  96);
    public static final Color DANGER         = new Color(192, 57,  43);
    public static final Color WARNING        = new Color(211,159,  28);
    public static final Color INFO           = new Color(41, 128, 185);

    public static final Color BG_MAIN        = new Color(241, 243, 246);
    public static final Color BG_SIDEBAR     = new Color(26,  42,  74);
    public static final Color BG_SIDEBAR_SEL = new Color(52, 152, 219);
    public static final Color BG_SIDEBAR_HOV = new Color(39,  60, 107);
    public static final Color BG_CARD        = Color.WHITE;
    public static final Color BG_TABLE_HDR   = new Color(44,  62,  80);
    public static final Color BG_ROW_ALT     = new Color(235, 240, 244);

    public static final Color TEXT_MAIN      = new Color(44,  62,  80);
    public static final Color TEXT_MUTED     = new Color(127,140, 141);
    public static final Color TEXT_SIDEBAR   = new Color(189,195, 199);
    public static final Color TEXT_WHITE     = Color.WHITE;

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font FONT_HEADER  = new Font("SansSerif", Font.BOLD,  24);
    public static final Font FONT_TITLE   = new Font("SansSerif", Font.BOLD,  17);
    public static final Font FONT_BODY    = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("SansSerif", Font.PLAIN, 11);
    public static final Font FONT_BTN     = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_TABLE   = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font FONT_SIDEBAR = new Font("SansSerif", Font.BOLD,  12);
    public static final Font FONT_LABEL   = new Font("SansSerif", Font.BOLD,  13);

    // ── Apply global L&F ────────────────────────────────────────────────────
    public static void apply() {
        try {
            // Use the native Windows look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        UIManager.put("Table.font",       FONT_TABLE);
        UIManager.put("Label.font",       FONT_BODY);
        UIManager.put("Button.font",      FONT_BTN);
        UIManager.put("TextField.font",   FONT_BODY);
        UIManager.put("ComboBox.font",    FONT_BODY);
        UIManager.put("TextArea.font",    FONT_BODY);
    }

    // ── Button factory ───────────────────────────────────────────────────────
    public static JButton btn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(TEXT_WHITE);
        b.setFont(FONT_BTN);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return b;
    }

    public static JButton btnPrimary(String t) { return btn(t, PRIMARY); }
    public static JButton btnSuccess(String t)  { return btn(t, ACCENT);  }
    public static JButton btnDanger(String t)   { return btn(t, DANGER);  }
    public static JButton btnWarning(String t)  {
        JButton b = btn(t, WARNING);
        b.setForeground(TEXT_MAIN);
        return b;
    }
    public static JButton btnInfo(String t)     { return btn(t, INFO);    }

    /** Rounded card panel (white background + subtle border). */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(BG_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(218, 223, 230), 1, true),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        return p;
    }

    /** Standard labelled text field row for forms. */
    public static JTextField labeledField(JPanel form, GridBagConstraints g, int row, String label) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 1;
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        form.add(tf, g);
        return tf;
    }

    public static JComboBox<String> labeledCombo(JPanel form, GridBagConstraints g,
                                                  int row, String label, String[] opts) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 1;
        JComboBox<String> cb = new JComboBox<>(opts);
        cb.setFont(FONT_BODY);
        form.add(cb, g);
        return cb;
    }

    public static JTextArea labeledArea(JPanel form, GridBagConstraints g, int row, String label) {
        g.gridx = 0; g.gridy = row; g.weightx = 0; g.anchor = GridBagConstraints.NORTH;
        JLabel lbl = new JLabel(label);
        lbl.setFont(FONT_LABEL);
        form.add(lbl, g);
        g.gridx = 1; g.weightx = 1; g.anchor = GridBagConstraints.CENTER;
        JTextArea ta = new JTextArea(3, 20);
        ta.setFont(FONT_BODY);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        form.add(sp, g);
        return ta;
    }
}
