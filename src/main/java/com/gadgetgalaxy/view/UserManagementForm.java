package com.gadgetgalaxy.view;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.dao.UserDAO;
import com.gadgetgalaxy.exception.DatabaseException;
import com.gadgetgalaxy.exception.ValidationException;
import com.gadgetgalaxy.model.SalesRepresentative;
import com.gadgetgalaxy.model.StoreManager;
import com.gadgetgalaxy.model.User;
import com.gadgetgalaxy.service.AuthenticationService;
import com.gadgetgalaxy.util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * User management panel – only accessible by Store Managers.
 * Demonstrates role-based access control, user CRUD, and password hashing.
 */
public class UserManagementForm extends JPanel {

    private final AppController controller;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private List<User> currentUsers;

    public UserManagementForm(AppController controller) {
        this.controller = controller;
        initUI();
        loadUsers();
    }

    private void initUI() {
        setBackground(UIConstants.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JLabel title = new JLabel("User Management");
        title.setFont(UIConstants.FONT_TITLE);
        title.setForeground(UIConstants.TEXT_HEADER);
        header.add(title, BorderLayout.WEST);

        JRoundedButton addUserBtn = new JRoundedButton("+ Add User", UIConstants.ACCENT_BLUE, Color.WHITE);
        addUserBtn.addActionListener(e -> showUserDialog(null));
        header.add(addUserBtn, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Full Name", "Username", "Email", "Phone", "Role", "Status", "Created"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        userTable = new JTable(tableModel);
        ProductForm.styleTable(userTable);
        userTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        userTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        userTable.getColumnModel().getColumn(7).setPreferredWidth(120);

        JScrollPane scroll = new JScrollPane(userTable);
        scroll.getViewport().setBackground(UIConstants.BG_DARK);
        scroll.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        add(scroll, BorderLayout.CENTER);

        // Bottom actions
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setOpaque(false);

        JRoundedButton editBtn = new JRoundedButton("Edit User", UIConstants.ACCENT_PURPLE, Color.WHITE);
        editBtn.addActionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user to edit."); return; }
            showUserDialog(currentUsers.get(row));
        });

        JRoundedButton toggleBtn = new JRoundedButton("Toggle Status", UIConstants.ACCENT_ORANGE, Color.WHITE);
        toggleBtn.addActionListener(e -> toggleUserStatus());

        JRoundedButton deleteBtn = new JRoundedButton("Delete User", UIConstants.ACCENT_RED, Color.WHITE);
        deleteBtn.addActionListener(e -> deleteUser());

        bottom.add(editBtn);
        bottom.add(toggleBtn);
        bottom.add(deleteBtn);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadUsers() {
        try {
            currentUsers = new UserDAO().findAll();
            tableModel.setRowCount(0);
            for (User u : currentUsers) {
                tableModel.addRow(new Object[]{
                        u.getUserId(),
                        u.getFullName(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getPhone() != null ? u.getPhone() : "–",
                        u.getRoleName(),
                        u.getStatus(),
                        u.getCreatedAt() != null ? u.getCreatedAt().toLocalDate().toString() : "N/A"
                });
            }
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed to load users: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showUserDialog(User existing) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                existing == null ? "Add New User" : "Edit User", true);
        dialog.setSize(420, 470);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UIConstants.BG_CARD);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIConstants.BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 4, 5, 4);
        gbc.weightx = 1.0;

        JTextField fullNameField = dField(existing != null ? existing.getFullName() : "");
        JTextField usernameField = dField(existing != null ? existing.getUsername() : "");
        JPasswordField passwordField = new JPasswordField();
        stylePass(passwordField);
        JTextField emailField = dField(existing != null ? existing.getEmail() : "");
        JTextField phoneField = dField(existing != null ? (existing.getPhone() != null ? existing.getPhone() : "") : "");

        String[] roles = {"Store Manager", "Sales Representative"};
        JComboBox<String> roleCombo = new JComboBox<>(roles);
        roleCombo.setBackground(UIConstants.BG_INPUT);
        roleCombo.setForeground(UIConstants.TEXT_PRIMARY);
        roleCombo.setFont(UIConstants.FONT_BODY);
        if (existing != null) roleCombo.setSelectedIndex(existing.getRoleId() - 1);

        String[] statuses = {"ACTIVE", "INACTIVE"};
        JComboBox<String> statusCombo = new JComboBox<>(statuses);
        statusCombo.setBackground(UIConstants.BG_INPUT);
        statusCombo.setForeground(UIConstants.TEXT_PRIMARY);
        statusCombo.setFont(UIConstants.FONT_BODY);
        if (existing != null) statusCombo.setSelectedItem(existing.getStatus());

        JLabel passNote = new JLabel(existing != null ? "(leave blank to keep current)" : "(min. 6 characters)");
        passNote.setFont(UIConstants.FONT_SMALL);
        passNote.setForeground(UIConstants.TEXT_MUTED);

        addFRow(form, gbc, 0, "Full Name *", fullNameField);
        addFRow(form, gbc, 1, "Username *", usernameField);
        addFRow(form, gbc, 2, "Password *", passwordField);
        gbc.gridy = 3; gbc.gridx = 1; gbc.gridwidth = 1;
        form.add(passNote, gbc);
        addFRow(form, gbc, 4, "Email *", emailField);
        addFRow(form, gbc, 5, "Phone", phoneField);
        addFRow(form, gbc, 6, "Role", roleCombo);
        addFRow(form, gbc, 7, "Status", statusCombo);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JRoundedButton cancelBtn = new JRoundedButton("Cancel", UIConstants.BG_INPUT, UIConstants.TEXT_MUTED);
        JRoundedButton saveBtn = new JRoundedButton("Save User", UIConstants.ACCENT_BLUE, Color.WHITE);

        saveBtn.addActionListener(ev -> {
            try {
                ValidationUtil.validateNotEmpty(fullNameField.getText(), "Full Name");
                ValidationUtil.validateNotEmpty(usernameField.getText(), "Username");
                ValidationUtil.validateEmail(emailField.getText().trim());

                String pwdRaw = new String(passwordField.getPassword());
                String hash;
                if (existing != null && pwdRaw.isEmpty()) {
                    hash = existing.getPasswordHash(); // Keep existing hash
                } else {
                    ValidationUtil.validatePasswordStrength(pwdRaw);
                    hash = AuthenticationService.hashPassword(pwdRaw);
                }

                int roleId = roleCombo.getSelectedIndex() + 1;
                String status = (String) statusCombo.getSelectedItem();

                User user;
                if (roleId == 1) {
                    user = new StoreManager(fullNameField.getText().trim(), usernameField.getText().trim(),
                            hash, emailField.getText().trim(), phoneField.getText().trim(), status);
                } else {
                    user = new SalesRepresentative(fullNameField.getText().trim(), usernameField.getText().trim(),
                            hash, emailField.getText().trim(), phoneField.getText().trim(), status);
                }

                UserDAO dao = new UserDAO();
                if (existing != null) {
                    user.setUserId(existing.getUserId());
                    dao.update(user);
                    JOptionPane.showMessageDialog(dialog, "User updated successfully!");
                } else {
                    dao.insert(user);
                    JOptionPane.showMessageDialog(dialog, "User created successfully!");
                }
                controller.logAction((existing != null ? "Updated" : "Created") + " user: " + user.getUsername());
                dialog.dispose();
                loadUsers();
            } catch (ValidationException | DatabaseException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        cancelBtn.addActionListener(ev -> dialog.dispose());
        btnRow.add(cancelBtn);
        btnRow.add(saveBtn);

        gbc.gridy = 8; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(14, 4, 4, 4);
        form.add(btnRow, gbc);

        dialog.add(new JScrollPane(form));
        dialog.setVisible(true);
    }

    private void toggleUserStatus() {
        int row = userTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
        User u = currentUsers.get(row);
        if (u.getUserId() == controller.getCurrentUser().getUserId()) {
            JOptionPane.showMessageDialog(this, "Cannot toggle your own account status.", "Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newStatus = "ACTIVE".equals(u.getStatus()) ? "INACTIVE" : "ACTIVE";
        u.setStatus(newStatus);
        try {
            new UserDAO().update(u);
            controller.logAction("Toggled user status to " + newStatus + " for: " + u.getUsername());
            JOptionPane.showMessageDialog(this, "User status set to " + newStatus + ".");
            loadUsers();
        } catch (DatabaseException e) {
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user."); return; }
        User u = currentUsers.get(row);
        if (u.getUserId() == controller.getCurrentUser().getUserId()) {
            JOptionPane.showMessageDialog(this, "Cannot delete your own account.", "Restricted", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete user: " + u.getFullName() + " (" + u.getUsername() + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                new UserDAO().delete(u.getUserId());
                controller.logAction("Deleted user: " + u.getUsername());
                JOptionPane.showMessageDialog(this, "User deleted.");
                loadUsers();
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

    private void stylePass(JPasswordField f) {
        f.setBackground(UIConstants.BG_INPUT);
        f.setForeground(UIConstants.TEXT_PRIMARY);
        f.setCaretColor(UIConstants.ACCENT_BLUE);
        f.setFont(UIConstants.FONT_BODY);
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    private void addFRow(JPanel p, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridy = row; gbc.gridx = 0; gbc.gridwidth = 1; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UIConstants.FONT_SMALL);
        lbl.setForeground(UIConstants.TEXT_MUTED);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        p.add(field, gbc);
    }
}
