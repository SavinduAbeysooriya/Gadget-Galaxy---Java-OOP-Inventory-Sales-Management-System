# 📱 Gadget Galaxy — Inventory & Sales Management System

A desktop-based Inventory and Sales Management System for an electronics retail store, built with **Pure Java**, **Java Swing**, **JDBC**, and **MySQL**. Demonstrates core and advanced OOP concepts including encapsulation, inheritance, polymorphism, abstraction, interfaces, multithreading, exception handling, collections framework, and file handling. Follows **MVC architecture** with role-based access control.

---

## 🧰 Tech Stack

| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Java Swing | Desktop GUI |
| MySQL 8.x | Database |
| JDBC | Database connectivity |
| Maven | Build & dependency management |
| MVC Architecture | Code structure pattern |
| Java Collections Framework | Data management |
| Multithreading | Clock, stock monitor, auto-backup |
| File Handling | Audit log file output |

---

## ✅ Prerequisites

Before running this project, make sure you have the following installed:

- **Java JDK 17 or higher** → https://www.oracle.com/java/technologies/downloads/
- **Apache Maven 3.6+** → https://maven.apache.org/download.cgi
- **XAMPP** (for MySQL) → https://www.apachefriends.org/ *(or any MySQL 8.x server)*

Verify installations by running:
```bash
java -version
mvn -version
```

---

## 🗂️ Project Structure

```
gadget-galaxy-system/
├── src/
│   └── main/
│       ├── java/com/gadgetgalaxy/
│       │   ├── controller/       # AppController (MVC Controller)
│       │   ├── dao/              # Data Access Objects (DB queries)
│       │   ├── exception/        # Custom exceptions
│       │   ├── main/             # Main.java (entry point)
│       │   ├── model/            # Entity classes (Product, Sale, User...)
│       │   ├── service/          # Business logic layer
│       │   ├── util/             # DBConnection, FileUtil, InvoiceGenerator
│       │   └── view/             # Swing UI forms (Dashboard, LoginForm...)
│       └── resources/
│           └── database/
│               └── schema.sql    # Database schema + sample data
├── pom.xml                       # Maven build config
└── README.md
```

---

## ⚙️ Setup & Run Guide (A to Z)

### Step 1 — Start MySQL via XAMPP

1. Open **XAMPP Control Panel**
2. Click **Start** next to **MySQL**
3. Confirm it shows green / "Running"

> By default, XAMPP MySQL runs on port `3306` with username `root` and **no password**.

---

### Step 2 — Import the Database Schema

Open a terminal and run:

```bash
"C:\xampp\mysql\bin\mysql.exe" -u root < "src\main\resources\database\schema.sql"
```

Or use **phpMyAdmin**:
1. Open browser → go to `http://localhost/phpmyadmin`
2. Click **Import** tab
3. Choose file: `src/main/resources/database/schema.sql`
4. Click **Go**

This will create the `gadget_galaxy_db` database with all tables and sample data.

---

### Step 3 — Configure Database Connection

Open `src/main/java/com/gadgetgalaxy/util/DBConnection.java` and verify:

```java
private static final String HOST     = "localhost";
private static final String PORT     = "3306";
private static final String DATABASE = "gadget_galaxy_db";
private static final String USER     = "root";
private static final String PASSWORD = "";   // Leave empty for XAMPP default
```

> If you set a custom MySQL root password, enter it in the `PASSWORD` field.

---

### Step 4 — Build the Project

In the project root directory, run:

```bash
mvn clean compile
```

Expected output:
```
[INFO] BUILD SUCCESS
```

---

### Step 5 — Run the Application

**On Windows PowerShell:**
```powershell
mvn exec:java "-Dexec.mainClass=com.gadgetgalaxy.main.Main"
```

**On Windows CMD / macOS / Linux:**
```bash
mvn exec:java -Dexec.mainClass=com.gadgetgalaxy.main.Main
```

The login window will open.

---

## 🔐 Default Login Credentials

| Role | Username | Password |
|---|---|---|
| Store Manager | `admin` | `admin123` |
| Sales Representative | `sales` | `sales123` |

> Passwords are stored as **SHA-256 hashes** in the database.

---

## 🖥️ Features by Role

### Store Manager (`admin`)
- Full Dashboard with revenue, sales, product, and stock stats
- Product management (Add / Edit / Delete)
- Inventory management with low stock alerts
- Sales processing and invoice generation
- User account management
- Sales & inventory reports
- Audit log viewer

### Sales Representative (`sales`)
- Dashboard overview
- Browse products and inventory
- Process sales transactions
- Generate customer invoices

---

## 🧵 Background Threads

The application runs 3 daemon threads automatically after login:

| Thread | Interval | Purpose |
|---|---|---|
| Clock Thread | Every 1 second | Updates live clock in top bar |
| Low Stock Monitor | Every 30 seconds | Alerts if any product is below reorder level |
| Auto-Backup Thread | Every 60 seconds | Backs up inventory data to file |

---

## 📄 Invoice & File Output

- Invoices are generated as `.txt` files upon completing a sale
- Audit logs are written to a local log file via `FileUtil`
- Default output directory: project root `/invoices/` and `/logs/`

---

## 🛠️ Troubleshooting

| Problem | Solution |
|---|---|
| `Communications link failure` | MySQL is not running — start it in XAMPP Control Panel |
| `Access denied for user 'root'` | Wrong password in `DBConnection.java` — set correct password or leave empty for XAMPP |
| `Invalid username or password` on login | Password hash mismatch — re-run `schema.sql` to reset user data |
| Icons not showing | Requires Windows 10/11 with **Segoe UI Emoji** font (built-in) |
| `BUILD FAILURE` on PowerShell | Wrap the `-D` argument in quotes: `"-Dexec.mainClass=..."` |
| `Unknown lifecycle phase` error | Same as above — PowerShell parses `=` differently |

---

## 🎓 OOP Concepts Demonstrated

| Concept | Where Used |
|---|---|
| **Encapsulation** | All model classes (`Product`, `User`, `Sale`, etc.) |
| **Inheritance** | `Smartphone`, `Laptop`, `Tablet`, `Accessory` extend `Product`; `StoreManager`, `SalesRepresentative` extend `User` |
| **Polymorphism** | `Product` references used for all product subtypes |
| **Abstraction** | Abstract `Product` and `User` base classes |
| **Interfaces** | `DAO<T>` generic interface implemented by all DAOs |
| **Multithreading** | Clock, LowStockMonitor, AutoBackup threads in `Dashboard` |
| **Exception Handling** | Custom exceptions: `DatabaseException`, `AuthenticationException`, `ValidationException`, `InsufficientStockException` |
| **Collections Framework** | `List`, `Queue` used throughout services and connection pool |
| **File Handling** | `FileUtil` for audit logs, `InvoiceGenerator` for invoice `.txt` files |

---

## 📦 Build & Package

To package as a runnable JAR:
```bash
mvn clean package
```
Output JAR will be in `target/gadget-galaxy-system-1.0-SNAPSHOT.jar`

---

*Developed as a Java OOP course project demonstrating enterprise-level desktop application design patterns.*
