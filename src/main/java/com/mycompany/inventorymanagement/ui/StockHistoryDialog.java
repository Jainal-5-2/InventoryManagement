package com.mycompany.inventorymanagement;

import com.mycompany.inventorymanagement.model.StockMovement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.mycompany.inventorymanagement.UITheme.*;

/**
 * Per-product stock-movement history. Read-only.
 */
public class StockHistoryDialog extends JDialog {

    public StockHistoryDialog(Window owner, Product product, ProductDAO productDAO) {
        super(owner, "Stock history — " + product.getName(),
                ModalityType.APPLICATION_MODAL);
        initUI(product, productDAO);
    }

    private void initUI(Product product, ProductDAO productDAO) {
        setSize(720, 480);
        setMinimumSize(new Dimension(560, 360));
        setLocationRelativeTo(getOwner());
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        // ---- Header ----
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SURFACE);
        header.setBorder(new EmptyBorder(20, 26, 8, 26));

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));

        JLabel heading = new JLabel(product.getName());
        heading.setFont(FONT_HEADING);
        heading.setForeground(TEXT_DARK);
        heading.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel(
                (product.getCategoryName() == null ? "Uncategorized" : product.getCategoryName())
                + "  ·  currently " + product.getQuantity() + " in stock"
                + "  ·  low-stock threshold " + product.getLowStockThreshold());
        subtitle.setFont(FONT_SMALL);
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        titleBlock.add(heading);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        header.add(titleBlock, BorderLayout.WEST);

        JButton close = new InventoryPanel.SecondaryButton("Close");
        close.setPreferredSize(new Dimension(90, 36));
        close.addActionListener(e -> dispose());
        header.add(close, BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        // ---- Table ----
        String[] cols = {"When", "Change", "Reason", "By"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable table = new JTable(model);
        table.setFont(FONT_BASE);
        table.setRowHeight(34);
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

        table.getColumnModel().getColumn(0).setPreferredWidth(160);
        table.getColumnModel().getColumn(1).setPreferredWidth(80);
        table.getColumnModel().getColumn(2).setPreferredWidth(320);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);

        // Change column — signed, coloured
        DefaultTableCellRenderer changeR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    String s = String.valueOf(v);
                    c.setForeground(s.startsWith("-")
                            ? new Color(220, 60, 60)
                            : new Color(34, 160, 90));
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(1).setCellRenderer(changeR);

        DefaultTableCellRenderer mutedR = new DefaultTableCellRenderer();
        mutedR.setForeground(TEXT_MUTED);
        table.getColumnModel().getColumn(0).setCellRenderer(mutedR);
        table.getColumnModel().getColumn(2).setCellRenderer(mutedR);
        table.getColumnModel().getColumn(3).setCellRenderer(mutedR);

        // ---- Populate ----
        List<StockMovement> movements = productDAO.findMovementsByProduct(product.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

        for (StockMovement m : movements) {
            String when = m.getMovedAt() != null
                    ? m.getMovedAt().toLocalDateTime().format(fmt)
                    : "—";
            String delta = (m.getChangeQty() > 0 ? "+" : "") + m.getChangeQty();
            String reason = m.getReason() != null && !m.getReason().isBlank()
                    ? m.getReason()
                    : "—";
            String by = m.getUserName() != null ? m.getUserName() : "—";
            model.addRow(new Object[]{when, delta, reason, by});
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        JPanel tableWrapper = new JPanel(new BorderLayout());
        tableWrapper.setOpaque(false);
        tableWrapper.setBorder(new EmptyBorder(8, 26, 12, 26));
        tableWrapper.add(scroll, BorderLayout.CENTER);

        root.add(tableWrapper, BorderLayout.CENTER);

        // ---- Footer ----
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 26, 18, 26));

        JLabel countLabel = new JLabel(
                movements.isEmpty()
                ? "No movements recorded for this product."
                : movements.size() + (movements.size() == 1 ? " movement" : " movements"));
        countLabel.setFont(FONT_SMALL);
        countLabel.setForeground(TEXT_MUTED);
        footer.add(countLabel, BorderLayout.WEST);

        // Net change = sum of deltas, useful sanity check
        int net = 0;
        for (StockMovement m : movements) {
            net += m.getChangeQty();
        }
        JLabel netLabel = new JLabel("Net change: " + (net > 0 ? "+" : "") + net);
        netLabel.setFont(FONT_SMALL);
        netLabel.setForeground(net >= 0 ? new Color(34, 160, 90) : new Color(220, 60, 60));
        footer.add(netLabel, BorderLayout.EAST);

        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }
}
