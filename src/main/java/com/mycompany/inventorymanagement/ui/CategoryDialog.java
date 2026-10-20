package com.mycompany.inventorymanagement.ui;

import com.mycompany.inventorymanagement.Category;
import com.mycompany.inventorymanagement.InventoryPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

import static com.mycompany.inventorymanagement.UITheme.*;
import com.mycompany.inventorymanagement.dao.CategoryDAO;

/**
 * Manage categories: add, rename, delete. Deleting prompts with the number of
 * products currently assigned to the category, since deletion orphans them
 * (schema: products.category_id ON DELETE SET NULL).
 */
public class CategoryDialog extends JDialog {

    private final CategoryDAO categoryDAO = new CategoryDAO();
    private DefaultTableModel model;
    private JTable table;

    private boolean changed = false;

    public CategoryDialog(Window owner) {
        super(owner, "Manage Categories", ModalityType.APPLICATION_MODAL);
        initUI();
        reload();
    }

    /** True if any add/rename/delete happened — caller refreshes. */
    public boolean isChanged() { return changed; }

    private void initUI() {
        setSize(560, 500);
        setMinimumSize(new Dimension(480, 400));
        setLocationRelativeTo(getOwner());
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(20, 26, 10, 26));

        JLabel heading = new JLabel("Categories");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        header.add(heading, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // ---- Table ----
        String[] cols = { "ID", "Name" };
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setFont(FONT_BASE);
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(237, 237, 252));
        table.setSelectionForeground(TEXT_DARK);
        table.setFillsViewportHeight(true);

        JTableHeader headerRow = table.getTableHeader();
        headerRow.setFont(new Font("Segoe UI", Font.BOLD, 12));
        headerRow.setForeground(TEXT_MUTED);
        headerRow.setBackground(new Color(250, 251, 254));
        headerRow.setPreferredSize(new Dimension(0, 34));
        headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                new Color(230, 232, 238)));
        headerRow.setReorderingAllowed(false);

        table.getColumnModel().getColumn(0).setPreferredWidth(60);
        table.getColumnModel().getColumn(1).setPreferredWidth(400);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(6, 26, 10, 26));
        tableWrapper.add(scroll, BorderLayout.CENTER);

        root.add(tableWrapper, BorderLayout.CENTER);

        // ---- Actions ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(8, 26, 20, 26));

        JButton addBtn = new AccentButton("Add");
        addBtn.setPreferredSize(new Dimension(90, 40));
        addBtn.addActionListener(e -> doAdd());

        JButton renameBtn = new InventoryPanel.SecondaryButton("Rename");
        renameBtn.setPreferredSize(new Dimension(100, 40));
        renameBtn.addActionListener(e -> doRename());

        JButton deleteBtn = new InventoryPanel.DangerButton("Delete");
        deleteBtn.setPreferredSize(new Dimension(100, 40));
        deleteBtn.addActionListener(e -> doDelete());

        JButton closeBtn = new InventoryPanel.SecondaryButton("Close");
        closeBtn.setPreferredSize(new Dimension(90, 40));
        closeBtn.addActionListener(e -> dispose());

        actions.add(addBtn);
        actions.add(renameBtn);
        actions.add(deleteBtn);
        actions.add(closeBtn);

        root.add(actions, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void reload() {
        model.setRowCount(0);
        for (Category c : categoryDAO.findAll()) {
            model.addRow(new Object[]{ c.getId(), c.getName() });
        }
    }

    private Category getSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        String name = (String) model.getValueAt(row, 1);
        return new Category(id, name);
    }

    private void doAdd() {
        String name = JOptionPane.showInputDialog(this,
                "New category name:", "Add Category",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null) return;
        name = name.trim();
        if (name.isEmpty()) {
            warn("Category name cannot be empty.");
            return;
        }
        int newId = categoryDAO.insert(name);
        if (newId < 0) {
            warn("A category with that name already exists.");
            return;
        }
        changed = true;
        reload();
    }

    private void doRename() {
        Category selected = getSelected();
        if (selected == null) {
            warn("Select a category to rename.");
            return;
        }

        String newName = (String) JOptionPane.showInputDialog(this,
                "Rename \"" + selected.getName() + "\" to:",
                "Rename Category",
                JOptionPane.PLAIN_MESSAGE,
                null, null, selected.getName());
        if (newName == null) return;
        newName = newName.trim();
        if (newName.isEmpty()) {
            warn("Category name cannot be empty.");
            return;
        }
        if (newName.equals(selected.getName())) return;

        CategoryDAO.Result result = categoryDAO.rename(selected.getId(), newName);
        switch (result) {
            case SUCCESS -> { changed = true; reload(); }
            case DUPLICATE -> warn("A category with that name already exists.");
            default -> warn("Failed to rename category.");
        }
    }

    private void doDelete() {
        Category selected = getSelected();
        if (selected == null) {
            warn("Select a category to delete.");
            return;
        }

        int productCount = categoryDAO.countProductsIn(selected.getId());

        String message;
        if (productCount > 0) {
            message = "Delete \"" + selected.getName() + "\"?\n\n"
                    + productCount + (productCount == 1
                            ? " product is currently in this category and will become Uncategorized."
                            : " products are currently in this category and will become Uncategorized.");
        } else {
            message = "Delete \"" + selected.getName() + "\"?";
        }

        int choice = JOptionPane.showConfirmDialog(this, message,
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) return;

        CategoryDAO.Result result = categoryDAO.delete(selected.getId());
        if (result == CategoryDAO.Result.SUCCESS) {
            changed = true;
            reload();
        } else {
            warn("Failed to delete category.");
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation",
                JOptionPane.WARNING_MESSAGE);
    }
}