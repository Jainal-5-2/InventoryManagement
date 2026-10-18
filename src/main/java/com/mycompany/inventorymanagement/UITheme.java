package com.mycompany.inventorymanagement;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

public final class UITheme {

    public static final Color ACCENT         = new Color(79, 70, 229);
    public static final Color ACCENT_HOVER   = new Color(99, 91, 255);
    public static final Color ACCENT_PRESSED = new Color(63, 56, 190);
    public static final Color TEXT_DARK      = new Color(30, 30, 40);
    public static final Color TEXT_MUTED     = new Color(120, 120, 140);
    public static final Color ERROR_RED      = new Color(220, 60, 60);
    public static final Color SUCCESS_GREEN  = new Color(34, 160, 90);
    public static final Color FIELD_BORDER   = new Color(210, 210, 220);
    public static final Color SURFACE        = Color.WHITE;
    public static final Color GRADIENT_TOP    = new Color(99, 91, 255);
    public static final Color GRADIENT_BOTTOM = new Color(139, 92, 246);

    public static final Font FONT_BASE    = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_BRAND   = new Font("Segoe UI", Font.BOLD, 34);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 15);

    private UITheme() {}

    public static void applyGlobalFont() {
        UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    }

    public static void styleTextField(JTextField field, String placeholder) {
        field.setFont(FONT_BASE);
        field.putClientProperty("JTextField.placeholderText", placeholder);
        field.putClientProperty("JComponent.roundRect", true);

        Border idle = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14));
        Border focus = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ACCENT, 2, true),
                BorderFactory.createEmptyBorder(9, 13, 9, 13));

        field.setBorder(idle);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { field.setBorder(focus); }
            @Override public void focusLost(FocusEvent e)   { field.setBorder(idle); }
        });
        field.setPreferredSize(new Dimension(0, 46));
    }

    public static JLabel linkLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_LABEL);
        label.setForeground(ACCENT);
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }

    // -------- Reusable custom components --------

    public static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius, Color bg) {
            this.radius = radius;
            setBackground(bg);
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 6; i > 0; i--) {
                g2.setColor(new Color(0, 0, 0, 4));
                g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2,
                                 radius + i, radius + i);
            }
            g2.setColor(getBackground());
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1,
                                                radius, radius));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class AccentButton extends JButton {
        private boolean hovered = false;
        private boolean pressed = false;

        public AccentButton(String text) {
            super(text);
            setForeground(Color.WHITE);
            setFont(FONT_BUTTON);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                @Override public void mouseExited (MouseEvent e) { hovered = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true;  repaint(); }
                @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = pressed ? ACCENT_PRESSED : (hovered ? ACCENT_HOVER : ACCENT);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static class GradientBrandPanel extends JPanel {
        public GradientBrandPanel() {
            setLayout(new GridBagLayout());
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(
                    0, 0, GRADIENT_TOP,
                    0, getHeight(), GRADIENT_BOTTOM);
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    public static class LogoMark extends JLabel {
        public LogoMark(int size) {
            setPreferredSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(255, 255, 255, 40));
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND,
                                         BasicStroke.JOIN_ROUND));
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            g2.drawRect(cx - 14, cy - 14, 28, 28);
            g2.drawLine(cx - 14, cy - 4, cx + 14, cy - 4);
            g2.drawLine(cx, cy - 14, cx, cy + 14);
            g2.dispose();
        }
    }

    public static JPanel buildBrandPanel(String title, String subtitle, String tagline) {
        GradientBrandPanel brand = new GradientBrandPanel();

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        LogoMark logo = new LogoMark(72);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(FONT_BRAND);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setForeground(new Color(255, 255, 255, 200));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagLabel = new JLabel(tagline);
        tagLabel.setForeground(new Color(255, 255, 255, 160));
        tagLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        tagLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        content.add(logo);
        content.add(Box.createVerticalStrut(20));
        content.add(titleLabel);
        content.add(Box.createVerticalStrut(6));
        content.add(subLabel);
        content.add(Box.createVerticalStrut(24));
        content.add(tagLabel);

        brand.add(content);
        return brand;
    }
}