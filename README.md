# MediPoint — Smart Medical Store POS & Management System

> **Tagline:** *"Smarter Pharmacy. Better Control."*

MediPoint is a real-world enterprise medical store management and Point of Sale (POS) system built with modern architectural principles, role-based security, relational MySQL persistence, and a lightweight, high-performance vanilla web client.

---

## 🏗️ System Architecture & Workflow

```text
Medicine Catalog (MySQL)
        ↓
Inventory Management (Stock & Expiry Tracking)
        ↓
Point of Sale (Fast Billing Terminal)
        ↓
Checkout (Transactional Validation)
        ↓
Sale Record + Snapshot Line Items
        ↓
Atomic Stock Reduction (@Version Concurrency Protection)
        ↓
Stock Movement Audit Trail (SALE / ADJUSTMENT)
        ↓
Sales Register & Invoices (Historical Pricing)
        ↓
Live MySQL Dashboard Analytics
```

---

## 🛠️ Technology Stack

| Layer | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | HTML5, CSS3, JavaScript (ES6+) | Vanilla Single Page Application (SPA), Bootstrap 5.3, Lucide Icons |
| **Backend** | Java 17+, Spring Boot 3.2.4 | Enterprise REST API framework |
| **Security** | Spring Security 6, JJWT 0.12.5 | Stateless Bearer token validation, BCrypt password hashing, Method Security |
| **ORM / Data** | Spring Data JPA, Hibernate | Relational persistence, optimistic locking (`@Version`), transactions (`@Transactional`) |
| **Database** | MySQL 8.0+ | Relational SQL database engine |
| **Validation** | Jakarta Bean Validation | Declarative DTO constraint validation (`@Valid`, `@NotBlank`, `@Min`, `@DecimalMin`) |

---

## 🚀 Step 2 Modules Implemented

### 1. Medicine Management (`/api/v1/medicines`)
- **Owner Privileges:** Full CRUD operations, batch registration, pricing configuration (purchase and selling price), deactivation/reactivation, batch uniqueness validation.
- **Worker Privileges:** Real-time catalog search by name, generic name, brand, and batch. Available stock and selling price lookup. Confidential purchase price and profit data are strictly masked to `null` via DTO projections.
- **Data Safety:** Multi-column uniqueness constraint on `(name, batch_number)` preventing duplicate batches; manufacturing date verified against expiry date.

### 2. Inventory Management (`/api/v1/inventory`)
- **Key Performance Indicators (KPIs):**
  - Total Active Medicines (SKUs)
  - Total Physical Stock Units
  - Low Stock Alerts (`currentStock <= minimumStock && currentStock > 0`)
  - Out of Stock Medicines (`currentStock = 0`)
  - Expiring Soon Medicines (within configurable 30-day threshold)
  - Expired Medicines (`expiryDate < currentDate`, locked from sale)
  - Total Inventory Valuation (Confidential to Store Owner)
- **Stock Adjustment:** Store Owner can adjust inventory counts with mandatory audit reasons (`Physical stock count`, `Damaged`, `Lost`, `Manual correction`, `Other`).
- **Stock Movements Audit:** Every adjustment or sale automatically generates an immutable `StockMovement` record with previous stock, new stock, difference quantity, reference ID, and audit user.

### 3. Point of Sale (POS) (`/api/v1/sales`)
- **Fast Search:** Instant backend search filtering out-of-stock and expired drugs.
- **Responsive Cart:** Interactive quantity increment/decrement, direct numeric entry, stock boundary enforcement (`quantity <= currentStock`).
- **Customer Information:** Optional capture of customer name, phone number, and email.
- **Payment Methods:** Support for `CASH`, `UPI`, `CARD`, and `OTHER` with `PAID` or `PENDING` status.
- **Instant Receipt Modal:** Formatted printable receipt featuring invoice number, line items, discounts, taxes, and grand total.

### 4. Sales Register & Invoices (`/api/v1/sales`)
- **Sequential Invoicing:** Unique formatted invoice numbers (`MED-YYYYMMDD-XXXX`).
- **Historical Snapshot Integrity:** Line items capture selling price and purchase price at the moment of checkout. Future catalog price updates do not alter past sale records.
- **Profit Calculation:** Authoritative backend calculation: `(sellingPrice - purchasePrice) * quantity`. Workers cannot view profit metrics.
- **Concurrency & Stock Safety:** Handled using `@Version` optimistic locking in JPA and `@Transactional` rollback boundaries to eliminate race conditions between cashiers.

