-- SQL Schema for Gadget Galaxy Inventory & Sales Management System
-- MySQL Compatible

CREATE DATABASE IF NOT EXISTS gadget_galaxy_db;
USE gadget_galaxy_db;

-- 1. Roles table
CREATE TABLE IF NOT EXISTS roles (
    role_id INT AUTO_INCREMENT PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Users table
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, -- SHA-256 produces 255-char hex string
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    role_id INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
    FOREIGN KEY (role_id) REFERENCES roles(role_id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Categories table
CREATE TABLE IF NOT EXISTS categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Brands table
CREATE TABLE IF NOT EXISTS brands (
    brand_id INT AUTO_INCREMENT PRIMARY KEY,
    brand_name VARCHAR(50) NOT NULL UNIQUE,
    country VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Products table
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_code VARCHAR(50) NOT NULL UNIQUE,
    product_name VARCHAR(100) NOT NULL,
    model VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    brand_id INT NOT NULL,
    specifications TEXT,
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    warranty_months INT DEFAULT 0 CHECK (warranty_months >= 0),
    image_path VARCHAR(255),
    created_by INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES categories(category_id) ON UPDATE CASCADE,
    FOREIGN KEY (brand_id) REFERENCES brands(brand_id) ON UPDATE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Index on product code for faster product lookup
CREATE INDEX idx_product_code ON products(product_code);

-- 6. Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL UNIQUE,
    quantity_in_stock INT NOT NULL DEFAULT 0 CHECK (quantity_in_stock >= 0),
    reorder_level INT NOT NULL DEFAULT 5 CHECK (reorder_level >= 0),
    last_stock_update TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Customers table
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Sales table
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT,
    sold_by INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10,2) NOT NULL CHECK (total_amount >= 0),
    payment_method ENUM('CASH', 'CARD', 'MOBILE') NOT NULL,
    sale_status ENUM('COMPLETED', 'CANCELLED') DEFAULT 'COMPLETED',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (sold_by) REFERENCES users(user_id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_invoice_no ON sales(invoice_no);

-- 9. Sale Items table
CREATE TABLE IF NOT EXISTS sale_items (
    sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10,2) NOT NULL CHECK (subtotal >= 0),
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Suppliers table
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 11. Product Suppliers table (Many-to-Many mapping)
CREATE TABLE IF NOT EXISTS product_suppliers (
    product_supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    supplier_id INT NOT NULL,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uq_prod_supp (product_id, supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 12. Audit Logs table
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    action VARCHAR(255) NOT NULL,
    log_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- ====================================================
-- INSERT SAMPLE DATA
-- ====================================================

-- Insert Roles
INSERT INTO roles (role_id, role_name) VALUES 
(1, 'Store Manager'),
(2, 'Sales Representative');

-- Insert Users
-- Passwords are SHA-256 hashes of:
-- admin -> admin123 -> 240eb5183622750256923c23c6d182e785b7cc66ed0f274051a2d1d054452178
-- sales -> sales123 -> 3b907d0f7a086bc9bf133f923b36ed655f053e1f57f5aa7c5f87b8f9e616238b
INSERT INTO users (user_id, full_name, username, password_hash, email, phone, role_id, status) VALUES
(1, 'Alexander Pierce', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'alex.pierce@gadgetgalaxy.com', '+15550198', 1, 'ACTIVE'),
(2, 'Sarah Jenkins', 'sales', '6bc0a63cb29c92306020c0a6bbc358cc4628db277dc06e253535e126517ad637', 'sarah.j@gadgetgalaxy.com', '+15550143', 2, 'ACTIVE');

-- Insert Categories
INSERT INTO categories (category_id, category_name, description) VALUES
(1, 'Smartphones', 'Mobile cellular phones with advanced features'),
(2, 'Laptops', 'Portable personal computers for work and study'),
(3, 'Tablets', 'Touchscreen handheld devices smaller than laptops'),
(4, 'Smartwatches', 'Wearable smart computing devices for wrists'),
(5, 'Headphones', 'Over-ear and in-ear audio listening accessories'),
(6, 'Accessories', 'Chargers, cases, cables, and other electronics accessories');

-- Insert Brands
INSERT INTO brands (brand_id, brand_name, country) VALUES
(1, 'Samsung', 'South Korea'),
(2, 'Apple', 'USA'),
(3, 'Dell', 'USA'),
(4, 'Lenovo', 'China'),
(5, 'Sony', 'Japan'),
(6, 'Anker', 'China');

-- Insert Products
-- Smartphone, Laptop, Tablet, Accessory models will be instantiated in code
INSERT INTO products (product_id, product_code, product_name, model, category_id, brand_id, specifications, unit_price, warranty_months, created_by) VALUES
(1, 'SM-G998B', 'Samsung Galaxy S21 Ultra', 'Galaxy S21 Ultra', 1, 1, 'OS: Android 11, RAM: 12GB, Storage: 256GB, Screen: 6.8"', 1199.99, 24, 1),
(2, 'AP-M1X82', 'Apple MacBook Pro 14"', 'MacBook Pro M1', 2, 2, 'OS: macOS, Processor: M1 Pro, RAM: 16GB, Storage: 512GB SSD', 1999.00, 12, 1),
(3, 'AP-MHR23', 'Apple iPad Air', 'iPad Air 4th Gen', 3, 2, 'OS: iPadOS, RAM: 4GB, Storage: 64GB, Stylus: Apple Pencil 2 Support', 599.00, 12, 1),
(4, 'SM-R870N', 'Samsung Galaxy Watch 4', 'Watch 4 Classic', 4, 1, 'OS: Wear OS, Screen: 1.4" Super AMOLED, Type: Smartwatch', 249.99, 12, 1),
(5, 'SO-WH1000XM4', 'Sony WH-1000XM4 Headphones', 'WH-1000XM4', 5, 5, 'Type: Over-ear, Wireless: Yes, Noise Cancelling: Yes', 348.00, 12, 1),
(6, 'AK-A2633', 'Anker Nano II 65W Charger', 'Nano II 65W', 6, 6, 'Type: Charger, Port: USB-C, Wireless: No', 39.99, 18, 1);

-- Insert Inventory
INSERT INTO inventory (product_id, quantity_in_stock, reorder_level) VALUES
(1, 15, 5),
(2, 8, 3),
(3, 12, 4),
(4, 25, 8),
(5, 3, 5), -- This one is below reorder level to demonstrate low stock alerts!
(6, 50, 10);

-- Insert Suppliers
INSERT INTO suppliers (supplier_id, supplier_name, contact_person, phone, email, address) VALUES
(1, 'Galaxy Global Distribution', 'Johnathan Doe', '+15558900', 'john@galaxyglobal.com', '102 Tech Plaza, Silicon Valley, CA'),
(2, 'Apex Tech Wholesale', 'Emily Watson', '+15551212', 'emily@apexwholesale.com', '78 Warehouse Rd, Dallas, TX');

-- Insert Product Suppliers Mapping
INSERT INTO product_suppliers (product_id, supplier_id) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 2),
(5, 2),
(6, 2);

-- Insert Customers
INSERT INTO customers (customer_id, customer_name, phone, email, address) VALUES
(1, 'David Miller', '+15554321', 'david.miller@gmail.com', '456 Elm Street, Seattle, WA'),
(2, 'Jessica Taylor', '+15556789', 'jessica.t@yahoo.com', '789 Maple Drive, Chicago, IL');

-- Insert Sales
INSERT INTO sales (sale_id, invoice_no, customer_id, sold_by, sale_date, total_amount, payment_method, sale_status) VALUES
(1, 'INV-2026-0001', 1, 2, '2026-05-24 10:15:00', 1487.99, 'CARD', 'COMPLETED'),
(2, 'INV-2026-0002', 2, 2, '2026-05-24 14:30:00', 2038.99, 'CASH', 'COMPLETED');

-- Insert Sale Items
INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES
(1, 1, 1, 1199.99, 1199.99),
(1, 4, 1, 249.99, 249.99),
(1, 6, 1, 38.01, 38.01), -- Price adjustments
(2, 2, 1, 1999.00, 1999.00),
(2, 6, 1, 39.99, 39.99);

-- Insert Audit Logs
INSERT INTO audit_logs (user_id, action, log_time) VALUES
(1, 'System database initialized and populated with sample data.', '2026-05-24 09:00:00'),
(2, 'Completed sale INV-2026-0001 for David Miller.', '2026-05-24 10:15:00'),
(2, 'Completed sale INV-2026-0002 for Jessica Taylor.', '2026-05-24 14:30:00');
