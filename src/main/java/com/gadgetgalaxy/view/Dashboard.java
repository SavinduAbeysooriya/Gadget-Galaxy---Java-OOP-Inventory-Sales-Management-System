package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Inventory;
import com.gadgetgalaxy.model.Sale;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.service.AuthenticationService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main Dashboard window with sidebar navigation, live clock thread, and dynamic content area.
 * Demonstrates Swing JFrame with complex layout, inner panels, and threading.
 */
public class Dashboard extends JFrame {

    private final AppController controller;
    private JPanel contentArea;
    private JLabel clockLabel;
    private JLabel userLabel;
    private JLabel roleLabel;
    private JPanel activeNavButton;

    // Nav panel references for sidebar buttons
    private JPanel btnDashboard, btnProducts, btnInventory, btnSales, btnUsers, btnReports, btnLogout;

    public Dashboard(AppController controller) {
        this.controller = controller;
        initUI();
        startClockThread();
        startLowStockMonitorThread();
        startAutoBackupThread();
        showHomePanel();
    }

    private void initUI() {
        User user = controller.getCurrentUser();
        setTitle("Gadget Galaxy – " + (user != null ? user.getFullName() : "Dashboard"));
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 650));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildTopBar(), BorderLayout.NORTH);
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(UIConstants.BG_DARK);
        add(contentArea, BorderLayout.CENTER);
    }

    // ===================== SIDEBAR =====================
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setPreferredSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 0));
        sidebar.setBackground(UIConstants.BG_SIDEBAR);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, UIConstants.BORDER_COLOR));

        // Logo
        JPanel logoPanel = new JPanel();
        logoPanel.setOpaque(false);
        logoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 18));
        logoPanel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 65));
        JLabel logo = new JLabel("✦  Gadget Galaxy");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        logo.setForeground(UIConstants.ACCENT_BLUE);
        logoPanel.add(logo);
        sidebar.add(logoPanel);

        // Divider
        sidebar.add(createSeparator());
        sidebar.add(Box.createVerticalStrut(6));

        // Nav label
        JLabel navLabel = new JLabel("  NAVIGATION");
        navLabel.setFont(UIConstants.FONT_SMALL);
        navLabel.setForeground(UIConstants.TEXT_MUTED);
        navLabel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 24));
        sidebar.add(navLabel);
        sidebar.add(Box.createVerticalStrut(4));

        // Nav buttons
        btnDashboard = createNavButton("⊞", "Dashboard", () -> showHomePanel());
        btnProducts  = createNavButton("▣", "Products", () -> showPanel(new ProductForm(controller)));
        btnInventory = createNavButton("≡", "Inventory", () -> showPanel(new InventoryForm(controller)));
        btnSales     = createNavButton("◎", "Sales", () -> showPanel(new SalesForm(controller)));

        sidebar.add(btnDashboard);
        sidebar.add(btnProducts);
        sidebar.add(btnInventory);
        sidebar.add(btnSales);

        // Manager-only section
        User user = controller.getCurrentUser();
        if (user != null && user.getRoleId() == 1) {
            sidebar.add(Box.createVerticalStrut(8));
            JLabel mgLabel = new JLabel("  MANAGEMENT");
            mgLabel.setFont(UIConstants.FONT_SMALL);
            mgLabel.setForeground(UIConstants.TEXT_MUTED);
            mgLabel.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 24));
            sidebar.add(mgLabel);
            sidebar.add(Box.createVerticalStrut(4));

            btnUsers   = createNavButton("◉", "Users", () -> showPanel(new UserManagementForm(controller)));
            btnReports = createNavButton("◈", "Reports", () -> showPanel(new ReportsForm(controller)));
            sidebar.add(btnUsers);
            sidebar.add(btnReports);
        }

        // Spacer
        sidebar.add(Box.createVerticalGlue());
        sidebar.add(createSeparator());

        // Logout button
        btnLogout = createNavButton("⬡", "Logout", this::doLogout);
        ((JLabel) btnLogout.getComponent(0)).setForeground(UIConstants.ACCENT_RED);
        sidebar.add(btnLogout);
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    private JPanel createNavButton(String icon, String label, Runnable action) {
        JPanel btn = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(icon + "  " + label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        btn.add(lbl);

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn != activeNavButton) {
                    btn.setOpaque(true);
                    btn.setBackground(new Color(30, 36, 55));
                    lbl.setForeground(UIConstants.TEXT_PRIMARY);
                    btn.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn != activeNavButton) {
                    btn.setOpaque(false);
                    lbl.setForeground(UIConstants.TEXT_MUTED);
                    btn.repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                setActiveNav(btn, lbl);
                action.run();
            }
        });

        return btn;
    }

    private void setActiveNav(JPanel btn, JLabel lbl) {
        // Deactivate previous
        if (activeNavButton != null) {
            activeNavButton.setOpaque(false);
            activeNavButton.repaint();
            for (Component c : activeNavButton.getComponents()) {
                if (c instanceof JLabel) ((JLabel) c).setForeground(UIConstants.TEXT_MUTED);
            }
        }
        activeNavButton = btn;
        btn.setOpaque(true);
        btn.setBackground(new Color(40, 60, 100));
        lbl.setForeground(UIConstants.ACCENT_BLUE);
        btn.repaint();
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator();
        sep.setForeground(UIConstants.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(UIConstants.SIDEBAR_WIDTH, 1));
        return sep;
    }

    // ===================== TOP BAR =====================
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIConstants.BG_CARD);
        topBar.setPreferredSize(new Dimension(0, UIConstants.HEADER_HEIGHT));
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(0, 20, 0, 20)
        ));

        // Left: Page title placeholder
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(UIConstants.FONT_HEADER);
        titleLabel.setForeground(UIConstants.TEXT_HEADER);
        topBar.add(titleLabel, BorderLayout.WEST);

        // Right: User info + Clock
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        rightPanel.setOpaque(false);

        clockLabel = new JLabel();
        clockLabel.setFont(UIConstants.FONT_MONO);
        clockLabel.setForeground(UIConstants.TEXT_MUTED);

        User user = controller.getCurrentUser();
        String userName = user != null ? user.getFullName() : "Unknown";
        String roleStr = user != null ? user.getRoleName() : "";

        userLabel = new JLabel(userName);
        userLabel.setFont(UIConstants.FONT_SUBHEAD);
        userLabel.setForeground(UIConstants.TEXT_PRIMARY);

        roleLabel = new JLabel(roleStr);
        roleLabel.setFont(UIConstants.FONT_SMALL);
        roleLabel.setForeground(UIConstants.ACCENT_BLUE);

        JPanel userPanel = new JPanel();
        userPanel.setOpaque(false);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.Y_AXIS));
        userLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        roleLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        userPanel.add(userLabel);
        userPanel.add(roleLabel);

        // Avatar circle
        JLabel avatar = new JLabel(String.valueOf(userName.charAt(0)).toUpperCase()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.ACCENT_PURPLE);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(UIConstants.FONT_SUBHEAD);
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth() - fm.stringWidth(t)) / 2, (getHeight() + fm.getAscent()) / 2 - 2);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(34, 34); }
        };
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);

        rightPanel.add(clockLabel);
        rightPanel.add(userPanel);
        rightPanel.add(avatar);

        topBar.add(rightPanel, BorderLayout.EAST);
        return topBar;
    }

    // ===================== THREADS =====================

    /**
     * Thread 1: Real-time clock update.
     */
    private void startClockThread() {
        Thread clockThread = new Thread(() -> {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("EEE, MMM dd yyyy  HH:mm:ss");
            while (!Thread.currentThread().isInterrupted()) {
                String time = LocalDateTime.now().format(dtf);
                SwingUtilities.invokeLater(() -> clockLabel.setText(time));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        clockThread.setDaemon(true);
        clockThread.setName("ClockThread");
        clockThread.start();
    }

    /**
     * Thread 2: Low stock monitor – checks every 30 seconds and alerts if stock is low.
     */
    private void startLowStockMonitorThread() {
        Thread monitorThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(30_000); // Check every 30 seconds
                    List<Inventory> lowStock = controller.getInventoryService().getLowStockItems();
                    if (!lowStock.isEmpty()) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(this,
                                    "⚠ LOW STOCK ALERT: " + lowStock.size() + " product(s) are below reorder level.\n" +
                                            "Go to Inventory tab to review.",
                                    "Low Stock Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        });
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (DatabaseException e) {
                    System.err.println("Low-stock monitor error: " + e.getMessage());
                }
            }
        });
        monitorThread.setDaemon(true);
        monitorThread.setName("LowStockMonitorThread");
        monitorThread.start();
    }

    /**
     * Thread 3: Background auto-backup every 60 seconds.
     */
    private void startAutoBackupThread() {
        Thread backupThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Thread.sleep(60_000); // Backup every 60 seconds
                    controller.getInventoryService().backupInventory();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (DatabaseException e) {
                    System.err.println("Auto-backup error: " + e.getMessage());
                }
            }
        });
        backupThread.setDaemon(true);
        backupThread.setName("AutoBackupThread");
        backupThread.start();
    }

    // ===================== PANEL NAVIGATION =====================

    public void showPanel(JPanel panel) {
        contentArea.removeAll();
        contentArea.add(panel, BorderLayout.CENTER);
        contentArea.revalidate();
        contentArea.repaint();
    }

    private void showHomePanel() {
        setActiveNav(btnDashboard, (JLabel) btnDashboard.getComponent(0));
        showPanel(buildHomePanel());
    }

    private JPanel buildHomePanel() {
        JPanel home = new JPanel(new BorderLayout());
        home.setBackground(UIConstants.BG_DARK);
        home.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // Title
        JLabel title = new JLabel("Dashboard Overview");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        home.add(title, BorderLayout.NORTH);

        // Cards row
        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        // Fetch stats
        int totalProducts = 0, totalSales = 0, lowStockCount = 0, totalUsers = 0;
        double totalRevenue = 0;
        try {
            totalProducts = controller.getProductService().getAllProducts().size();
            List<Sale> sales = controller.getSalesService().getAllSales();
            totalSales = sales.size();
            for (Sale s : sales) if ("COMPLETED".equalsIgnoreCase(s.getSaleStatus())) totalRevenue += s.getTotalAmount();
            lowStockCount = controller.getInventoryService().getLowStockItems().size();
            if (controller.isManager()) {
                totalUsers = controller.getAuthService() != null ? new com.gadgetgalaxy.dao.UserDAO().findAll().size() : 0;
            }
        } catch (Exception e) {
            System.err.println("Dashboard stats error: " + e.getMessage());
        }

        cardsRow.add(buildStatCard("Total Revenue", String.format("$%.2f", totalRevenue), UIConstants.ACCENT_TEAL, "◎"));
        cardsRow.add(buildStatCard("Total Sales", String.valueOf(totalSales), UIConstants.ACCENT_BLUE, "◈"));
        cardsRow.add(buildStatCard("Products", String.valueOf(totalProducts), UIConstants.ACCENT_PURPLE, "▣"));

        Color lowStockColor = lowStockCount > 0 ? UIConstants.ACCENT_ORANGE : UIConstants.ACCENT_TEAL;
        cardsRow.add(buildStatCard("Low Stock Alerts", String.valueOf(lowStockCount), lowStockColor, "⚠"));

        home.add(cardsRow, BorderLayout.CENTER);

        // Quick info section
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 16, 0));
        infoPanel.setOpaque(false);

        // Recent sales preview
        JPanel recentSalesPanel = buildSectionPanel("Recent Sales");
        infoPanel.add(recentSalesPanel);

        // System status
        JPanel statusPanel = buildSectionPanel("System Status");
        JPanel statusContent = new JPanel();
        statusContent.setOpaque(false);
        statusContent.setLayout(new BoxLayout(statusContent, BoxLayout.Y_AXIS));
        addStatusRow(statusContent, "Database Connection", "Connected", UIConstants.ACCENT_TEAL);
        addStatusRow(statusContent, "Clock Thread", "Running", UIConstants.ACCENT_TEAL);
        addStatusRow(statusContent, "Stock Monitor Thread", "Running", UIConstants.ACCENT_TEAL);
        addStatusRow(statusContent, "Auto-Backup Thread", "Running", UIConstants.ACCENT_TEAL);
        addStatusRow(statusContent, "Low Stock Products", lowStockCount + " items", lowStockColor);
        statusPanel.add(statusContent, BorderLayout.CENTER);
        infoPanel.add(statusPanel);

        home.add(infoPanel, BorderLayout.SOUTH);

        return home;
    }

    private JPanel buildStatCard(String title, String value, Color accentColor, String icon) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                // Accent top bar
                g2.setColor(accentColor);
                g2.fillRoundRect(0, 0, getWidth(), 4, 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        iconLabel.setForeground(accentColor);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        valueLabel.setForeground(UIConstants.TEXT_HEADER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UIConstants.FONT_SMALL);
        titleLabel.setForeground(UIConstants.TEXT_MUTED);

        card.add(iconLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        card.add(titleLabel);

        return card;
    }

    private JPanel buildSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        panel.setPreferredSize(new Dimension(0, 180));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SUBHEAD);
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(lbl, BorderLayout.NORTH);

        return panel;
    }

    private void addStatusRow(JPanel parent, String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setForeground(UIConstants.TEXT_MUTED);

        JLabel val = new JLabel("● " + value);
        val.setFont(UIConstants.FONT_BODY);
        val.setForeground(color);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        parent.add(row);
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to log out?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            controller.getAuthService().logout();
            LoginForm loginForm = new LoginForm(controller);
            loginForm.setVisible(true);
            this.dispose();
        }
    }
}
