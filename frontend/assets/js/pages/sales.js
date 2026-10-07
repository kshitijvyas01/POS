/* =================================================================
   MEDIPOINT SALES MANAGEMENT MODULE (STEP 2)
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Features: Sales Register, Invoice Lookup, Modal Details, Owner Profit Privacy
   ================================================================= */

import { api } from '../api.js';
import { auth } from '../auth.js';
import { toast } from '../components/toast.js';

let state = {
  sales: [],
  page: 0,
  totalPages: 1,
  totalElements: 0,
  searchQuery: '',
  loading: false
};

export async function renderSalesPage() {
  const isOwner = auth.isOwner();

  return `
    <div class="sales-container animate-fade-in">
      <!-- Header -->
      <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <h2 class="fw-bold mb-1" style="color: var(--slate-900);">Sales Register & Invoices</h2>
          <p class="text-muted small mb-0">Browse historical sales records, customer invoices, and audit transactional details.</p>
        </div>
        <div>
          <a href="#pos" class="btn btn-primary shadow-sm">
            <i data-lucide="plus-circle"></i>
            <span>Open POS Terminal</span>
          </a>
        </div>
      </div>

      <!-- Search Toolbar -->
      <div class="med-card mb-4 p-3">
        <div class="d-flex flex-wrap gap-3 align-items-center justify-content-between">
          <div class="header-search" style="min-width: 300px;">
            <i data-lucide="search"></i>
            <input type="text" id="sales-search-input" placeholder="Search by invoice number, customer name or phone..." value="${escapeHtml(state.searchQuery)}">
          </div>
          <div class="small text-muted" id="sales-count-badge">
            Loading sales register...
          </div>
        </div>
      </div>

      <!-- Sales Table Card -->
      <div class="med-card" id="sales-table-container">
        <div class="p-5 text-center">
          <div class="spinner-border text-success" role="status"></div>
          <p class="mt-2 text-muted small">Loading sales from MySQL database...</p>
        </div>
      </div>

      <!-- Details Modal Placeholder -->
      <div id="sales-modal-placeholder"></div>
    </div>
  `;
}

export function attachSalesEvents() {
  const searchInput = document.getElementById('sales-search-input');

  let timer = null;
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(timer);
      timer = setTimeout(() => {
        state.searchQuery = e.target.value.trim();
        state.page = 0;
        loadSales();
      }, 300);
    });
  }

  loadSales();
}

