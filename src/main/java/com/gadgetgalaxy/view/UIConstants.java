package com.gadgetgalaxy.view;

import java.awt.*;

/**
 * Defines the visual design system for the Gadget Galaxy UI.
 * Uses a sleek dark theme with vibrant accent colors.
 */
public final class UIConstants {
    private UIConstants() {}

    // ===== COLOR PALETTE =====
    public static final Color BG_DARK       = new Color(12, 14, 20);       // Primary background
    public static final Color BG_CARD       = new Color(22, 26, 38);       // Card/panel background
    public static final Color BG_SIDEBAR    = new Color(16, 20, 32);       // Sidebar background
    public static final Color BG_INPUT      = new Color(30, 35, 50);       // Input field background
    public static final Color BG_TABLE_ROW  = new Color(26, 31, 45);       // Alternate table row
    public static final Color BG_TABLE_HDR  = new Color(18, 22, 35);       // Table header

    public static final Color ACCENT_BLUE   = new Color(64, 156, 255);     // Primary accent (links, active items)
    public static final Color ACCENT_PURPLE = new Color(130, 80, 255);     // Secondary accent
    public static final Color ACCENT_TEAL   = new Color(0, 200, 180);      // Success / positive
    public static final Color ACCENT_RED    = new Color(255, 80, 80);      // Danger / error
    public static final Color ACCENT_ORANGE = new Color(255, 160, 40);     // Warning / low stock

    public static final Color TEXT_PRIMARY  = new Color(220, 225, 240);    // Primary text
    public static final Color TEXT_MUTED    = new Color(120, 130, 160);    // Secondary / muted text
    public static final Color TEXT_HEADER   = new Color(255, 255, 255);    // White for headers
    public static final Color BORDER_COLOR  = new Color(40, 48, 70);       // Subtle borders

    // ===== FONTS =====
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADER   = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBHEAD  = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD, 12);
    public static final Font FONT_MONO     = new Font("Consolas", Font.PLAIN, 12);

    // ===== DIMENSIONS =====
    public static final int SIDEBAR_WIDTH = 210;
    public static final int HEADER_HEIGHT = 55;

    // ===== HELPERS =====
    public static JRoundedButton createButton(String text, Color bg, Color fg) {
        JRoundedButton btn = new JRoundedButton(text, bg, fg);
        return btn;
    }

    public static javax.swing.JTextField createTextField(String placeholder) {
        javax.swing.JTextField field = new javax.swing.JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setBackground(BG_INPUT);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setFont(FONT_BODY);
        field.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER_COLOR, 1),
                javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        field.setOpaque(false);
        return field;
    }
}