### 5. Live MySQL Dashboard Integration (`/api/v1/dashboard`)
- **Owner Dashboard:** Displays live today's sales count, today's gross revenue (`₹`), today's net profit (`₹`), catalog count, low-stock count, expiring batches, and latest sales transactions.
- **Worker Workspace:** Shows daily counter bills count, available medicines, low-stock alerts, and quick POS shortcuts. Store revenue and profit metrics are restricted and masked.

---

## 🗄️ Relational Database Schema (MySQL)

All business data is persisted relationally in MySQL across 5 relational tables:

```text
users
  ├── sales (1:N via worker_id)
  └── stock_movements (1:N via created_by)

medicines
  ├── sale_items (1:N via medicine_id)
  └── stock_movements (1:N via medicine_id)

sales
  └── sale_items (1:N via sale_id)
```

### Table Definitions

#### 1. `users`
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_user_email (email),
    INDEX idx_user_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 2. `medicines`
```sql
CREATE TABLE medicines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    generic_name VARCHAR(150),
    brand VARCHAR(100),
    category VARCHAR(50),
    manufacturer VARCHAR(100),
    batch_number VARCHAR(50) NOT NULL,
    manufacturing_date DATE,
    expiry_date DATE NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    current_stock INT NOT NULL DEFAULT 0,
    minimum_stock INT NOT NULL DEFAULT 10,
    unit VARCHAR(30) DEFAULT 'Strip',
    supplier_id VARCHAR(50),
    supplier_name VARCHAR(100),
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    created_by VARCHAR(100),
    updated_by VARCHAR(100),
    UNIQUE KEY uk_medicine_name_batch (name, batch_number),
    INDEX idx_medicine_name (name),
    INDEX idx_medicine_generic (generic_name),
    INDEX idx_medicine_brand (brand),
    INDEX idx_medicine_batch (batch_number),
    INDEX idx_medicine_expiry (expiry_date),
    INDEX idx_medicine_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 3. `sales`
```sql
CREATE TABLE sales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT,
    customer_name VARCHAR(100),
    customer_phone VARCHAR(20),
    customer_email VARCHAR(150),
    worker_id BIGINT,
    worker_name VARCHAR(100),
    items_count INT NOT NULL DEFAULT 0,
    subtotal DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    tax DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(12,2) NOT NULL,
    total_profit DECIMAL(12,2) NOT NULL,
    payment_method VARCHAR(20) NOT NULL DEFAULT 'CASH',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PAID',
    sale_date DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    INDEX idx_sale_invoice (invoice_number),
    INDEX idx_sale_date (sale_date),
    INDEX idx_sale_payment_method (payment_method)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 4. `sale_items`
