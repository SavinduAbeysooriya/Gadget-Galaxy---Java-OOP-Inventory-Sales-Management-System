package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.BrandDAO;
import com.gadgetgalaxy.dao.CategoryDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Product management panel.
 * Demonstrates JTable, image upload, polymorphism, and LKR currency.
 */
public class ProductForm extends JPanel {

    private static final String IMAGE_DIR = "images";

    private final AppController controller;
    private JTable productTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private List<Product> currentProducts;

    public ProductForm(AppController controller) {
        this.controller = controller;
        new File(IMAGE_DIR).mkdirs();
        initUI();
        loadProducts();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel titleLabel = new JLabel("Products");
        titleLabel.setFont(UIConstants.FONT_TITLE);
        titleLabel.setForeground(UIConstants.TEXT_HEADER);
        header.add(titleLabel, BorderLayout.WEST);

        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerRight.setOpaque(false);

        searchField = new JTextField(18);
        searchField.setBackground(UIConstants.BG_INPUT);
        searchField.setForeground(UIConstants.TEXT_PRIMARY);
        searchField.setCaretColor(UIConstants.ACCENT_BLUE);
        searchField.setFont(UIConstants.FONT_BODY);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchProducts(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchProducts(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });

        JRoundedButton addBtn = new JRoundedButton("+ Add Product", UIConstants.ACCENT_BLUE, Color.WHITE);
        addBtn.setPreferredSize(new Dimension(140, 34));
        addBtn.addActionListener(e -> showProductDialog(null));

        headerRight.add(searchField);
        if (controller.isManager()) headerRight.add(addBtn);
        header.add(headerRight, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"Img", "ID", "Code", "Name", "Type", "Category", "Brand", "Price (LKR)", "Warranty"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? ImageIcon.class : Object.class; }
        };
        productTable = new JTable(tableModel);
        styleTable(productTable);
        productTable.setRowHeight(52);
        productTable.getColumnModel().getColumn(0).setPreferredWidth(56);
        productTable.getColumnModel().getColumn(0).setMaxWidth(56);
        productTable.getColumnModel().getColumn(1).setPreferredWidth(45);
        productTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        productTable.getColumnModel().getColumn(3).setPreferredWidth(190);
        productTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        productTable.getColumnModel().getColumn(7).setPreferredWidth(100);
        productTable.getColumnModel().getColumn(8).setPreferredWidth(80);

