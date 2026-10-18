package com.mycompany.inventorymanagement;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.mycompany.inventorymanagement.UITheme.*;

public class LoginFrame extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 560);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(SURFACE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.45;
        root.add(UITheme.buildBrandPanel(
                "Inventory",
                "Management System",
                "Track. Manage. Grow."), gbc);

        gbc.gridx = 1; gbc.weightx = 0.55;
        root.add(buildFormPanel(), gbc);

        setContentPane(root);
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(SURFACE);

        JPanel card = new RoundedPanel(20, SURFACE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(40, 44, 40, 44));
        card.setPreferredSize(new Dimension(380, 440));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        JLabel heading = new JLabel("Welcome back");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        card.add(heading, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        JLabel sub = new JLabel("Sign in to your account");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_MUTED);
        card.add(sub, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(28, 0, 0, 0);
        usernameField = new JTextField();
        UITheme.styleTextField(usernameField, "Username");
        card.add(usernameField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(14, 0, 0, 0);
        passwordField = new JPasswordField();
        UITheme.styleTextField(passwordField, "Password");
        passwordField.putClientProperty("JPasswordField.showRevealButton", true);
        card.add(passwordField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(24, 0, 0, 0);
        loginButton = new AccentButton("Sign In");
        loginButton.setPreferredSize(new Dimension(0, 44));
        loginButton.addActionListener(e -> doLogin());
        card.add(loginButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(14, 0, 0, 0);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ERROR_RED);
        card.add(statusLabel, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(16, 0, 0, 0);
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);

        JLabel noAccount = new JLabel("Don't have an account?");
        noAccount.setFont(FONT_LABEL);
        noAccount.setForeground(TEXT_MUTED);

        JLabel createLink = UITheme.linkLabel("Create one");
        createLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new RegisterFrame().setVisible(true);
            }
        });

        linkRow.add(noAccount);
        linkRow.add(createLink);
        card.add(linkRow, gbc);

        getRootPane().setDefaultButton(loginButton);

        wrapper.add(card);
        return wrapper;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setForeground(ERROR_RED);
            statusLabel.setText("Please enter username and password.");
            return;
        }

        UserDAO dao = new UserDAO();
        User user = dao.authenticate(username, password);

        if (user == null) {
            statusLabel.setForeground(ERROR_RED);
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            return;
        }

        statusLabel.setForeground(SUCCESS_GREEN);
        statusLabel.setText("Welcome, " + user.getFullName());

        // Brief pause so the user sees the success message, then open the dashboard
        Timer timer = new Timer(400, e -> {
            dispose();
            new DashboardFrame(user).setVisible(true);
        });
        timer.setRepeats(false);
        timer.start();
    }

    public static void main(String[] args) {
        UITheme.applyGlobalFont();
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}