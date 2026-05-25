package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.CustomerDAO;
import com.gadgetgalaxy.dao.SalesDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.InsufficientStockException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Sales creation and history panel.
 * Supports selecting existing customers, manually adding new ones,
 * invoice viewing, sale deletion, and LKR currency.
 */
public class SalesForm extends JPanel {

    private final AppController controller;
    private final CustomerDAO customerDAO = new CustomerDAO();

    // Cart state
    private final List<SaleItem> cartItems = new ArrayList<>();
    private final List<Product> cartProducts = new ArrayList<>();

    // Cart table
    private JTable cartTable;
    private DefaultTableModel cartModel;

    // Customer section
    private JComboBox<String> customerCombo;
    private List<Customer> customerList = new ArrayList<>();
    private JTextField custNameField, custPhoneField, custEmailField;
    private JPanel manualPanel;
    private JRadioButton rbExisting, rbManual, rbWalkin;

    // Product
    private JComboBox<String> productCombo;
    private JTextField quantityField;
    private List<Product> availableProducts = new ArrayList<>();

    // Totals
    private JLabel totalLabel;

    // Sales history
    private JTable historyTable;
    private DefaultTableModel historyModel;
    private List<Sale> currentSales = new ArrayList<>();

    public SalesForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadProducts();
        loadCustomers();
        loadSalesHistory();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Sales / Point of Sale");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildPOSPanel(), buildHistoryPanel());
        split.setDividerLocation(660);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    // ===================== POS PANEL =====================

