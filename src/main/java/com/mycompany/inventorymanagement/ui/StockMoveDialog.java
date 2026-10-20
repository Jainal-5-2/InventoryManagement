package com.mycompany.inventorymanagement.ui;

import com.mycompany.inventorymanagement.InventoryPanel;
import com.mycompany.inventorymanagement.Product;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import static com.mycompany.inventorymanagement.UITheme.*;

/**
 * Stock in / out dialog. Enter a quantity and (optionally) a reason.
 * The caller applies the delta — this dialog only gathers input.
 */
public class StockMoveDialog extends JDialog {

    private boolean confirmed = false;
    private int quantity;
    private String reason;

    public StockMoveDialog(Window owner, Product product, boolean in) {
        super(owner, in ? "Stock In" : "Stock Out", ModalityType.APPLICATION_MODAL);
        initUI(product, in);
    }

    public boolean isConfirmed() { return confirmed; }
    public int getQuantity()     { return quantity; }
    public String getReason()    { return reason; }

    private void initUI(Product product, boolean in) {
        setSize(420, 420);
        setMinimumSize(new Dimension(380, 380));
        setLocationRelativeTo(getOwner());
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(24, 30, 8, 30));

        JLabel heading = new JLabel(in ? "Receive stock" : "Remove stock");
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        header.add(heading, BorderLayout.WEST);

        root.add(header, BorderLayout.NORTH);

        // ---- Form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(new EmptyBorder(0, 30, 16, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Product summary
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 0, 0);
        JLabel prodLabel = new JLabel(product.getName()
                + "  (currently " + product.getQuantity() + ")");
        prodLabel.setFont(FONT_LABEL);
        prodLabel.setForeground(TEXT_MUTED);
        form.add(prodLabel, gbc);

        // Quantity label
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        form.add(fieldLabel("Quantity"), gbc);

        // Quantity field
        gbc.gridy = 2;
        gbc.insets = new Insets(4, 0, 0, 0);
        JTextField qtyField = new JTextField();
        styleField(qtyField, "0");
        form.add(qtyField, gbc);

        // Reason label
        gbc.gridy = 3;
        gbc.insets = new Insets(14, 0, 0, 0);
        form.add(fieldLabel("Reason (optional)"), gbc);

        // Reason field — a small text area so multi-line reasons fit
        gbc.gridy = 4;
        gbc.insets = new Insets(4, 0, 0, 0);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea reasonArea = new JTextArea(3, 0);
        reasonArea.setFont(FONT_BASE);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);
        reasonArea.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        reasonArea.setBackground(SURFACE);
        reasonArea.setForeground(TEXT_DARK);
        JScrollPane reasonScroll = new JScrollPane(reasonArea);
        reasonScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        reasonScroll.setPreferredSize(new Dimension(0, 80));
        form.add(reasonScroll, gbc);

        JScrollPane formScroller = new JScrollPane(form);
        formScroller.setBorder(BorderFactory.createEmptyBorder());
        formScroller.getViewport().setBackground(SURFACE);
        formScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        formScroller.getVerticalScrollBar().setUnitIncrement(16);

        root.add(formScroller, BorderLayout.CENTER);

        // ---- Actions ----
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(16, 30, 22, 30));

        JButton cancel = new InventoryPanel.SecondaryButton("Cancel");
        cancel.setPreferredSize(new Dimension(100, 40));
        cancel.addActionListener(e -> dispose());

        JButton ok = new AccentButton(in ? "Add stock" : "Remove stock");
        ok.setPreferredSize(new Dimension(150, 40));
        ok.addActionListener(e -> {
            int q;
            try {
                q = Integer.parseInt(qtyField.getText().trim());
                if (q <= 0) {
                    warn("Quantity must be greater than zero.");
                    return;
                }
            } catch (NumberFormatException ex) {
                warn("Quantity must be a whole number.");
                return;
            }
            quantity = q;
            reason = reasonArea.getText().trim();
            confirmed = true;
            dispose();
        });

        actions.add(cancel);
        actions.add(ok);
        root.add(actions, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(ok);
        setContentPane(root);
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private void styleField(JTextField f, String placeholder) {
        f.setFont(FONT_BASE);
        f.putClientProperty("JTextField.placeholderText", placeholder);
        f.putClientProperty("JComponent.roundRect", true);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        f.setPreferredSize(new Dimension(0, 42));
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Validation",
                JOptionPane.WARNING_MESSAGE);
    }
}