        if (controller.isManager()) {
            productTable.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() == 2 && productTable.getSelectedRow() >= 0)
                        showProductDialog(currentProducts.get(productTable.getSelectedRow()));
                }
            });
        }

        JScrollPane scroll = new JScrollPane(productTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR, 1));
        add(scroll, BorderLayout.CENTER);

        // Bottom actions
        if (controller.isManager()) {
            JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
            bottomBar.setOpaque(false);

            JRoundedButton editBtn = new JRoundedButton("✏ Edit Selected", UIConstants.ACCENT_PURPLE, Color.WHITE);
            editBtn.addActionListener(e -> {
                int row = productTable.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a product to edit."); return; }
                showProductDialog(currentProducts.get(row));
            });

            JRoundedButton delBtn = new JRoundedButton("🗑 Delete Selected", UIConstants.ACCENT_RED, Color.WHITE);
            delBtn.addActionListener(e -> {
                int row = productTable.getSelectedRow();
                if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a product to delete."); return; }
                Product p = currentProducts.get(row);
                int c = JOptionPane.showConfirmDialog(this, "Delete product: " + p.getProductName() + "?",
                        "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (c == JOptionPane.YES_OPTION) {
                    try {
                        controller.getProductService().deleteProduct(p.getProductId(), controller.getCurrentUser().getUserId());
                        JOptionPane.showMessageDialog(this, "Product deleted successfully.");
                        loadProducts();
                    } catch (DatabaseException ex) {
                        JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });

            bottomBar.add(editBtn);
            bottomBar.add(delBtn);
            add(bottomBar, BorderLayout.SOUTH);
        }
    }

    private void loadProducts() {
        try {
            currentProducts = controller.getProductService().getAllProducts();
            populateTable(currentProducts);
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) { loadProducts(); return; }
        try {
            currentProducts = controller.getProductService().searchProducts(query);
            populateTable(currentProducts);
        } catch (DatabaseException e) {
            System.err.println("Search error: " + e.getMessage());
        }
    }

    private void populateTable(List<Product> products) {
        tableModel.setRowCount(0);
        CategoryDAO catDAO = new CategoryDAO();
        BrandDAO brandDAO = new BrandDAO();
        for (Product p : products) {
            String catName = "", brandName = "";
            try {
                Category cat = catDAO.findById(p.getCategoryId());
                Brand brand = brandDAO.findById(p.getBrandId());
                catName   = cat   != null ? cat.getCategoryName()   : "N/A";
                brandName = brand != null ? brand.getBrandName() : "N/A";
            } catch (DatabaseException ignored) {}

            ImageIcon thumb = loadThumbnail(p.getImagePath(), 44, 44);
            tableModel.addRow(new Object[]{
                    thumb,
                    p.getProductId(),
                    p.getProductCode(),
                    p.getProductName(),
                    p.getProductType(),
                    catName,
                    brandName,
                    String.format("LKR %.2f", p.getUnitPrice()),
                    p.getWarrantyMonths() + " mo"
            });
        }
    }

    // ===================== PRODUCT DIALOG =====================

    private void showProductDialog(Product existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add New Product" : "Edit Product", true);
        dialog.setSize(540, 660);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;

        JTextField codeField     = dialogField(existing != null ? existing.getProductCode() : "");
        JTextField nameField     = dialogField(existing != null ? existing.getProductName() : "");
        JTextField modelField    = dialogField(existing != null ? existing.getModel() : "");
        JTextField priceField    = dialogField(existing != null ? String.valueOf(existing.getUnitPrice()) : "");
        JTextField warrantyField = dialogField(existing != null ? String.valueOf(existing.getWarrantyMonths()) : "12");
        JTextField specsField    = dialogField(existing != null ? existing.getSpecifications() : "");

        JComboBox<String> categoryCombo = new JComboBox<>();
        JComboBox<String> brandCombo    = new JComboBox<>();
        styleCombo(categoryCombo); styleCombo(brandCombo);

        List<Category> cats   = null;
        List<Brand>    brands = null;
        try {
            cats   = new CategoryDAO().findAll();
            brands = new BrandDAO().findAll();
        } catch (DatabaseException ignored) {}

        if (cats   != null) for (Category c : cats)  categoryCombo.addItem(c.getCategoryId() + ": " + c.getCategoryName());
        if (brands != null) for (Brand b   : brands) brandCombo.addItem(b.getBrandId() + ": " + b.getBrandName());

        if (existing != null) {
            for (int i = 0; i < categoryCombo.getItemCount(); i++)
                if (categoryCombo.getItemAt(i).startsWith(existing.getCategoryId() + ":")) { categoryCombo.setSelectedIndex(i); break; }
            for (int i = 0; i < brandCombo.getItemCount(); i++)
                if (brandCombo.getItemAt(i).startsWith(existing.getBrandId() + ":")) { brandCombo.setSelectedIndex(i); break; }
        }

        // Image upload
        final String[] selectedImagePath = { existing != null ? existing.getImagePath() : null };

        JLabel imagePreview = new JLabel("No image", SwingConstants.CENTER);
        imagePreview.setPreferredSize(new Dimension(100, 100));
        imagePreview.setMinimumSize(new Dimension(100, 100));
        imagePreview.setBackground(UIConstants.BG_INPUT);
        imagePreview.setForeground(UIConstants.TEXT_MUTED);
        imagePreview.setFont(UIConstants.FONT_SMALL);
        imagePreview.setOpaque(true);
        imagePreview.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        if (existing != null && existing.getImagePath() != null) {
            ImageIcon icon = loadThumbnail(existing.getImagePath(), 96, 96);
            if (icon != null) { imagePreview.setIcon(icon); imagePreview.setText(""); }
        }

        JLabel imgPathLabel = new JLabel(existing != null && existing.getImagePath() != null
                ? new File(existing.getImagePath()).getName() : "No file chosen");
        imgPathLabel.setFont(UIConstants.FONT_SMALL);
        imgPathLabel.setForeground(UIConstants.TEXT_MUTED);

        JRoundedButton chooseImgBtn = new JRoundedButton("📁 Choose Image", UIConstants.ACCENT_PURPLE, Color.WHITE);
        chooseImgBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select Product Image");
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image files (jpg, png, gif, bmp)", "jpg", "jpeg", "png", "gif", "bmp"));
            if (fc.showOpenDialog(dialog) == JFileChooser.APPROVE_OPTION) {
                File chosen = fc.getSelectedFile();
                try {
                    new File(IMAGE_DIR).mkdirs();
                    String ext = chosen.getName().substring(chosen.getName().lastIndexOf('.'));
                    String destName = System.currentTimeMillis() + ext;
                    File dest = new File(IMAGE_DIR + File.separator + destName);
                    Files.copy(chosen.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    selectedImagePath[0] = dest.getPath();
                    imgPathLabel.setText(destName);
                    ImageIcon icon = loadThumbnail(dest.getPath(), 96, 96);
                    if (icon != null) { imagePreview.setIcon(icon); imagePreview.setText(""); }
                    else { imagePreview.setIcon(null); imagePreview.setText("Preview N/A"); }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Failed to copy image: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JPanel imgRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        imgRow.setOpaque(false);
        imgRow.add(imagePreview);
        JPanel imgControls = new JPanel();
        imgControls.setOpaque(false);
        imgControls.setLayout(new BoxLayout(imgControls, BoxLayout.Y_AXIS));
        imgControls.add(chooseImgBtn);
        imgControls.add(Box.createVerticalStrut(6));
        imgControls.add(imgPathLabel);
        imgRow.add(imgControls);

        addFormRow(form, gbc, 0, "Product Code *",     codeField);
        addFormRow(form, gbc, 1, "Product Name *",     nameField);
        addFormRow(form, gbc, 2, "Model *",            modelField);
        addFormRow(form, gbc, 3, "Category *",         categoryCombo);
        addFormRow(form, gbc, 4, "Brand *",            brandCombo);
        addFormRow(form, gbc, 5, "Unit Price (LKR) *", priceField);
        addFormRow(form, gbc, 6, "Warranty (months)",  warrantyField);
        addFormRow(form, gbc, 7, "Specifications",     specsField);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel imgLbl = new JLabel("Product Image");
        imgLbl.setFont(UIConstants.FONT_SMALL);
        imgLbl.setForeground(UIConstants.TEXT_MUTED);
        form.add(imgLbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(imgRow, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnPanel.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn   = new JRoundedButton("💾 Save Product", UIConstants.ACCENT_BLUE, Color.WHITE);

        final List<Category> finalCats   = cats;
        final List<Brand>    finalBrands = brands;

        saveBtn.addActionListener(e -> {
            try {
                int catIdx   = categoryCombo.getSelectedIndex();
                int brandIdx = brandCombo.getSelectedIndex();
                if (catIdx < 0 || brandIdx < 0 || finalCats == null || finalBrands == null) {
                    JOptionPane.showMessageDialog(dialog, "Please select category and brand."); return;
                }
                int    catId    = finalCats.get(catIdx).getCategoryId();
                int    brandId  = finalBrands.get(brandIdx).getBrandId();
                double price    = Double.parseDouble(priceField.getText().trim());
                int    warranty = Integer.parseInt(warrantyField.getText().trim());
                String imgPath  = selectedImagePath[0];

                Product product;
                if (catId == 1) {
                    product = new Smartphone(codeField.getText().trim(), nameField.getText().trim(), modelField.getText().trim(),
                            catId, brandId, specsField.getText().trim(), price, warranty, imgPath,
                            controller.getCurrentUser().getUserId(), "Unknown", 0, 0);
                } else if (catId == 2) {
                    product = new Laptop(codeField.getText().trim(), nameField.getText().trim(), modelField.getText().trim(),
                            catId, brandId, specsField.getText().trim(), price, warranty, imgPath,
                            controller.getCurrentUser().getUserId(), "Unknown", 0, 0, 0.0);
                } else if (catId == 3) {
                    product = new Tablet(codeField.getText().trim(), nameField.getText().trim(), modelField.getText().trim(),
                            catId, brandId, specsField.getText().trim(), price, warranty, imgPath,
                            controller.getCurrentUser().getUserId(), "Unknown", false, 0.0);
                } else {
                    product = new Accessory(codeField.getText().trim(), nameField.getText().trim(), modelField.getText().trim(),
                            catId, brandId, specsField.getText().trim(), price, warranty, imgPath,
                            controller.getCurrentUser().getUserId(), "General", false);
                }

                if (existing != null) {
                    product.setProductId(existing.getProductId());
                    controller.getProductService().updateProduct(product, controller.getCurrentUser().getUserId());
                    JOptionPane.showMessageDialog(dialog, "Product updated successfully!");
                } else {
                    controller.getProductService().createProduct(product, controller.getCurrentUser().getUserId());
                    JOptionPane.showMessageDialog(dialog, "Product added successfully!");
                }
                dialog.dispose();
                loadProducts();
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(dialog, "Invalid number in price or warranty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (ValidationException | DatabaseException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        gbc.gridy = 9; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(16, 4, 4, 4);
        form.add(btnPanel, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    // ===================== IMAGE HELPER =====================

    private ImageIcon loadThumbnail(String path, int w, int h) {
        if (path == null || path.isEmpty()) return null;
        File f = new File(path);
        if (!f.exists()) return null;
        try {
            Image scaled = new ImageIcon(f.getAbsolutePath()).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    // ===================== HELPERS =====================

    private void addFormRow(JPanel form, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        form.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        form.add(field, gbc);
    }

    private JTextField dialogField(String val) {
        JTextField f = new JTextField(val);
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return f;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(UIConstants.BG_INPUT);
        cb.setForeground(UIConstants.TEXT_PRIMARY);
        cb.setFont(UIConstants.FONT_BODY);
    }

    public static void styleTable(JTable table) {
        table.setBackground(UIConstants.BG_DARK);
        table.setForeground(UIConstants.TEXT_PRIMARY);
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(36);
        table.setGridColor(UIConstants.BORDER_COLOR);
        table.setShowGrid(true);
        table.setSelectionBackground(new Color(40, 70, 120));
        table.setSelectionForeground(UIConstants.TEXT_HEADER);
        table.getTableHeader().setBackground(UIConstants.BG_TABLE_HDR);
        table.getTableHeader().setForeground(UIConstants.TEXT_MUTED);
        table.getTableHeader().setFont(UIConstants.FONT_SUBHEAD);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
                if (v instanceof ImageIcon) {
                    JLabel lbl = new JLabel((ImageIcon) v, SwingConstants.CENTER);
                    lbl.setOpaque(true);
                    lbl.setBackground(sel ? new Color(40, 70, 120) : (r % 2 == 0 ? UIConstants.BG_DARK : UIConstants.BG_TABLE_ROW));
                    return lbl;
                }
                Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
                comp.setBackground(sel ? new Color(40, 70, 120) : (r % 2 == 0 ? UIConstants.BG_DARK : UIConstants.BG_TABLE_ROW));
                comp.setForeground(sel ? UIConstants.TEXT_HEADER : UIConstants.TEXT_PRIMARY);
                ((JLabel) comp).setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return comp;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
    }
}