    private JPanel buildPOSPanel() {
        JPanel pos = new JPanel(new BorderLayout(0, 10));
        pos.setBackground(UIConstants.BG_DARK);

        // Top: product add row
        JPanel addItemPanel = createCard("Add Items to Cart");
        JPanel prodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        prodRow.setOpaque(false);

        productCombo = new JComboBox<>();
        productCombo.setBackground(UIConstants.BG_INPUT);
        productCombo.setForeground(UIConstants.TEXT_PRIMARY);
        productCombo.setFont(UIConstants.FONT_BODY);
        productCombo.setPreferredSize(new Dimension(300, 32));

        quantityField = new JTextField("1", 5);
        styleField(quantityField);

        JRoundedButton addToCartBtn = new JRoundedButton("Add to Cart", UIConstants.ACCENT_BLUE, Color.WHITE);
        addToCartBtn.addActionListener(e -> addToCart());

        prodRow.add(label("Product:"));
        prodRow.add(productCombo);
        prodRow.add(label("Qty:"));
        prodRow.add(quantityField);
        prodRow.add(addToCartBtn);
        addItemPanel.add(prodRow, BorderLayout.CENTER);
        pos.add(addItemPanel, BorderLayout.NORTH);

        // Center: cart table
        JPanel cartPanel = createCard("Shopping Cart");
        String[] cartCols = {"Product", "Qty", "Unit Price (LKR)", "Subtotal (LKR)"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        ProductForm.styleTable(cartTable);
        cartTable.setPreferredScrollableViewportSize(new Dimension(580, 150));

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.getViewport().setBackground(UIConstants.BG_DARK);
        cartScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        JPanel cartBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        cartBtns.setOpaque(false);
        JRoundedButton removeBtn = new JRoundedButton("Remove Item", UIConstants.ACCENT_RED, Color.WHITE);
        removeBtn.addActionListener(e -> removeFromCart());
        JRoundedButton clearBtn = new JRoundedButton("Clear Cart", new Color(50, 50, 70), UIConstants.TEXT_MUTED);
        clearBtn.addActionListener(e -> clearCart());
        cartBtns.add(removeBtn);
        cartBtns.add(clearBtn);

        totalLabel = new JLabel("Total: LKR 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(UIConstants.ACCENT_TEAL);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 4, 0));

        cartPanel.add(cartScroll, BorderLayout.CENTER);
        cartPanel.add(cartBtns, BorderLayout.EAST);
        cartPanel.add(totalLabel, BorderLayout.SOUTH);
        pos.add(cartPanel, BorderLayout.CENTER);

        // Bottom: customer + checkout
        pos.add(buildCheckoutPanel(), BorderLayout.SOUTH);

        return pos;
    }

    private JPanel buildCheckoutPanel() {
        JPanel card = createCard("Customer & Checkout");
        card.setLayout(new BorderLayout(0, 8));

        // Radio buttons for customer mode
        rbWalkin   = new JRadioButton("Walk-in (No Customer)");
        rbExisting = new JRadioButton("Select Existing Customer");
        rbManual   = new JRadioButton("Add New Customer");
        styleRadio(rbWalkin);
        styleRadio(rbExisting);
        styleRadio(rbManual);
        rbWalkin.setSelected(true);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbWalkin); bg.add(rbExisting); bg.add(rbManual);

        JPanel radioRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        radioRow.setOpaque(false);
        radioRow.add(rbWalkin);
        radioRow.add(rbExisting);
        radioRow.add(rbManual);
        card.add(radioRow, BorderLayout.NORTH);

        // Existing customer selector
        JPanel existingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        existingPanel.setOpaque(false);
        customerCombo = new JComboBox<>();
        customerCombo.setBackground(UIConstants.BG_INPUT);
        customerCombo.setForeground(UIConstants.TEXT_PRIMARY);
        customerCombo.setFont(UIConstants.FONT_BODY);
        customerCombo.setPreferredSize(new Dimension(320, 30));
        existingPanel.add(label("Customer:"));
        existingPanel.add(customerCombo);
        existingPanel.setVisible(false);

        // Manual customer fields
        manualPanel = new JPanel(new GridBagLayout());
        manualPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 6, 3, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        custNameField  = styledField();
        custPhoneField = styledField();
        custEmailField = styledField();
        addGbcRow(manualPanel, gbc, 0, "Name *:", custNameField);
        addGbcRow(manualPanel, gbc, 1, "Phone *:", custPhoneField);
        addGbcRow(manualPanel, gbc, 2, "Email:", custEmailField);
        manualPanel.setVisible(false);

        JPanel dynamicArea = new JPanel(new BorderLayout());
        dynamicArea.setOpaque(false);
        dynamicArea.add(existingPanel, BorderLayout.NORTH);
        dynamicArea.add(manualPanel, BorderLayout.CENTER);
        card.add(dynamicArea, BorderLayout.CENTER);

        // Radio listeners
        rbWalkin.addActionListener(e -> { existingPanel.setVisible(false); manualPanel.setVisible(false); });
        rbExisting.addActionListener(e -> { existingPanel.setVisible(true); manualPanel.setVisible(false); });
        rbManual.addActionListener(e -> { existingPanel.setVisible(false); manualPanel.setVisible(true); });

        // Payment + checkout button
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bottomRow.setOpaque(false);
        String[] payMethods = {"CASH", "CARD", "MOBILE"};
        JComboBox<String> payCombo = new JComboBox<>(payMethods);
        payCombo.setBackground(UIConstants.BG_INPUT);
        payCombo.setForeground(UIConstants.TEXT_PRIMARY);
        payCombo.setFont(UIConstants.FONT_BODY);
        payCombo.setPreferredSize(new Dimension(120, 30));

        JRoundedButton checkoutBtn = new JRoundedButton("✅ Complete Checkout & Generate Invoice", UIConstants.ACCENT_TEAL, Color.WHITE);
        checkoutBtn.addActionListener(e -> doCheckout((String) payCombo.getSelectedItem()));

        bottomRow.add(label("Payment:"));
        bottomRow.add(payCombo);
        bottomRow.add(checkoutBtn);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    // ===================== HISTORY PANEL =====================

    private JPanel buildHistoryPanel() {
        JPanel historyCard = createCard("Sales History");

        String[] cols = {"Invoice", "Date", "Customer", "Total (LKR)", "Payment", "Status"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        ProductForm.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        historyCard.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setOpaque(false);

        JRoundedButton viewInvoiceBtn = new JRoundedButton("🧾 View Invoice", UIConstants.ACCENT_BLUE, Color.WHITE);
        viewInvoiceBtn.addActionListener(e -> viewInvoice());

        JRoundedButton deleteBtn = new JRoundedButton("🗑 Delete Sale", UIConstants.ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSale());

        JRoundedButton refreshBtn = new JRoundedButton("🔄 Refresh", UIConstants.ACCENT_PURPLE, Color.WHITE);
        refreshBtn.addActionListener(e -> loadSalesHistory());

        btnRow.add(viewInvoiceBtn);
        btnRow.add(deleteBtn);
        btnRow.add(refreshBtn);
        historyCard.add(btnRow, BorderLayout.SOUTH);

        return historyCard;
    }

    // ===================== DATA LOADING =====================

    private void loadProducts() {
        try {
            availableProducts = controller.getProductService().getAllProducts();
            productCombo.removeAllItems();
            for (Product p : availableProducts) {
                productCombo.addItem(p.getProductCode() + " – " + p.getProductName() + " (LKR " + String.format("%.2f", p.getUnitPrice()) + ")");
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadCustomers() {
        try {
            customerList = customerDAO.findAll();
            customerCombo.removeAllItems();
            for (Customer c : customerList) {
                customerCombo.addItem(c.getCustomerName() + " (" + c.getPhone() + ")");
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesHistory() {
        try {
            currentSales = controller.getSalesService().getAllSales();
            historyModel.setRowCount(0);
            for (Sale s : currentSales) {
                String custName = "Walk-in";
                if (s.getCustomerId() != null) {
                    try {
                        Customer c = customerDAO.findById(s.getCustomerId());
                        if (c != null) custName = c.getCustomerName();
                    } catch (DatabaseException ignored) {}
                }
                String date = s.getSaleDate() != null ?
                        s.getSaleDate().toLocalDate() + " " + s.getSaleDate().toLocalTime().toString().substring(0, 5) : "N/A";
                historyModel.addRow(new Object[]{
                        s.getInvoiceNo(), date, custName,
                        String.format("LKR %.2f", s.getTotalAmount()),
                        s.getPaymentMethod(), s.getSaleStatus()
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load sales history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== CART ACTIONS =====================

    private void addToCart() {
        int idx = productCombo.getSelectedIndex();
        if (idx < 0 || idx >= availableProducts.size()) {
            JOptionPane.showMessageDialog(this, "Please select a product.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid positive quantity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Product p = availableProducts.get(idx);
        for (SaleItem existing : cartItems) {
            if (existing.getProductId() == p.getProductId()) {
                existing.setQuantity(existing.getQuantity() + qty);
                updateCartTable();
                return;
            }
        }
        cartItems.add(new SaleItem(0, p.getProductId(), qty, p.getUnitPrice()));
        cartProducts.add(p);
        updateCartTable();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to remove."); return; }
        cartItems.remove(row);
        cartProducts.remove(row);
        updateCartTable();
    }

    private void clearCart() {
        cartItems.clear();
        cartProducts.clear();
        updateCartTable();
    }

    private void updateCartTable() {
        cartModel.setRowCount(0);
        double total = 0.0;
        for (int i = 0; i < cartItems.size(); i++) {
            SaleItem item = cartItems.get(i);
            Product p = cartProducts.get(i);
            cartModel.addRow(new Object[]{
                    p.getProductName(), item.getQuantity(),
                    String.format("LKR %.2f", item.getUnitPrice()),
                    String.format("LKR %.2f", item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        totalLabel.setText(String.format("Grand Total:  LKR %.2f", total));
    }

    // ===================== CHECKOUT =====================

    private void doCheckout(String paymentMethod) {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer customer = null;

        if (rbExisting.isSelected()) {
            int idx = customerCombo.getSelectedIndex();
            if (idx >= 0 && idx < customerList.size()) {
                customer = customerList.get(idx);
            }
        } else if (rbManual.isSelected()) {
            String name = custNameField.getText().trim();
            String phone = custPhoneField.getText().trim();
            if (name.isEmpty() || phone.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Customer name and phone are required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            customer = new Customer(name, phone, custEmailField.getText().trim(), "");
        }

        try {
            String invoice = controller.getSalesService().processCheckout(
                    customer, cartItems, paymentMethod, controller.getCurrentUser().getUserId()
            );
            showInvoiceDialog(invoice);
            clearCart();
            custNameField.setText(""); custPhoneField.setText(""); custEmailField.setText("");
            loadCustomers();
            loadSalesHistory();
        } catch (ValidationException | DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Checkout failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (InsufficientStockException e) {
            JOptionPane.showMessageDialog(this,
                    "Insufficient stock!\n" + e.getMessage() +
                    "\nAvailable: " + e.getAvailableStock() + " | Requested: " + e.getRequestedQty(),
                    "Stock Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===================== INVOICE VIEW =====================

    private void viewInvoice() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a sale to view its invoice."); return; }
        Sale sale = currentSales.get(row);
        try {
            List<SaleItem> items = controller.getSalesService().getSaleItems(sale.getSaleId());
            Customer customer = sale.getCustomerId() != null ? customerDAO.findById(sale.getCustomerId()) : null;

            java.util.Map<Integer, String> productNames = new java.util.HashMap<>();
            for (SaleItem item : items) {
                try {
                    Product p = new com.gadgetgalaxy.dao.ProductDAO().findById(item.getProductId());
                    if (p != null) productNames.put(item.getProductId(), p.getProductName());
                } catch (DatabaseException ignored) {}
            }
            com.gadgetgalaxy.model.User soldBy = new com.gadgetgalaxy.dao.UserDAO().findById(sale.getSoldBy());
            String invoiceText = com.gadgetgalaxy.util.InvoiceGenerator.generateInvoice(sale, items, customer, soldBy, productNames);
            showInvoiceDialog(invoiceText);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load invoice: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showInvoiceDialog(String invoiceText) {
        JTextArea area = new JTextArea(invoiceText, 30, 55);
        area.setEditable(false);
        area.setFont(UIConstants.FONT_MONO);
        area.setBackground(UIConstants.BG_CARD);
        area.setForeground(UIConstants.TEXT_PRIMARY);
        JScrollPane sp = new JScrollPane(area);
        JOptionPane.showMessageDialog(this, sp, "Invoice", JOptionPane.INFORMATION_MESSAGE);
    }

    // ===================== DELETE SALE =====================

    private void deleteSale() {
        if (!controller.isManager()) {
            JOptionPane.showMessageDialog(this, "Only Store Managers can delete sales.", "Access Denied", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int row = historyTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a sale to delete."); return; }
        Sale sale = currentSales.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete sale " + sale.getInvoiceNo() + "?\nThis will restore inventory stock.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                new SalesDAO().delete(sale.getSaleId());
                controller.logAction("Deleted sale: " + sale.getInvoiceNo());
                JOptionPane.showMessageDialog(this, "Sale deleted successfully.");
                loadSalesHistory();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===================== HELPERS =====================

    private JPanel createCard(String title) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
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

    private JTextField styledField() {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
    }

    private void styleRadio(JRadioButton rb) {
        rb.setOpaque(false);
        rb.setForeground(UIConstants.TEXT_MUTED);
        rb.setFont(UIConstants.FONT_BODY);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(UIConstants.TEXT_MUTED);
        l.setFont(UIConstants.FONT_BODY);
        return l;
    }

    private void addGbcRow(JPanel p, GridBagConstraints gbc, int row, String labelText, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(field, gbc);
    }
}
