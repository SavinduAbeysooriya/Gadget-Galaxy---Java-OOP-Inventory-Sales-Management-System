package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.ProductDAO;
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
 * Demonstrates cart management, customer registration, and invoice generation.
 */
public class SalesForm extends JPanel {

    private final AppController controller;

    // Cart state
    private final List<SaleItem> cartItems = new ArrayList<>();
    private final List<Product> cartProducts = new ArrayList<>();

    // Cart table
    private JTable cartTable;
    private DefaultTableModel cartModel;

    // Customer fields
    private JTextField custNameField, custPhoneField, custEmailField;

    // Product search
    private JTextField productSearchField;
    private JComboBox<String> productCombo;
    private JTextField quantityField;
    private List<Product> availableProducts = new ArrayList<>();

    // Totals
    private JLabel totalLabel;

    // Sales history
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public SalesForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadProducts();
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

        // Main split: left=POS, right=history
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildPOSPanel(), buildHistoryPanel());
        split.setDividerLocation(640);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildPOSPanel() {
        JPanel pos = new JPanel(new BorderLayout(0, 12));
        pos.setBackground(UIConstants.BG_DARK);

        // ===== PRODUCT SEARCH & ADD =====
        JPanel addItemPanel = createCard("Add Items to Cart");

        JPanel prodRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        prodRow.setOpaque(false);

        productCombo = new JComboBox<>();
        productCombo.setBackground(UIConstants.BG_INPUT);
        productCombo.setForeground(UIConstants.TEXT_PRIMARY);
        productCombo.setFont(UIConstants.FONT_BODY);
        productCombo.setPreferredSize(new Dimension(280, 32));

        quantityField = new JTextField("1", 5);
        quantityField.setBackground(UIConstants.BG_INPUT);
        quantityField.setForeground(UIConstants.TEXT_PRIMARY);
        quantityField.setCaretColor(UIConstants.ACCENT_BLUE);
        quantityField.setFont(UIConstants.FONT_BODY);
        quantityField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));

        JRoundedButton addToCartBtn = new JRoundedButton("Add to Cart", UIConstants.ACCENT_BLUE, Color.WHITE);
        addToCartBtn.addActionListener(e -> addToCart());

        prodRow.add(new JLabel("Product:") {{ setForeground(UIConstants.TEXT_MUTED); setFont(UIConstants.FONT_BODY); }});
        prodRow.add(productCombo);
        prodRow.add(new JLabel("Qty:") {{ setForeground(UIConstants.TEXT_MUTED); setFont(UIConstants.FONT_BODY); }});
        prodRow.add(quantityField);
        prodRow.add(addToCartBtn);
        addItemPanel.add(prodRow, BorderLayout.CENTER);

        pos.add(addItemPanel, BorderLayout.NORTH);

        // ===== CART TABLE =====
        JPanel cartPanel = createCard("Shopping Cart");
        String[] cartCols = {"Product", "Qty", "Unit Price", "Subtotal"};
        cartModel = new DefaultTableModel(cartCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        ProductForm.styleTable(cartTable);
        cartTable.setPreferredScrollableViewportSize(new Dimension(580, 160));

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

        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        totalLabel.setForeground(UIConstants.ACCENT_TEAL);
        totalLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 4, 0));

        cartPanel.add(cartScroll, BorderLayout.CENTER);
        cartPanel.add(cartBtns, BorderLayout.EAST);
        cartPanel.add(totalLabel, BorderLayout.SOUTH);

        pos.add(cartPanel, BorderLayout.CENTER);

        // ===== CUSTOMER + CHECKOUT =====
        JPanel checkoutPanel = createCard("Customer & Checkout");
        checkoutPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        custNameField = styledField("Customer Name (optional)");
        custPhoneField = styledField("Phone Number (required if named)");
        custEmailField = styledField("Email");

        String[] payMethods = {"CASH", "CARD", "MOBILE"};
        JComboBox<String> payCombo = new JComboBox<>(payMethods);
        payCombo.setBackground(UIConstants.BG_INPUT);
        payCombo.setForeground(UIConstants.TEXT_PRIMARY);
        payCombo.setFont(UIConstants.FONT_BODY);

        addRow(checkoutPanel, gbc, 0, "Name:", custNameField);
        addRow(checkoutPanel, gbc, 1, "Phone:", custPhoneField);
        addRow(checkoutPanel, gbc, 2, "Email:", custEmailField);
        addRow(checkoutPanel, gbc, 3, "Payment:", payCombo);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 6, 4, 6);
        JRoundedButton checkoutBtn = new JRoundedButton("Complete Checkout & Generate Invoice", UIConstants.ACCENT_TEAL, Color.WHITE);
        checkoutBtn.addActionListener(e -> {
            String method = (String) payCombo.getSelectedItem();
            doCheckout(method);
        });
        checkoutPanel.add(checkoutBtn, gbc);

        pos.add(checkoutPanel, BorderLayout.SOUTH);

        return pos;
    }

    private JPanel buildHistoryPanel() {
        JPanel historyCard = createCard("Sales History");

        String[] cols = {"Invoice", "Date", "Customer", "Total", "Payment", "Status"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyModel);
        ProductForm.styleTable(historyTable);
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(130);
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        historyCard.add(scroll, BorderLayout.CENTER);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnRow.setOpaque(false);
        JRoundedButton refreshBtn = new JRoundedButton("Refresh History", UIConstants.ACCENT_PURPLE, Color.WHITE);
        refreshBtn.addActionListener(e -> loadSalesHistory());
        btnRow.add(refreshBtn);
        historyCard.add(btnRow, BorderLayout.SOUTH);

        return historyCard;
    }

    private void loadProducts() {
        try {
            availableProducts = controller.getProductService().getAllProducts();
            productCombo.removeAllItems();
            for (Product p : availableProducts) {
                productCombo.addItem(p.getProductId() + " | " + p.getProductCode() + " – " + p.getProductName() + " ($" + p.getUnitPrice() + ")");
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSalesHistory() {
        try {
            List<Sale> sales = controller.getSalesService().getAllSales();
            historyModel.setRowCount(0);
            for (Sale s : sales) {
                String custName = "Walk-in";
                if (s.getCustomerId() != null) {
                    try {
                        Customer c = new com.gadgetgalaxy.dao.CustomerDAO().findById(s.getCustomerId());
                        if (c != null) custName = c.getCustomerName();
                    } catch (DatabaseException e) { /* ignore */ }
                }
                String date = s.getSaleDate() != null ?
                        s.getSaleDate().toLocalDate().toString() + " " + s.getSaleDate().toLocalTime().toString().substring(0, 5) : "N/A";
                historyModel.addRow(new Object[]{
                        s.getInvoiceNo(),
                        date,
                        custName,
                        String.format("$%.2f", s.getTotalAmount()),
                        s.getPaymentMethod(),
                        s.getSaleStatus()
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load sales history: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

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
        // Check if already in cart — update quantity
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getProductId() == p.getProductId()) {
                SaleItem existing = cartItems.get(i);
                existing.setQuantity(existing.getQuantity() + qty);
                updateCartTable();
                return;
            }
        }

        SaleItem item = new SaleItem(0, p.getProductId(), qty, p.getUnitPrice());
        cartItems.add(item);
        cartProducts.add(p);
        updateCartTable();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row < 0 || row >= cartItems.size()) {
            JOptionPane.showMessageDialog(this, "Select an item from the cart to remove.");
            return;
        }
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
                    p.getProductName(),
                    item.getQuantity(),
                    String.format("$%.2f", item.getUnitPrice()),
                    String.format("$%.2f", item.getSubtotal())
            });
            total += item.getSubtotal();
        }
        totalLabel.setText(String.format("Grand Total:  $%.2f", total));
    }

    private void doCheckout(String paymentMethod) {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add products before checking out.", "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Customer customer = null;
        String custName = custNameField.getText().trim();
        if (!custName.isEmpty()) {
            customer = new Customer(custName,
                    custPhoneField.getText().trim(),
                    custEmailField.getText().trim(),
                    "");
        }

        try {
            String invoice = controller.getSalesService().processCheckout(
                    customer, cartItems, paymentMethod, controller.getCurrentUser().getUserId()
            );
            // Show invoice
            JTextArea invoiceArea = new JTextArea(invoice, 28, 50);
            invoiceArea.setEditable(false);
            invoiceArea.setFont(UIConstants.FONT_MONO);
            invoiceArea.setBackground(UIConstants.BG_CARD);
            invoiceArea.setForeground(UIConstants.TEXT_PRIMARY);
            JScrollPane sp = new JScrollPane(invoiceArea);

            JOptionPane.showMessageDialog(this, sp, "Sale Completed – Invoice", JOptionPane.INFORMATION_MESSAGE);

            clearCart();
            custNameField.setText("");
            custPhoneField.setText("");
            custEmailField.setText("");
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

    // ===== HELPERS =====

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

    private JTextField styledField(String hint) {
        JTextField f = new JTextField();
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return f;
    }

    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }
}
