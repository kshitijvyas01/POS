-- =========================================================================
-- MEDIPOINT PHARMACY — COMPREHENSIVE STORE DUMMY DATA SEED SCRIPT
-- =========================================================================

USE medipoint;

-- 1. Insert Additional Staff / Pharmacist
INSERT INTO users (full_name, email, phone, password, role, status, created_at, updated_at)
VALUES 
('Priya Sharma', 'priya.worker@medipoint.com', '+91 9876543212', '$2a$10$ax9C5o5jzP/YeebVfB3Ee.0FWWTeQyxVr078Y5S03Os5o1lVS.ynC', 'WORKER', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE updated_at = NOW();

-- 2. Insert Rich Medical Inventory Across Key Therapeutic Categories
INSERT INTO medicines 
(name, generic_name, brand, category, manufacturer, batch_number, manufacturing_date, expiry_date, purchase_price, selling_price, current_stock, minimum_stock, unit, supplier_id, supplier_name, description, status, created_at, updated_at, created_by, updated_by, version)
VALUES
-- Cardiology & Blood Pressure
('Amlodipine 5mg', 'Amlodipine Besylate', 'Amlong 5', 'Cardiology', 'Micro Labs Ltd', 'AML-2024-K10', '2024-04-10', '2027-04-09', 22.00, 38.50, 110, 20, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Calcium channel blocker for hypertension', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Telmisartan 40mg', 'Telmisartan IP', 'Telma 40', 'Cardiology', 'Glenmark Pharma', 'TEL-2024-L11', '2024-05-15', '2027-05-14', 68.00, 115.00, 75, 15, 'Strip', 'SUPP-002', 'MedPlus Distributors', 'Angiotensin II receptor blocker for BP control', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Atorvastatin 10mg', 'Atorvastatin Calcium', 'Atorva 10', 'Cardiology', 'Zydus Cadila', 'ATV-2024-M12', '2024-03-01', '2027-03-01', 54.00, 92.00, 90, 20, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Statin medication for cholesterol reduction', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Diabetes Management
('Metformin 500mg', 'Metformin Hydrochloride IP', 'Glycomet 500', 'Diabetes', 'USV Private Ltd', 'MET-2024-N13', '2024-06-01', '2027-06-01', 18.50, 32.00, 140, 25, 'Strip', 'SUPP-003', 'National Pharma Distributors', 'First-line medication for type 2 diabetes', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Glimepiride 2mg', 'Glimepiride IP', 'Amaryl 2mg', 'Diabetes', 'Sanofi India', 'GLM-2024-P14', '2024-02-10', '2027-02-09', 42.00, 74.00, 65, 15, 'Strip', 'SUPP-002', 'MedPlus Distributors', 'Sulfonylurea antidiabetic tablets', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Antibiotics & Infection
('Augmentin 625 Duo', 'Amoxicillin + Clavulanic Acid', 'Augmentin 625', 'Antibiotic', 'GSK Healthcare', 'AUG-2024-Q15', '2024-08-01', '2026-08-01', 135.00, 205.00, 50, 15, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Broad-spectrum antibacterial for respiratory and ENT infections', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Ciprofloxacin 500mg', 'Ciprofloxacin Hydrochloride', 'Ciplox 500', 'Antibiotic', 'Cipla Ltd', 'CIP-2024-R16', '2024-03-20', '2027-03-19', 28.00, 48.00, 80, 20, 'Strip', 'SUPP-002', 'MedPlus Distributors', 'Fluoroquinolone antibiotic for bacterial infections', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Gastrointestinal & Antacids
('Omeprazole 20mg', 'Omeprazole IP', 'Omez 20', 'Digestive', 'Dr. Reddy''s Laboratories', 'OMZ-2024-S17', '2024-05-10', '2027-05-09', 32.00, 58.50, 115, 25, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Antacid capsule for GERD and stomach ulcers', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Rabeprazole + Domperidone', 'Rabeprazole + Domperidone SR', 'Razo-D', 'Digestive', 'Sun Pharma', 'RZD-2024-T18', '2024-07-01', '2027-07-01', 95.00, 168.00, 40, 10, 'Strip', 'SUPP-003', 'National Pharma Distributors', 'Capsule for acidity with nausea relief', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Digene Gel Mint 200ml', 'Magnesium Hydroxide + Simethicone', 'Digene', 'Antacid', 'Abbott Healthcare', 'DIG-2024-U19', '2024-06-15', '2026-12-15', 78.00, 130.00, 30, 10, 'Bottle', 'SUPP-001', 'Apollo Med Logistics', 'Fast-acting liquid antacid mint flavour', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Respiratory & Anti-Allergy
('Montelukast + Levocetirizine', 'Montelukast Sodium + Levocetirizine', 'Montair LC', 'Respiratory', 'Cipla Ltd', 'MLC-2024-V20', '2024-04-25', '2027-04-24', 110.00, 192.00, 55, 15, 'Strip', 'SUPP-002', 'MedPlus Distributors', 'Dual action relief for allergic rhinitis and asthma', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Budesonide Inhaler 200mcg', 'Budesonide Inhalation IP', 'Budecort 200 Inhaler', 'Respiratory', 'Cipla Ltd', 'BUD-2024-W21', '2024-06-01', '2026-11-30', 215.00, 325.00, 22, 5, 'Unit', 'SUPP-002', 'MedPlus Distributors', 'Corticosteroid inhaler for asthma maintenance', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Pain Management & Anti-Inflammatory
('Combiflam Tablets', 'Ibuprofen + Paracetamol', 'Combiflam', 'Pain Relief', 'Sanofi India', 'CMB-2024-X22', '2024-05-01', '2027-05-01', 24.00, 43.50, 130, 30, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Fast dual-action pain and fever relief', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Volini Pain Relief Gel 50g', 'Diclofenac Diethylamine Gel', 'Volini', 'Pain Relief', 'Sun Pharma', 'VOL-2024-Y23', '2024-07-10', '2026-10-10', 85.00, 145.00, 40, 10, 'Tube', 'SUPP-003', 'National Pharma Distributors', 'Quick-absorbing topical pain relief gel', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- Vitamins & Daily Supplements
('Shelcal 500', 'Calcium Carbonate + Vitamin D3', 'Shelcal 500', 'Vitamin', 'Torrent Pharma', 'SHL-2024-Z24', '2024-04-01', '2027-04-01', 62.00, 108.00, 95, 20, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Calcium and Vitamin D3 daily supplement', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Becosules Z Capsules', 'Vitamin B-Complex + Zinc + C', 'Becosules Z', 'Vitamin', 'Pfizer India', 'BEC-2024-AA25', '2024-05-20', '2027-05-20', 30.00, 52.00, 110, 25, 'Strip', 'SUPP-002', 'MedPlus Distributors', 'B-complex with Zinc for immunity and mouth ulcers', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Neurobion Forte', 'Vitamin B1, B6, B12', 'Neurobion Forte', 'Vitamin', 'Procter & Gamble Health', 'NEU-2024-AB26', '2024-06-10', '2027-06-10', 22.50, 39.00, 120, 20, 'Strip', 'SUPP-001', 'Apollo Med Logistics', 'Nerve health and daily vitality supplement', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),

-- First Aid & Antiseptics
('Betadine 10% Ointment 20g', 'Povidone Iodine 10% w/w', 'Betadine', 'First Aid', 'Win-Medicare', 'BET-2024-AC27', '2024-03-15', '2027-03-14', 68.00, 112.00, 35, 10, 'Tube', 'SUPP-003', 'National Pharma Distributors', 'Microbicidal antiseptic ointment for cuts & wounds', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Dettol Antiseptic Liquid 125ml', 'Chloroxylenol Antiseptic', 'Dettol', 'First Aid', 'Reckitt Benckiser', 'DET-2024-AD28', '2024-06-01', '2027-06-01', 52.00, 84.00, 50, 15, 'Bottle', 'SUPP-001', 'Apollo Med Logistics', 'Multi-purpose disinfectant and wound wash', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0),
('Hansaplast Bandage (Pack of 20)', 'Medicated Washproof Bandage', 'Hansaplast', 'First Aid', 'Beiersdorf India', 'HAN-2024-AE29', '2024-07-01', '2028-07-01', 28.00, 48.00, 85, 20, 'Pack', 'SUPP-003', 'National Pharma Distributors', 'Antiseptic adhesive wound plasters', 'ACTIVE', NOW(), NOW(), 'Store Owner', 'Store Owner', 0);

-- =========================================================================
-- 3. Realistic Sales Records (Populating POS History & Dashboard KPIs)
-- =========================================================================

-- Sale 1 (Today morning: 2026-10-07 10:15:00) - Rajesh Kumar (CASH)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261007-0001', '2026-10-07 10:15:00', 'Rajesh Kumar', '+91 98201 12345', 'rajesh.k@gmail.com', 253.50, 13.50, 0.00, 240.00, 92.00, 'CASH', 'PAID', 3, 2, 'Pharmacy Worker', '2026-10-07 10:15:00', 'Pharmacy Worker');

SET @sale_id_1 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_1, 1, 'Paracetamol 500mg', 'PCM-2024-A1', 2, 15.00, 28.00, 56.00, 26.00),
(@sale_id_1, 3, 'Cetirizine 10mg', 'CET-2024-C3', 1, 12.00, 24.00, 24.00, 12.00),
(@sale_id_1, 15, 'Omeprazole 20mg', 'OMZ-2024-S17', 3, 32.00, 58.50, 175.50, 79.50);

-- Sale 2 (Today afternoon: 2026-10-07 14:30:00) - Dr. Anita Sharma (UPI)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261007-0002', '2026-10-07 14:30:00', 'Dr. Anita Sharma', '+91 98202 23456', 'anita.clinic@yahoo.com', 530.00, 30.00, 0.00, 500.00, 185.00, 'UPI', 'PAID', 2, 1, 'Store Owner', '2026-10-07 14:30:00', 'Store Owner');

SET @sale_id_2 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_2, 13, 'Augmentin 625 Duo', 'AUG-2024-Q15', 2, 135.00, 205.00, 410.00, 140.00),
(@sale_id_2, 4, 'Vitamin C 500mg', 'VIT-2024-D4', 2, 18.00, 35.00, 70.00, 34.00);

-- Sale 3 (Today evening: 2026-10-07 18:20:00) - Vikram Malhotra (CARD)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261007-0003', '2026-10-07 18:20:00', 'Vikram Malhotra', '+91 98203 34567', 'vikram.m@gmail.com', 433.00, 13.00, 0.00, 420.00, 149.00, 'CARD', 'PAID', 2, 2, 'Pharmacy Worker', '2026-10-07 18:20:00', 'Pharmacy Worker');

SET @sale_id_3 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_3, 11, 'Telmisartan 40mg', 'TEL-2024-L11', 2, 68.00, 115.00, 230.00, 94.00),
(@sale_id_3, 22, 'Shelcal 500', 'SHL-2024-Z24', 2, 62.00, 108.00, 216.00, 92.00);

-- Sale 4 (Today: 2026-10-07 19:40:00) - Walk-in Customer (CASH)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261007-0004', '2026-10-07 19:40:00', 'Walk-in Customer', '+91 98990 00000', '', 167.50, 7.50, 0.00, 160.00, 68.00, 'CASH', 'PAID', 2, 2, 'Pharmacy Worker', '2026-10-07 19:40:00', 'Pharmacy Worker');

SET @sale_id_4 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_4, 20, 'Combiflam Tablets', 'CMB-2024-X22', 2, 24.00, 43.50, 87.00, 39.00),
(@sale_id_4, 5, 'ORS Sachet', 'ORS-2024-E5', 3, 16.50, 22.50, 67.50, 18.00);

-- Sale 5 (Yesterday: 2026-10-06 11:10:00) - Sunita Patel (UPI)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261006-0001', '2026-10-06 11:10:00', 'Sunita Patel', '+91 98204 45678', 'sunita.p@outlook.com', 517.00, 17.00, 0.00, 500.00, 184.00, 'UPI', 'PAID', 3, 1, 'Store Owner', '2026-10-06 11:10:00', 'Store Owner');

SET @sale_id_5 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_5, 2, 'Azithromycin 500mg', 'AZI-2024-B2', 2, 72.00, 119.50, 239.00, 95.00),
(@sale_id_5, 17, 'Digene Gel Mint 200ml', 'DIG-2024-U19', 1, 78.00, 130.00, 130.00, 52.00),
(@sale_id_5, 23, 'Becosules Z Capsules', 'BEC-2024-AA25', 2, 30.00, 52.00, 104.00, 44.00);

-- Sale 6 (Yesterday: 2026-10-06 16:45:00) - Manoj Joshi (CARD)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261006-0002', '2026-10-06 16:45:00', 'Manoj Joshi', '+91 98207 78901', 'manoj.joshi@gmail.com', 470.00, 20.00, 0.00, 450.00, 170.00, 'CARD', 'PAID', 2, 2, 'Pharmacy Worker', '2026-10-06 16:45:00', 'Pharmacy Worker');

SET @sale_id_6 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_6, 21, 'Volini Pain Relief Gel 50g', 'VOL-2024-Y23', 2, 85.00, 145.00, 290.00, 120.00),
(@sale_id_6, 18, 'Montelukast + Levocetirizine', 'MLC-2024-V20', 1, 110.00, 192.00, 192.00, 82.00);

-- Sale 7 (Earlier: 2026-10-05 14:15:00) - Sneha Reddy (UPI)
INSERT INTO sales (invoice_number, sale_date, customer_name, customer_phone, customer_email, subtotal, discount, tax, total_amount, total_profit, payment_method, payment_status, items_count, worker_id, worker_name, created_at, created_by)
VALUES ('MED-20261005-0001', '2026-10-05 14:15:00', 'Sneha Reddy', '+91 98206 67890', 'sneha.reddy@gmail.com', 364.00, 14.00, 0.00, 350.00, 131.00, 'UPI', 'PAID', 3, 2, 'Pharmacy Worker', '2026-10-05 14:15:00', 'Pharmacy Worker');

SET @sale_id_7 = LAST_INSERT_ID();

INSERT INTO sale_items (sale_id, medicine_id, medicine_name, batch_number, quantity, purchase_price, selling_price, subtotal, profit)
VALUES 
(@sale_id_7, 12, 'Metformin 500mg', 'MET-2024-N13', 3, 18.50, 32.00, 96.00, 40.50),
(@sale_id_7, 24, 'Neurobion Forte', 'NEU-2024-AB26', 3, 22.50, 39.00, 117.00, 49.50),
(@sale_id_7, 25, 'Betadine 10% Ointment 20g', 'BET-2024-AC27', 1, 68.00, 112.00, 112.00, 44.00);

-- =========================================================================
-- 4. Initial Stock Movements Audit History
-- =========================================================================
INSERT INTO stock_movements 
(medicine_id, medicine_name, batch_number, movement_type, quantity, previous_stock, new_stock, reason, reference_id, created_at, created_by)
VALUES
(1, 'Paracetamol 500mg', 'PCM-2024-A1', 'PURCHASE', 150, 0, 150, 'Initial wholesale batch received from distributor', 'PO-2024-001', '2026-10-01 09:00:00', 'Store Owner'),
(1, 'Paracetamol 500mg', 'PCM-2024-A1', 'SALE', -2, 122, 120, 'Counter POS invoice checkout', 'MED-20261007-0001', '2026-10-07 10:15:00', 'Pharmacy Worker'),
(2, 'Azithromycin 500mg', 'AZI-2024-B2', 'PURCHASE', 50, 0, 50, 'Wholesale order delivery', 'PO-2024-002', '2026-10-02 11:30:00', 'Store Owner'),
(2, 'Azithromycin 500mg', 'AZI-2024-B2', 'SALE', -2, 47, 45, 'Prescription invoice checkout', 'MED-20261006-0001', '2026-10-06 11:10:00', 'Store Owner'),
(6, 'Pantoprazole 40mg', 'PAN-2024-F6', 'ADJUSTMENT', -5, 13, 8, 'Damaged strips written off during morning audit', 'AUDIT-20261005', '2026-10-05 16:00:00', 'Store Owner');
