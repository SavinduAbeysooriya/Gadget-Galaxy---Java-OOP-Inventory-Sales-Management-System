package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.SupplierDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Supplier management panel – admin only. Full CRUD.
 */
public class SupplierManagementForm extends JPanel {

    private final AppController controller;
    private final SupplierDAO supplierDAO = new SupplierDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private List<Supplier> suppliers;
    private JTextField searchField;

    public SupplierManagementForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadSuppliers();
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

        JLabel title = new JLabel("🏭 Suppliers");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        header.add(title, BorderLayout.WEST);

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

        JRoundedButton addBtn = new JRoundedButton("+ Add Supplier", UIConstants.ACCENT_BLUE, Color.WHITE);
        addBtn.addActionListener(e -> showDialog(null));
        right.add(addBtn);

        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JScrollPane buildTable() {
        String[] cols = {"ID", "Supplier Name", "Contact Person", "Phone", "Email", "Address"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ProductForm.styleTable(table);
        table.setRowHeight(30);
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(140);
        table.getColumnModel().getColumn(3).setPreferredWidth(110);
        table.getColumnModel().getColumn(4).setPreferredWidth(170);
        table.getColumnModel().getColumn(5).setPreferredWidth(220);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0)
                    showDialog(suppliers.get(table.convertRowIndexToModel(table.getSelectedRow())));
            }
        });

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
        tableModel.addTableModelListener(e -> countLbl.setText("Total: " + tableModel.getRowCount() + " suppliers"));
        bottom.add(countLbl, BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);

        JRoundedButton refreshBtn = new JRoundedButton("🔄 Refresh", new Color(40, 50, 75), UIConstants.TEXT_MUTED);
        refreshBtn.addActionListener(e -> loadSuppliers());

        JRoundedButton editBtn = new JRoundedButton("✏ Edit", UIConstants.ACCENT_PURPLE, Color.WHITE);
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a supplier to edit."); return; }
            showDialog(suppliers.get(table.convertRowIndexToModel(row)));
        });

        JRoundedButton deleteBtn = new JRoundedButton("🗑 Delete", UIConstants.ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSupplier());

        btns.add(refreshBtn);
        btns.add(editBtn);
        btns.add(deleteBtn);
        bottom.add(btns, BorderLayout.EAST);
        return bottom;
    }

    private void loadSuppliers() {
        try {
            suppliers = supplierDAO.findAll();
            tableModel.setRowCount(0);
            for (Supplier s : suppliers) {
                tableModel.addRow(new Object[]{
                        s.getSupplierId(), s.getSupplierName(), nvl(s.getContactPerson()),
                        nvl(s.getPhone()), nvl(s.getEmail()), nvl(s.getAddress())
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load suppliers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void filter() {
        String t = searchField.getText().trim();
        sorter.setRowFilter(t.isEmpty() ? null : RowFilter.regexFilter("(?i)" + t));
    }

    private void showDialog(Supplier existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Supplier" : "Edit Supplier", true);
        dialog.setSize(460, 400);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        JTextField nameField    = dField(existing != null ? existing.getSupplierName() : "");
        JTextField contactField = dField(existing != null ? nvl(existing.getContactPerson()) : "");
        JTextField phoneField   = dField(existing != null ? nvl(existing.getPhone()) : "");
        JTextField emailField   = dField(existing != null ? nvl(existing.getEmail()) : "");

        JTextArea addressArea = new JTextArea(existing != null ? nvl(existing.getAddress()) : "", 3, 20);
        addressArea.setBackground(UIConstants.BG_INPUT);
        addressArea.setForeground(UIConstants.TEXT_PRIMARY);
        addressArea.setCaretColor(UIConstants.ACCENT_BLUE);
        addressArea.setFont(UIConstants.FONT_BODY);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addrScroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        addrScroll.setPreferredSize(new Dimension(0, 65));

        addRow(form, gbc, 0, "Supplier Name *", nameField);
        addRow(form, gbc, 1, "Contact Person", contactField);
        addRow(form, gbc, 2, "Phone *", phoneField);
        addRow(form, gbc, 3, "Email", emailField);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel addrLbl = new JLabel("Address");
        addrLbl.setFont(UIConstants.FONT_SMALL);
        addrLbl.setForeground(UIConstants.TEXT_MUTED);
        form.add(addrLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.CENTER;
        form.add(addrScroll, gbc);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn   = new JRoundedButton("💾 Save Supplier", UIConstants.ACCENT_BLUE, Color.WHITE);

        saveBtn.addActionListener(ev -> {
            String name  = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            if (name.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Supplier name is required.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            if (phone.isEmpty()) { JOptionPane.showMessageDialog(dialog, "Phone is required.", "Validation", JOptionPane.WARNING_MESSAGE); return; }
            try {
                if (existing == null) {
                    Supplier s = new Supplier(name, contactField.getText().trim(), phone, emailField.getText().trim(), addressArea.getText().trim());
                    supplierDAO.insert(s);
                    controller.logAction("Added supplier: " + name);
                    JOptionPane.showMessageDialog(dialog, "Supplier added successfully!");
                } else {
                    existing.setSupplierName(name);
                    existing.setContactPerson(contactField.getText().trim());
                    existing.setPhone(phone);
                    existing.setEmail(emailField.getText().trim());
                    existing.setAddress(addressArea.getText().trim());
                    supplierDAO.update(existing);
                    controller.logAction("Updated supplier: " + name);
                    JOptionPane.showMessageDialog(dialog, "Supplier updated successfully!");
                }
                dialog.dispose();
                loadSuppliers();
            } catch (DatabaseException ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(14, 4, 4, 4);
        form.add(btnRow, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void deleteSupplier() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a supplier to delete."); return; }
        Supplier s = suppliers.get(table.convertRowIndexToModel(viewRow));
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete supplier \"" + s.getSupplierName() + "\"?\nAll product-supplier links will also be removed.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                supplierDAO.delete(s.getSupplierId());
                controller.logAction("Deleted supplier: " + s.getSupplierName());
                JOptionPane.showMessageDialog(this, "Supplier deleted.");
                loadSuppliers();
            } catch (DatabaseException e) {
                JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

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
