# Inventory Management System

A desktop application for keeping track of products and stock levels: add and edit products, see which items are running low, and view inventory reports with totals and charts. Built with **Java Swing** (FlatLaf look and feel) and **MySQL**.

> **Project status: work in progress.** The core features below work. Stock movements, per-product low-stock limits, and several other planned features are not finished yet. See the [To-do list](#to-do) at the end.

## Features (working now)

### Accounts
- Create an account (full name, username of at least 3 characters, password of at least 6 characters)
- Sign in and sign out

### Dashboard
- Welcome message for the signed-in user
- Three summary cards: total products, low-stock items, and total inventory value
- Updates automatically when products change

### Inventory
- Table of all products: ID, name, category, quantity, price, and total value (quantity x price)
- Search by product name or category name
- Filter by category
- Add, edit, and delete products (double-click a row to edit)
- Quantities at or below the low-stock limit are shown in bold red
- Product count at the bottom of the table

### Reports
- Summary: total products, low-stock items, total value, and number of categories
- Pie chart of inventory value by product
- Low-stock table
- Full breakdown table with unit price and total value per product, plus a grand total

Amounts are shown in Philippine pesos.

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java (the project file targets JDK 25; the code itself needs Java 14 or newer) |
| UI | Swing + [FlatLaf](https://www.formdev.com/flatlaf/) 3.4.1 |
| Charts | JFreeChart 1.5.4 |
| Database | MySQL via `mysql-connector-j` 9.1.0 |
| Build | Maven |

## Project structure

```
InventoryManagement/
├── pom.xml
├── db.sql                                      # Schema + sample data
└── src/main/java/com/mycompany/inventorymanagement/
    ├── Main.java                               # Entry point: opens the login screen
    ├── LoginFrame.java, RegisterFrame.java     # Sign in and create account
    ├── DashboardFrame.java                     # Main window: sidebar + dashboard cards
    ├── InventoryPanel.java                     # Product table and add/edit dialog
    ├── ReportsPanel.java                       # Reports page (pie chart, tables)
    ├── DBConnection.java                       # Connection settings
    ├── ProductDAO.java, UserDAO.java           # Database access
    ├── Product.java, Category.java, User.java  # Models
    └── UITheme.java, Currency.java             # Colors, fonts, shared components, formatting
```

## Requirements

- A JDK that matches `maven.compiler.release` in `pom.xml` (currently **25**). If you have an older JDK, change that value (for example to `17`); nothing in the code needs anything newer than Java 14.
- Maven 3.8+ (or NetBeans, which bundles it)
- MySQL Server 8+

## Setup

### 1. Create the database

```bash
mysql -u root -p < db.sql
```

This creates the `inventory_db` database, all tables, and sample data.

### 2. Configure the connection

Open `DBConnection.java` and set your MySQL credentials:

```java
private static final String USER     = "root";
private static final String PASSWORD = "root";   // change to your password
```

### 3. Run

**NetBeans:** open the project folder and press **Run** (main class: `com.mycompany.inventorymanagement.Main`).

**Command line:**

```bash
mvn compile exec:java
```

> `mvn package` builds a plain JAR without the libraries bundled, so it cannot be started with `java -jar` yet.

## Sample account and data

| Username | Password |
|---|---|
| `admin` | `admin123` |

The script also adds 4 categories (Electronics, Accessories, Office Supplies, Furniture) and 5 products (Laptop, Mouse, Keyboard, Notebook, Office Chair). New users can sign up from the login screen.

## Database design

| Table | Purpose |
|---|---|
| `users` | Login accounts (username, password, full name) |
| `categories` | Product categories |
| `products` | Name, category, quantity, price, and a low-stock threshold column |
| `stock_movements` | Audit trail of stock changes (table exists but is not used yet) |

## Low-stock rule

A product counts as low on stock when its quantity is **5 or less**. This limit is currently one fixed value in the code (`Product.LOW_STOCK_THRESHOLD`).

## To-do

### Planned features

- [ ] **Stock movements:** record every quantity change in the `stock_movements` table (who, when, how much, why) and show a history page
- [ ] **Stock in / stock out:** add buttons to receive or remove stock with a reason, instead of editing the quantity directly
- [ ] **Per-product low-stock limit:** use the `low_stock_threshold` column that already exists in the database, and let the user set it in the product form
- [ ] **Manage categories:** add, rename, and delete categories from the app (they can only be changed in the database now)
- [ ] **Export reports** to CSV or PDF
- [ ] **Sortable columns** in the inventory table
- [ ] **Dashboard improvements:** list of the lowest-stock items and recent stock activity
- [ ] **Suppliers and purchase records**
- [ ] Separate **admin and staff roles** (the database is currently single-role)

### Fixes and cleanup

- [ ] **Reports pie chart does not refresh** after products change. The tables and numbers update, but the chart is only built once when the app opens
- [ ] **Hash passwords** (for example with BCrypt). They are stored as plain text now
- [ ] **Better database error messages.** If MySQL is not running, the login screen currently says "Invalid username or password" instead of reporting the connection problem
- [ ] **Move the database settings** out of `DBConnection.java` into a config file
- [ ] Remove the unused `InventoryManagement.java` stub class
- [ ] Remove the `flatlaf-extras` dependency from `pom.xml` if it stays unused
- [ ] Add the Maven Shade plugin so `mvn package` produces a runnable JAR
- [ ] Add basic unit tests for `ProductDAO`

## Notes and limitations

- Unfinished project, built for learning. It has no audit log, backup, or multi-user permissions yet.
- Every account has full access to all products and reports.
- Deleting a product permanently removes it, along with any stock movement records linked to it.