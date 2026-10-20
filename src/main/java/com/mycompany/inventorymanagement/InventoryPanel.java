package com.mycompany.inventorymanagement;

import com.mycompany.inventorymanagement.ui.CategoryDialog;
import com.mycompany.inventorymanagement.ui.StockMoveDialog;

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
    private final Integer currentUserId;   // nullable — no user context in some tests

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private JComboBox<Category> categoryFilter;
    private JLabel countLabel;

    private Runnable onDataChanged;

    public InventoryPanel(Integer currentUserId) {
        this.currentUserId = currentUserId;
        initUI();
        reload();
    }

    public void setOnDataChanged(Runnable callback) {
        this.onDataChanged = callback;
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildTableArea(), BorderLayout.CENTER);
    }

    // ---------------- Toolbar ----------------
    private JPanel buildToolbar() {
        JPanel bar = new JPanel();
        bar.setOpaque(false);
        bar.setLayout(new BoxLayout(bar, BoxLayout.Y_AXIS));

        // ---- Row 1: filters ----
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.setAlignmentX(Component.LEFT_ALIGNMENT);

        searchField = new JTextField();
        searchField.setFont(FONT_BASE);
        searchField.putClientProperty("JTextField.placeholderText", "Search products...");
        searchField.putClientProperty("JComponent.roundRect", true);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));
        searchField.setPreferredSize(new Dimension(260, 40));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                applyFilters();
            }
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

        filters.add(searchField);
        filters.add(categoryFilter);

        // ---- Row 2: actions ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton stockInBtn = new AccentButton("Stock In");
        stockInBtn.setPreferredSize(new Dimension(110, 40));
        stockInBtn.addActionListener(e -> doStockMove(true));

        JButton stockOutBtn = new AccentButton("Stock Out");
        stockOutBtn.setPreferredSize(new Dimension(115, 40));
        stockOutBtn.addActionListener(e -> doStockMove(false));

        JButton addBtn = new AccentButton("+ Add Product");
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> openDialog(null));

        JButton categoriesBtn = new SecondaryButton("Categories");
        categoriesBtn.setPreferredSize(new Dimension(110, 40));
        categoriesBtn.addActionListener(e -> doManageCategories());

        JButton editBtn = new SecondaryButton("Edit");
        editBtn.setPreferredSize(new Dimension(90, 40));
        editBtn.addActionListener(e -> {
            Product selected = getSelectedProduct();
            if (selected == null) {
                showInfo("Select a product to edit.");
                return;
            }
            openDialog(selected);
        });

        JButton historyBtn = new SecondaryButton("History");
        historyBtn.setPreferredSize(new Dimension(100, 40));
        historyBtn.addActionListener(e -> doShowHistory());

        JButton deleteBtn = new DangerButton("Delete");
        deleteBtn.setPreferredSize(new Dimension(100, 40));
        deleteBtn.addActionListener(e -> doDelete());

        actions.add(stockInBtn);
        actions.add(stockOutBtn);
        actions.add(addBtn);
        actions.add(categoriesBtn);
        actions.add(editBtn);
        actions.add(historyBtn);
        actions.add(deleteBtn);

        bar.add(filters);
        bar.add(Box.createVerticalStrut(10));
        bar.add(actions);
        return bar;
    }

    // ---------------- Table ----------------
    private JPanel buildTableArea() {
        JPanel wrap = new RoundedPanel(14, SURFACE);
        wrap.setLayout(new BorderLayout());
        wrap.setBorder(new EmptyBorder(4, 4, 4, 4));

        String[] cols = {"ID", "Name", "Category", "Quantity", "Price", "Total", "Threshold"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
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

        // Hide the helper column — renderer still reads it from the model.
        table.getColumnModel().getColumn(6).setMinWidth(0);
        table.getColumnModel().getColumn(6).setMaxWidth(0);
        table.getColumnModel().getColumn(6).setWidth(0);

        table.getColumnModel().getColumn(3).setCellRenderer(new LowStockRenderer());
        table.getColumnModel().getColumn(0).setCellRenderer(new RightAlignRenderer());
        table.getColumnModel().getColumn(4).setCellRenderer(new PriceRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new TotalRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    Product p = getSelectedProduct();
                    if (p != null) {
                        openDialog(p);
                    }
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
                Currency.format(p.getTotalValue()),
                p.getLowStockThreshold()
            });
        }
        countLabel.setText(list.size() + (list.size() == 1 ? " product" : " products"));
    }

    private Product getSelectedProduct() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int id = (int) model.getValueAt(row, 0);
        return productDAO.findById(id);
    }

    private void doDelete() {
        Product p = getSelectedProduct();
        if (p == null) {
            showInfo("Select a product to delete.");
            return;
        }

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

    // ---------------- Stock in / out ----------------
    private void doStockMove(boolean in) {
        Product selected = getSelectedProduct();
        if (selected == null) {
            showInfo("Select a product first.");
            return;
        }

        StockMoveDialog dialog = new StockMoveDialog(
                SwingUtilities.getWindowAncestor(this), selected, in);
        dialog.setVisible(true);

        if (!dialog.isConfirmed()) {
            return;
        }

        int delta = dialog.getQuantity();
        if (!in) {
            delta = -delta;
        }

        int newQty = selected.getQuantity() + delta;
        if (newQty < 0) {
            showInfo("Stock out would result in negative quantity. "
                    + "Only " + selected.getQuantity() + " in stock.");
            return;
        }

        selected.setQuantity(newQty);
        String reason = dialog.getReason();
        if (reason == null || reason.isBlank()) {
            reason = in ? "Stock in" : "Stock out";
        } else {
            reason = reason.replaceAll("\\s+", " ").trim();
        }

        if (productDAO.update(selected, reason, currentUserId)) {
            reload();
            notifyDataChanged();
        } else {
            showInfo("Failed to save stock change.");
        }
    }

    // ---------------- History ----------------
    private void doShowHistory() {
        Product p = getSelectedProduct();
        if (p == null) {
            showInfo("Select a product first.");
            return;
        }
        StockHistoryDialog dialog = new StockHistoryDialog(
                SwingUtilities.getWindowAncestor(this), p, productDAO);
        dialog.setVisible(true);
    }

    // ---------------- Categories ----------------
    private void doManageCategories() {
        CategoryDialog dialog = new CategoryDialog(
                SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        if (dialog.isChanged()) {
            Category current = (Category) categoryFilter.getSelectedItem();
            int previousId = current == null ? -1 : current.getId();

            categoryFilter.removeAllItems();
            categoryFilter.addItem(new Category(-1, "All categories"));
            for (Category c : productDAO.findAllCategories()) {
                categoryFilter.addItem(c);
            }
            for (int i = 0; i < categoryFilter.getItemCount(); i++) {
                if (categoryFilter.getItemAt(i).getId() == previousId) {
                    categoryFilter.setSelectedIndex(i);
                    break;
                }
            }

            reload();
            notifyDataChanged();
        }
    }

    // ---------------- Add / Edit ----------------
    private void openDialog(Product existing) {
        ProductDialog dialog = new ProductDialog(
                SwingUtilities.getWindowAncestor(this), existing,
                productDAO, currentUserId);
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

        private static final int THRESHOLD_COLUMN = 6;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (!isSelected) {
                int qty = Integer.parseInt(value.toString());
                Object thr = table.getModel().getValueAt(row, THRESHOLD_COLUMN);
                int threshold = (thr instanceof Integer) ? (Integer) thr
                        : Product.DEFAULT_LOW_STOCK_THRESHOLD;
                if (qty <= threshold) {
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

        RightAlignRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }

    private static class PriceRenderer extends DefaultTableCellRenderer {

        PriceRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setForeground(TEXT_DARK);
            }
            return c;
        }
    }

    private static class TotalRenderer extends DefaultTableCellRenderer {

        TotalRenderer() {
            setHorizontalAlignment(SwingConstants.RIGHT);
        }

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

    // ---------------- Shared button styles ----------------
    // public so dialogs in com.mycompany.inventorymanagement.ui can reuse them.
    public static class SecondaryButton extends JButton {

        private boolean hovered = false;

        public SecondaryButton(String text) {
            super(text);
            setFont(FONT_BUTTON);
            setForeground(TEXT_DARK);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
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

    public static class DangerButton extends JButton {

        private boolean hovered = false;

        public DangerButton(String text) {
            super(text);
            setFont(FONT_BUTTON);
            setForeground(new Color(220, 60, 60));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
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
}