```sql
CREATE TABLE sale_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    medicine_id BIGINT,
    medicine_name VARCHAR(150) NOT NULL,
    batch_number VARCHAR(50) NOT NULL,
    quantity INT NOT NULL,
    selling_price DECIMAL(10,2) NOT NULL,
    purchase_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    profit DECIMAL(12,2) NOT NULL,
    FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    FOREIGN KEY (medicine_id) REFERENCES medicines(id) ON DELETE SET NULL,
    INDEX idx_sale_item_sale_id (sale_id),
    INDEX idx_sale_item_medicine_id (medicine_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### 5. `stock_movements`
```sql
CREATE TABLE stock_movements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    medicine_id BIGINT NOT NULL,
    medicine_name VARCHAR(150),
    batch_number VARCHAR(50),
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    reference_id VARCHAR(100),
    reason VARCHAR(255),
    created_at DATETIME NOT NULL,
    created_by VARCHAR(100),
    FOREIGN KEY (medicine_id) REFERENCES medicines(id),
    INDEX idx_movement_medicine_id (medicine_id),
    INDEX idx_movement_created_at (created_at),
    INDEX idx_movement_type (movement_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 📡 Complete REST API Reference

All protected endpoints require an `Authorization: Bearer <JWT_TOKEN>` header.

### Authentication & User
| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/auth/login` | Public | Authenticates and returns JWT token |
| `POST` | `/api/v1/auth/register` | Public | Registers user |
| `GET` | `/api/v1/auth/me` | Authenticated | Retrieves current session user |
| `POST` | `/api/v1/auth/logout` | Authenticated | Terminates session |

### Medicine Management
| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/medicines` | `OWNER`, `WORKER` | Paginated search and filtering of catalog |
| `GET` | `/api/v1/medicines/{id}` | `OWNER`, `WORKER` | Details (Purchase price masked for worker) |
| `POST` | `/api/v1/medicines` | `OWNER` | Creates medicine and records initial stock movement |
| `PUT` | `/api/v1/medicines/{id}` | `OWNER` | Updates medicine details and logs stock changes |
| `PATCH`| `/api/v1/medicines/{id}/status` | `OWNER` | Toggles status between `ACTIVE` and `INACTIVE` |
| `DELETE`| `/api/v1/medicines/{id}` | `OWNER` | Soft deletes / deactivates medicine |
| `GET` | `/api/v1/medicines/pos-search` | `OWNER`, `WORKER` | Fast search for POS terminal (active & in-stock) |
| `GET` | `/api/v1/medicines/low-stock` | `OWNER`, `WORKER` | Medicines below reorder threshold |
| `GET` | `/api/v1/medicines/expiring` | `OWNER`, `WORKER` | Medicines expiring within threshold |
| `GET` | `/api/v1/medicines/expired` | `OWNER`, `WORKER` | Expired medicines |

### Inventory Control
| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/inventory/summary` | `OWNER`, `WORKER` | Inventory KPI telemetry metrics |
| `GET` | `/api/v1/inventory/items` | `OWNER`, `WORKER` | Inventory table with calculated stock status |
| `POST` | `/api/v1/inventory/adjust` | `OWNER` | Corrects physical stock and logs movement |
| `GET` | `/api/v1/inventory/movements` | `OWNER` | Immutable stock movements audit trail |

### Sales & POS
| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/sales` | `OWNER`, `WORKER` | Checks out cart, deducts stock, calculates profit |
| `GET` | `/api/v1/sales` | `OWNER`, `WORKER` | Sales history (Worker restricted to own bills) |
| `GET` | `/api/v1/sales/{id}` | `OWNER`, `WORKER` | Invoice line items (Profit masked for worker) |
| `GET` | `/api/v1/sales/invoice/{inv}`| `OWNER`, `WORKER` | Lookup by invoice number |

### Dashboard & Settings
| Method | Endpoint | Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/v1/dashboard/summary` | `OWNER`, `WORKER` | Live dashboard metrics and alert feed |
| `GET` | `/api/v1/settings` | `OWNER` | Store settings |
| `PUT` | `/api/v1/settings` | `OWNER` | Updates store profile and tax configurations |

---

## 🚀 How to Run the Updated Application

### 1. Start MySQL Server
Ensure MySQL 8.0+ is running locally and database `medipoint` is created:
```sql
CREATE DATABASE medipoint CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Configure Environment
Verify `backend/.env` has your local MySQL credentials:
```env
DB_HOST=localhost
DB_PORT=3306
DB_NAME=medipoint
DB_USERNAME=root
DB_PASSWORD=your_password
```

### 3. Build & Run the Backend
```powershell
cd "c:\D drive data\consultancy\backend"

# Run all 29 automated test suites
mvn test

# Package the application JAR
mvn package -DskipTests

# Start the Spring Boot Application
mvn spring-boot:run
```
The Spring Boot backend will run on **http://localhost:8080**.

### 4. Serve the Frontend
In another terminal, serve the `frontend/` directory:
```powershell
# Using Python:
python -m http.server 3000 --directory "c:\D drive data\consultancy\frontend"

# Or using npx:
npx http-server "c:\D drive data\consultancy\frontend" -p 3000
```
Open **http://localhost:3000** in your browser.

### 5. Demo Accounts
The `DatabaseInitializer` automatically seeds realistic pharmacy medicines and demo accounts if the database is fresh:
- **Store Owner:** `owner@medipoint.com` / `owner123` *(Full financial and management privileges)*
- **Pharmacy Worker:** `worker@medipoint.com` / `worker123` *(Operational billing & POS; financial data hidden)*

---

## 🧪 Automated Test Verification

The project includes 29 comprehensive automated tests across the security, service, repository, and controller layers:
- `JwtServiceTest`: Token generation, validation, and expiration verification.
- `ValidationTest`: Bean validation on DTO boundaries.
- `AuthServiceTest`: User registration, BCrypt password verification, and authentication.
- `SecurityRoleAuthorizationTest`: Route-level access verification for Owner settings.
- `Step2SecurityRoleTest`: Spring Security role checks for Medicine and Inventory routes.
- `MedicineServiceTest`: CRUD operations, duplicate batch prevention, and worker cost masking.
- `InventoryServiceTest`: Stock adjustment logic, non-negative boundary checks, and summary computation.
- `SaleServiceTest`: Complete checkout flow, stock deduction, stock movement generation, profit calculation, and expiry prevention.

Build result: **`29/29 tests passing (0 failures, 0 errors)`**.
