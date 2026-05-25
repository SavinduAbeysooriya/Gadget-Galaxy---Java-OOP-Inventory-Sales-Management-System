package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.CustomerDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Customers panel – visible to all roles.
 * Managers can add, edit, and delete. Sales reps view only.
 */
public class CustomersForm extends JPanel {

    private final AppController controller;
    private final CustomerDAO customerDAO = new CustomerDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Customer> customers;
    private JTextField searchField;

    public CustomersForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadCustomers();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    // ===================== HEADER =====================

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel titleIcon = new JLabel("\uD83D\uDC64");
        titleIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        titleIcon.setForeground(UIConstants.TEXT_HEADER);
        JLabel titleText = new JLabel(" Customers");
        titleText.setFont(UIConstants.FONT_TITLE);
        titleText.setForeground(UIConstants.TEXT_HEADER);
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titlePanel.setOpaque(false);
        titlePanel.add(titleIcon);
        titlePanel.add(titleText);
        header.add(titlePanel, BorderLayout.WEST);

        // Search bar + add button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightPanel.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setBackground(UIConstants.BG_INPUT);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);
        searchField.setCaretColor(UIConstants.ACCENT_BLUE);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filterTable(); }
        });

        JLabel searchLbl = new JLabel("🔍 Search:");
        searchLbl.setForeground(UIConstants.TEXT_MUTED);
        searchLbl.setFont(UIConstants.FONT_BODY);

        rightPanel.add(searchLbl);
        rightPanel.add(searchField);

        if (controller.isManager()) {
            JRoundedButton addBtn = new JRoundedButton("+ Add Customer", UIConstants.ACCENT_BLUE, Color.WHITE);
            addBtn.addActionListener(e -> showDialog(null));
            rightPanel.add(addBtn);
        }

        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ===================== TABLE =====================

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Name", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ProductForm.styleTable(table);
        table.setRowHeight(28);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(2).setPreferredWidth(120);
        table.getColumnModel().getColumn(3).setPreferredWidth(190);
        table.getColumnModel().getColumn(4).setPreferredWidth(260);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Double-click to edit (manager only)
        if (controller.isManager()) {
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        int row = table.getSelectedRow();
                        if (row >= 0) showDialog(customers.get(table.convertRowIndexToModel(row)));
                    }
                }
            });
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        return scroll;
    }

    // ===================== FOOTER =====================

    private JPanel buildFooter() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        // Row count label
        JLabel countLabel = new JLabel();
        countLabel.setFont(UIConstants.FONT_SMALL);
        countLabel.setForeground(UIConstants.TEXT_MUTED);
        bottom.add(countLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        JRoundedButton refreshBtn = new JRoundedButton("🔄 Refresh", new Color(40, 50, 75), UIConstants.TEXT_MUTED);
        refreshBtn.addActionListener(e -> loadCustomers());
        btnPanel.add(refreshBtn);

        if (controller.isManager()) {
            JRoundedButton editBtn = new JRoundedButton("✏ Edit", UIConstants.ACCENT_PURPLE, Color.WHITE);
            editBtn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Select a customer to edit."); return; }
                showDialog(customers.get(table.convertRowIndexToModel(row)));
            });

            JRoundedButton deleteBtn = new JRoundedButton("🗑 Delete", UIConstants.ACCENT_RED, Color.WHITE);
            deleteBtn.addActionListener(e -> deleteCustomer());

            btnPanel.add(editBtn);
            btnPanel.add(deleteBtn);
        }

        bottom.add(btnPanel, BorderLayout.EAST);

        // Update count after table loads
        tableModel.addTableModelListener(e -> countLabel.setText("Total: " + tableModel.getRowCount() + " customers"));

        return bottom;
    }

    // ===================== LOAD =====================

    private void loadCustomers() {
        try {
            customers = customerDAO.findAll();
            tableModel.setRowCount(0);
            for (Customer c : customers) {
                tableModel.addRow(new Object[]{
                        c.getCustomerId(),
                        c.getCustomerName(),
                        nvl(c.getPhone()),
                        nvl(c.getEmail()),
                        nvl(c.getAddress())
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load customers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filterTable() {
        String text = searchField.getText().trim();
        if (text.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
        }
    }

    // ===================== DIALOG =====================

    private void showDialog(Customer existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Customer" : "Edit Customer", true);
        dialog.setSize(460, 380);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        JTextField nameField  = dField(existing != null ? existing.getCustomerName() : "");
        JTextField phoneField = dField(existing != null ? nvl(existing.getPhone()) : "");
        JTextField emailField = dField(existing != null ? nvl(existing.getEmail()) : "");

        // Address as multi-line JTextArea
        JTextArea addressArea = new JTextArea(existing != null ? nvl(existing.getAddress()) : "", 3, 20);
        addressArea.setBackground(UIConstants.BG_INPUT);
        addressArea.setForeground(UIConstants.TEXT_PRIMARY);
        addressArea.setCaretColor(UIConstants.ACCENT_BLUE);
        addressArea.setFont(UIConstants.FONT_BODY);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addressScroll = new JScrollPane(addressArea);
        addressScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        addressScroll.setPreferredSize(new Dimension(0, 70));

        addRow(form, gbc, 0, "Full Name *", nameField);
        addRow(form, gbc, 1, "Phone *", phoneField);
        addRow(form, gbc, 2, "Email", emailField);

        // Address label + area (label top-aligned)
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel addrLbl = new JLabel("Address");
        addrLbl.setFont(UIConstants.FONT_SMALL);
        addrLbl.setForeground(UIConstants.TEXT_MUTED);
        form.add(addrLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        form.add(addressScroll, gbc);

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn   = new JRoundedButton("💾 Save Customer", UIConstants.ACCENT_BLUE, Color.WHITE);

        saveBtn.addActionListener(ev -> {
            String name  = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Full name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (phone.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Phone number is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                String email   = emailField.getText().trim();
                String address = addressArea.getText().trim();
                if (existing == null) {
                    Customer c = new Customer(name, phone, email, address);
                    customerDAO.insert(c);
                    controller.logAction("Added customer: " + name);
                    JOptionPane.showMessageDialog(dialog, "Customer added successfully!");
                } else {
                    existing.setCustomerName(name);
                    existing.setPhone(phone);
                    existing.setEmail(email);
                    existing.setAddress(address);
                    customerDAO.update(existing);
                    controller.logAction("Updated customer: " + name);
                    JOptionPane.showMessageDialog(dialog, "Customer updated successfully!");
                }
                dialog.dispose();
                loadCustomers();
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 4, 4, 4);
        form.add(btnRow, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    // ===================== DELETE =====================

    private void deleteCustomer() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a customer to delete."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        Customer c = customers.get(modelRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete customer \"" + c.getCustomerName() + "\" (" + c.getPhone() + ")?\n" +
                "This will not delete their past sales records.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                customerDAO.delete(c.getCustomerId());
                controller.logAction("Deleted customer: " + c.getCustomerName());
                JOptionPane.showMessageDialog(this, "Customer deleted successfully.");
                loadCustomers();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, "Failed to delete: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ===================== HELPERS =====================

    private JTextField dField(String val) {
        JTextField f = new JTextField(val);
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return f;
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        p.add(field, gbc);
    }

    private String nvl(String s) { return s != null ? s : ""; }
}
