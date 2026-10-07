/* =================================================================
   MEDIPOINT INVENTORY MANAGEMENT MODULE (STEP 2)
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Features: Stock Monitoring, Expiry Tracking, Stock Adjustment, Audit Trail
   ================================================================= */

import { api } from '../api.js';
import { auth } from '../auth.js';
import { toast } from '../components/toast.js';

let state = {
  summary: null,
  activeTab: 'all', // 'all', 'low', 'out', 'expiring', 'expired', 'movements'
  items: [],
  movements: [],
  searchQuery: '',
  page: 0,
  totalPages: 1,
  totalElements: 0,
  loading: false
};

export async function renderInventoryPage() {
  const isOwner = auth.isOwner();

  return `
    <div class="inventory-container animate-fade-in">
      <!-- Page Header -->
      <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <h2 class="fw-bold mb-1" style="color: var(--slate-900);">Inventory & Stock Control</h2>
          <p class="text-muted small mb-0">Live stock health metrics, expiry monitoring, and transactional adjustments.</p>
        </div>
      </div>

      <!-- KPI Summary Cards -->
      <div class="kpi-grid" id="inventory-kpi-grid">
        <div class="kpi-card">
          <div>
            <div class="kpi-label">Total Medicines</div>
            <div class="kpi-value text-dark" id="kpi-total-meds">—</div>
            <div class="small text-muted">Active Catalog SKUs</div>
          </div>
          <div class="kpi-icon"><i data-lucide="package"></i></div>
        </div>

        <div class="kpi-card">
          <div>
            <div class="kpi-label">Stock Units</div>
            <div class="kpi-value text-success" id="kpi-total-units">—</div>
            <div class="small text-muted">Total physical units</div>
          </div>
          <div class="kpi-icon" style="background-color: var(--primary-50); color: var(--primary-600);"><i data-lucide="boxes"></i></div>
        </div>

        <div class="kpi-card" style="border-left: 4px solid var(--warning-500);">
          <div>
            <div class="kpi-label">Low Stock</div>
            <div class="kpi-value text-warning" id="kpi-low-stock">—</div>
            <div class="small text-muted">At or below reorder level</div>
          </div>
          <div class="kpi-icon" style="background-color: var(--warning-50); color: var(--warning-500);"><i data-lucide="alert-triangle"></i></div>
        </div>

        <div class="kpi-card" style="border-left: 4px solid var(--danger-500);">
          <div>
            <div class="kpi-label">Out of Stock</div>
            <div class="kpi-value text-danger" id="kpi-out-stock">—</div>
            <div class="small text-muted">Zero inventory available</div>
          </div>
          <div class="kpi-icon" style="background-color: var(--danger-50); color: var(--danger-500);"><i data-lucide="slash"></i></div>
        </div>

        <div class="kpi-card" style="border-left: 4px solid #8b5cf6;">
          <div>
            <div class="kpi-label">Expiring Soon</div>
            <div class="kpi-value" style="color: #8b5cf6;" id="kpi-expiring">—</div>
            <div class="small text-muted">Within 30 days</div>
          </div>
          <div class="kpi-icon" style="background-color: #f5f3ff; color: #8b5cf6;"><i data-lucide="clock"></i></div>
        </div>

        <div class="kpi-card" style="border-left: 4px solid #b91c1c;">
          <div>
            <div class="kpi-label">Expired Stock</div>
            <div class="kpi-value text-danger" id="kpi-expired">—</div>
            <div class="small text-muted">Locked from sale</div>
          </div>
          <div class="kpi-icon" style="background-color: #fee2e2; color: #b91c1c;"><i data-lucide="calendar-x"></i></div>
        </div>
      </div>

      <!-- Navigation Tabs -->
      <div class="nav-tabs-custom mb-3">
        <button class="nav-tab-btn ${state.activeTab === 'all' ? 'active' : ''}" data-tab="all">
          All Stock Items
        </button>
        <button class="nav-tab-btn ${state.activeTab === 'low' ? 'active' : ''}" data-tab="low">
          Low Stock Alerts
        </button>
        <button class="nav-tab-btn ${state.activeTab === 'out' ? 'active' : ''}" data-tab="out">
          Out of Stock
        </button>
        <button class="nav-tab-btn ${state.activeTab === 'expiring' ? 'active' : ''}" data-tab="expiring">
          Expiring Soon (30d)
        </button>
        <button class="nav-tab-btn ${state.activeTab === 'expired' ? 'active' : ''}" data-tab="expired">
          Expired Medicines
        </button>
        ${isOwner ? `
          <button class="nav-tab-btn ${state.activeTab === 'movements' ? 'active' : ''}" data-tab="movements">
            Stock Movements Log
          </button>
        ` : ''}
      </div>

      <!-- Inventory Content Area -->
      <div class="med-card" id="inventory-content-container">
        <div class="p-5 text-center">
          <div class="spinner-border text-success" role="status"></div>
          <p class="mt-2 text-muted small">Loading inventory data...</p>
        </div>
      </div>

      <!-- Modal Placeholder -->
      <div id="inventory-modal-placeholder"></div>
    </div>
  `;
}

