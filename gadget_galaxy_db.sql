-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 27, 2026 at 01:48 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `gadget_galaxy_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `audit_logs`
--

CREATE TABLE `audit_logs` (
  `log_id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `action` varchar(255) NOT NULL,
  `log_time` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `audit_logs`
--

INSERT INTO `audit_logs` (`log_id`, `user_id`, `action`, `log_time`) VALUES
(43, 4, 'Updated Product ID 1: Samsung Galaxy S21 Ultra', '2026-05-25 06:15:55'),
(44, 4, 'Updated Product ID 2: Apple MacBook Pro 14\"', '2026-05-25 06:16:47'),
(45, 4, 'Updated Product ID 3: Apple iPad Air', '2026-05-25 06:17:36'),
(46, 4, 'Updated Product ID 4: Samsung Galaxy Watch 4', '2026-05-25 06:18:56'),
(47, 4, 'Updated Product ID 5: Sony WH-1000XM4 Headphones', '2026-05-25 06:19:38'),
(48, 4, 'Updated Product ID 6: Anker Nano II 65W Charger', '2026-05-25 06:20:42'),
(49, 4, 'User successfully logged in.', '2026-05-25 06:24:34'),
(50, 4, 'Created inventory record for: Samsung Galaxy S21 Ultra', '2026-05-25 06:24:45'),
(51, 4, 'Added 20 units of stock for: Samsung Galaxy S21 Ultra', '2026-05-25 06:24:50'),
(52, 4, 'Updated reorder level to 10 for Product: Samsung Galaxy S21 Ultra', '2026-05-25 06:25:02'),
(53, 4, 'Created inventory record for: Apple MacBook Pro 14\"', '2026-05-25 06:25:05'),
(54, 4, 'Added 25 units of stock for: Apple MacBook Pro 14\"', '2026-05-25 06:25:11'),
(55, 4, 'Created inventory record for: Apple iPad Air', '2026-05-25 06:25:15'),
(56, 4, 'Added 30 units of stock for: Apple iPad Air', '2026-05-25 06:25:17'),
(57, 4, 'Created inventory record for: Samsung Galaxy Watch 4', '2026-05-25 06:25:21'),
(58, 4, 'Added 35 units of stock for: Samsung Galaxy Watch 4', '2026-05-25 06:25:24'),
(59, 4, 'Created inventory record for: Sony WH-1000XM4 Headphones', '2026-05-25 06:25:27'),
(60, 4, 'Added 40 units of stock for: Sony WH-1000XM4 Headphones', '2026-05-25 06:25:30'),
(61, 4, 'Created inventory record for: Anker Nano II 65W Charger', '2026-05-25 06:25:32'),
(62, 4, 'Added 45 units of stock for: Anker Nano II 65W Charger', '2026-05-25 06:25:35'),
(63, 4, 'Updated reorder level to 15 for Product: Anker Nano II 65W Charger', '2026-05-25 06:25:47'),
(64, 4, 'Processed sale INV-20260525-0001. Total: LKR 1798.99', '2026-05-25 06:27:05'),
(65, 4, 'Updated customer: Isuru', '2026-05-25 06:29:03'),
(66, 4, 'Updated supplier: Apex Tech Wholesale', '2026-05-25 06:29:35'),
(67, 4, 'Updated supplier: Galaxy Global Distribution', '2026-05-25 06:29:53'),
(68, 4, 'Added product-supplier link: product 1 → supplier 2', '2026-05-25 06:30:03'),
(69, 4, 'Added product-supplier link: product 2 → supplier 1', '2026-05-25 06:30:12'),
(70, 4, 'Added product-supplier link: product 3 → supplier 1', '2026-05-25 06:30:28'),
(71, 4, 'Added product-supplier link: product 4 → supplier 1', '2026-05-25 06:30:35'),
(72, 4, 'Added product-supplier link: product 2 → supplier 2', '2026-05-25 06:30:40'),
(73, 4, 'Removed product-supplier link ID: 18', '2026-05-25 06:30:48'),
(74, 4, 'User successfully logged in.', '2026-05-25 06:34:14'),
(75, 4, 'Added product-supplier link: Apple MacBook Pro 14\" → Apex Tech Wholesale', '2026-05-25 06:34:32'),
(76, 4, 'Added product-supplier link: Samsung Galaxy S21 Ultra → Galaxy Global Distribution', '2026-05-25 06:34:40'),
(77, 4, 'User successfully logged in.', '2026-05-25 06:41:31'),
(78, 4, 'Toggled user status to INACTIVE for: sales', '2026-05-25 06:42:04'),
(79, 4, 'Toggled user status to ACTIVE for: sales', '2026-05-25 06:42:08'),
(80, 4, 'User logged out.', '2026-05-25 06:43:27'),
(81, 3, 'User successfully logged in.', '2026-05-25 06:43:35'),
(82, 3, 'Processed sale INV-20260525-0002. Total: LKR 1699.97', '2026-05-25 06:43:58'),
(83, 1, 'User successfully logged in.', '2026-08-27 11:42:06');

