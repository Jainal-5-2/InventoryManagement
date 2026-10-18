package com.mycompany.inventorymanagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static com.mycompany.inventorymanagement.UITheme.*;

public class InventoryPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<Category> categoryFilter;
    private JLabel countLabel;

    private Runnable onDataChanged;

    public InventoryPanel() {
        initUI();
        reload();
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) onDataChanged.run();
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    // ---------------- Toolbar ----------------

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new BorderLayout(12, 0));
        bar.setOpaque(false);

        JPanel filters = new JPanel(new BorderLayout(10, 0));
        filters.setOpaque(false);

        searchField = new JTextField();
        searchField.setFont(FONT_BASE);
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        searchField.setPreferredSize(new Dimension(260, 40));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { applyFilters(); }
        });

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(FONT_BASE);
        categoryFilter.putClientProperty("JComponent.roundRect", true);
        categoryFilter.setPreferredSize(new Dimension(180, 40));
        categoryFilter.addItem(new Category(-1, "All categories"));
        for (Category c : productDAO.findAllCategories()) {
            categoryFilter.addItem(c);
        }
        categoryFilter.addActionListener(e -> applyFilters());

        filters.add(searchField, BorderLayout.WEST);
        filters.add(categoryFilter, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        JButton addBtn = new AccentButton("+ Add Product");
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> openDialog(null));

        JButton editBtn = new SecondaryButton("Edit");
        editBtn.setPreferredSize(new Dimension(90, 40));
        editBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected == null) { showInfo("Select a product to edit."); return; }
            openDialog(selected);
        });

        JButton deleteBtn = new DangerButton("Delete");
        deleteBtn.setPreferredSize(new Dimension(100, 40));
        deleteBtn.addActionListener(e -> doDelete());

        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);

        bar.add(filters, BorderLayout.WEST);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    // ---------------- Table ----------------

    private JPanel buildTableArea() {
        JPanel wrap = new RoundedPanel(14, SURFACE);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] cols = { "ID", "Name", "Category", "Quantity", "Price", "Total" };
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

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(130);

        table.getColumnModel().getColumn(3).setCellRenderer(new LowStockRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new PriceRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new TotalRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    Product p = getSelectedProduct();
                    if (p != null) openDialog(p);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(12, 16, 6, 16));

        countLabel = new JLabel("0 products");
        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(TEXT_MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        wrap.add(scroll, BorderLayout.CENTER);
        wrap.add(footer, BorderLayout.SOUTH);

        return wrap;
    }

    // ---------------- Data operations ----------------

    private void reload() {
        applyFilters();
    }

    private void applyFilters() {
        String keyword = searchField.getText().trim();
        Category selected = (Category) categoryFilter.getSelectedItem();
        int categoryId = (selected == null) ? -1 : selected.getId();

        List<Product> list;
        if (categoryId > 0 && !keyword.isEmpty()) {
            list = productDAO.searchByCategoryAndKeyword(categoryId, keyword);
        } else if (categoryId > 0) {
            list = productDAO.findByCategory(categoryId);
        } else if (!keyword.isEmpty()) {
            list = productDAO.search(keyword);
        } else {
            list = productDAO.findAll();
        }

        populate(list);
    }

    private void populate(List<Product> list) {
        model.setRowCount(0);
        for (Product p : list) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategoryName() == null ? "—" : p.getCategoryName(),
                    p.getQuantity(),
                    Currency.format(p.getPrice()),
                    Currency.format(p.getTotalValue())
            });
        }
        countLabel.setText(list.size() + (list.size() == 1 ? " product" : " products"));
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) return null;
        int id = (int) model.getValueAt(row, 0);
        return productDAO.findById(id);
    }

    private void doDelete() {
        Product p = getSelectedProduct();
        if (p == null) { showInfo("Select a product to delete."); return; }

        int choice = JOptionPane.showConfirmDialog(this,
                "Delete \"" + p.getName() + "\"?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            if (productDAO.delete(p.getId())) {
                reload();
                notifyDataChanged();
            } else {
                showInfo("Failed to delete product.");
            }
        }
    }

    private void openDialog(Product existing) {
        ProductDialog dialog = new ProductDialog(
                SwingUtilities.getWindowAncestor(this), existing);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            reload();
            notifyDataChanged();
        }
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // ---------------- Renderers ----------------

    private static class LowStockRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                int qty = Integer.parseInt(value.toString());
                if (qty <= Product.LOW_STOCK_THRESHOLD) {
                    c.setForeground(new Color(220, 60, 60));
                    setFont(getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(TEXT_DARK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
            }
            return c;
        }
    }

    private static class RightAlignRenderer extends DefaultTableCellRenderer {
        RightAlignRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }
    }

    private static class PriceRenderer extends DefaultTableCellRenderer {
        PriceRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (!isSelected) c.setForeground(TEXT_DARK);
            return c;
        }
    }

    private static class TotalRenderer extends DefaultTableCellRenderer {
        TotalRenderer() { setHorizontalAlignment(SwingConstants.RIGHT); }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(ACCENT);
                setFont(getFont().deriveFont(Font.BOLD));
            }
            return c;
        }
    }

    // ---------------- Buttons ----------------

    private static class SecondaryButton extends JButton {
        private boolean hovered = false;

        SecondaryButton(String text) {
            super(text);
            setFont(FONT_BUTTON);
            setForeground(TEXT_DARK);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? new Color(235, 236, 242) : new Color(245, 246, 250));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(new Color(210, 212, 222));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class DangerButton extends JButton {
        private boolean hovered = false;

        DangerButton(String text) {
            super(text);
            setFont(FONT_BUTTON);
            setForeground(new Color(220, 60, 60));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(hovered ? new Color(252, 235, 235) : new Color(252, 244, 244));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(new Color(240, 200, 200));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------- Add/Edit dialog ----------------

    private class ProductDialog extends JDialog {
        private final Product existing;
        private boolean saved = false;

        private JTextField nameField;
        private JComboBox<Category> categoryBox;
        private JTextField quantityField;
        private JTextField priceField;

        ProductDialog(Window owner, Product existing) {
            super(owner, existing == null ? "Add Product" : "Edit Product",
                    ModalityType.APPLICATION_MODAL);
            this.existing = existing;
            initUI();
        }

        boolean isSaved() { return saved; }

        private void initUI() {
            setSize(460, 520);
            setMinimumSize(new Dimension(420, 380));
            setLocationRelativeTo(getOwner());
            setResizable(true);

            JPanel root = new JPanel(new BorderLayout());
            root.setBackground(SURFACE);

            JPanel header = new JPanel(new BorderLayout());
            header.setBackground(SURFACE);
            header.setBorder(new EmptyBorder(24, 30, 16, 30));

            JLabel heading = new JLabel(existing == null ? "New product" : "Edit product");
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

            gbc.gridy = 0;
            form.add(fieldLabel("Name"), gbc);
            gbc.gridy = 1;
            gbc.insets = new Insets(4, 0, 0, 0);
            nameField = new JTextField();
            styleDialogField(nameField, "Product name");
            form.add(nameField, gbc);

            gbc.gridy = 2;
            gbc.insets = new Insets(14, 0, 0, 0);
            form.add(fieldLabel("Category"), gbc);
            gbc.gridy = 3;
            gbc.insets = new Insets(4, 0, 0, 0);
            categoryBox = new JComboBox<>();
            categoryBox.setFont(FONT_BASE);
            categoryBox.putClientProperty("JComponent.roundRect", true);
            categoryBox.setPreferredSize(new Dimension(0, 42));
            for (Category c : productDAO.findAllCategories()) {
                categoryBox.addItem(c);
            }
            form.add(categoryBox, gbc);

            gbc.gridy = 4;
            gbc.insets = new Insets(14, 0, 0, 0);
            form.add(fieldLabel("Quantity"), gbc);
            gbc.gridy = 5;
            gbc.insets = new Insets(4, 0, 0, 0);
            quantityField = new JTextField();
            styleDialogField(quantityField, "0");
            form.add(quantityField, gbc);

            gbc.gridy = 6;
            gbc.insets = new Insets(14, 0, 0, 0);
            form.add(fieldLabel("Price (PHP)"), gbc);
            gbc.gridy = 7;
            gbc.insets = new Insets(4, 0, 0, 0);
            priceField = new JTextField();
            styleDialogField(priceField, "0.00");
            form.add(priceField, gbc);

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

            JButton cancel = new SecondaryButton("Cancel");
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

            if (existing != null) prefill(existing);
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

        private void prefill(Product p) {
            nameField.setText(p.getName());
            quantityField.setText(String.valueOf(p.getQuantity()));
            priceField.setText(String.format("%.2f", p.getPrice()));
            for (int i = 0; i < categoryBox.getItemCount(); i++) {
                if (categoryBox.getItemAt(i).getId() == p.getCategoryId()) {
                    categoryBox.setSelectedIndex(i);
                    break;
                }
            }
        }

        private void doSave() {
            String name = nameField.getText().trim();
            Category cat = (Category) categoryBox.getSelectedItem();
            String qtyText = quantityField.getText().trim();
            String priceText = priceField.getText().trim();

            if (name.isEmpty()) { warn("Please enter a product name."); return; }
            if (cat == null) { warn("Please choose a category."); return; }

            int qty;
            try {
                qty = Integer.parseInt(qtyText);
                if (qty < 0) { warn("Quantity cannot be negative."); return; }
            } catch (NumberFormatException e) {
                warn("Quantity must be a whole number."); return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price < 0) { warn("Price cannot be negative."); return; }
            } catch (NumberFormatException e) {
                warn("Price must be a number."); return;
            }

            Product p = existing == null ? new Product() : existing;
            p.setName(name);
            p.setCategoryId(cat.getId());
            p.setQuantity(qty);
            p.setPrice(price);

            boolean ok = existing == null
                    ? productDAO.insert(p)
                    : productDAO.update(p);

            if (!ok) { warn("Failed to save product."); return; }

            saved = true;
            dispose();
        }

        private void warn(String msg) {
            JOptionPane.showMessageDialog(this, msg, "Validation",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
}