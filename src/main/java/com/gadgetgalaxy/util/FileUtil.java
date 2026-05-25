package com.gadgetgalaxy.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles operations related to File I/O: system logs, reports exports, and backups.
 * Demonstrates try-with-resources, File streams, and BufferedWriter.
 */
public final class FileUtil {

    private static final String LOG_DIR = "logs";
    private static final String BACKUP_DIR = "backups";
    private static final String REPORT_DIR = "reports";
    private static final String LOG_FILE = LOG_DIR + "/system_audit.log";

    static {
        // Ensure necessary directories exist
        try {
            Files.createDirectories(Paths.get(LOG_DIR));
            Files.createDirectories(Paths.get(BACKUP_DIR));
            Files.createDirectories(Paths.get(REPORT_DIR));
        } catch (IOException e) {
            System.err.println("Could not initialize directories: " + e.getMessage());
        }
    }

    private FileUtil() {} // Private constructor

    /**
     * Appends an audit log message to the log file.
     */
    public static synchronized void logAction(String username, String action) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] User: %s | Action: %s\n", timestamp, username, action);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(entry);
        } catch (IOException e) {
            System.err.println("Failed to write to audit log file: " + e.getMessage());
        }
    }

    /**
     * Exports arbitrary tabular data to a CSV report file.
     */
    public static void exportToCSV(List<String[]> data, String filename) throws IOException {
        String filepath = REPORT_DIR + "/" + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            for (String[] row : data) {
                // Join row cells with comma, wrapping cells in quotes to handle inner commas
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    String cell = row[i] == null ? "" : row[i].replace("\"", "\"\"");
                    line.append("\"").append(cell).append("\"");
                    if (i < row.length - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    /**
     * Backs up database data (represented as list of CSV rows) to the backup directory.
     */
    public static void saveBackup(String tableName, List<String[]> data) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = String.format("%s/%s_backup_%s.csv", BACKUP_DIR, tableName, timestamp);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (String[] row : data) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    String cell = row[i] == null ? "" : row[i].replace("\"", "\"\"");
                    line.append("\"").append(cell).append("\"");
                    if (i < row.length - 1) {
                        line.append(",");
                    }
                }
                writer.write(line.toString());
                writer.newLine();
            }
        }
    }

    /**
     * Writes full text content to a specific file. Useful for invoices.
     */
    public static void writeTextFile(String directory, String filename, String content) throws IOException {
        Files.createDirectories(Paths.get(directory));
        String filepath = directory + "/" + filename;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write(content);
        }
    }
}
