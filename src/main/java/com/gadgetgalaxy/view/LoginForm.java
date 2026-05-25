package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.exception.AuthenticationException;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * The Login Form – entry point to the application.
 * Demonstrates Swing JFrame, event handling, and service-layer usage from a view.
 */
public class LoginForm extends JFrame {

    private final AppController controller;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private JButton loginButton;

    public LoginForm(AppController controller) {
        this.controller = controller;
        initUI();
    }

    private void initUI() {
        setTitle("Gadget Galaxy – Login");
        setSize(440, 560);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with dark background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.BG_DARK,
                        0, getHeight(), new Color(18, 20, 35));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);

        // ===== LOGO / TITLE AREA =====
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        // Icon circle  
        JLabel iconLabel = new JLabel("✦") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIConstants.ACCENT_BLUE, getWidth(), getHeight(), UIConstants.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 26));
                FontMetrics fm = g2.getFontMetrics();
                String txt = "✦";
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt)) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(70, 70); }
        };
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("GADGET GALAXY");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        appName.setForeground(UIConstants.TEXT_HEADER);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagLine = new JLabel("Inventory & Sales Management");
        tagLine.setFont(UIConstants.FONT_SMALL);
        tagLine.setForeground(UIConstants.TEXT_MUTED);
        tagLine.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(iconLabel);
        titlePanel.add(Box.createVerticalStrut(12));
        titlePanel.add(appName);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(tagLine);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 24, 0);
        mainPanel.add(titlePanel, gbc);

        // ===== LOGIN CARD =====
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(UIConstants.BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new GridBagLayout());
        card.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));

        GridBagConstraints cgbc = new GridBagConstraints();
        cgbc.fill = GridBagConstraints.HORIZONTAL;
        cgbc.insets = new Insets(5, 0, 5, 0);
        cgbc.weightx = 1.0;

        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome Back");
        welcomeLabel.setFont(UIConstants.FONT_HEADER);
        welcomeLabel.setForeground(UIConstants.TEXT_HEADER);
        cgbc.gridx = 0; cgbc.gridy = 0;
        card.add(welcomeLabel, cgbc);

        JLabel subLabel = new JLabel("Sign in to continue");
        subLabel.setFont(UIConstants.FONT_SMALL);
        subLabel.setForeground(UIConstants.TEXT_MUTED);
        cgbc.gridy = 1;
        cgbc.insets = new Insets(0, 0, 18, 0);
        card.add(subLabel, cgbc);

        // Username label
        cgbc.insets = new Insets(5, 0, 3, 0);
        cgbc.gridy = 2;
        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(UIConstants.FONT_SUBHEAD);
        userLabel.setForeground(UIConstants.TEXT_MUTED);
        card.add(userLabel, cgbc);

        // Username field
        cgbc.gridy = 3;
        usernameField = createStyledTextField();
        card.add(usernameField, cgbc);

        // Password label
        cgbc.gridy = 4;
        cgbc.insets = new Insets(10, 0, 3, 0);
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(UIConstants.FONT_SUBHEAD);
        passLabel.setForeground(UIConstants.TEXT_MUTED);
        card.add(passLabel, cgbc);

        // Password field
        cgbc.gridy = 5;
        cgbc.insets = new Insets(5, 0, 5, 0);
        passwordField = new JPasswordField();
        stylePasswordField(passwordField);
        card.add(passwordField, cgbc);

        // Status label
        cgbc.gridy = 6;
        statusLabel = new JLabel(" ");
        statusLabel.setFont(UIConstants.FONT_SMALL);
        statusLabel.setForeground(UIConstants.ACCENT_RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(statusLabel, cgbc);

        // Login button
        cgbc.gridy = 7;
        cgbc.insets = new Insets(8, 0, 0, 0);
        loginButton = new JRoundedButton("Sign In", UIConstants.ACCENT_BLUE, Color.WHITE);
        loginButton.setPreferredSize(new Dimension(300, 42));
        loginButton.addActionListener(e -> doLogin());
        card.add(loginButton, cgbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(card, gbc);

        // Default credential hint
        JLabel hint = new JLabel("Manager: admin/admin123  |  Sales: sales/sales123");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.TEXT_MUTED);
        hint.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 2;
        gbc.insets = new Insets(14, 0, 0, 0);
        mainPanel.add(hint, gbc);

        setContentPane(mainPanel);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginButton);
    }

    private JTextField createStyledTextField() {
        JTextField f = new JTextField();
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setPreferredSize(new Dimension(300, 40));
        return f;
    }

    private void stylePasswordField(JPasswordField f) {
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        f.setPreferredSize(new Dimension(300, 40));
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        loginButton.setEnabled(false);
        statusLabel.setForeground(UIConstants.TEXT_MUTED);
        statusLabel.setText("Authenticating...");

        // Run login in background thread to avoid blocking GUI
        SwingWorker<User, Void> worker = new SwingWorker<>() {
            private String errorMessage;

            @Override
            protected User doInBackground() throws Exception {
                AuthenticationService auth = controller.getAuthService();
                return auth.login(username, password);
            }

            @Override
            protected void done() {
                try {
                    User user = get();
                    // Launch Dashboard, close login
                    SwingUtilities.invokeLater(() -> {
                        Dashboard dashboard = new Dashboard(controller);
                        dashboard.setVisible(true);
                        LoginForm.this.dispose();
                    });
                } catch (java.util.concurrent.ExecutionException ee) {
                    Throwable cause = ee.getCause();
                    statusLabel.setForeground(UIConstants.ACCENT_RED);
                    if (cause instanceof AuthenticationException) {
                        statusLabel.setText(cause.getMessage());
                    } else if (cause instanceof DatabaseException) {
                        statusLabel.setText("Database error: " + cause.getMessage());
                    } else {
                        statusLabel.setText("Unexpected error: " + cause.getMessage());
                    }
                    loginButton.setEnabled(true);
                } catch (Exception ex) {
                    statusLabel.setForeground(UIConstants.ACCENT_RED);
                    statusLabel.setText("Error: " + ex.getMessage());
                    loginButton.setEnabled(true);
                }
            }
        };
        worker.execute();
    }
}
