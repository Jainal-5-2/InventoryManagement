package com.mycompany.inventorymanagement;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.mycompany.inventorymanagement.UITheme.*;

public class RegisterFrame extends JFrame {

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JButton registerButton;
    private JLabel statusLabel;

    public RegisterFrame() {
        initUI();
    }

    private void initUI() {
        setTitle("Inventory Management — Register");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(880, 620);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(SURFACE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        gbc.gridx = 0; gbc.weightx = 0.45;
        root.add(UITheme.buildBrandPanel(
                "Join Us",
                "Create your account",
                "Set up your inventory in seconds."), gbc);

        gbc.gridx = 1; gbc.weightx = 0.55;
        root.add(buildFormPanel(), gbc);

        setContentPane(root);
    }

    private JPanel buildFormPanel() {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setBackground(SURFACE);

        JPanel card = new RoundedPanel(20, SURFACE);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(36, 44, 36, 44));
        card.setPreferredSize(new Dimension(380, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        gbc.gridy = 0;
        JLabel heading = new JLabel("Create account");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        card.add(heading, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        JLabel sub = new JLabel("Fill in the details below");
        sub.setFont(FONT_LABEL);
        sub.setForeground(TEXT_MUTED);
        card.add(sub, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(24, 0, 0, 0);
        fullNameField = new JTextField();
        UITheme.styleTextField(fullNameField, "Full name");
        card.add(fullNameField, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(14, 0, 0, 0);
        usernameField = new JTextField();
        UITheme.styleTextField(usernameField, "Username");
        card.add(usernameField, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(14, 0, 0, 0);
        passwordField = new JPasswordField();
        UITheme.styleTextField(passwordField, "Password");
        passwordField.putClientProperty("JPasswordField.showRevealButton", true);
        card.add(passwordField, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(14, 0, 0, 0);
        confirmField = new JPasswordField();
        UITheme.styleTextField(confirmField, "Confirm password");
        confirmField.putClientProperty("JPasswordField.showRevealButton", true);
        card.add(confirmField, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(22, 0, 0, 0);
        registerButton = new AccentButton("Create Account");
        registerButton.setPreferredSize(new Dimension(0, 44));
        registerButton.addActionListener(e -> doRegister());
        card.add(registerButton, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(14, 0, 0, 0);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(FONT_SMALL);
        statusLabel.setForeground(ERROR_RED);
        card.add(statusLabel, gbc);

        gbc.gridy = 8;
        gbc.insets = new Insets(16, 0, 0, 0);
        JPanel linkRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkRow.setOpaque(false);

        JLabel already = new JLabel("Already have an account?");
        already.setFont(FONT_LABEL);
        already.setForeground(TEXT_MUTED);

        JLabel backToLogin = UITheme.linkLabel("Sign in");
        backToLogin.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        linkRow.add(already);
        linkRow.add(backToLogin);
        card.add(linkRow, gbc);

        getRootPane().setDefaultButton(registerButton);

        wrapper.add(card);
        return wrapper;
    }

    private void doRegister() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmField.getPassword());

        if (fullName.isEmpty() || username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }

        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            confirmField.setText("");
            return;
        }

        UserDAO dao = new UserDAO();
        UserDAO.RegisterResult result = dao.register(username, password, fullName);

        switch (result) {
            case SUCCESS -> {
                statusLabel.setForeground(SUCCESS_GREEN);
                statusLabel.setText("Account created. Redirecting to login...");

                Timer timer = new Timer(1200, e -> {
                    dispose();
                    new LoginFrame().setVisible(true);
                });
                timer.setRepeats(false);
                timer.start();
            }
            case USERNAME_TAKEN -> showError("That username is already taken.");
            case ERROR          -> showError("Something went wrong. Please try again.");
        }
    }

    private void showError(String message) {
        statusLabel.setForeground(ERROR_RED);
        statusLabel.setText(message);
    }

    public static void main(String[] args) {
        UITheme.applyGlobalFont();
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new RegisterFrame().setVisible(true));
    }
}