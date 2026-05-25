package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.BrandDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.model.Brand;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Brand management panel – only accessible by Store Managers.
 * Provides full CRUD for product brands.
 */
public class BrandManagementForm extends JPanel {

    private final AppController controller;
    private final BrandDAO brandDAO = new BrandDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Brand> brands;

    public BrandManagementForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadBrands();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("Brand Management");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        header.add(title, BorderLayout.WEST);

        JRoundedButton addBtn = new JRoundedButton("+ Add Brand", UIConstants.ACCENT_BLUE, Color.WHITE);
        addBtn.addActionListener(e -> showDialog(null));
        header.add(addBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Brand Name", "Country"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ProductForm.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);

        JRoundedButton editBtn = new JRoundedButton("Edit", UIConstants.ACCENT_PURPLE, Color.WHITE);
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a brand to edit."); return; }
            showDialog(brands.get(row));
        });

        JRoundedButton deleteBtn = new JRoundedButton("Delete", UIConstants.ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteBrand());

        bottom.add(editBtn);
        bottom.add(deleteBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadBrands() {
        try {
            brands = brandDAO.findAll();
            tableModel.setRowCount(0);
            for (Brand b : brands) {
                tableModel.addRow(new Object[]{
                        b.getBrandId(),
                        b.getBrandName(),
                        b.getCountry() != null ? b.getCountry() : ""
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load brands: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDialog(Brand existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add Brand" : "Edit Brand", true);
        dialog.setSize(400, 230);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.weightx = 1.0;

        JTextField nameField = dField(existing != null ? existing.getBrandName() : "");
        JTextField countryField = dField(existing != null ? (existing.getCountry() != null ? existing.getCountry() : "") : "");

        addRow(form, gbc, 0, "Brand Name *", nameField);
        addRow(form, gbc, 1, "Country", countryField);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn = new JRoundedButton("Save", UIConstants.ACCENT_BLUE, Color.WHITE);

        saveBtn.addActionListener(ev -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Brand name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                if (existing == null) {
                    Brand brand = new Brand(name, countryField.getText().trim());
                    brandDAO.insert(brand);
                    controller.logAction("Added brand: " + name);
                    JOptionPane.showMessageDialog(dialog, "Brand added successfully!");
                } else {
                    existing.setBrandName(name);
                    existing.setCountry(countryField.getText().trim());
                    brandDAO.update(existing);
                    controller.logAction("Updated brand: " + name);
                    JOptionPane.showMessageDialog(dialog, "Brand updated successfully!");
                }
                dialog.dispose();
                loadBrands();
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

    private void deleteBrand() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a brand to delete."); return; }
        Brand brand = brands.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete brand \"" + brand.getBrandName() + "\"?\nProducts using this brand will be affected.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                brandDAO.delete(brand.getBrandId());
                controller.logAction("Deleted brand: " + brand.getBrandName());
                JOptionPane.showMessageDialog(this, "Brand deleted.");
                loadBrands();
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
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return f;
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