async function loadSales() {
  const container = document.getElementById('sales-table-container');
  const countBadge = document.getElementById('sales-count-badge');
  if (!container) return;

  try {
    state.loading = true;
    const res = await api.getSales({
      query: state.searchQuery,
      page: state.page,
      size: 15
    });

    const pageData = res.data;
    state.sales = pageData.content || [];
    state.totalPages = pageData.totalPages || 1;
    state.totalElements = pageData.totalElements || 0;

    if (countBadge) {
      countBadge.textContent = `Showing ${state.sales.length} of ${state.totalElements} sales records`;
    }

    renderSalesTable(container);
  } catch (err) {
    console.error("Failed to load sales:", err);
    container.innerHTML = `
      <div class="p-5 text-center text-danger">
        <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 0.5rem;"></i>
        <div class="fw-bold">Unable to load sales records</div>
        <div class="small text-muted mt-1">${escapeHtml(err.message)}</div>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
  } finally {
    state.loading = false;
  }
}

function renderSalesTable(container) {
  const isOwner = auth.isOwner();

  if (state.sales.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon"><i data-lucide="receipt"></i></div>
        <h4 class="fw-bold text-dark">No sales records found</h4>
        <p class="text-muted small mb-3">
          ${state.searchQuery ? `No sales match "${escapeHtml(state.searchQuery)}".` : 'No billing transactions recorded yet.'}
        </p>
        <a href="#pos" class="btn btn-primary">Go to POS</a>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const rows = state.sales.map(s => {
    return `
      <tr>
        <td>
          <div class="fw-bold font-monospace text-dark">${escapeHtml(s.invoiceNumber)}</div>
          <div class="small text-muted">${formatDate(s.saleDate)}</div>
        </td>
        <td>
          <div class="fw-semibold">${escapeHtml(s.customerName || 'Walk-in')}</div>
          <div class="small text-muted">${escapeHtml(s.customerPhone || '—')}</div>
        </td>
        <td class="small text-muted">${escapeHtml(s.workerName || 'Cashier')}</td>
        <td class="text-center fw-semibold">${s.itemsCount}</td>
        <td class="fw-bold text-success fs-6">₹${Number(s.totalAmount).toFixed(2)}</td>
        ${isOwner ? `
          <td class="fw-bold text-dark small">
            ${s.totalProfit != null ? `₹${Number(s.totalProfit).toFixed(2)}` : '—'}
          </td>
        ` : ''}
        <td>
          <span class="badge bg-light text-dark">${s.paymentMethod}</span>
        </td>
        <td>
          <span class="badge ${s.paymentStatus === 'PAID' ? 'bg-success' : 'bg-warning'} text-white">
            ${s.paymentStatus}
          </span>
        </td>
        <td class="text-end">
          <button class="btn btn-outline btn-sm btn-view-sale" data-id="${s.id}">
            <i data-lucide="eye" style="width: 14px; height: 14px;"></i> Details
          </button>
        </td>
      </tr>
    `;
  }).join('');

  container.innerHTML = `
    <div class="med-table-wrapper">
      <table class="med-table">
        <thead>
          <tr>
            <th>Invoice</th>
            <th>Customer</th>
            <th>Cashier</th>
            <th class="text-center">Items</th>
            <th>Total Amount</th>
            ${isOwner ? `<th>Net Profit</th>` : ''}
            <th>Payment</th>
            <th>Status</th>
            <th class="text-end">Actions</th>
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
          <button class="btn btn-outline btn-sm" id="btn-sales-prev" ${state.page === 0 ? 'disabled' : ''}>Prev</button>
          <button class="btn btn-outline btn-sm" id="btn-sales-next" ${state.page >= state.totalPages - 1 ? 'disabled' : ''}>Next</button>
        </div>
      </div>
    ` : ''}
  `;

  container.querySelectorAll('.btn-view-sale').forEach(btn => {
    btn.addEventListener('click', async () => {
      const id = btn.getAttribute('data-id');
      try {
        const res = await api.getSale(id);
        openSaleDetailsModal(res.data);
      } catch (err) {
        toast.error('Load Error', err.message);
      }
    });
  });

  const prevBtn = document.getElementById('btn-sales-prev');
  const nextBtn = document.getElementById('btn-sales-next');
  if (prevBtn) prevBtn.addEventListener('click', () => { state.page--; loadSales(); });
  if (nextBtn) nextBtn.addEventListener('click', () => { state.page++; loadSales(); });

  if (window.lucide) window.lucide.createIcons();
}

function openSaleDetailsModal(sale) {
  const placeholder = document.getElementById('sales-modal-placeholder');
  const isOwner = auth.isOwner();

  const itemRows = (sale.items || []).map(item => `
    <tr>
      <td>
        <div class="fw-bold text-dark">${escapeHtml(item.medicineName)}</div>
        <div class="small text-muted font-monospace">Batch: ${escapeHtml(item.batchNumber)}</div>
      </td>
      <td class="text-center">${item.quantity}</td>
      <td class="text-end">₹${Number(item.sellingPrice).toFixed(2)}</td>
      ${isOwner ? `
        <td class="text-end text-muted small">
          ${item.purchasePrice != null ? `₹${Number(item.purchasePrice).toFixed(2)}` : '—'}
        </td>
      ` : ''}
      <td class="text-end fw-semibold text-dark">₹${Number(item.subtotal).toFixed(2)}</td>
      ${isOwner ? `
        <td class="text-end fw-bold text-success small">
          ${item.profit != null ? `₹${Number(item.profit).toFixed(2)}` : '—'}
        </td>
      ` : ''}
    </tr>
  `).join('');

  placeholder.innerHTML = `
    <div class="med-modal-overlay">
      <div class="med-modal med-modal-lg">
        <div class="med-modal-header">
          <div class="med-modal-title">
            <i data-lucide="receipt" class="text-success"></i>
            <span>Invoice ${escapeHtml(sale.invoiceNumber)}</span>
          </div>
          <button class="btn btn-outline btn-sm" id="btn-close-sale-modal">✕</button>
        </div>

        <div class="med-modal-body">
          <!-- Metadata Bar -->
          <div class="row g-3 p-3 bg-light rounded mb-3">
            <div class="col-md-3">
              <label class="form-label text-muted small">Invoice Number</label>
              <div class="fw-bold font-monospace text-dark">${escapeHtml(sale.invoiceNumber)}</div>
            </div>
            <div class="col-md-3">
              <label class="form-label text-muted small">Date & Time</label>
              <div class="small fw-semibold">${formatDate(sale.saleDate)}</div>
            </div>
            <div class="col-md-3">
              <label class="form-label text-muted small">Customer Name</label>
              <div class="fw-semibold">${escapeHtml(sale.customerName || 'Walk-in')}</div>
            </div>
            <div class="col-md-3">
              <label class="form-label text-muted small">Billed By</label>
              <div class="small">${escapeHtml(sale.workerName || 'Cashier')}</div>
            </div>
          </div>

          <!-- Items Table -->
          <div class="med-table-wrapper border rounded mb-3">
            <table class="med-table">
              <thead>
                <tr>
                  <th>Medicine</th>
                  <th class="text-center">Qty</th>
                  <th class="text-end">Selling Price</th>
                  ${isOwner ? `<th class="text-end">Cost Price</th>` : ''}
                  <th class="text-end">Subtotal</th>
                  ${isOwner ? `<th class="text-end">Profit</th>` : ''}
                </tr>
              </thead>
              <tbody>
                ${itemRows}
              </tbody>
            </table>
          </div>

          <!-- Totals Breakdown -->
          <div class="row justify-content-end">
            <div class="col-md-5">
              <div class="p-3 bg-light rounded">
                <div class="d-flex justify-content-between small mb-1">
                  <span>Subtotal:</span>
                  <span>₹${Number(sale.subtotal).toFixed(2)}</span>
                </div>
                ${sale.discount > 0 ? `
                  <div class="d-flex justify-content-between small mb-1 text-danger">
                    <span>Discount:</span>
                    <span>-₹${Number(sale.discount).toFixed(2)}</span>
                  </div>
                ` : ''}
                ${sale.tax > 0 ? `
                  <div class="d-flex justify-content-between small mb-1">
                    <span>Tax:</span>
                    <span>+₹${Number(sale.tax).toFixed(2)}</span>
                  </div>
                ` : ''}
                <div class="d-flex justify-content-between fw-bold fs-5 text-success border-top pt-2 mt-2">
                  <span>Grand Total:</span>
                  <span>₹${Number(sale.totalAmount).toFixed(2)}</span>
                </div>
                ${isOwner && sale.totalProfit != null ? `
                  <div class="d-flex justify-content-between fw-bold text-dark border-top pt-2 mt-2 small">
                    <span>Net Sale Profit:</span>
                    <span>₹${Number(sale.totalProfit).toFixed(2)}</span>
                  </div>
                ` : ''}
                <div class="d-flex justify-content-between small text-muted mt-2">
                  <span>Payment: ${sale.paymentMethod}</span>
                  <span class="badge bg-success text-white">${sale.paymentStatus}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="med-modal-footer">
          <button class="btn btn-outline" id="btn-print-invoice">
            <i data-lucide="printer"></i> Print Invoice
          </button>
          <button class="btn btn-primary" id="btn-dismiss-sale-modal">Done</button>
        </div>
      </div>
    </div>
  `;

  const close = () => { placeholder.innerHTML = ''; };
  document.getElementById('btn-close-sale-modal').addEventListener('click', close);
  document.getElementById('btn-dismiss-sale-modal').addEventListener('click', close);
  document.getElementById('btn-print-invoice').addEventListener('click', () => window.print());

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
