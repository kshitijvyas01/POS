/* =================================================================
   MEDIPOINT DASHBOARD PAGE VIEW (STEP 2 INTEGRATION)
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Features: Live MySQL metrics, dynamic recent sales table, store alerts
   ================================================================= */

import { auth } from '../auth.js';
import { api } from '../api.js';

export async function renderDashboardPage() {
  const user = auth.getUser();
  const isOwner = auth.isOwner();

  let summary = {
    todaySalesCount: 0,
    todayRevenue: isOwner ? "₹0.00" : "RESTRICTED",
    todayProfit: isOwner ? "₹0.00" : "RESTRICTED",
    totalMedicines: 0,
    lowStockCount: 0,
    outOfStockCount: 0,
    expiringSoonCount: 0,
    financialsVisible: isOwner,
    recentSales: [],
    recentAlerts: [],
    emptyStateNotice: "No sales transactions recorded yet today. Open Point of Sale (POS) to start billing."
  };

  try {
    const apiRes = await api.getDashboardSummary();
    if (apiRes && apiRes.data) {
      summary = apiRes.data;
    }
  } catch (e) {
    console.warn("Could not retrieve dynamic dashboard summary from server:", e.message);
  }

  // Owner cards vs Worker cards
  const kpiCardsHtml = isOwner ? `
    <!-- Owner KPI Summary Cards Grid -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div>
          <div class="kpi-label">Today's Sales</div>
          <div class="kpi-value text-dark">${summary.todaySalesCount}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Completed Transactions</div>
        </div>
        <div class="kpi-icon"><i data-lucide="shopping-cart"></i></div>
      </div>

      <div class="kpi-card">
        <div>
          <div class="kpi-label">Today's Revenue</div>
          <div class="kpi-value text-success">${summary.todayRevenue}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Gross Invoiced Value</div>
        </div>
        <div class="kpi-icon" style="background-color: var(--primary-50); color: var(--primary-600);"><i data-lucide="indian-rupee"></i></div>
      </div>

      <div class="kpi-card">
        <div>
          <div class="kpi-label">Today's Profit</div>
          <div class="kpi-value text-success">${summary.todayProfit}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Net Invoiced Margin</div>
        </div>
        <div class="kpi-icon" style="background-color: #ecfdf5; color: #059669;"><i data-lucide="trending-up"></i></div>
      </div>

      <div class="kpi-card">
        <div>
          <div class="kpi-label">Total Catalog</div>
          <div class="kpi-value text-dark">${summary.totalMedicines}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Active Drug SKUs</div>
        </div>
        <div class="kpi-icon"><i data-lucide="pill"></i></div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid var(--warning-500);">
        <div>
          <div class="kpi-label">Low Stock</div>
          <div class="kpi-value text-warning">${summary.lowStockCount}</div>
          <div style="font-size: 0.75rem; color: var(--warning-500);">Below Reorder Level</div>
        </div>
        <div class="kpi-icon" style="background-color: var(--warning-50); color: var(--warning-500);"><i data-lucide="alert-triangle"></i></div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid #8b5cf6;">
        <div>
          <div class="kpi-label">Expiring Soon</div>
          <div class="kpi-value" style="color: #8b5cf6;">${summary.expiringSoonCount}</div>
          <div style="font-size: 0.75rem; color: #8b5cf6;">Within 30 Days</div>
        </div>
        <div class="kpi-icon" style="background-color: #f5f3ff; color: #8b5cf6;"><i data-lucide="clock"></i></div>
      </div>
    </div>
  ` : `
    <!-- Worker Operational Cards Grid (Financials Masked) -->
    <div class="kpi-grid">
      <div class="kpi-card">
        <div>
          <div class="kpi-label">Today's Sales</div>
          <div class="kpi-value text-dark">${summary.todaySalesCount}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Counter Billings Completed</div>
        </div>
        <div class="kpi-icon"><i data-lucide="shopping-cart"></i></div>
      </div>

      <div class="kpi-card">
        <div>
          <div class="kpi-label">Total Medicines</div>
          <div class="kpi-value text-dark">${summary.totalMedicines}</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Active in Database</div>
        </div>
        <div class="kpi-icon"><i data-lucide="pill"></i></div>
      </div>

      <div class="kpi-card" style="border-left: 4px solid var(--warning-500);">
        <div>
          <div class="kpi-label">Low Stock Alerts</div>
          <div class="kpi-value text-warning">${summary.lowStockCount}</div>
          <div style="font-size: 0.75rem; color: var(--warning-500);">Items Below Minimum</div>
        </div>
        <div class="kpi-icon" style="background-color: var(--warning-50); color: var(--warning-500);"><i data-lucide="alert-triangle"></i></div>
      </div>

      <div class="kpi-card">
        <div>
          <div class="kpi-label">Quick POS Status</div>
          <div class="kpi-value text-success" style="font-size: 1.5rem;">Ready</div>
          <div style="font-size: 0.75rem; color: var(--slate-500);">Terminal Accessible</div>
        </div>
        <div class="kpi-icon"><i data-lucide="zap"></i></div>
      </div>
    </div>
  `;

  // Render Recent Sales Section
  let recentSalesHtml;
  if (summary.recentSales && summary.recentSales.length > 0) {
    const saleRows = summary.recentSales.map(s => `
      <tr>
        <td>
          <div class="fw-bold font-monospace small text-dark">${escapeHtml(s.invoiceNumber)}</div>
          <div class="text-muted small" style="font-size: 0.72rem;">${formatDate(s.saleDate)}</div>
        </td>
        <td>
          <div class="small fw-semibold text-dark">${escapeHtml(s.customerName || 'Walk-in')}</div>
        </td>
        <td class="text-center small">${s.itemsCount}</td>
        <td class="fw-bold text-success small">₹${Number(s.totalAmount).toFixed(2)}</td>
        <td><span class="badge bg-light text-dark small">${s.paymentMethod}</span></td>
      </tr>
    `).join('');

    recentSalesHtml = `
      <div class="med-table-wrapper">
        <table class="med-table">
          <thead>
            <tr>
              <th>Invoice</th>
              <th>Customer</th>
              <th class="text-center">Items</th>
              <th>Amount</th>
              <th>Payment</th>
            </tr>
          </thead>
          <tbody>
            ${saleRows}
          </tbody>
        </table>
      </div>
      <div class="p-3 text-end border-top">
        <a href="#sales" class="btn btn-outline btn-sm">View Full Sales Register →</a>
      </div>
    `;
  } else {
    recentSalesHtml = `
      <div class="empty-state p-4">
        <div class="empty-state-icon" style="width: 48px; height: 48px;">
          <i data-lucide="receipt"></i>
        </div>
        <h5 class="fw-bold text-dark mb-1">No sales recorded today</h5>
        <p class="text-muted small mb-3">${escapeHtml(summary.emptyStateNotice || 'Start billing from Point of Sale terminal.')}</p>
        <a href="#pos" class="btn btn-primary btn-sm">
          <i data-lucide="shopping-cart"></i> Open POS Terminal
        </a>
      </div>
    `;
  }

  // Render Dynamic Alerts
  const alertsHtml = (summary.recentAlerts || []).map(alert => `
    <li class="list-group-item px-0 d-flex align-items-start gap-2 border-0 mb-2">
      <div class="p-1 rounded ${alert.type === 'DANGER' ? 'bg-danger text-white' : (alert.type === 'WARNING' ? 'bg-warning text-dark' : 'bg-success text-white')}">
        <i data-lucide="${alert.type === 'DANGER' ? 'alert-circle' : (alert.type === 'WARNING' ? 'alert-triangle' : 'check')}" style="width: 14px; height: 14px;"></i>
      </div>
      <div class="flex-grow-1" style="min-width: 0;">
        <div class="fw-bold small text-dark">${escapeHtml(alert.title)}</div>
        <div class="text-muted small" style="font-size: 0.75rem;">${escapeHtml(alert.message)}</div>
      </div>
    </li>
  `).join('');

  return `
    <div>
      <!-- Welcome Banner -->
      <div style="background: linear-gradient(135deg, var(--primary-900), var(--primary-950)); color: #ffffff; border-radius: var(--radius-md); padding: 1.75rem 2rem; margin-bottom: 2rem; display: flex; align-items: center; justify-content: space-between;" class="shadow-sm">
        <div>
          <div style="font-size: 0.75rem; font-weight: 700; color: var(--primary-400); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.25rem;">
            MediPoint POS Platform · ${isOwner ? 'Store Owner Dashboard' : 'Pharmacy Worker Workspace'}
          </div>
          <h2 style="color: #ffffff; margin: 0; font-size: 1.5rem; font-weight: 800;">Welcome back, ${user?.displayName || 'User'}!</h2>
          <p style="color: var(--slate-300); margin: 0.25rem 0 0 0; font-size: 0.875rem;">
            ${isOwner ? 'Real-time MySQL stock telemetry, sales transactions, and financial control.' : 'POS billing terminal, medicine search, and stock directory.'}
          </p>
        </div>
        <div class="d-none d-md-block">
          <span class="badge ${isOwner ? 'bg-warning text-dark' : 'bg-info text-dark'}" style="font-size: 0.875rem; padding: 0.5rem 1rem;">
            ROLE: ${isOwner ? 'STORE OWNER' : 'WORKER'}
          </span>
        </div>
      </div>

      <!-- KPI Summary Cards -->
      ${kpiCardsHtml}

      <!-- Main Dashboard Row -->
      <div class="row g-4 mb-4">
        <!-- Recent Sales Section -->
        <div class="col-12 col-lg-8">
          <div class="med-card h-100">
            <div class="med-card-header">
              <div>
                <h5 class="fw-bold mb-0">Latest Sales Transactions</h5>
                <div class="small text-muted">Real-time counter bills from MySQL database</div>
              </div>
              <a href="#pos" class="btn btn-primary btn-sm">
                <i data-lucide="plus"></i> New Bill
              </a>
            </div>
            ${recentSalesHtml}
          </div>
        </div>

        <!-- Inventory Alerts & Telemetry -->
        <div class="col-12 col-lg-4">
          <div class="med-card h-100">
            <div class="med-card-header">
              <h5 class="fw-bold mb-0">Store Health Alerts</h5>
              <i data-lucide="bell" style="color: var(--primary-600); width: 18px; height: 18px;"></i>
            </div>
            <div class="p-3">
              <ul class="list-group list-group-flush">
                ${alertsHtml}
              </ul>

              <div class="mt-4 p-3 bg-light rounded border">
                <div class="fw-bold text-dark small mb-1">Architecture Active</div>
                <div class="text-muted small">MySQL Data Store + Spring Security JWT + JPA Hibernate</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Quick Access Modules Grid -->
      <div class="med-card p-4">
        <h5 class="fw-bold mb-3">Quick Navigation Modules</h5>
        <div class="row g-3">
          <div class="col-6 col-md-3">
            <a href="#pos" class="card text-decoration-none h-100 p-3 border hover-shadow" style="border-radius: var(--radius-sm); transition: transform 0.2s;">
              <div class="text-primary mb-2"><i data-lucide="shopping-cart" style="width:24px; height:24px;"></i></div>
              <div class="fw-bold text-dark" style="font-size: 0.9375rem;">Point of Sale</div>
              <div class="text-muted small">Fast Billing & Checkout</div>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a href="#medicines" class="card text-decoration-none h-100 p-3 border hover-shadow" style="border-radius: var(--radius-sm); transition: transform 0.2s;">
              <div class="text-success mb-2"><i data-lucide="pill" style="width:24px; height:24px;"></i></div>
              <div class="fw-bold text-dark" style="font-size: 0.9375rem;">Medicines</div>
              <div class="text-muted small">Manage Catalog & Batches</div>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a href="#inventory" class="card text-decoration-none h-100 p-3 border hover-shadow" style="border-radius: var(--radius-sm); transition: transform 0.2s;">
              <div class="text-warning mb-2"><i data-lucide="boxes" style="width:24px; height:24px;"></i></div>
              <div class="fw-bold text-dark" style="font-size: 0.9375rem;">Inventory</div>
              <div class="text-muted small">Track Stocks & Adjustments</div>
            </a>
          </div>
          <div class="col-6 col-md-3">
            <a href="#sales" class="card text-decoration-none h-100 p-3 border hover-shadow" style="border-radius: var(--radius-sm); transition: transform 0.2s;">
              <div class="text-info mb-2"><i data-lucide="trending-up" style="width:24px; height:24px;"></i></div>
              <div class="fw-bold text-dark" style="font-size: 0.9375rem;">Sales Register</div>
              <div class="text-muted small">Historical Invoices</div>
            </a>
          </div>
        </div>
      </div>
    </div>
  `;
}

function formatDate(isoStr) {
  if (!isoStr) return '—';
  try {
    const d = new Date(isoStr);
    return d.toLocaleString('en-IN', {
      day: '2-digit', month: 'short', year: 'numeric',
      hour: '2-digit', minute: '2-digit'
    });
  } catch (e) {
    return isoStr;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}
