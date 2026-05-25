package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.exception.AuthenticationException;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.service.AuthenticationService;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Enhanced Login Form with modern UX:
 * - Animated focus borders on input fields
 * - Show / hide password toggle
 * - Glowing Sign In button with hover effect
 * - Smooth loading state
 */
public class LoginForm extends JFrame {

    private final AppController controller;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    // Design tokens
    private static final Color BG_TOP        = new Color(10, 12, 22);
    private static final Color BG_BOTTOM     = new Color(16, 20, 40);
    private static final Color CARD_BG       = new Color(20, 24, 40);
    private static final Color CARD_BORDER   = new Color(45, 55, 85);
    private static final Color FIELD_BG      = new Color(26, 31, 52);
    private static final Color FIELD_BORDER  = new Color(50, 60, 90);
    private static final Color FOCUS_COLOR   = new Color(64, 156, 255);
    private static final Color BTN_FROM      = new Color(50, 130, 255);
    private static final Color BTN_TO        = new Color(100, 80, 255);
    private static final Color TEXT_WHITE    = new Color(240, 245, 255);
    private static final Color TEXT_MUTED    = new Color(110, 125, 165);
    private static final Color TEXT_LABEL    = new Color(150, 165, 200);
    private static final Color ERROR_COLOR   = new Color(255, 85, 85);
    private static final Color SUCCESS_COLOR = new Color(0, 210, 170);

