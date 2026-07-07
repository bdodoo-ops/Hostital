package ui;

import manager.UserManager;
import model.User;
import util.AppTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginWindow extends JFrame {

    private final UserManager userMgr = new UserManager();
    private JTextField     userField;
    private JPasswordField passField;
    private JLabel         errorLabel;

    public LoginWindow() {
        super("Hospital Management System - Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(420, 440);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        JPanel header = new JPanel();
        header.setBackground(AppTheme.PRIMARY);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(30, 0, 24, 0));

        JLabel title = new JLabel("Hospital Management System", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Sign in to continue", SwingConstants.CENTER);
        sub.setFont(AppTheme.FONT_SMALL);
        sub.setForeground(new Color(220, 232, 245));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(30, 40, 10, 40));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.insets = new Insets(6, 0, 6, 0);

        JLabel uLbl = new JLabel("Username");
        uLbl.setFont(AppTheme.FONT_LABEL);
        g.gridy = 0; form.add(uLbl, g);

        userField = new JTextField();
        userField.setFont(AppTheme.FONT_BODY);
        g.gridy = 1; form.add(userField, g);

        JLabel pLbl = new JLabel("Password");
        pLbl.setFont(AppTheme.FONT_LABEL);
        g.gridy = 2; g.insets = new Insets(14, 0, 6, 0); form.add(pLbl, g);

        passField = new JPasswordField();
        passField.setFont(AppTheme.FONT_BODY);
        g.gridy = 3; g.insets = new Insets(6, 0, 6, 0); form.add(passField, g);

        errorLabel = new JLabel(" ");
        errorLabel.setFont(AppTheme.FONT_SMALL);
        errorLabel.setForeground(AppTheme.DANGER);
        g.gridy = 4; form.add(errorLabel, g);

        JButton loginBtn = AppTheme.btnPrimary("Log In");
        loginBtn.setPreferredSize(new Dimension(100, 36));
        g.gridy = 5; g.insets = new Insets(14, 0, 0, 0); form.add(loginBtn, g);

        JLabel hint = new JLabel("Default admin login:  admin / admin123", SwingConstants.CENTER);
        hint.setFont(AppTheme.FONT_SMALL);
        hint.setForeground(AppTheme.TEXT_MUTED);
        g.gridy = 6; g.insets = new Insets(18, 0, 0, 0); form.add(hint, g);

        root.add(form, BorderLayout.CENTER);

        ActionListener attempt = e -> attemptLogin();
        loginBtn.addActionListener(attempt);
        userField.addActionListener(attempt);
        passField.addActionListener(attempt);
        getRootPane().setDefaultButton(loginBtn);
    }

    private void attemptLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        User user = userMgr.authenticate(username, password);
        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            passField.setText("");
            return;
        }
        dispose();
        SwingUtilities.invokeLater(() -> new MainWindow(user));
    }
}
