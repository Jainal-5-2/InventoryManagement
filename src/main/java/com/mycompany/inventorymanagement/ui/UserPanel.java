package com.mycompany.inventorymanagement;

import com.mycompany.inventorymanagement.ui.UserDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.mycompany.inventorymanagement.UITheme.*;

/**
 * Users management page — list accounts, add / edit / delete.
 * Does NOT yet support roles (single-role schema). Every user can do everything.
 */
public class UserPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final Integer currentUserId;

    private JTable table;
    private DefaultTableModel model;
    private JLabel countLabel;

    public UserPanel(Integer currentUserId) {
        this.currentUserId = currentUserId;
        initUI();
        reload();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JLabel heading = new JLabel("Manage accounts");
        heading.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        heading.setForeground(TEXT_MUTED);
        bar.add(heading, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addBtn = new AccentButton("+ Add User");
        addBtn.setPreferredSize(new Dimension(140, 40));
        addBtn.addActionListener(e -> openDialog(null));

        JButton editBtn = new InventoryPanel.SecondaryButton("Edit");
        editBtn.setPreferredSize(new Dimension(90, 40));
        editBtn.addActionListener(e -> {
            User selected = getSelectedUser();
            if (selected == null) {
                showInfo("Select a user to edit.");
                return;
            }
            openDialog(selected);
        });

        JButton deleteBtn = new InventoryPanel.DangerButton("Delete");
        deleteBtn.setPreferredSize(new Dimension(100, 40));
        deleteBtn.addActionListener(e -> doDelete());

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);

        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildTableArea() {
        JPanel wrap = new RoundedPanel(14, SURFACE);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] cols = { "ID", "Username", "Full Name", "Created" };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(FONT_BASE);
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(237, 237, 252));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(TEXT_MUTED);
        header.setBackground(new Color(250, 251, 254));
        header.setPreferredSize(new Dimension(0, 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(230, 232, 238)));
        header.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(180);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(180);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(rightR);

        DefaultTableCellRenderer mutedR = new DefaultTableCellRenderer();
        mutedR.setForeground(TEXT_MUTED);
        table.getColumnModel().getColumn(3).setCellRenderer(mutedR);

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    User u = getSelectedUser();
                    if (u != null) openDialog(u);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 16, 6, 16));

        countLabel = new JLabel("0 users");
        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(TEXT_MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        wrap.add(scroll, BorderLayout.CENTER);
        wrap.add(footer, BorderLayout.SOUTH);

        return wrap;
    }

    private void reload() {
        model.setRowCount(0);
        List<User> users = userDAO.findAll();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        for (User u : users) {
            String created = u.getCreatedAt() != null
                    ? u.getCreatedAt().toLocalDateTime().format(fmt)
                    : "—";
            String suffix = u.getId() == currentUserId ? "  (you)" : "";
            model.addRow(new Object[]{
                u.getId(),
                u.getUsername(),
                u.getFullName() + suffix,
                created
            });
        }
        countLabel.setText(users.size() + (users.size() == 1 ? " user" : " users"));
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return userDAO.findById(id);
    }

    private void openDialog(User existing) {
        UserDialog dialog = new UserDialog(
                SwingUtilities.getWindowAncestor(this), existing, userDAO);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
        }
    }

    private void doDelete() {
        User u = getSelectedUser();
        if (u == null) {
            showInfo("Select a user to delete.");
            return;
        }

        if (u.getId() == currentUserId) {
            showInfo("You cannot delete the account you are signed in as.");
            return;
        }

        if (userDAO.countAll() <= 1) {
            showInfo("Cannot delete the only remaining user.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete user \"" + u.getUsername() + "\"?\n\n"
                + "Their stock movements will be preserved, but no longer "
                + "show who made them.",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (userDAO.delete(u.getId())) {
                reload();
            } else {
                showInfo("Failed to delete user.");
            }
        }
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }
}