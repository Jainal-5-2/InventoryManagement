package com.mycompany.inventorymanagement;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mycompany.inventorymanagement.model.StockMovement;

import static com.mycompany.inventorymanagement.UITheme.*;

public class DashboardFrame extends JFrame {

    private final User currentUser;

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentArea = new JPanel(cardLayout);
    private final Map<String, JLabel> navLabels = new LinkedHashMap<>();
    private final Map<String, JPanel> navPanels = new LinkedHashMap<>();
    private String activeKey = "dashboard";
    private JLabel pageTitleLabel;

    private InventoryPanel inventoryPanel;
    private ReportsPanel reportsPanel;
    private UserPanel usersPanel;
    private OverviewPanel overviewPanel;

    public DashboardFrame(User user) {
        this.currentUser = user;
        initUI();
    }

    private void initUI() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(SURFACE);

        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildMainArea(), BorderLayout.CENTER);

        setContentPane(root);
        showPanel("dashboard");
    }

    // ---------------- Sidebar ----------------
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setBackground(new Color(248, 249, 252));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setLayout(new BorderLayout());
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1,
                new Color(230, 232, 238)));

        JPanel brandBox = new JPanel();
        brandBox.setOpaque(false);
        brandBox.setLayout(new BoxLayout(brandBox, BoxLayout.Y_AXIS));
        brandBox.setBorder(new EmptyBorder(24, 22, 20, 22));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);

        JLabel brandMark = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                g2.drawRect(cx - 8, cy - 8, 16, 16);
                g2.drawLine(cx - 8, cy - 2, cx + 8, cy - 2);
                g2.dispose();
            }
        };
        brandMark.setPreferredSize(new Dimension(30, 30));

        JLabel brandTitle = new JLabel("Inventory");
        brandTitle.setFont(new Font("Segoe UI", Font.BOLD, 17));
        brandTitle.setForeground(TEXT_DARK);

        brandRow.add(brandMark);
        brandRow.add(brandTitle);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        brandBox.add(brandRow);
        sidebar.add(brandBox, BorderLayout.NORTH);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(8, 14, 8, 14));

        nav.add(buildNavItem("Dashboard", "dashboard"));
        nav.add(Box.createVerticalStrut(4));
        nav.add(buildNavItem("Inventory", "inventory"));
        nav.add(Box.createVerticalStrut(4));
        nav.add(buildNavItem("Reports", "reports"));
        nav.add(Box.createVerticalStrut(4));
        nav.add(buildNavItem("Users", "users"));

        JPanel navWrapper = new JPanel(new BorderLayout());
        navWrapper.setOpaque(false);
        navWrapper.add(nav, BorderLayout.NORTH);
        sidebar.add(navWrapper, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.setBorder(new EmptyBorder(12, 14, 18, 14));

        JPanel userChip = new JPanel(new BorderLayout(10, 0));
        userChip.setOpaque(false);
        userChip.setBorder(new EmptyBorder(8, 8, 8, 8));

        JLabel avatar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(220, 222, 235));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(ACCENT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String initial = currentUser.getFullName() != null
                        && !currentUser.getFullName().isEmpty()
                        ? currentUser.getFullName().substring(0, 1).toUpperCase()
                        : "U";
                int w = fm.stringWidth(initial);
                g2.drawString(initial, (getWidth() - w) / 2,
                        (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
        };
        avatar.setPreferredSize(new Dimension(34, 34));

        JPanel userInfo = new JPanel();
        userInfo.setOpaque(false);
        userInfo.setLayout(new BoxLayout(userInfo, BoxLayout.Y_AXIS));

        JLabel nameLabel = new JLabel(currentUser.getFullName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        nameLabel.setForeground(TEXT_DARK);

        JLabel userLabel = new JLabel("@" + currentUser.getUsername());
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        userLabel.setForeground(TEXT_MUTED);

        userInfo.add(nameLabel);
        userInfo.add(userLabel);

        userChip.add(avatar, BorderLayout.WEST);
        userChip.add(userInfo, BorderLayout.CENTER);
        userChip.setAlignmentX(Component.LEFT_ALIGNMENT);

        bottom.add(userChip);
        bottom.add(Box.createVerticalStrut(6));
        bottom.add(buildLogoutButton());

        sidebar.add(bottom, BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildNavItem(String text, String key) {
        JPanel item = new JPanel(new BorderLayout());
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        item.setPreferredSize(new Dimension(0, 42));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setBorder(new EmptyBorder(0, 14, 0, 14));

        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(TEXT_DARK);
        item.add(label, BorderLayout.CENTER);

        navLabels.put(key, label);
        navPanels.put(key, item);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPanel(key);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(activeKey)) {
                    item.setOpaque(true);
                    item.setBackground(new Color(240, 241, 248));
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!key.equals(activeKey)) {
                    item.setOpaque(false);
                    item.repaint();
                }
            }
        });

        return item;
    }

    private void updateNavHighlight(String newActiveKey) {
        if (navPanels.containsKey(activeKey)) {
            JPanel prev = navPanels.get(activeKey);
            JLabel prevLabel = navLabels.get(activeKey);
            prev.setOpaque(false);
            prevLabel.setForeground(TEXT_DARK);
            prevLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            prev.repaint();
        }

        activeKey = newActiveKey;
        if (navPanels.containsKey(activeKey)) {
            JPanel curr = navPanels.get(activeKey);
            JLabel currLabel = navLabels.get(activeKey);
            curr.setOpaque(true);
            curr.setBackground(ACCENT);
            currLabel.setForeground(Color.WHITE);
            currLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            curr.repaint();
        }
    }

    private JButton buildLogoutButton() {
        JButton logout = new AccentButton("Logout");
        logout.setPreferredSize(new Dimension(0, 38));
        logout.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Log out of your account?",
                    "Confirm Logout",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        return logout;
    }

    // ---------------- Main area ----------------
    private JPanel buildMainArea() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(SURFACE);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(SURFACE);
        topBar.setBorder(new EmptyBorder(20, 28, 16, 28));

        pageTitleLabel = new JLabel("Dashboard");
        pageTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        pageTitleLabel.setForeground(TEXT_DARK);

        topBar.add(pageTitleLabel, BorderLayout.WEST);

        contentArea.setBackground(SURFACE);
        contentArea.setBorder(new EmptyBorder(0, 28, 28, 28));

        inventoryPanel = new InventoryPanel(currentUser.getId());
        reportsPanel = new ReportsPanel();
        usersPanel = new UserPanel(currentUser.getId());
        overviewPanel = new OverviewPanel();

        inventoryPanel.setOnDataChanged(() -> {
            if (reportsPanel != null) {
                reportsPanel.refresh();
            }
            if (overviewPanel != null) {
                overviewPanel.refresh();
            }
        });

        contentArea.add(overviewPanel, "dashboard");
        contentArea.add(inventoryPanel, "inventory");
        contentArea.add(reportsPanel, "reports");
        contentArea.add(usersPanel, "users");

        main.add(topBar, BorderLayout.NORTH);
        main.add(contentArea, BorderLayout.CENTER);

        return main;
    }

    private void showPanel(String key) {
        cardLayout.show(contentArea, key);
        pageTitleLabel.setText(
                key.substring(0, 1).toUpperCase() + key.substring(1));
        updateNavHighlight(key);

        if ("reports".equals(key) && reportsPanel != null) {
            reportsPanel.refresh();
        }
        if ("dashboard".equals(key) && overviewPanel != null) {
            overviewPanel.refresh();
        }
    }

    // ---------------- Overview ----------------
    private class OverviewPanel extends JPanel {

        private StatCard productsCard;
        private StatCard lowStockCard;
        private StatCard valueCard;
        private final ProductDAO productDAO = new ProductDAO();

        private DefaultTableModel activityModel;
        private JLabel emptyActivityLabel;

        // Low-stock card state
        private DefaultTableModel lowStockModel;
        private JLabel lowStockCountLabel;
        private JLabel lowStockHintLabel;
        private JLabel emptyLowStockLabel;

        OverviewPanel() {
            setOpaque(false);
            setLayout(new BorderLayout());

            JPanel top = new JPanel();
            top.setOpaque(false);
            top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

            JPanel heading = new JPanel();
            heading.setOpaque(false);
            heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));
            heading.setBorder(new EmptyBorder(4, 0, 18, 0));

            JLabel welcome = new JLabel("Welcome back, "
                    + currentUser.getFullName());
            welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
            welcome.setForeground(TEXT_DARK);
            welcome.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel sub = new JLabel("Here's what's happening with your inventory today.");
            sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            sub.setForeground(TEXT_MUTED);
            sub.setAlignmentX(Component.LEFT_ALIGNMENT);

            heading.add(welcome);
            heading.add(Box.createVerticalStrut(4));
            heading.add(sub);
            heading.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel cards = new JPanel(new GridLayout(1, 3, 18, 0));
            cards.setOpaque(false);
            cards.setAlignmentX(Component.LEFT_ALIGNMENT);

            productsCard = new StatCard("Total Products", "0", "items in system",
                    ACCENT, ICON_BOX, "", true);
            lowStockCard = new StatCard("Low Stock", "0", "needs restocking",
                    new Color(220, 60, 60), ICON_ALERT, "", false);
            valueCard = new StatCard("Total Value", Currency.format(0),
                    "across all items",
                    new Color(34, 160, 90), ICON_DOLLAR, "", true);

            cards.add(productsCard);
            cards.add(lowStockCard);
            cards.add(valueCard);

            top.add(heading);
            top.add(cards);

            // ---- Middle row: low-stock card + recent activity card ----
            JPanel middleRow = new JPanel(new GridLayout(1, 2, 16, 0));
            middleRow.setOpaque(false);
            middleRow.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel lowStockCardPanel = buildLowStockCard();
            JPanel activityCard = buildActivityCard();
            middleRow.add(lowStockCardPanel);
            middleRow.add(activityCard);

            JPanel wrapper = new JPanel(new BorderLayout(0, 16));
            wrapper.setOpaque(false);
            wrapper.add(top, BorderLayout.NORTH);
            wrapper.add(middleRow, BorderLayout.CENTER);

            add(wrapper, BorderLayout.CENTER);

            refresh();
        }

        // ---- Low-stock card ----
        private JPanel buildLowStockCard() {
            JPanel card = new RoundedPanel(14, SURFACE);
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(18, 18, 18, 18));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 4, 10, 0));

            JLabel title = new JLabel("Low Stock");
            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            title.setForeground(TEXT_DARK);
            header.add(title, BorderLayout.WEST);

            lowStockCountLabel = new JLabel("0 items");
            lowStockCountLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lowStockCountLabel.setForeground(new Color(220, 60, 60));
            header.add(lowStockCountLabel, BorderLayout.EAST);

            card.add(header, BorderLayout.NORTH);

            String[] cols = {"Product", "Qty", "Threshold"};
            lowStockModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            JTable table = new JTable(lowStockModel);
            styleTable(table);

            table.getColumnModel().getColumn(0).setPreferredWidth(280);
            table.getColumnModel().getColumn(1).setPreferredWidth(70);
            table.getColumnModel().getColumn(2).setPreferredWidth(90);

            // Qty renderer — red/bold when at or below threshold
            DefaultTableCellRenderer qtyR = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable t, Object v,
                        boolean sel, boolean focus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if (!sel) {
                        int qty = Integer.parseInt(String.valueOf(v));
                        Object thr = t.getModel().getValueAt(row, 2);
                        int threshold = (thr instanceof Integer) ? (Integer) thr : 0;
                        if (qty <= threshold) {
                            c.setForeground(new Color(220, 60, 60));
                            setFont(getFont().deriveFont(Font.BOLD));
                        } else {
                            c.setForeground(TEXT_MUTED);
                            setFont(getFont().deriveFont(Font.PLAIN));
                        }
                    }
                    return c;
                }
            };
            table.getColumnModel().getColumn(1).setCellRenderer(qtyR);

            DefaultTableCellRenderer thrR = new DefaultTableCellRenderer();
            thrR.setHorizontalAlignment(SwingConstants.CENTER);
            thrR.setForeground(TEXT_MUTED);
            table.getColumnModel().getColumn(2).setCellRenderer(thrR);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(SURFACE);
            scroll.setPreferredSize(new Dimension(0, 260));

            card.add(scroll, BorderLayout.CENTER);

            // Footer: hint text (either "N low" or "nothing low, showing lowest")
            lowStockHintLabel = new JLabel(" ");
            lowStockHintLabel.setFont(FONT_SMALL);
            lowStockHintLabel.setForeground(TEXT_MUTED);
            lowStockHintLabel.setBorder(new EmptyBorder(8, 4, 0, 4));

            emptyLowStockLabel = new JLabel("No products yet.", SwingConstants.CENTER);
            emptyLowStockLabel.setFont(FONT_SMALL);
            emptyLowStockLabel.setForeground(TEXT_MUTED);
            emptyLowStockLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
            emptyLowStockLabel.setVisible(false);

            JPanel footer = new JPanel(new BorderLayout());
            footer.setOpaque(false);
            footer.add(lowStockHintLabel, BorderLayout.NORTH);
            footer.add(emptyLowStockLabel, BorderLayout.CENTER);

            card.add(footer, BorderLayout.SOUTH);

            return card;
        }

        private JPanel buildActivityCard() {
            JPanel card = new RoundedPanel(14, SURFACE);
            card.setLayout(new BorderLayout());
            card.setBorder(new EmptyBorder(18, 18, 18, 18));

            JPanel header = new JPanel(new BorderLayout());
            header.setOpaque(false);
            header.setBorder(new EmptyBorder(0, 4, 10, 0));

            JLabel title = new JLabel("Recent Activity");
            title.setFont(new Font("Segoe UI", Font.BOLD, 15));
            title.setForeground(TEXT_DARK);
            header.add(title, BorderLayout.WEST);

            JLabel hint = new JLabel("last 10 stock changes");
            hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            hint.setForeground(TEXT_MUTED);
            header.add(hint, BorderLayout.EAST);

            card.add(header, BorderLayout.NORTH);

            String[] cols = {"When", "Product", "Change", "Reason", "By"};
            activityModel = new DefaultTableModel(cols, 0) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            };

            JTable table = new JTable(activityModel);
            styleTable(table);

            table.getColumnModel().getColumn(0).setPreferredWidth(110);
            table.getColumnModel().getColumn(1).setPreferredWidth(180);
            table.getColumnModel().getColumn(2).setPreferredWidth(70);
            table.getColumnModel().getColumn(3).setPreferredWidth(200);
            table.getColumnModel().getColumn(4).setPreferredWidth(120);

            DefaultTableCellRenderer deltaR = new DefaultTableCellRenderer() {
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
            table.getColumnModel().getColumn(2).setCellRenderer(deltaR);

            DefaultTableCellRenderer mutedR = new DefaultTableCellRenderer();
            mutedR.setForeground(TEXT_MUTED);
            table.getColumnModel().getColumn(0).setCellRenderer(mutedR);
            table.getColumnModel().getColumn(3).setCellRenderer(mutedR);
            table.getColumnModel().getColumn(4).setCellRenderer(mutedR);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(SURFACE);
            scroll.setPreferredSize(new Dimension(0, 260));

            card.add(scroll, BorderLayout.CENTER);

            emptyActivityLabel = new JLabel("No stock activity yet.", SwingConstants.CENTER);
            emptyActivityLabel.setFont(FONT_SMALL);
            emptyActivityLabel.setForeground(TEXT_MUTED);
            emptyActivityLabel.setBorder(new EmptyBorder(20, 0, 20, 0));
            emptyActivityLabel.setVisible(false);
            card.add(emptyActivityLabel, BorderLayout.SOUTH);

            return card;
        }

        public void refresh() {
            int products = productDAO.countAll();
            int lowStock = productDAO.countLowStock();
            double value = productDAO.totalInventoryValue();

            productsCard.setValue(String.valueOf(products));
            productsCard.setSubtitle(products == 1 ? "item in system" : "items in system");
            productsCard.setTrend(products == 0 ? "No products yet" : products + " tracked");

            lowStockCard.setValue(String.valueOf(lowStock));
            lowStockCard.setSubtitle(lowStock == 0
                    ? "all items healthy"
                    : (lowStock == 1 ? "item needs restocking" : "items need restocking"));
            lowStockCard.setTrend(lowStock == 0 ? "All good" : "Restock soon");
            lowStockCard.setTrendUp(lowStock == 0);

            valueCard.setValue(Currency.format(value));
            valueCard.setTrend("Live total");

            refreshLowStockTable(lowStock);
            refreshActivity();
        }

        private void refreshLowStockTable(int lowStockCount) {
            lowStockModel.setRowCount(0);

            List<Product> toShow;
            boolean usingFallback = false;

            if (lowStockCount > 0) {
                toShow = productDAO.findLowStock();
                if (toShow.size() > 8) {
                    toShow = toShow.subList(0, 8);
                }
            } else {
                toShow = productDAO.findLowestStock(8);
                usingFallback = true;
            }

            if (toShow.isEmpty()) {
                // No products at all in the system
                emptyLowStockLabel.setVisible(true);
                lowStockCountLabel.setText("0 items");
                lowStockCountLabel.setForeground(TEXT_MUTED);
                lowStockHintLabel.setText(" ");
                return;
            }
            emptyLowStockLabel.setVisible(false);

            for (Product p : toShow) {
                lowStockModel.addRow(new Object[]{
                    p.getName(),
                    p.getQuantity(),
                    p.getLowStockThreshold()
                });
            }

            if (usingFallback) {
                lowStockCountLabel.setText(toShow.size() + " items");
                lowStockCountLabel.setForeground(TEXT_MUTED);
                lowStockHintLabel.setText("No items are below their threshold; "
                        + "showing the lowest quantities.");
            } else {
                lowStockCountLabel.setText(lowStockCount + (lowStockCount == 1
                        ? " item low" : " items low"));
                lowStockCountLabel.setForeground(new Color(220, 60, 60));
                lowStockHintLabel.setText("At or below each product's own threshold.");
            }
        }

        private void refreshActivity() {
            activityModel.setRowCount(0);

            List<StockMovement> recent = productDAO.findRecentMovements(10);

            if (recent.isEmpty()) {
                emptyActivityLabel.setVisible(true);
                return;
            }
            emptyActivityLabel.setVisible(false);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, HH:mm");

            for (StockMovement m : recent) {
                String when = m.getMovedAt() != null
                        ? m.getMovedAt().toLocalDateTime().format(fmt)
                        : "—";
                String product = m.getProductName() != null
                        ? m.getProductName()
                        : ("#" + m.getProductId());
                String delta = (m.getChangeQty() > 0 ? "+" : "") + m.getChangeQty();
                String reason = m.getReason() != null && !m.getReason().isBlank()
                        ? m.getReason()
                        : "—";
                String by = m.getUserName() != null ? m.getUserName() : "—";

                activityModel.addRow(new Object[]{when, product, delta, reason, by});
            }
        }

        private void styleTable(JTable table) {
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
            headerRow.setPreferredSize(new Dimension(0, 32));
            headerRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0,
                    new Color(230, 232, 238)));
            headerRow.setReorderingAllowed(false);
        }
    }

    private static class StatCard extends JPanel {

        private final Color accent;
        private final int iconType;

        private JLabel valueLabel;
        private JLabel subLabel;
        private JLabel trendLabel;
        private boolean trendUp;

        StatCard(String title, String value, String subtitle,
                Color accent, int iconType, String trend, boolean trendUp) {

            this.accent = accent;
            this.iconType = iconType;
            this.trendUp = trendUp;

            setLayout(new BorderLayout());
            setOpaque(false);
            setBorder(new EmptyBorder(20, 22, 20, 22));

            JPanel content = new JPanel();
            content.setOpaque(false);
            content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            titleLabel.setForeground(TEXT_MUTED);

            JLabel badge = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(accent.getRed(), accent.getGreen(),
                            accent.getBlue(), 30));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                    g2.setColor(accent);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND,
                            BasicStroke.JOIN_ROUND));
                    int cx = getWidth() / 2;
                    int cy = getHeight() / 2;

                    switch (iconType) {
                        case ICON_ALERT -> {
                            g2.drawLine(cx, cy - 8, cx, cy + 1);
                            g2.fillOval(cx - 1, cy + 4, 3, 3);
                        }
                        case ICON_DOLLAR -> {
                            g2.setFont(new Font("Segoe UI", Font.BOLD, 15));
                            FontMetrics fm = g2.getFontMetrics();
                            String s = "₱";
                            g2.drawString(s, cx - fm.stringWidth(s) / 2,
                                    cy + fm.getAscent() / 2 - 1);
                        }
                        default -> {
                            g2.drawRect(cx - 7, cy - 7, 14, 14);
                            g2.drawLine(cx - 7, cy - 1, cx + 7, cy - 1);
                            g2.drawLine(cx, cy - 7, cx, cy + 7);
                        }
                    }
                    g2.dispose();
                }
            };
            badge.setPreferredSize(new Dimension(34, 34));

            topRow.add(titleLabel, BorderLayout.WEST);
            topRow.add(badge, BorderLayout.EAST);

            valueLabel = new JLabel(value);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
            valueLabel.setForeground(accent);
            valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            subLabel = new JLabel(subtitle);
            subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            subLabel.setForeground(TEXT_MUTED);
            subLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            trendLabel = new JLabel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    Color trendColor = StatCard.this.trendUp
                            ? new Color(34, 160, 90)
                            : new Color(220, 60, 60);
                    g2.setColor(new Color(trendColor.getRed(), trendColor.getGreen(),
                            trendColor.getBlue(), 28));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            trendLabel.setText(trend);
            trendLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
            trendLabel.setForeground(trendUp
                    ? new Color(34, 160, 90)
                    : new Color(220, 60, 60));
            trendLabel.setBorder(new EmptyBorder(4, 10, 4, 10));
            trendLabel.setOpaque(false);
            trendLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            trendLabel.setMaximumSize(new Dimension(250, 22));

            content.add(topRow);
            content.add(Box.createVerticalStrut(14));
            content.add(valueLabel);
            content.add(Box.createVerticalStrut(4));
            content.add(subLabel);
            content.add(Box.createVerticalStrut(12));

            JPanel trendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            trendRow.setOpaque(false);
            trendRow.setAlignmentX(Component.LEFT_ALIGNMENT);
            trendRow.add(trendLabel);

            content.add(trendRow);

            add(content, BorderLayout.CENTER);
        }

        void setValue(String v) {
            valueLabel.setText(v);
        }

        void setSubtitle(String s) {
            subLabel.setText(s);
        }

        void setTrend(String t) {
            trendLabel.setText(t);
            trendLabel.setVisible(t != null && !t.isEmpty());
        }

        void setTrendUp(boolean up) {
            this.trendUp = up;
            Color trendColor = up
                    ? new Color(34, 160, 90)
                    : new Color(220, 60, 60);
            trendLabel.setForeground(trendColor);
            trendLabel.repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            for (int i = 5; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 5));
                g2.fillRoundRect(i, i, w - i * 2, h - i * 2, 16 + i, 16 + i);
            }

            GradientPaint gp = new GradientPaint(
                    0, 0, Color.WHITE,
                    0, h, new Color(250, 251, 254));
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0, w - 1, h - 1, 16, 16));

            g2.setColor(new Color(230, 232, 238));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Double(0.5, 0.5, w - 1.5, h - 1.5, 16, 16));

            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static final int ICON_BOX = 0;
    private static final int ICON_ALERT = 1;
    private static final int ICON_DOLLAR = 2;
}
