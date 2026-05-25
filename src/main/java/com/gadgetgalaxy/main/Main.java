package com.gadgetgalaxy.main;

import com.gadgetgalaxy.controller.AppController;
import com.gadgetgalaxy.util.DBConnection;
import com.gadgetgalaxy.view.LoginForm;

import javax.swing.*;

/**
 * Application Entry Point for Gadget Galaxy Inventory & Sales Management System.
 *
 * Demonstrates:
 *   - static keyword (main method)
 *   - Swing Look & Feel configuration
 *   - Service wiring via AppController
 *   - SwingUtilities.invokeLater for thread-safe GUI startup
 */
public class Main {

    public static void main(String[] args) {
        // Configure a modern UI look
        configureUI();

        // Start the application on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AppController controller = new AppController();

            LoginForm loginForm = new LoginForm(controller);
            loginForm.setVisible(true);

            // Register shutdown hook to cleanly close DB connections
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down Gadget Galaxy...");
                DBConnection.shutdown();
            }));
        });
    }

    private static void configureUI() {
        try {
            // Use system look and feel as base, then override with our custom theme
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());

            // Global Swing defaults
            UIManager.put("Panel.background",              new java.awt.Color(12, 14, 20));
            UIManager.put("OptionPane.background",         new java.awt.Color(22, 26, 38));
            UIManager.put("OptionPane.messageForeground",  new java.awt.Color(220, 225, 240));
            UIManager.put("Button.background",             new java.awt.Color(64, 156, 255));
            UIManager.put("Button.foreground",             java.awt.Color.WHITE);
            UIManager.put("TextField.background",          new java.awt.Color(30, 35, 50));
            UIManager.put("TextField.foreground",          new java.awt.Color(220, 225, 240));
            UIManager.put("TextField.caretForeground",     new java.awt.Color(64, 156, 255));
            UIManager.put("PasswordField.background",      new java.awt.Color(30, 35, 50));
            UIManager.put("PasswordField.foreground",      new java.awt.Color(220, 225, 240));
            UIManager.put("ComboBox.background",           new java.awt.Color(30, 35, 50));
            UIManager.put("ComboBox.foreground",           new java.awt.Color(220, 225, 240));
            UIManager.put("Table.background",              new java.awt.Color(12, 14, 20));
            UIManager.put("Table.foreground",              new java.awt.Color(220, 225, 240));
            UIManager.put("TableHeader.background",        new java.awt.Color(18, 22, 35));
            UIManager.put("TableHeader.foreground",        new java.awt.Color(120, 130, 160));
            UIManager.put("ScrollPane.background",         new java.awt.Color(12, 14, 20));
            UIManager.put("Viewport.background",           new java.awt.Color(12, 14, 20));
            UIManager.put("ScrollBar.thumb",               new java.awt.Color(50, 60, 90));
            UIManager.put("ScrollBar.track",               new java.awt.Color(22, 26, 38));
            UIManager.put("SplitPane.background",          new java.awt.Color(12, 14, 20));
            UIManager.put("SplitPaneDivider.background",   new java.awt.Color(40, 48, 70));

        } catch (Exception e) {
            System.err.println("Failed to configure UI look and feel: " + e.getMessage());
        }
    }
}
