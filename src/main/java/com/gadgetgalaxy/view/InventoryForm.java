package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.CategoryDAO;
import com.gadgetgalaxy.dao.ProductDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.Category;
import com.gadgetgalaxy.model.Inventory;
import com.gadgetgalaxy.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Inventory management panel.
 * Shows all products with stock levels, low-stock highlighting, and update dialogs.
 */
public class InventoryForm extends JPanel {

    private final AppController controller;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Inventory> currentInventory;

    public InventoryForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadInventory();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ===== HEADER =====
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Inventory");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        header.add(title, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setBackground(UIConstants.BG_INPUT);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);
        searchField.setCaretColor(UIConstants.ACCENT_BLUE);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JRoundedButton refreshBtn = new JRoundedButton("Refresh", UIConstants.ACCENT_PURPLE, Color.WHITE);
        refreshBtn.addActionListener(e -> loadInventory());

        headerRight.add(new JLabel("Search: ") {{
            setForeground(UIConstants.TEXT_MUTED); setFont(UIConstants.FONT_BODY);
        }});
        headerRight.add(searchField);
        headerRight.add(refreshBtn);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ===== TABLE =====
        String[] cols = {"Inv. ID", "Product Code", "Product Name", "Category", "In Stock", "Reorder Level", "Status", "Last Updated"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        ProductForm.styleTable(inventoryTable);
        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // Check if this row is low stock
                if (!isSelected && row < currentInventory.size()) {
                    Inventory inv = currentInventory.get(row);
                    if (inv.isLowStock()) {
                        c.setBackground(new Color(50, 30, 20));
                        c.setForeground(UIConstants.ACCENT_ORANGE);
                    } else {
                        c.setBackground(row % 2 == 0 ? UIConstants.BG_DARK : UIConstants.BG_TABLE_ROW);
                        c.setForeground(UIConstants.TEXT_PRIMARY);
                    }
                } else if (isSelected) {
                    c.setBackground(new Color(40, 70, 120));
                    c.setForeground(UIConstants.TEXT_HEADER);
                }
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(inventoryTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);

        // ===== BOTTOM BAR =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottom.setOpaque(false);

        JRoundedButton addStockBtn = new JRoundedButton("+ Add Stock", UIConstants.ACCENT_TEAL, Color.WHITE);
        addStockBtn.addActionListener(e -> showAddStockDialog());

        if (controller.isManager()) {
            JRoundedButton reorderBtn = new JRoundedButton("Set Reorder Level", UIConstants.ACCENT_PURPLE, Color.WHITE);
            reorderBtn.addActionListener(e -> showReorderDialog());
            bottom.add(addStockBtn);
            bottom.add(reorderBtn);
        } else {
            bottom.add(addStockBtn);
        }

        JLabel hint = new JLabel("  ⚠ Rows highlighted in orange are below reorder level.");
        hint.setFont(UIConstants.FONT_SMALL);
        hint.setForeground(UIConstants.ACCENT_ORANGE);
        bottom.add(hint);

        add(bottom, BorderLayout.SOUTH);
    }

    private void loadInventory() {
        try {
            currentInventory = controller.getInventoryService().getAllInventory();
            populateTable(currentInventory);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            loadInventory();
            return;
        }
        tableModel.setRowCount(0);
        if (currentInventory != null) {
            for (Inventory inv : currentInventory) {
                // Check if the table already has this row
            }
        }
        // Re-populate filtered
        try {
            List<Product> found = controller.getProductService().searchProducts(query);
            tableModel.setRowCount(0);
            for (Product p : found) {
                Inventory inv = controller.getInventoryService().getInventoryByProductId(p.getProductId());
                if (inv != null) {
                    addInventoryRow(inv, p);
                }
            }
        } catch (DatabaseException e) {
            System.err.println("Filter error: " + e.getMessage());
        }
    }

    private void populateTable(List<Inventory> inventoryList) {
        tableModel.setRowCount(0);
        ProductDAO prodDAO = new ProductDAO();
        CategoryDAO catDAO = new CategoryDAO();
        for (Inventory inv : inventoryList) {
            try {
                Product p = prodDAO.findById(inv.getProductId());
                if (p != null) addInventoryRow(inv, p);
            } catch (DatabaseException e) { /* skip */ }
        }
    }

    private void addInventoryRow(Inventory inv, Product p) {
        String catName = "";
        try {
            Category cat = new CategoryDAO().findById(p.getCategoryId());
            catName = cat != null ? cat.getCategoryName() : "N/A";
        } catch (DatabaseException e) { catName = "N/A"; }

        String status = inv.isLowStock() ? "⚠ LOW STOCK" : "✓ OK";
        String lastUpdate = inv.getLastStockUpdate() != null ?
                inv.getLastStockUpdate().toLocalDate().toString() : "N/A";

        tableModel.addRow(new Object[]{
                inv.getInventoryId(),
                p.getProductCode(),
                p.getProductName(),
                catName,
                inv.getQuantityInStock(),
                inv.getReorderLevel(),
                status,
                lastUpdate
        });
    }

    private void showAddStockDialog() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0 || row >= currentInventory.size()) {
            JOptionPane.showMessageDialog(this, "Please select a product from the inventory table.");
            return;
        }
        Inventory inv = currentInventory.get(row);
        String productName = "Unknown";
        try {
            Product p = new ProductDAO().findById(inv.getProductId());
            if (p != null) productName = p.getProductName();
        } catch (DatabaseException e) { /* ignore */ }

        String input = JOptionPane.showInputDialog(this,
                "Add stock units for: " + productName + "\n(Current stock: " + inv.getQuantityInStock() + ")",
                "Add Stock", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int qty = Integer.parseInt(input.trim());
            controller.getInventoryService().addStock(inv.getProductId(), qty, controller.getCurrentUser().getUserId());
            JOptionPane.showMessageDialog(this, qty + " units added successfully!");
            loadInventory();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer quantity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showReorderDialog() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0 || row >= currentInventory.size()) {
            JOptionPane.showMessageDialog(this, "Please select a product from the inventory table.");
            return;
        }
        Inventory inv = currentInventory.get(row);
        String input = JOptionPane.showInputDialog(this,
                "Set reorder level (current: " + inv.getReorderLevel() + "):",
                "Reorder Level", JOptionPane.QUESTION_MESSAGE);
        if (input == null || input.trim().isEmpty()) return;

        try {
            int level = Integer.parseInt(input.trim());
            controller.getInventoryService().updateReorderLevel(inv.getProductId(), level, controller.getCurrentUser().getUserId());
            JOptionPane.showMessageDialog(this, "Reorder level updated to " + level + ".");
            loadInventory();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