export function attachInventoryEvents() {
  document.querySelectorAll('.nav-tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.nav-tab-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.activeTab = btn.getAttribute('data-tab');
      state.page = 0;
      loadTabData();
    });
  });

  loadSummary();
  loadTabData();
}

async function loadSummary() {
  try {
    const res = await api.getInventorySummary();
    const data = res.data;
    state.summary = data;

    const setVal = (id, val) => {
      const el = document.getElementById(id);
      if (el) el.textContent = val;
    };

    setVal('kpi-total-meds', data.totalMedicines);
    setVal('kpi-total-units', data.totalStockUnits);
    setVal('kpi-low-stock', data.lowStockCount);
    setVal('kpi-out-stock', data.outOfStockCount);
    setVal('kpi-expiring', data.expiringSoonCount);
    setVal('kpi-expired', data.expiredCount);
  } catch (err) {
    console.error("Failed to load inventory summary:", err);
  }
}

async function loadTabData() {
  const container = document.getElementById('inventory-content-container');
  if (!container) return;

  container.innerHTML = `
    <div class="p-5 text-center">
      <div class="spinner-border text-success" role="status"></div>
      <p class="mt-2 text-muted small">Fetching data from server...</p>
    </div>
  `;

  try {
    if (state.activeTab === 'movements') {
      const res = await api.getStockMovements({ page: state.page, size: 20 });
      state.movements = res.data.content || [];
      state.totalPages = res.data.totalPages || 1;
      renderMovementsTable(container);
    } else {
      let res;
      if (state.activeTab === 'low') {
        res = await api.getLowStockMedicines(state.page, 15);
      } else if (state.activeTab === 'expiring') {
        res = await api.getExpiringMedicines(30, state.page, 15);
      } else if (state.activeTab === 'expired') {
        res = await api.getExpiredMedicines(state.page, 15);
      } else {
        res = await api.getInventoryItems({ page: state.page, size: 15 });
      }

      state.items = res.data.content || [];
      state.totalPages = res.data.totalPages || 1;
      renderStockTable(container);
    }
  } catch (err) {
    console.error("Failed to load inventory tab data:", err);
    container.innerHTML = `
      <div class="p-5 text-center text-danger">
        <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 0.5rem;"></i>
        <div class="fw-bold">Unable to load inventory data</div>
        <div class="small text-muted mt-1">${escapeHtml(err.message)}</div>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
  }
}

function renderStockTable(container) {
  const isOwner = auth.isOwner();

  if (state.items.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon"><i data-lucide="check-circle"></i></div>
        <h4 class="fw-bold text-dark">No records found</h4>
        <p class="text-muted small">No medicines match this category filter.</p>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const rows = state.items.map(m => {
    let stockBadge = 'badge-instock';
    let stockText = 'In Stock';
    if (m.stockStatus === 'EXPIRED') {
      stockBadge = 'badge-expired';
      stockText = 'Expired';
    } else if (m.stockStatus === 'OUT_OF_STOCK') {
      stockBadge = 'badge-outofstock';
      stockText = 'Out of Stock';
    } else if (m.stockStatus === 'LOW_STOCK') {
      stockBadge = 'badge-lowstock';
      stockText = 'Low Stock';
    }

    return `
      <tr>
        <td>
          <div class="fw-bold text-dark">${escapeHtml(m.name)}</div>
          <div class="text-muted small">${escapeHtml(m.genericName || '—')}</div>
        </td>
        <td><code>${escapeHtml(m.batchNumber)}</code></td>
        <td>
          <div class="small fw-semibold ${m.expired ? 'text-danger' : (m.expiringSoon ? 'text-warning' : '')}">
            ${m.expiryDate || '—'}
          </div>
        </td>
        <td class="fw-bold fs-6">
          ${m.currentStock} <span class="text-muted small fw-normal">${escapeHtml(m.unit || '')}</span>
        </td>
        <td class="text-muted small">${m.minimumStock}</td>
        <td class="fw-semibold text-success">₹${Number(m.sellingPrice || 0).toFixed(2)}</td>
        <td>
          <span class="badge-status ${stockBadge}">${stockText}</span>
        </td>
        ${isOwner ? `
          <td class="text-end">
            <button class="btn btn-outline btn-sm btn-adjust" data-id="${m.id}" data-name="${escapeHtml(m.name)}" data-stock="${m.currentStock}">
              <i data-lucide="sliders" style="width: 14px; height: 14px;"></i> Adjust
            </button>
          </td>
        ` : ''}
      </tr>
    `;
  }).join('');

  container.innerHTML = `
    <div class="med-table-wrapper">
      <table class="med-table">
        <thead>
          <tr>
            <th>Medicine</th>
            <th>Batch</th>
            <th>Expiry</th>
            <th>Current Stock</th>
            <th>Min Stock</th>
            <th>Selling Price</th>
            <th>Stock Status</th>
            ${isOwner ? `<th class="text-end">Actions</th>` : ''}
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    </div>

    ${state.totalPages > 1 ? `
      <div class="p-3 border-top d-flex justify-content-between align-items-center">
        <span class="small text-muted">Page ${state.page + 1} of ${state.totalPages}</span>
        <div class="d-flex gap-2">
          <button class="btn btn-outline btn-sm" id="btn-stock-prev" ${state.page === 0 ? 'disabled' : ''}>Prev</button>
          <button class="btn btn-outline btn-sm" id="btn-stock-next" ${state.page >= state.totalPages - 1 ? 'disabled' : ''}>Next</button>
        </div>
      </div>
    ` : ''}
  `;

  if (isOwner) {
    container.querySelectorAll('.btn-adjust').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        const name = btn.getAttribute('data-name');
        const stock = btn.getAttribute('data-stock');
        openStockAdjustmentModal(id, name, stock);
      });
    });
  }

  const prevBtn = document.getElementById('btn-stock-prev');
  const nextBtn = document.getElementById('btn-stock-next');
  if (prevBtn) prevBtn.addEventListener('click', () => { state.page--; loadTabData(); });
  if (nextBtn) nextBtn.addEventListener('click', () => { state.page++; loadTabData(); });

  if (window.lucide) window.lucide.createIcons();
}

function renderMovementsTable(container) {
  if (state.movements.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon"><i data-lucide="history"></i></div>
        <h4 class="fw-bold text-dark">No stock movements logged</h4>
        <p class="text-muted small">Stock adjustments and sales transactions will automatically populate here.</p>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const rows = state.movements.map(mov => {
    const isSale = mov.type === 'SALE';
    const typeBadge = isSale ? 'badge-danger-outline' : 'badge-primary';

    return `
      <tr>
        <td class="small text-muted">${formatDate(mov.createdAt)}</td>
        <td>
          <div class="fw-bold">${escapeHtml(mov.medicineName)}</div>
          <div class="text-muted small">Batch: ${escapeHtml(mov.batchNumber)}</div>
        </td>
        <td>
          <span class="badge ${isSale ? 'bg-danger text-white' : 'bg-success text-white'}">${mov.type}</span>
        </td>
        <td class="fw-bold">${mov.previousStock} → ${mov.newStock}</td>
        <td class="fw-bold ${mov.newStock < mov.previousStock ? 'text-danger' : 'text-success'}">
          ${mov.newStock < mov.previousStock ? `-${mov.quantity}` : `+${mov.quantity}`}
        </td>
        <td class="small font-monospace">${escapeHtml(mov.referenceId || '—')}</td>
        <td class="small text-muted">${escapeHtml(mov.reason || '—')}</td>
        <td class="small text-muted">${escapeHtml(mov.createdBy || 'System')}</td>
      </tr>
    `;
  }).join('');

  container.innerHTML = `
    <div class="med-table-wrapper">
      <table class="med-table">
        <thead>
          <tr>
            <th>Timestamp</th>
            <th>Medicine</th>
            <th>Movement Type</th>
            <th>Stock Shift</th>
            <th>Change</th>
            <th>Reference ID</th>
            <th>Reason</th>
            <th>Logged By</th>
          </tr>
        </thead>
        <tbody>
          ${rows}
        </tbody>
      </table>
    </div>

    ${state.totalPages > 1 ? `
      <div class="p-3 border-top d-flex justify-content-between align-items-center">
        <span class="small text-muted">Page ${state.page + 1} of ${state.totalPages}</span>
        <div class="d-flex gap-2">
          <button class="btn btn-outline btn-sm" id="btn-mov-prev" ${state.page === 0 ? 'disabled' : ''}>Prev</button>
          <button class="btn btn-outline btn-sm" id="btn-mov-next" ${state.page >= state.totalPages - 1 ? 'disabled' : ''}>Next</button>
        </div>
      </div>
    ` : ''}
  `;

  const prevBtn = document.getElementById('btn-mov-prev');
  const nextBtn = document.getElementById('btn-mov-next');
  if (prevBtn) prevBtn.addEventListener('click', () => { state.page--; loadTabData(); });
  if (nextBtn) nextBtn.addEventListener('click', () => { state.page++; loadTabData(); });

  if (window.lucide) window.lucide.createIcons();
}

function openStockAdjustmentModal(medicineId, medicineName, currentStock) {
  const placeholder = document.getElementById('inventory-modal-placeholder');

  placeholder.innerHTML = `
    <div class="med-modal-overlay">
      <div class="med-modal">
        <div class="med-modal-header">
          <div class="med-modal-title">
            <i data-lucide="sliders" class="text-success"></i>
            <span>Adjust Inventory Stock</span>
          </div>
          <button class="btn btn-outline btn-sm" id="btn-close-adj">✕</button>
        </div>
        <form id="adjustment-form">
          <div class="med-modal-body">
            <div class="mb-3">
              <label class="form-label text-muted">Medicine</label>
              <div class="fw-bold fs-6">${escapeHtml(medicineName)}</div>
            </div>

            <div class="mb-3">
              <label class="form-label text-muted">Current On-Hand Stock</label>
              <div class="fw-bold fs-5 text-dark">${currentStock} Units</div>
            </div>

            <div class="form-group mb-3">
              <label class="form-label">New Corrected Stock *</label>
              <input type="number" min="0" class="form-input" id="adj-new-stock" required value="${currentStock}">
              <div class="form-text small text-muted">Enter physical count. Difference will generate an audit log.</div>
            </div>

            <div class="form-group mb-3">
              <label class="form-label">Reason for Adjustment *</label>
              <select class="form-select" id="adj-reason" required>
                <option value="Physical stock count">Physical stock count / Reconciliation</option>
                <option value="Damaged">Damaged / Broken packaging</option>
                <option value="Lost">Lost / Inventory shrinkage</option>
                <option value="Manual correction">Manual data entry correction</option>
                <option value="Other">Other reason</option>
              </select>
            </div>
          </div>
          <div class="med-modal-footer">
            <button type="button" class="btn btn-outline" id="btn-cancel-adj">Cancel</button>
            <button type="submit" class="btn btn-primary" id="btn-submit-adj">
              <i data-lucide="check"></i> Confirm Adjustment
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  const closeAdj = () => { placeholder.innerHTML = ''; };
  document.getElementById('btn-close-adj').addEventListener('click', closeAdj);
  document.getElementById('btn-cancel-adj').addEventListener('click', closeAdj);

  const form = document.getElementById('adjustment-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const newStock = parseInt(document.getElementById('adj-new-stock').value, 10);
    const reason = document.getElementById('adj-reason').value;
    const submitBtn = document.getElementById('btn-submit-adj');

    if (isNaN(newStock) || newStock < 0) {
      toast.error('Validation Error', 'New stock cannot be negative.');
      return;
    }

    try {
      submitBtn.disabled = true;
      submitBtn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Applying...`;

      await api.adjustStock({
        medicineId: Number(medicineId),
        newStock,
        reason
      });

      toast.success('Stock Adjusted', `Stock for "${medicineName}" updated to ${newStock}.`);
      closeAdj();
      loadSummary();
      loadTabData();
    } catch (err) {
      toast.error('Adjustment Failed', err.message);
      submitBtn.disabled = false;
      submitBtn.innerHTML = `<i data-lucide="check"></i> Confirm Adjustment`;
      if (window.lucide) window.lucide.createIcons();
    }
  });

  if (window.lucide) window.lucide.createIcons();
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
