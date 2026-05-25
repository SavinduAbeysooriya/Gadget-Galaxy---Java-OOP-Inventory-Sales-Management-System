package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.ProductSupplierDAO;
import com.gadgetgalaxy.dao.ProductDAO;
import com.gadgetgalaxy.dao.SupplierDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Product;
import com.gadgetgalaxy.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Product-Supplier mapping management panel – admin only.
 * Manages the many-to-many relationship between products and suppliers.
 */
public class ProductSupplierForm extends JPanel {

    private final AppController controller;
    private final ProductSupplierDAO psDAO = new ProductSupplierDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final SupplierDAO supplierDAO = new SupplierDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<int[]> mappings;   // each int[]{id, productId, supplierId}
    private List<Product> products;
    private List<Supplier> suppliers;
    private JTextField searchField;

    public ProductSupplierForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadData();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel titleIcon = new JLabel("\uD83D\uDD17");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        titleIcon.setForeground(UIConstants.TEXT_HEADER);
        JLabel titleText = new JLabel(" Product\u2013Supplier Links");
        titleText.setFont(UIConstants.FONT_TITLE);
        titleText.setForeground(UIConstants.TEXT_HEADER);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(titleIcon);
        titlePanel.add(titleText);
        header.add(titlePanel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        searchField = new JTextField(16);
        searchField.setBackground(UIConstants.BG_INPUT);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);
        searchField.setCaretColor(UIConstants.ACCENT_BLUE);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JLabel searchLbl = new JLabel("🔍");
        searchLbl.setForeground(UIConstants.TEXT_MUTED);
        right.add(searchLbl);
        right.add(searchField);

        JRoundedButton addBtn = new JRoundedButton("+ Add Link", UIConstants.ACCENT_BLUE, Color.WHITE);
        addBtn.addActionListener(e -> showDialog());
        right.add(addBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"Link ID", "Product Name", "Supplier Name"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ProductForm.styleTable(table);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(65);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setPreferredWidth(260);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        return scroll;
    }

    private JPanel buildFooter() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JLabel countLbl = new JLabel();
        countLbl.setFont(UIConstants.FONT_SMALL);
        countLbl.setForeground(UIConstants.TEXT_MUTED);
        tableModel.addTableModelListener(e -> countLbl.setText("Total: " + tableModel.getRowCount() + " links"));
        bottom.add(countLbl, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JRoundedButton refreshBtn = new JRoundedButton("🔄 Refresh", new Color(40, 50, 75), UIConstants.TEXT_MUTED);
        refreshBtn.addActionListener(e -> loadData());

        JRoundedButton deleteBtn = new JRoundedButton("🗑 Remove Link", UIConstants.ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteLink());

        btns.add(refreshBtn);
        btns.add(deleteBtn);
        bottom.add(btns, BorderLayout.EAST);
        return bottom;
    }

    private void loadData() {
        try {
            products  = productDAO.findAll();
            suppliers = supplierDAO.findAll();
            mappings  = psDAO.findAll();
            tableModel.setRowCount(0);
            for (int[] row : mappings) {
                String productName  = findProductName(row[1]);
                String supplierName = findSupplierName(row[2]);
            tableModel.addRow(new Object[]{ row[0], productName, supplierName });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filter() {
        String t = searchField.getText().trim();
        sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
    }

    private void showDialog() {
        if (products == null || products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No products available. Add products first."); return;
        }
        if (suppliers == null || suppliers.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No suppliers available. Add suppliers first."); return;
        }

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add Product–Supplier Link", true);
        dialog.setSize(440, 220);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 4, 8, 4);
        gbc.weightx = 1.0;

        JComboBox<String> productCombo  = new JComboBox<>();
        JComboBox<String> supplierCombo = new JComboBox<>();
        styleCombo(productCombo);
        styleCombo(supplierCombo);

        for (Product p : products) productCombo.addItem(p.getProductName());

        // Holds the currently available (unlinked) suppliers for the selected product
        java.util.List<Supplier> availableSuppliers = new java.util.ArrayList<>();

        Runnable refreshSuppliers = () -> {
            supplierCombo.removeAllItems();
            availableSuppliers.clear();
            int pIdx = productCombo.getSelectedIndex();
            if (pIdx < 0 || pIdx >= products.size()) return;
            int productId = products.get(pIdx).getProductId();
            try {
                java.util.List<Integer> linked = psDAO.findSupplierIdsByProduct(productId);
                for (Supplier s : suppliers) {
                    if (!linked.contains(s.getSupplierId())) {
                        availableSuppliers.add(s);
                        supplierCombo.addItem(s.getSupplierName());
                    }
                }
                if (availableSuppliers.isEmpty()) {
                    supplierCombo.addItem("— All suppliers already linked —");
                }
            } catch (DatabaseException ex) {
                System.err.println("Supplier filter error: " + ex.getMessage());
            }
        };

        productCombo.addActionListener(e -> refreshSuppliers.run());
        refreshSuppliers.run();

        addRow(form, gbc, 0, "Product *",  productCombo);
        addRow(form, gbc, 1, "Supplier *", supplierCombo);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn   = new JRoundedButton("🔗 Add Link", UIConstants.ACCENT_BLUE, Color.WHITE);

        saveBtn.addActionListener(ev -> {
            int pIdx = productCombo.getSelectedIndex();
            int sIdx = supplierCombo.getSelectedIndex();
            if (pIdx < 0 || sIdx < 0 || availableSuppliers.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "No available suppliers for this product.", "Cannot Add", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (sIdx >= availableSuppliers.size()) {
                JOptionPane.showMessageDialog(dialog, "Select a valid supplier."); return;
            }
            int productId  = products.get(pIdx).getProductId();
            int supplierId = availableSuppliers.get(sIdx).getSupplierId();
            try {
                psDAO.insert(productId, supplierId);
                controller.logAction("Added product-supplier link: " + findProductName(productId) + " → " + findSupplierName(supplierId));
                JOptionPane.showMessageDialog(dialog, "Link added successfully!");
                dialog.dispose();
                loadData();
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 4, 4, 4);
        form.add(btnRow, gbc);

        dialog.add(form);
        dialog.setVisible(true);
    }

    private void deleteLink() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a link to remove."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int[] mapping = mappings.get(modelRow);
        String productName  = findProductName(mapping[1]);
        String supplierName = findSupplierName(mapping[2]);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Remove link between:\n  Product:  " + productName + "\n  Supplier: " + supplierName + "?",
                "Confirm Remove", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                psDAO.deleteById(mapping[0]);
                controller.logAction("Removed product-supplier link ID: " + mapping[0]);
                JOptionPane.showMessageDialog(this, "Link removed successfully.");
                loadData();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String findProductName(int productId) {
        if (products == null) return "ID:" + productId;
        for (Product p : products) if (p.getProductId() == productId) return p.getProductName();
        return "ID:" + productId;
    }

    private String findSupplierName(int supplierId) {
        if (suppliers == null) return "ID:" + supplierId;
        for (Supplier s : suppliers) if (s.getSupplierId() == supplierId) return s.getSupplierName();
        return "ID:" + supplierId;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(UIConstants.BG_INPUT);
        cb.setForeground(UIConstants.TEXT_PRIMARY);
        cb.setFont(UIConstants.FONT_BODY);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(field, gbc);
    }
}