-- --------------------------------------------------------

--
-- Table structure for table `brands`
--

CREATE TABLE `brands` (
  `brand_id` int(11) NOT NULL,
  `brand_name` varchar(50) NOT NULL,
  `country` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `brands`
--

INSERT INTO `brands` (`brand_id`, `brand_name`, `country`) VALUES
(1, 'Samsung', 'South Korea'),
(2, 'Apple', 'USA'),
(3, 'Dell', 'USA'),
(4, 'Lenovo', 'China'),
(5, 'Sony', 'Japan'),
(6, 'Anker', 'China'),
(7, 'HP', 'China');

-- --------------------------------------------------------

--
-- Table structure for table `categories`
--

CREATE TABLE `categories` (
  `category_id` int(11) NOT NULL,
  `category_name` varchar(50) NOT NULL,
  `description` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `categories`
--

INSERT INTO `categories` (`category_id`, `category_name`, `description`) VALUES
(1, 'Smartphones', 'Mobile cellular phones with advanced features'),
(2, 'Laptops', 'Portable personal computers for work and study'),
(3, 'Tablets', 'Touchscreen handheld devices smaller than laptops'),
(4, 'Smartwatches', 'Wearable smart computing devices for wrists'),
(5, 'Headphones', 'Over-ear and in-ear audio listening accessories'),
(6, 'Accessories', 'Chargers, cases, cables, and other electronics accessories'),
(7, 'Televisions', 'Smart TVs, LED TVs and Android televisions');

-- --------------------------------------------------------

--
-- Table structure for table `customers`
--

CREATE TABLE `customers` (
  `customer_id` int(11) NOT NULL,
  `customer_name` varchar(100) NOT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `customers`
--

INSERT INTO `customers` (`customer_id`, `customer_name`, `phone`, `email`, `address`) VALUES
(5, 'Isuru', '0772892789', 'isuru@gmail.com', '20/A, Tuduwamulla, Ambalangoda');

-- --------------------------------------------------------

--
-- Table structure for table `inventory`
--

CREATE TABLE `inventory` (
  `inventory_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity_in_stock` int(11) NOT NULL DEFAULT 0 CHECK (`quantity_in_stock` >= 0),
  `reorder_level` int(11) NOT NULL DEFAULT 5 CHECK (`reorder_level` >= 0),
  `last_stock_update` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `inventory`
--

INSERT INTO `inventory` (`inventory_id`, `product_id`, `quantity_in_stock`, `reorder_level`, `last_stock_update`) VALUES
(14, 1, 18, 10, '2026-05-25 06:43:58'),
(15, 2, 25, 5, '2026-05-25 06:25:11'),
(16, 3, 29, 5, '2026-05-25 06:27:05'),
(17, 4, 33, 5, '2026-05-25 06:43:58'),
(18, 5, 40, 5, '2026-05-25 06:25:30'),
(19, 6, 45, 15, '2026-05-25 06:25:47');

-- --------------------------------------------------------

--
-- Table structure for table `products`
--

CREATE TABLE `products` (
  `product_id` int(11) NOT NULL,
  `product_code` varchar(50) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `model` varchar(50) NOT NULL,
  `category_id` int(11) NOT NULL,
  `brand_id` int(11) NOT NULL,
  `specifications` text DEFAULT NULL,
  `unit_price` decimal(10,2) NOT NULL CHECK (`unit_price` >= 0),
  `warranty_months` int(11) DEFAULT 0 CHECK (`warranty_months` >= 0),
  `image_path` varchar(255) DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `products`
--

INSERT INTO `products` (`product_id`, `product_code`, `product_name`, `model`, `category_id`, `brand_id`, `specifications`, `unit_price`, `warranty_months`, `image_path`, `created_by`, `created_at`) VALUES
(1, 'SM-G998B', 'Samsung Galaxy S21 Ultra', 'Galaxy S21 Ultra', 1, 1, 'OS: Unknown, RAM: 0GB, Storage: 0GB', 1199.99, 24, 'images\\1779689753873.jpg', 1, '2026-05-25 01:44:08'),
(2, 'AP-M1X82', 'Apple MacBook Pro 14\"', 'MacBook Pro M1', 2, 2, 'Processor: Unknown, RAM: 0GB, Storage: 0GB, Screen: 0.0\"', 1999.00, 12, 'images\\1779689796365.jpg', 1, '2026-05-25 01:44:08'),
(3, 'AP-MHR23', 'Apple iPad Air', 'iPad Air 4th Gen', 3, 2, 'OS: Unknown, Stylus: No, Screen: 0.0\"', 599.00, 12, 'images\\1779689855594.png', 1, '2026-05-25 01:44:08'),
(4, 'SM-R870N', 'Samsung Galaxy Watch 4', 'Watch 4 Classic', 4, 1, 'Type: General, Wireless: No', 249.99, 12, 'images\\1779689935496.jpg', 1, '2026-05-25 01:44:08'),
(5, 'SO-WH1000XM4', 'Sony WH-1000XM4 Headphones', 'WH-1000XM4', 5, 5, 'Type: General, Wireless: No', 348.00, 12, 'images\\1779689976976.jpg', 1, '2026-05-25 01:44:08'),
(6, 'AK-A2633', 'Anker Nano II 65W Charger', 'Nano II 65W', 6, 6, 'Type: General, Wireless: No', 39.99, 18, 'images\\1779690041897.png', 1, '2026-05-25 01:44:08');

-- --------------------------------------------------------

--
-- Table structure for table `product_suppliers`
--

CREATE TABLE `product_suppliers` (
  `product_supplier_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `supplier_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `product_suppliers`
--

INSERT INTO `product_suppliers` (`product_supplier_id`, `product_id`, `supplier_id`) VALUES
(20, 1, 1),
(14, 1, 2),
(15, 2, 1),
(19, 2, 2),
(16, 3, 1),
(17, 4, 1);

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `role_id` int(11) NOT NULL,
  `role_name` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`role_id`, `role_name`) VALUES
(2, 'Sales Representative'),
(1, 'Store Manager');

-- --------------------------------------------------------

--
-- Table structure for table `sales`
--

CREATE TABLE `sales` (
  `sale_id` int(11) NOT NULL,
  `invoice_no` varchar(50) NOT NULL,
  `customer_id` int(11) DEFAULT NULL,
  `sold_by` int(11) NOT NULL,
  `sale_date` timestamp NOT NULL DEFAULT current_timestamp(),
  `total_amount` decimal(10,2) NOT NULL CHECK (`total_amount` >= 0),
  `payment_method` enum('CASH','CARD','MOBILE') NOT NULL,
  `sale_status` enum('COMPLETED','CANCELLED') DEFAULT 'COMPLETED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sales`
--

INSERT INTO `sales` (`sale_id`, `invoice_no`, `customer_id`, `sold_by`, `sale_date`, `total_amount`, `payment_method`, `sale_status`) VALUES
(8, 'INV-20260525-0001', 5, 4, '2026-05-25 06:27:05', 1798.99, 'CASH', 'COMPLETED'),
(9, 'INV-20260525-0002', 5, 3, '2026-05-25 06:43:58', 1699.97, 'CARD', 'COMPLETED');

-- --------------------------------------------------------

--
-- Table structure for table `sale_items`
--

CREATE TABLE `sale_items` (
  `sale_item_id` int(11) NOT NULL,
  `sale_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `quantity` int(11) NOT NULL CHECK (`quantity` > 0),
  `unit_price` decimal(10,2) NOT NULL CHECK (`unit_price` >= 0),
  `subtotal` decimal(10,2) NOT NULL CHECK (`subtotal` >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `sale_items`
--

INSERT INTO `sale_items` (`sale_item_id`, `sale_id`, `product_id`, `quantity`, `unit_price`, `subtotal`) VALUES
(18, 8, 1, 1, 1199.99, 1199.99),
(19, 8, 3, 1, 599.00, 599.00),
(20, 9, 1, 1, 1199.99, 1199.99),
(21, 9, 4, 2, 249.99, 499.98);

-- --------------------------------------------------------

--
-- Table structure for table `suppliers`
--

CREATE TABLE `suppliers` (
  `supplier_id` int(11) NOT NULL,
  `supplier_name` varchar(100) NOT NULL,
  `contact_person` varchar(100) DEFAULT NULL,
  `phone` varchar(20) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `address` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `suppliers`
--

INSERT INTO `suppliers` (`supplier_id`, `supplier_name`, `contact_person`, `phone`, `email`, `address`) VALUES
(1, 'Galaxy Global Distribution', 'Caldera', '0772727678', 'john@galaxyglobal.com', '102 Tech Plaza, Silicon Valley, CA'),
(2, 'Apex Tech Wholesale', 'Eshan', '0772728273', 'emily@apexwholesale.com', '78 Warehouse Rd, Dallas, TX');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int(11) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `email` varchar(100) NOT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `full_name`, `username`, `password_hash`, `email`, `phone`, `role_id`, `created_at`, `status`) VALUES
(1, 'Alexander Pierce', 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'alex.pierce@gadgetgalaxy.com', '+15550198', 1, '2026-05-25 01:44:08', 'ACTIVE'),
(2, 'Sarah Jenkins', 'sales', '6bc0a63cb29c92306020c0a6bbc358cc4628db277dc06e253535e126517ad637', 'sarah.j@gadgetgalaxy.com', '+15550143', 2, '2026-05-25 01:44:08', 'ACTIVE'),
(3, 'Tharindu Chandrawansha', 'tharindu', 'ee79976c9380d5e337fc1c095ece8c8f22f91f306ceeb161fa51fecede2c4ba1', 'tharindu@gmail.com', '0771234567', 2, '2026-05-25 02:26:31', 'ACTIVE'),
(4, 'Savi Abey', 'savi', 'ee79976c9380d5e337fc1c095ece8c8f22f91f306ceeb161fa51fecede2c4ba1', 'savindu147@gmail.com', '0772892789', 1, '2026-05-25 02:27:26', 'ACTIVE');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD PRIMARY KEY (`log_id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `brands`
--
ALTER TABLE `brands`
  ADD PRIMARY KEY (`brand_id`),
  ADD UNIQUE KEY `brand_name` (`brand_name`);

--
-- Indexes for table `categories`
--
ALTER TABLE `categories`
  ADD PRIMARY KEY (`category_id`),
  ADD UNIQUE KEY `category_name` (`category_name`);

--
-- Indexes for table `customers`
--
ALTER TABLE `customers`
  ADD PRIMARY KEY (`customer_id`);

--
-- Indexes for table `inventory`
--
ALTER TABLE `inventory`
  ADD PRIMARY KEY (`inventory_id`),
  ADD UNIQUE KEY `product_id` (`product_id`);

--
-- Indexes for table `products`
--
ALTER TABLE `products`
  ADD PRIMARY KEY (`product_id`),
  ADD UNIQUE KEY `product_code` (`product_code`),
  ADD KEY `category_id` (`category_id`),
  ADD KEY `brand_id` (`brand_id`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `idx_product_code` (`product_code`);

--
-- Indexes for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  ADD PRIMARY KEY (`product_supplier_id`),
  ADD UNIQUE KEY `uq_prod_supp` (`product_id`,`supplier_id`),
  ADD KEY `supplier_id` (`supplier_id`);

--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`role_id`),
  ADD UNIQUE KEY `role_name` (`role_name`);

--
-- Indexes for table `sales`
--
ALTER TABLE `sales`
  ADD PRIMARY KEY (`sale_id`),
  ADD UNIQUE KEY `invoice_no` (`invoice_no`),
  ADD KEY `customer_id` (`customer_id`),
  ADD KEY `sold_by` (`sold_by`),
  ADD KEY `idx_invoice_no` (`invoice_no`);

--
-- Indexes for table `sale_items`
--
ALTER TABLE `sale_items`
  ADD PRIMARY KEY (`sale_item_id`),
  ADD KEY `sale_id` (`sale_id`),
  ADD KEY `product_id` (`product_id`);

--
-- Indexes for table `suppliers`
--
ALTER TABLE `suppliers`
  ADD PRIMARY KEY (`supplier_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `role_id` (`role_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `audit_logs`
--
ALTER TABLE `audit_logs`
  MODIFY `log_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=84;

--
-- AUTO_INCREMENT for table `brands`
--
ALTER TABLE `brands`
  MODIFY `brand_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `categories`
--
ALTER TABLE `categories`
  MODIFY `category_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `customers`
--
ALTER TABLE `customers`
  MODIFY `customer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `inventory`
--
ALTER TABLE `inventory`
  MODIFY `inventory_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT for table `products`
--
ALTER TABLE `products`
  MODIFY `product_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  MODIFY `product_supplier_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `roles`
--
ALTER TABLE `roles`
  MODIFY `role_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT for table `sales`
--
ALTER TABLE `sales`
  MODIFY `sale_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `sale_items`
--
ALTER TABLE `sale_items`
  MODIFY `sale_item_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `suppliers`
--
ALTER TABLE `suppliers`
  MODIFY `supplier_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `audit_logs`
--
ALTER TABLE `audit_logs`
  ADD CONSTRAINT `audit_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `inventory`
--
ALTER TABLE `inventory`
  ADD CONSTRAINT `inventory_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `products`
--
ALTER TABLE `products`
  ADD CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `products_ibfk_2` FOREIGN KEY (`brand_id`) REFERENCES `brands` (`brand_id`) ON UPDATE CASCADE,
  ADD CONSTRAINT `products_ibfk_3` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `product_suppliers`
--
ALTER TABLE `product_suppliers`
  ADD CONSTRAINT `product_suppliers_ibfk_1` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `product_suppliers_ibfk_2` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers` (`supplier_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `sales`
--
ALTER TABLE `sales`
  ADD CONSTRAINT `sales_ibfk_1` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`customer_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT `sales_ibfk_2` FOREIGN KEY (`sold_by`) REFERENCES `users` (`user_id`) ON UPDATE CASCADE;

--
-- Constraints for table `sale_items`
--
ALTER TABLE `sale_items`
  ADD CONSTRAINT `sale_items_ibfk_1` FOREIGN KEY (`sale_id`) REFERENCES `sales` (`sale_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `sale_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`product_id`) ON UPDATE CASCADE;

--
-- Constraints for table `users`
--
ALTER TABLE `users`
  ADD CONSTRAINT `users_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