    public LoginForm(AppController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Gadget Galaxy – Login");
        setSize(460, 600);
        setMinimumSize(new Dimension(420, 560));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(false);

        // ── Root panel with gradient background ──────────────────────────
        JPanel root = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, BG_TOP, getWidth(), getHeight(), BG_BOTTOM));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // Subtle radial glow at top-center
                RadialGradientPaint glow = new RadialGradientPaint(
                        new Point(getWidth() / 2, 60), 200,
                        new float[]{0f, 1f},
                        new Color[]{new Color(64, 100, 255, 30), new Color(0, 0, 0, 0)}
                );
                g2.setPaint(glow);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setLayout(new GridBagLayout());
        root.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // ── Logo area ────────────────────────────────────────────────────
        JPanel logoPanel = buildLogoPanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 28, 0);
        root.add(logoPanel, gbc);

        // ── Card ─────────────────────────────────────────────────────────
        JPanel card = buildCard();
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        root.add(card, gbc);

        setContentPane(root);
        getRootPane().setDefaultButton(loginButton);
    }

    // ── Logo Panel ───────────────────────────────────────────────────────

    private JPanel buildLogoPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Gradient icon badge
        JLabel icon = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Outer glow
                g2.setColor(new Color(64, 156, 255, 40));
                g2.fillOval(-6, -6, getWidth() + 12, getHeight() + 12);
                // Gradient circle
                g2.setPaint(new GradientPaint(0, 0, BTN_FROM, getWidth(), getHeight(), BTN_TO));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                // Icon text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
                FontMetrics fm = g2.getFontMetrics();
                String t = "📱";
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent()) / 2 - 4);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(72, 72); }
        };
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel name = new JLabel("GADGET GALAXY");
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        name.setForeground(TEXT_WHITE);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Inventory & Sales Management");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sub.setForeground(new Color(100, 140, 220));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(icon);
        panel.add(Box.createVerticalStrut(14));
        panel.add(name);
        panel.add(Box.createVerticalStrut(5));
        panel.add(sub);
        return panel;
    }

    // ── Card Panel ───────────────────────────────────────────────────────

    private JPanel buildCard() {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Card shadow
                for (int i = 8; i > 0; i--) {
                    g2.setColor(new Color(0, 0, 0, 12 * i / 8));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 20, 20);
                }
                // Card body
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                // Top accent line (gradient)
                g2.setPaint(new GradientPaint(0, 0, BTN_FROM, getWidth(), 0, BTN_TO));
                g2.setStroke(new BasicStroke(2.5f));
                g2.drawLine(30, 1, getWidth() - 30, 1);
                // Border
                g2.setColor(CARD_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        // Welcome heading
        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 20));
        welcome.setForeground(TEXT_WHITE);
        c.gridx = 0; c.gridy = 0;
        c.insets = new Insets(0, 0, 4, 0);
        card.add(welcome, c);

        JLabel signInSub = new JLabel("Sign in to your account");
        signInSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        signInSub.setForeground(TEXT_MUTED);
        c.gridy = 1;
        c.insets = new Insets(0, 0, 26, 0);
        card.add(signInSub, c);

        // Username
        c.gridy = 2;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Username"), c);

        usernameField = new JTextField();
        styleField(usernameField);
        c.gridy = 3;
        c.insets = new Insets(0, 0, 18, 0);
        card.add(usernameField, c);

        // Password
        c.gridy = 4;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(fieldLabel("Password"), c);

        JPanel passRow = buildPasswordRow();
        c.gridy = 5;
        c.insets = new Insets(0, 0, 6, 0);
        card.add(passRow, c);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(ERROR_COLOR);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        c.gridy = 6;
        c.insets = new Insets(4, 0, 4, 0);
        card.add(statusLabel, c);

        // Sign In button
        loginButton = buildLoginButton();
        c.gridy = 7;
        c.insets = new Insets(10, 0, 0, 0);
        card.add(loginButton, c);

        return card;
    }

    // ── Password row with eye toggle ─────────────────────────────────────

    private JPanel buildPasswordRow() {
        passwordField = new JPasswordField();
        styleField(passwordField);

        JToggleButton eyeBtn = new JToggleButton("👁");
        eyeBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 13));
        eyeBtn.setForeground(TEXT_MUTED);
        eyeBtn.setBackground(FIELD_BG);
        eyeBtn.setOpaque(true);
        eyeBtn.setContentAreaFilled(true);
        eyeBtn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, FIELD_BORDER),
                BorderFactory.createEmptyBorder(0, 8, 0, 8)
        ));
        eyeBtn.setFocusPainted(false);
        eyeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        eyeBtn.setPreferredSize(new Dimension(42, 44));
        eyeBtn.setToolTipText("Show / Hide password");
        eyeBtn.addActionListener(e -> {
            boolean show = eyeBtn.isSelected();
            passwordField.setEchoChar(show ? (char) 0 : '\u2022');
            eyeBtn.setForeground(show ? FOCUS_COLOR : TEXT_MUTED);
        });

        // Remove right border from password field so it joins flush with eye button
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 1, 0, FIELD_BORDER),
                BorderFactory.createEmptyBorder(10, 14, 10, 8)
        ));

        JPanel row = new JPanel(new BorderLayout(0, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(0, 44));
        row.add(passwordField, BorderLayout.CENTER);
        row.add(eyeBtn, BorderLayout.EAST);
        return row;
    }

    // ── Glowing Sign In button ────────────────────────────────────────────

    private JButton buildLoginButton() {
        JButton btn = new JButton("Sign In") {
            private float hoverAlpha = 0f;
            private Timer hoverTimer;

            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) {
                        animateHover(true);
                    }
                    @Override public void mouseExited(MouseEvent e) {
                        animateHover(false);
                    }
                    private void animateHover(boolean in) {
                        if (hoverTimer != null) hoverTimer.stop();
                        hoverTimer = new Timer(16, ev -> {
                            hoverAlpha = in ? Math.min(1f, hoverAlpha + 0.1f)
                                           : Math.max(0f, hoverAlpha - 0.1f);
                            repaint();
                            if ((in && hoverAlpha >= 1f) || (!in && hoverAlpha <= 0f))
                                hoverTimer.stop();
                        });
                        hoverTimer.start();
                    }
                });
            }

            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Base gradient
                Color c1 = blend(BTN_FROM, BTN_FROM.brighter(), hoverAlpha);
                Color c2 = blend(BTN_TO,   BTN_TO.brighter(),   hoverAlpha);
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                // Glow on hover
                if (hoverAlpha > 0) {
                    g2.setColor(new Color(100, 160, 255, (int)(40 * hoverAlpha)));
                    g2.setStroke(new BasicStroke(3f));
                    g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                }

                // Text
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                FontMetrics fm = g2.getFontMetrics();
                String text = isEnabled() ? getText() : "Signing in...";
                g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }

            private Color blend(Color a, Color b, float t) {
                return new Color(
                        (int)(a.getRed()   + (b.getRed()   - a.getRed())   * t),
                        (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                        (int)(a.getBlue()  + (b.getBlue()  - a.getBlue())  * t)
                );
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(0, 46));
        btn.addActionListener(e -> doLogin());
        return btn;
    }

    // ── Field styling ─────────────────────────────────────────────────────

    private void styleField(JTextField f) {
        f.setBackground(FIELD_BG);
        f.setForeground(TEXT_WHITE);
        f.setCaretColor(FOCUS_COLOR);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(0, 44));
        f.setBorder(BorderFactory.createCompoundBorder(
                new FocusRoundBorder(FIELD_BORDER, FOCUS_COLOR, 10),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        // Focus border color change
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                ((FocusRoundBorder) ((javax.swing.border.CompoundBorder) f.getBorder()).getOutsideBorder()).setFocused(true);
                f.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                ((FocusRoundBorder) ((javax.swing.border.CompoundBorder) f.getBorder()).getOutsideBorder()).setFocused(false);
                f.repaint();
            }
        });
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(TEXT_LABEL);
        return l;
    }

    // ── Custom rounded focus border ───────────────────────────────────────

    private static class FocusRoundBorder extends AbstractBorder {
        private Color normalColor, focusColor;
        private final int radius;
        private boolean focused = false;

        FocusRoundBorder(Color normal, Color focus, int radius) {
            this.normalColor = normal;
            this.focusColor  = focus;
            this.radius      = radius;
        }

        void setFocused(boolean f) { this.focused = f; }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (focused) {
                // Glow effect
                g2.setColor(new Color(focusColor.getRed(), focusColor.getGreen(), focusColor.getBlue(), 40));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(x - 1, y - 1, w + 1, h + 1, radius + 2, radius + 2);
                g2.setColor(focusColor);
                g2.setStroke(new BasicStroke(1.5f));
            } else {
                g2.setColor(normalColor);
                g2.setStroke(new BasicStroke(1f));
            }
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }

        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public Insets getBorderInsets(Component c, Insets i) {
            i.set(1, 1, 1, 1); return i;
        }
    }

    // ── Login logic ───────────────────────────────────────────────────────

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password.", false);
            shakeComponent(loginButton);
            return;
        }

        loginButton.setEnabled(false);
        showStatus("Authenticating...", null);

        SwingWorker<User, Void> worker = new SwingWorker<>() {
            @Override protected User doInBackground() throws Exception {
                return controller.getAuthService().login(username, password);
            }
            @Override protected void done() {
                try {
                    get();
                    showStatus("Success! Opening dashboard...", true);
                    Timer delay = new Timer(600, e -> {
                        Dashboard dashboard = new Dashboard(controller);
                        dashboard.setVisible(true);
                        LoginForm.this.dispose();
                    });
                    delay.setRepeats(false);
                    delay.start();
                } catch (java.util.concurrent.ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    if (cause instanceof AuthenticationException) {
                        showStatus(cause.getMessage(), false);
                    } else if (cause instanceof DatabaseException) {
                        showStatus("Database error: " + cause.getMessage(), false);
                    } else {
                        showStatus("Unexpected error: " + cause.getMessage(), false);
                    }
                    shakeComponent(loginButton);
                    loginButton.setEnabled(true);
                } catch (Exception ex) {
                    showStatus("Error: " + ex.getMessage(), false);
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void showStatus(String msg, Boolean success) {
        statusLabel.setText(msg);
        if (success == null)       statusLabel.setForeground(TEXT_MUTED);
        else if (success)          statusLabel.setForeground(SUCCESS_COLOR);
        else                       statusLabel.setForeground(ERROR_COLOR);
    }

    /** Horizontal shake animation on error */
    private void shakeComponent(Component comp) {
        Point origin = comp.getLocation();
        int[] offsets = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
        Timer t = new Timer(30, null);
        final int[] i = {0};
        t.addActionListener(e -> {
            if (i[0] < offsets.length) {
                comp.setLocation(origin.x + offsets[i[0]], origin.y);
                i[0]++;
            } else {
                comp.setLocation(origin);
                t.stop();
            }
        });
        t.start();
    }
}
