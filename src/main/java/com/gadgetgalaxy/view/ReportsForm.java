package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.exception.DatabaseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Reports panel for Store Managers.
 * Shows revenue summaries, audit logs, and export controls.
 */
public class ReportsForm extends JPanel {

    private final AppController controller;
    private JTable auditTable;
    private DefaultTableModel auditModel;

    public ReportsForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadData();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel title = new JLabel("Reports & Analytics");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        // Main split
        JPanel mainContent = new JPanel(new BorderLayout(0, 16));
        mainContent.setOpaque(false);

        // Stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 14, 0));
        statsRow.setOpaque(false);
        statsRow.setPreferredSize(new Dimension(0, 110));

        // Will be populated later in loadData()
        statsRow.setName("statsRow");
        mainContent.add(statsRow, BorderLayout.NORTH);

        // Audit log table
        JPanel auditCard = createCard("System Audit Log");
        String[] cols = {"Log ID", "Username", "Action", "Timestamp"};
        auditModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        auditTable = new JTable(auditModel);
        ProductForm.styleTable(auditTable);
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(350);
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(140);

        JScrollPane scroll = new JScrollPane(auditTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        auditCard.add(scroll, BorderLayout.CENTER);
        mainContent.add(auditCard, BorderLayout.CENTER);

        // Export buttons
        JPanel exportPanel = createCard("Export Reports");
        exportPanel.setPreferredSize(new Dimension(0, 90));
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        btnRow.setOpaque(false);

        JRoundedButton exportSalesBtn = new JRoundedButton("Export Sales CSV", UIConstants.ACCENT_TEAL, Color.WHITE);
        exportSalesBtn.addActionListener(e -> exportSales());

        JRoundedButton exportInvBtn = new JRoundedButton("Export Inventory CSV", UIConstants.ACCENT_BLUE, Color.WHITE);
        exportInvBtn.addActionListener(e -> exportInventory());

        JRoundedButton exportAuditBtn = new JRoundedButton("Export Audit Log CSV", UIConstants.ACCENT_PURPLE, Color.WHITE);
        exportAuditBtn.addActionListener(e -> exportAuditLog());

        JRoundedButton backupBtn = new JRoundedButton("Run Manual Backup", UIConstants.ACCENT_ORANGE, Color.WHITE);
        backupBtn.addActionListener(e -> runBackup());

        btnRow.add(exportSalesBtn);
        btnRow.add(exportInvBtn);
        btnRow.add(exportAuditBtn);
        btnRow.add(backupBtn);
        exportPanel.add(btnRow, BorderLayout.CENTER);
        mainContent.add(exportPanel, BorderLayout.SOUTH);

        add(mainContent, BorderLayout.CENTER);
    }

    private void loadData() {
        // Load stats
        try {
            Map<String, Object> summary = controller.getReportService().getSalesSummary();
            double revenue = (double) summary.get("totalRevenue");
            int completed = (int) summary.get("completedCount");
            int cancelled = (int) summary.get("cancelledCount");
            double avgVal = (double) summary.get("avgOrderValue");

            JPanel statsRow = (JPanel) ((JPanel) getComponent(1)).getComponent(0);
            statsRow.removeAll();
            statsRow.add(buildStatCard("Total Revenue", String.format("LKR %.2f", revenue), UIConstants.ACCENT_TEAL));
            statsRow.add(buildStatCard("Completed Sales", String.valueOf(completed), UIConstants.ACCENT_BLUE));
            statsRow.add(buildStatCard("Cancelled Sales", String.valueOf(cancelled), UIConstants.ACCENT_RED));
            statsRow.add(buildStatCard("Avg. Order Value", String.format("LKR %.2f", avgVal), UIConstants.ACCENT_PURPLE));
            statsRow.revalidate();
            statsRow.repaint();
        } catch (DatabaseException e) {
            System.err.println("Report stats error: " + e.getMessage());
        }

        // Load audit logs
        try {
            List<String[]> logs = controller.getAuditLogDAO().findAllLogs();
            auditModel.setRowCount(0);
            for (String[] row : logs) {
                auditModel.addRow(row);
            }
        } catch (DatabaseException e) {
            System.err.println("Audit log load error: " + e.getMessage());
        }
    }

    private JPanel buildStatCard(String title, String value, Color color) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(color);
                g2.fillRect(0, 0, 4, getHeight());
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 14));

        JLabel valLbl = new JLabel(value);
        valLbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        valLbl.setForeground(UIConstants.TEXT_HEADER);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(UIConstants.FONT_SMALL);
        titleLbl.setForeground(UIConstants.TEXT_MUTED);

        card.add(valLbl);
        card.add(Box.createVerticalStrut(4));
        card.add(titleLbl);

        return card;
    }

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIConstants.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JLabel lbl = new JLabel(title);
        lbl.setFont(UIConstants.FONT_SUBHEAD);
        lbl.setForeground(UIConstants.ACCENT_BLUE);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        card.add(lbl, BorderLayout.NORTH);
        return card;
    }

    private void exportSales() {
        try {
            controller.getReportService().exportSalesReport();
            JOptionPane.showMessageDialog(this, "Sales report exported to reports/sales_report.csv", "Export OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException | IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportInventory() {
        try {
            controller.getReportService().exportInventoryReport();
            JOptionPane.showMessageDialog(this, "Inventory report exported to reports/inventory_report.csv", "Export OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException | IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void exportAuditLog() {
        try {
            controller.getReportService().exportAuditLogsReport();
            JOptionPane.showMessageDialog(this, "Audit log exported to reports/audit_log_report.csv", "Export OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException | IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void runBackup() {
        try {
            controller.getInventoryService().backupInventory();
            JOptionPane.showMessageDialog(this, "Backup saved to backups/ directory.", "Backup OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Backup failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
