package com.mycompany.inventorymanagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.mycompany.inventorymanagement.UITheme.*;

/**
 * Add / edit product dialog. Extracted from InventoryPanel so each dialog lives
 * in its own file.
 */
public class ProductDialog extends JDialog {

    private final Product existing;
    private final ProductDAO productDAO;
    private final Integer currentUserId;

    private boolean saved = false;

    private JTextField nameField;
    private JComboBox<Category> categoryBox;
    private JTextField quantityField;
    private JTextField priceField;
    private JTextField thresholdField;

    public ProductDialog(Window owner, Product existing,
            ProductDAO productDAO, Integer currentUserId) {
        super(owner, existing == null ? "Add Product" : "Edit Product",
                ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        this.productDAO = productDAO;
        this.currentUserId = currentUserId;
        initUI();
    }

    public boolean isSaved() {
        return saved;
    }

    private void initUI() {
        setSize(460, 600);
        setMinimumSize(new Dimension(420, 480));
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

        // Row 0/1: Name
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        form.add(fieldLabel("Name"), gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(4, 0, 0, 0);
        nameField = new JTextField();
        styleDialogField(nameField, "Product name");
        form.add(nameField, gbc);

        // Row 2/3: Category
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

        // Row 4/5: Quantity
        gbc.gridy = 4;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Quantity"), gbc);
        gbc.gridy = 5;
        gbc.insets = new Insets(4, 0, 0, 0);
        quantityField = new JTextField();
        styleDialogField(quantityField, "0");
        form.add(quantityField, gbc);

        // Row 6/7: Price
        gbc.gridy = 6;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Price (PHP)"), gbc);
        gbc.gridy = 7;
        gbc.insets = new Insets(4, 0, 0, 0);
        priceField = new JTextField();
        styleDialogField(priceField, "0.00");
        form.add(priceField, gbc);

        // Row 8/9: Low-stock threshold
        gbc.gridy = 8;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Low-stock threshold"), gbc);
        gbc.gridy = 9;
        gbc.insets = new Insets(4, 0, 0, 0);
        thresholdField = new JTextField();
        styleDialogField(thresholdField,
                String.valueOf(Product.DEFAULT_LOW_STOCK_THRESHOLD));
        form.add(thresholdField, gbc);

        // Row 10: filler so fields stay top-aligned when stretched
        gbc.gridy = 10;
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
            prefill(existing);
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

    private void prefill(Product p) {
        nameField.setText(p.getName());
        quantityField.setText(String.valueOf(p.getQuantity()));
        priceField.setText(String.format("%.2f", p.getPrice()));
        thresholdField.setText(String.valueOf(p.getLowStockThreshold()));
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
        String thresholdText = thresholdField.getText().trim();

        if (name.isEmpty()) {
            warn("Please enter a product name.");
            return;
        }
        if (cat == null) {
            warn("Please choose a category.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyText);
            if (qty < 0) {
                warn("Quantity cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            warn("Quantity must be a whole number.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) {
                warn("Price cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            warn("Price must be a number.");
            return;
        }

        int threshold;
        try {
            threshold = Integer.parseInt(thresholdText);
            if (threshold < 0) {
                warn("Threshold cannot be negative.");
                return;
            }
        } catch (NumberFormatException e) {
            warn("Threshold must be a whole number.");
            return;
        }

        Product p = existing == null ? new Product() : existing;
        p.setName(name);
        p.setCategoryId(cat.getId());
        p.setQuantity(qty);
        p.setPrice(price);
        p.setLowStockThreshold(threshold);

        boolean ok = existing == null
                ? productDAO.insert(p, currentUserId)
                : productDAO.update(p, "Manual edit", currentUserId);

        if (!ok) {
            warn("Failed to save product.");
            return;
        }

        saved = true;
        dispose();
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation",
                JOptionPane.WARNING_MESSAGE);
    }
}
