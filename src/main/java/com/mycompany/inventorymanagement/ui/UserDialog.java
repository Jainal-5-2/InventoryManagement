package com.mycompany.inventorymanagement.ui;

import com.mycompany.inventorymanagement.InventoryPanel;
import com.mycompany.inventorymanagement.User;
import com.mycompany.inventorymanagement.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.mycompany.inventorymanagement.UITheme.*;

/**
 * Add or edit a user. When editing, username is read-only (it's the login key)
 * and the password field is optional — leave blank to keep the existing one.
 * Passwords are currently stored in plaintext, matching UserDAO.register.
 */
public class UserDialog extends JDialog {

    private final User existing;
    private final UserDAO userDAO;

    private boolean saved = false;

    private JTextField fullNameField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;

    public UserDialog(Window owner, User existing, UserDAO userDAO) {
        super(owner, existing == null ? "Add User" : "Edit User",
                ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        this.userDAO = userDAO;
        initUI();
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(440, 520);
        setMinimumSize(new Dimension(400, 440));
        setLocationRelativeTo(getOwner());
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(24, 30, 16, 30));

        JLabel heading = new JLabel(existing == null ? "New user" : "Edit user");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        header.add(heading, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 30, 16, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Full name
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(fieldLabel("Full name"), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        fullNameField = new JTextField();
        styleDialogField(fullNameField, "e.g. Jane Doe");
        form.add(fullNameField, gbc);

        // Username
        gbc.gridy = 2;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Username"), gbc);
        gbc.gridy = 3;
        gbc.insets = new Insets(4, 0, 0, 0);
        usernameField = new JTextField();
        styleDialogField(usernameField, "at least 3 characters");
        form.add(usernameField, gbc);

        // Password
        gbc.gridy = 4;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel(existing == null
                ? "Password" : "New password (leave blank to keep)"), gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 0, 0);
        passwordField = new JPasswordField();
        styleDialogField(passwordField, "at least 6 characters");
        passwordField.putClientProperty("JPasswordField.showRevealButton", true);
        form.add(passwordField, gbc);

        // Confirm
        gbc.gridy = 6;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Confirm password"), gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 0, 0);
        confirmField = new JPasswordField();
        styleDialogField(confirmField, "repeat password");
        confirmField.putClientProperty("JPasswordField.showRevealButton", true);
        form.add(confirmField, gbc);

        gbc.gridy = 8;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        form.add(Box.createGlue(), gbc);

        JScrollPane scroller = new JScrollPane(form);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(SURFACE);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.getVerticalScrollBar().setUnitIncrement(16);

        root.add(scroller, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(16, 30, 22, 30));

        JButton cancel = new InventoryPanel.SecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 40));
        cancel.addActionListener(e -> dispose());

        JButton save = new AccentButton(existing == null ? "Add" : "Save");
        save.setPreferredSize(new Dimension(120, 40));
        save.addActionListener(e -> doSave());

        actions.add(cancel);
        actions.add(save);
        root.add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(save);
        setContentPane(root);

        if (existing != null) {
            fullNameField.setText(existing.getFullName());
            usernameField.setText(existing.getUsername());
            usernameField.setEditable(false);
            usernameField.setBackground(new Color(245, 246, 250));
            usernameField.setForeground(TEXT_MUTED);
        }
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void styleDialogField(JTextField f, String placeholder) {
        f.setFont(FONT_BASE);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.putClientProperty("JComponent.roundRect", true);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        f.setPreferredSize(new Dimension(0, 42));
    }

    private void doSave() {
        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (fullName.isEmpty()) {
            warn("Please enter a full name.");
            return;
        }

        if (existing == null) {
            if (username.isEmpty()) {
                warn("Please enter a username.");
                return;
            }
            if (username.length() < 3) {
                warn("Username must be at least 3 characters.");
                return;
            }
            if (password.isEmpty()) {
                warn("Please enter a password.");
                return;
            }
            if (password.length() < 6) {
                warn("Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirm)) {
                warn("Passwords do not match.");
                return;
            }

            UserDAO.RegisterResult result = userDAO.register(username, password, fullName);
            switch (result) {
                case SUCCESS -> {
                    saved = true;
                    dispose();
                }
                case USERNAME_TAKEN ->
                    warn("That username is already taken.");
                default ->
                    warn("Failed to create user.");
            }
            return;
        }

        // Editing: password optional
        String newPassword = null;
        if (!password.isEmpty() || !confirm.isEmpty()) {
            if (password.length() < 6) {
                warn("Password must be at least 6 characters.");
                return;
            }
            if (!password.equals(confirm)) {
                warn("Passwords do not match.");
                return;
            }
            newPassword = password;
        }

        if (userDAO.updateUser(existing.getId(), fullName, newPassword)) {
            saved = true;
            dispose();
        } else {
            warn("Failed to save user.");
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation",
                JOptionPane.WARNING_MESSAGE);
    }
}
