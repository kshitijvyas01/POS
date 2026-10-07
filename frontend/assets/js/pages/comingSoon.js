/* =================================================================
   MEDIPOINT REUSABLE "COMING SOON" MODULE PLACEHOLDER VIEW
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   ================================================================= */

const MODULE_DETAILS = {
  pos: { title: "Point of Sale (POS)", icon: "shopping-cart", desc: "Fast barcode billing, batch selection, prescription scanning, and invoice printing." },
  medicines: { title: "Medicines & Drugs Directory", icon: "pill", desc: "Drug master list, salt composition, HSN codes, manufacturer details, and substitute locator." },
  inventory: { title: "Inventory Management", icon: "boxes", desc: "Real-time stock tracking, batch expiry management, low-stock reorder triggers, and shelf mapping." },
  purchases: { title: "Purchases & Supplier Orders", icon: "shopping-bag", desc: "Purchase order management, GRN verification, supplier invoice matching, and return notes." },
  vendors: { title: "Vendor & Distributor Directory", icon: "truck", desc: "Supplier ledgers, outstanding balances, distributor contact directory, and payment terms." },
  customers: { title: "Customer Medical Profiles", icon: "users", desc: "Patient purchase history, recurring prescription reminders, prescription attachments, and loyalty points." },
  sales: { title: "Sales Transactions & History", icon: "trending-up", desc: "Daily billing registers, return management, payment mode breakdown, and cashier cash drawer reconciliation." },
  payments: { title: "Payments & Collections", icon: "credit-card", desc: "UPI QR reconciliation, credit card terminal integrations, cash drawer logs, and vendor payout ledgers." },
  invoices: { title: "Invoices & GST Statements", icon: "file-text", desc: "GST-compliant tax invoices, GSTR-1 export, credit notes, and automated customer PDF receipts." },
  analytics: { title: "Analytics & Executive Reports", icon: "bar-chart-3", desc: "Revenue vs Profit analysis, fast-moving drug insights, ABC inventory classification, and demand forecasting." },
  employees: { title: "Employee & Staff Management", icon: "user-check", desc: "Staff directory, role permission assignments, shift schedules, and access logs." },
  attendance: { title: "Attendance Management", icon: "calendar-check", desc: "Biometric & digital shift check-in, shift rosters, and daily attendance logs." },
  leave: { title: "Leave Management", icon: "calendar-clock", desc: "Leave applications, approval workflows, leave balances, and absence records." },
  salary: { title: "Salary & Payroll Management", icon: "indian-rupee", desc: "Salary slips, payout structures, deductions, and monthly payroll registers." }
};

export function renderComingSoonPage(moduleId = 'pos') {
  const mod = MODULE_DETAILS[moduleId] || {
    title: moduleId.toUpperCase(),
    icon: "layers",
    desc: "Module functionality architecture prepared for subsequent phases."
  };

  return `
    <div class="card-medipoint text-center py-5 shadow-sm" style="max-width: 800px; margin: 2rem auto;">
      <div style="width: 80px; height: 80px; border-radius: 50%; background-color: var(--primary-50); color: var(--primary-600); display: inline-flex; align-items: center; justify-content: center; margin-bottom: 1.5rem; box-shadow: var(--shadow-glow);">
        <i data-lucide="${mod.icon}" style="width: 40px; height: 40px;"></i>
      </div>

      <div class="mb-3">
        <span class="badge bg-success text-white px-3 py-2 fw-semibold" style="font-size: 0.8125rem; letter-spacing: 0.05em;">
          PHASE 2 ROADMAP MODULE
        </span>
      </div>

      <h2 style="font-size: 1.75rem; font-weight: 800; color: var(--slate-900); margin-bottom: 0.75rem;">
        ${mod.title}
      </h2>

      <p style="color: var(--slate-600); font-size: 1rem; max-width: 540px; margin: 0 auto 2rem;">
        ${mod.desc}
      </p>

      <div class="p-4 bg-light rounded border text-start mx-auto mb-4" style="max-width: 580px;">
        <h5 class="fw-bold text-dark mb-2" style="font-size: 0.9375rem;">
          <i data-lucide="check-circle-2" class="me-2 text-success" style="width:18px;"></i>
          Step 1 Architectural Foundation Completed:
        </h5>
        <ul class="text-muted mb-0 small ps-4">
          <li class="mb-1">MySQL relational table architecture planned for <code>${moduleId}</code>.</li>
          <li class="mb-1">Role-based permission matrix and security filters active.</li>
          <li class="mb-1">Spring Boot backend & Spring Security authorization integrated.</li>
          <li>Frontend navigation item registered in application shell.</li>
        </ul>
      </div>

      <div>
        <a href="#dashboard" class="btn-medipoint btn-medipoint-primary">
          <i data-lucide="arrow-left"></i> Return to Dashboard
        </a>
      </div>
    </div>
  `;
}
