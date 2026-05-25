package com.gadgetgalaxy.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Custom JButton with rounded corners, hover animation, and emoji-safe text rendering.
 * Splits label into emoji prefix + text suffix so both render correctly.
 */
public class JRoundedButton extends JButton {
    private Color normalBg;
    private Color hoverBg;
    private Color fg;
    private boolean hovered = false;

    // Emoji font used for the icon prefix
    private static final Font EMOJI_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    private static final Font TEXT_FONT  = UIConstants.FONT_BUTTON;

    public JRoundedButton(String text, Color bg, Color fg) {
        super(""); // keep super text empty; we draw manually
        this.normalBg = bg;
        this.hoverBg  = bg.brighter();
        this.fg       = fg;
        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setForeground(fg);
        setFont(TEXT_FONT);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // Store label in action command so getText() still works for logic
        setActionCommand(text);
        putClientProperty("label", text);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
        });
    }

    /** Returns the display label (not the empty super text). */
    @Override
    public String getText() {
        Object lbl = getClientProperty("label");
        return lbl != null ? lbl.toString() : super.getText();
    }

    /** Allows updating the label at runtime. */
    @Override
    public void setText(String text) {
        putClientProperty("label", text);
        setActionCommand(text);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g2.setColor(hovered ? hoverBg : normalBg);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        // Draw label with emoji-aware split rendering
        String label = getText();
        if (label == null || label.isEmpty()) { g2.dispose(); return; }

        g2.setColor(isEnabled() ? fg : fg.darker());
        drawEmojiAwareText(g2, label, getWidth(), getHeight());
        g2.dispose();
    }

    /**
     * Splits the label at the first space after any leading emoji codepoints,
     * renders the emoji part with Segoe UI Emoji and the rest with Segoe UI.
     */
    private void drawEmojiAwareText(Graphics2D g2, String label, int w, int h) {
        // Find split point: leading emoji chars (codepoint > 0x2000) + optional space
        int splitIdx = 0;
        int[] codePoints = label.codePoints().toArray();
        for (int cp : codePoints) {
            if (cp > 0x2000) { // emoji / special symbol range
                splitIdx += Character.charCount(cp);
            } else {
                break;
            }
        }
        // Consume one trailing space after emoji if present
        if (splitIdx < label.length() && label.charAt(splitIdx) == ' ') splitIdx++;

        String emojiPart = label.substring(0, splitIdx);
        String textPart  = label.substring(splitIdx);

        // Measure both parts
        g2.setFont(EMOJI_FONT);
        FontMetrics emFm = g2.getFontMetrics();
        int emojiW = emFm.stringWidth(emojiPart);

        g2.setFont(TEXT_FONT);
        FontMetrics txFm = g2.getFontMetrics();
        int textW = txFm.stringWidth(textPart);

        int totalW = emojiW + textW;
        int startX = (w - totalW) / 2;
        int baseY  = (h + txFm.getAscent() - txFm.getDescent()) / 2;

        // Draw emoji part
        if (!emojiPart.isEmpty()) {
            g2.setFont(EMOJI_FONT);
            FontMetrics ef = g2.getFontMetrics();
            int emojiY = (h + ef.getAscent() - ef.getDescent()) / 2;
            g2.drawString(emojiPart, startX, emojiY);
        }

        // Draw text part
        if (!textPart.isEmpty()) {
            g2.setFont(TEXT_FONT);
            g2.drawString(textPart, startX + emojiW, baseY);
        }
    }
}
