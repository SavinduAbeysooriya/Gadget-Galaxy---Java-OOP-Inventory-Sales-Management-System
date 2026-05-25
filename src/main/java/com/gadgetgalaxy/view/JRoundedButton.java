package com.gadgetgalaxy.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom JButton with rounded corners and hover animation effect.
 * Demonstrates custom Swing component creation.
 */
public class JRoundedButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color fg;
    private boolean hovered = false;

    public JRoundedButton(String text, Color bg, Color fg) {
        super(text);
        this.normalBg = bg;
        this.hoverBg = bg.brighter();
        this.fg = fg;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(fg);
        setFont(UIConstants.FONT_BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(hovered ? hoverBg : normalBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
        g2.dispose();
        super.paintComponent(g);
    }
}
