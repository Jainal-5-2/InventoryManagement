package com.mycompany.inventorymanagement;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.List;

import static com.mycompany.inventorymanagement.UITheme.*;

public class ReportsPanel extends JPanel {

    private final ProductDAO productDAO = new ProductDAO();

    private JLabel totalProductsValue;
    private JLabel lowStockValue;
    private JLabel totalValueValue;
    private JLabel categoriesValue;

    private DefaultTableModel lowStockModel;
    private DefaultTableModel breakdownModel;
    private JLabel breakdownTotalLabel;

    public ReportsPanel() {
        initUI();
        refresh();
    }

    public void refresh() {
        // ---- Summary strip ----
        int products = productDAO.countAll();
        int lowStock = productDAO.countLowStock();
        double totalValue = productDAO.totalInventoryValue();
        int categories = productDAO.findAllCategories().size();

        totalProductsValue.setText(String.valueOf(products));
        lowStockValue.setText(String.valueOf(lowStock));
        totalValueValue.setText(Currency.format(totalValue));
        categoriesValue.setText(String.valueOf(categories));

        // ---- Low stock table ----
        lowStockModel.setRowCount(0);
        for (Product p : productDAO.findLowStock()) {
            lowStockModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategoryName() == null ? "—" : p.getCategoryName(),
                    p.getQuantity(),
                    Currency.format(p.getPrice())
            });
        }

        // ---- Full breakdown table ----
        breakdownModel.setRowCount(0);
        List<Product> all = productDAO.findAll();
        for (Product p : all) {
            breakdownModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getCategoryName() == null ? "—" : p.getCategoryName(),
                    p.getQuantity(),
                    Currency.format(p.getPrice()),
                    Currency.format(p.getTotalValue())
            });
        }
        breakdownTotalLabel.setText("Total: " + Currency.format(totalValue)
                + "  ·  " + all.size() + (all.size() == 1 ? " product" : " products"));
    }

    private void initUI() {
        setOpaque(false);
        setLayout(new BorderLayout());

        // ---- Stacked content ----
        JPanel stack = new JPanel();
        stack.setOpaque(false);
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));
        stack.setBorder(new EmptyBorder(0, 0, 8, 0));

        JPanel summary = buildSummaryStrip();
        summary.setPreferredSize(new Dimension(0, 90));
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        summary.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel middle = new JPanel(new GridLayout(1, 2, 18, 0));
        middle.setOpaque(false);
        middle.add(buildPieCard());
        middle.add(buildLowStockCard());
        middle.setPreferredSize(new Dimension(0, 300));
        middle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        middle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel breakdown = buildBreakdownCard();
        breakdown.setPreferredSize(new Dimension(0, 320));
        breakdown.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        breakdown.setAlignmentX(Component.LEFT_ALIGNMENT);

        stack.add(summary);
        stack.add(Box.createVerticalStrut(16));
        stack.add(middle);
        stack.add(Box.createVerticalStrut(16));
        stack.add(breakdown);

        // ---- Scroll wrapper ----
        JScrollPane scroller = new JScrollPane(stack);
        scroller.setBorder(BorderFactory.createEmptyBorder());
        scroller.getViewport().setBackground(SURFACE);
        scroller.setOpaque(false);
        scroller.getViewport().setOpaque(true);
        scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroller.getVerticalScrollBar().setUnitIncrement(18);

        add(scroller, BorderLayout.CENTER);
    }

    // ---------------- Summary strip ----------------

    private JPanel buildSummaryStrip() {
        JPanel strip = new JPanel(new GridLayout(1, 4, 14, 0));
        strip.setOpaque(false);

        totalProductsValue = new JLabel("0");
        lowStockValue      = new JLabel("0");
        totalValueValue    = new JLabel(Currency.format(0));
        categoriesValue    = new JLabel("0");

        strip.add(new MiniStat("Total Products", totalProductsValue, ACCENT));
        strip.add(new MiniStat("Low Stock",      lowStockValue,      new Color(220, 60, 60)));
        strip.add(new MiniStat("Total Value",    totalValueValue,    new Color(34, 160, 90)));
        strip.add(new MiniStat("Categories",     categoriesValue,    new Color(245, 158, 11)));

        return strip;
    }

    private static class MiniStat extends JPanel {
        MiniStat(String title, JLabel valueLabel, Color accent) {
            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(16, 18, 16, 18));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            titleLabel.setForeground(TEXT_MUTED);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
            valueLabel.setForeground(accent);
            valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            content.add(titleLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(valueLabel);

            add(content, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.setColor(new Color(230, 232, 238));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ---------------- Pie card ----------------

    private JPanel buildPieCard() {
        JPanel card = new RoundedPanel(14, SURFACE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel("Inventory Value by Product");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        title.setBorder(new EmptyBorder(0, 4, 10, 0));
        card.add(title, BorderLayout.NORTH);

        card.add(buildPieChart(), BorderLayout.CENTER);
        return card;
    }

    private ChartPanel buildPieChart() {
        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();

        for (Product p : productDAO.findAll()) {
            double value = p.getTotalValue();
            if (value <= 0) continue;
            dataset.setValue(p.getName() + " (" + Currency.format(value) + ")", value);
        }

        JFreeChart chart = ChartFactory.createPieChart(
                null, dataset, true, true, false);

        chart.setBackgroundPaint(SURFACE);
        chart.setPadding(new RectangleInsets(4, 4, 4, 4));
        chart.getLegend().setItemFont(new Font("Segoe UI", Font.PLAIN, 11));
        chart.getLegend().setBackgroundPaint(SURFACE);
        chart.getLegend().setBorder(0, 0, 0, 0);

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(SURFACE);
        plot.setOutlineVisible(false);
        plot.setSectionOutlinesVisible(false);
        plot.setShadowPaint(null);
        plot.setLabelFont(new Font("Segoe UI", Font.PLAIN, 11));
        plot.setLabelBackgroundPaint(null);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(TEXT_DARK);

        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{2}", new DecimalFormat("0"), new DecimalFormat("0.0%")));

        Color[] palette = {
                ACCENT,
                new Color(139, 92, 246),
                new Color(34, 160, 90),
                new Color(245, 158, 11),
                new Color(220, 60, 60),
                new Color(59, 130, 246),
                new Color(16, 185, 129),
                new Color(236, 72, 153)
        };
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable<?>) key, palette[i % palette.length]);
            i++;
        }

        ChartPanel panel = new ChartPanel(chart);
        panel.setOpaque(false);
        panel.setBackground(SURFACE);
        panel.setBorder(BorderFactory.createEmptyBorder());
        panel.setPopupMenu(null);
        panel.setMouseWheelEnabled(false);
        return panel;
    }

    // ---------------- Low stock card ----------------

    private JPanel buildLowStockCard() {
        JPanel card = new RoundedPanel(14, SURFACE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 10, 0));

        JLabel title = new JLabel("Low Stock Items");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        JLabel hint = new JLabel("≤ " + Product.LOW_STOCK_THRESHOLD + " units");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hint.setForeground(TEXT_MUTED);
        header.add(hint, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        String[] cols = { "ID", "Name", "Category", "Qty", "Price" };
        lowStockModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(lowStockModel);
        styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(50);
        table.getColumnModel().getColumn(4).setPreferredWidth(90);

        DefaultTableCellRenderer qtyR = new DefaultTableCellRenderer();
        qtyR.setHorizontalAlignment(SwingConstants.CENTER);
        qtyR.setForeground(new Color(220, 60, 60));
        qtyR.setFont(FONT_BASE.deriveFont(Font.BOLD));
        table.getColumnModel().getColumn(3).setCellRenderer(qtyR);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightR);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ---------------- Breakdown table ----------------

    private JPanel buildBreakdownCard() {
        JPanel card = new RoundedPanel(14, SURFACE);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(18, 18, 18, 18));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 4, 10, 0));

        JLabel title = new JLabel("Full Breakdown");
        title.setFont(new Font("Segoe UI", Font.BOLD, 15));
        title.setForeground(TEXT_DARK);
        header.add(title, BorderLayout.WEST);

        breakdownTotalLabel = new JLabel("");
        breakdownTotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        breakdownTotalLabel.setForeground(ACCENT);
        header.add(breakdownTotalLabel, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);

        String[] cols = { "ID", "Name", "Category", "Quantity", "Unit Price", "Total Value" };
        breakdownModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(breakdownModel);
        styleTable(table);

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(220);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(120);
        table.getColumnModel().getColumn(5).setPreferredWidth(140);

        DefaultTableCellRenderer centerR = new DefaultTableCellRenderer();
        centerR.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(3).setCellRenderer(centerR);

        DefaultTableCellRenderer rightR = new DefaultTableCellRenderer();
        rightR.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(0).setCellRenderer(rightR);
        table.getColumnModel().getColumn(4).setCellRenderer(rightR);

        DefaultTableCellRenderer totalR = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (!sel) {
                    c.setForeground(ACCENT);
                    setFont(getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        };
        table.getColumnModel().getColumn(5).setCellRenderer(totalR);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(SURFACE);

        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // ---------------- Shared table styling ----------------

    private void styleTable(JTable table) {
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
    }
}