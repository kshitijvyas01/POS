/* =================================================================
   MEDIPOINT MEDICINE MANAGEMENT MODULE (STEP 2)
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Features: Full CRUD, Search, Filter, Batch Uniqueness, Role Privacy
   ================================================================= */

import { api } from '../api.js';
import { auth } from '../auth.js';
import { toast } from '../components/toast.js';

let state = {
  medicines: [],
  page: 0,
  totalPages: 1,
  totalElements: 0,
  searchQuery: '',
  statusFilter: '',
  categoryFilter: '',
  loading: false,
  activeModal: null,
  selectedMedicine: null
};

export async function renderMedicinesPage() {
  const isOwner = auth.isOwner();

  return `
    <div class="medicines-container animate-fade-in">
      <!-- Page Header & Action Bar -->
      <div class="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-3">
        <div>
          <h2 class="fw-bold mb-1" style="color: var(--slate-900);">Medicines Directory</h2>
          <p class="text-muted small mb-0">Browse, search, and manage pharmaceutical stocks and batch information.</p>
        </div>
        <div class="d-flex gap-2">
          ${isOwner ? `
            <button class="btn btn-primary shadow-sm" id="btn-add-medicine">
              <i data-lucide="plus-circle"></i>
              <span>Add Medicine</span>
            </button>
          ` : ''}
        </div>
      </div>

      <!-- Filters & Search Toolbar -->
      <div class="med-card mb-4 p-3">
        <div class="d-flex flex-wrap gap-3 align-items-center justify-content-between">
          <div class="d-flex flex-wrap gap-2 flex-grow-1 align-items-center" style="max-width: 600px;">
            <div class="header-search flex-grow-1" style="width: auto; min-width: 240px;">
              <i data-lucide="search"></i>
              <input type="text" id="medicine-search-input" placeholder="Search by name, generic, brand, batch..." value="${escapeHtml(state.searchQuery)}">
            </div>
            <select class="form-select w-auto" id="medicine-status-filter" style="min-width: 130px; font-size: 0.875rem;">
              <option value="">All Statuses</option>
              <option value="ACTIVE" ${state.statusFilter === 'ACTIVE' ? 'selected' : ''}>Active</option>
              <option value="INACTIVE" ${state.statusFilter === 'INACTIVE' ? 'selected' : ''}>Inactive</option>
            </select>
          </div>
          <div class="text-muted small" id="medicine-count-badge">
            Loading catalog...
          </div>
        </div>
      </div>

      <!-- Main Medicine Table Container -->
      <div class="med-card" id="medicine-table-container">
        <div class="p-5 text-center">
          <div class="spinner-border text-success" role="status"></div>
          <p class="mt-2 text-muted small">Loading medicines from MySQL database...</p>
        </div>
      </div>

      <!-- Modal Container Placeholder -->
      <div id="medicine-modal-placeholder"></div>
    </div>
  `;
}

export function attachMedicinesEvents() {
  const searchInput = document.getElementById('medicine-search-input');
  const statusFilter = document.getElementById('medicine-status-filter');
  const btnAdd = document.getElementById('btn-add-medicine');

  let debounceTimer = null;
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        state.searchQuery = e.target.value;
        state.page = 0;
        loadMedicines();
      }, 300);
    });
  }

  if (statusFilter) {
    statusFilter.addEventListener('change', (e) => {
      state.statusFilter = e.target.value;
      state.page = 0;
      loadMedicines();
    });
  }

  if (btnAdd) {
    btnAdd.addEventListener('click', () => openMedicineFormModal());
  }

  loadMedicines();
}

async function loadMedicines() {
  const container = document.getElementById('medicine-table-container');
  const countBadge = document.getElementById('medicine-count-badge');
  if (!container) return;

  try {
    state.loading = true;
    const response = await api.getMedicines({
      query: state.searchQuery,
      status: state.statusFilter,
      page: state.page,
      size: 15
    });

    const pageData = response.data;
    state.medicines = pageData.content || [];
    state.totalPages = pageData.totalPages || 1;
    state.totalElements = pageData.totalElements || 0;

    if (countBadge) {
      countBadge.textContent = `Showing ${state.medicines.length} of ${state.totalElements} medicines`;
    }

    renderMedicineTable(container);
  } catch (error) {
    console.error("Failed to load medicines:", error);
    container.innerHTML = `
      <div class="p-5 text-center text-danger">
        <i data-lucide="alert-circle" style="width: 48px; height: 48px; margin-bottom: 0.5rem;"></i>
        <div class="fw-bold">Unable to load medicines</div>
        <div class="small text-muted mt-1">${escapeHtml(error.message)}</div>
        <button class="btn btn-outline btn-sm mt-3" id="btn-retry-medicines">Retry</button>
      </div>
    `;
    const retryBtn = document.getElementById('btn-retry-medicines');
    if (retryBtn) retryBtn.addEventListener('click', () => loadMedicines());
    if (window.lucide) window.lucide.createIcons();
  } finally {
    state.loading = false;
  }
}

function renderMedicineTable(container) {
  const isOwner = auth.isOwner();

  if (state.medicines.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">
          <i data-lucide="package-x"></i>
        </div>
        <h4 class="fw-bold text-dark">No medicines found</h4>
        <p class="text-muted small mb-3">
          ${state.searchQuery ? `No matches found for "${escapeHtml(state.searchQuery)}". Try another search.` : 'Your medicine inventory catalog is currently empty.'}
        </p>
        ${isOwner ? `
          <button class="btn btn-primary" id="btn-empty-add-medicine">
            <i data-lucide="plus"></i> Add First Medicine
          </button>
        ` : ''}
      </div>
    `;
    const emptyAddBtn = document.getElementById('btn-empty-add-medicine');
    if (emptyAddBtn) emptyAddBtn.addEventListener('click', () => openMedicineFormModal());
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const tableRows = state.medicines.map(m => {
    let stockBadgeClass = 'badge-instock';
    let stockBadgeText = 'In Stock';
    if (m.stockStatus === 'EXPIRED') {
      stockBadgeClass = 'badge-expired';
      stockBadgeText = 'Expired';
    } else if (m.stockStatus === 'OUT_OF_STOCK') {
      stockBadgeClass = 'badge-outofstock';
      stockBadgeText = 'Out of Stock';
    } else if (m.stockStatus === 'LOW_STOCK') {
      stockBadgeClass = 'badge-lowstock';
      stockBadgeText = 'Low Stock';
    }

    const isExpSoon = m.expiringSoon;
    const isExp = m.expired;
    let expBadge = '';
    if (isExp) {
      expBadge = `<span class="badge-status badge-expired ms-1">Expired</span>`;
    } else if (isExpSoon) {
      expBadge = `<span class="badge-status badge-lowstock ms-1">Expiring Soon</span>`;
    }

    const statusBadgeClass = m.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive';

    return `
      <tr data-id="${m.id}">
        <td>
          <div class="fw-bold text-dark">${escapeHtml(m.name)}</div>
          <div class="text-muted small" style="font-size: 0.75rem;">
            ${m.genericName ? escapeHtml(m.genericName) : '—'}
            ${m.category ? `<span class="badge bg-light text-secondary ms-1">${escapeHtml(m.category)}</span>` : ''}
          </div>
        </td>
        <td class="small">
          <div>${escapeHtml(m.brand || '—')}</div>
          <div class="text-muted" style="font-size: 0.75rem;">${escapeHtml(m.manufacturer || '')}</div>
        </td>
        <td><code class="text-dark fw-semibold">${escapeHtml(m.batchNumber)}</code></td>
        <td>
          <div class="small fw-semibold ${isExp ? 'text-danger' : (isExpSoon ? 'text-warning' : '')}">
            ${m.expiryDate || '—'}
          </div>
          ${expBadge}
        </td>
        <td class="fw-bold text-success">₹${Number(m.sellingPrice || 0).toFixed(2)}</td>
        ${isOwner ? `
          <td class="small text-muted">₹${m.purchasePrice != null ? Number(m.purchasePrice).toFixed(2) : '—'}</td>
        ` : ''}
        <td>
          <div class="fw-bold">${m.currentStock} <span class="text-muted small fw-normal">${escapeHtml(m.unit || '')}</span></div>
          <span class="badge-status ${stockBadgeClass}">${stockBadgeText}</span>
        </td>
        <td>
          <span class="badge-status ${statusBadgeClass}">${escapeHtml(m.status)}</span>
        </td>
        <td class="text-end">
          <div class="d-inline-flex gap-1">
            <button class="btn btn-outline btn-sm action-view" title="View Details" data-id="${m.id}">
              <i data-lucide="eye" style="width: 14px; height: 14px;"></i>
            </button>
            ${isOwner ? `
              <button class="btn btn-outline btn-sm action-edit" title="Edit Medicine" data-id="${m.id}">
                <i data-lucide="edit-3" style="width: 14px; height: 14px;"></i>
              </button>
              <button class="btn ${m.status === 'ACTIVE' ? 'btn-danger-outline' : 'btn-outline'} btn-sm action-toggle" title="${m.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}" data-id="${m.id}">
                <i data-lucide="${m.status === 'ACTIVE' ? 'power-off' : 'check'}" style="width: 14px; height: 14px;"></i>
              </button>
            ` : ''}
          </div>
        </td>
      </tr>
    `;
  }).join('');

  container.innerHTML = `
    <div class="med-table-wrapper">
      <table class="med-table">
        <thead>
          <tr>
            <th>Medicine</th>
            <th>Brand / Maker</th>
            <th>Batch</th>
            <th>Expiry</th>
            <th>Selling Price</th>
            ${isOwner ? `<th>Purchase Cost</th>` : ''}
            <th>Stock</th>
            <th>Status</th>
            <th class="text-end">Actions</th>
          </tr>
        </thead>
        <tbody>
          ${tableRows}
        </tbody>
      </table>
    </div>

    <!-- Pagination Controls -->
    ${state.totalPages > 1 ? `
      <div class="p-3 border-top d-flex align-items-center justify-content-between">
        <span class="small text-muted">Page ${state.page + 1} of ${state.totalPages}</span>
        <div class="d-flex gap-2">
          <button class="btn btn-outline btn-sm" id="btn-prev-page" ${state.page === 0 ? 'disabled' : ''}>
            <i data-lucide="chevron-left" style="width: 14px; height: 14px;"></i> Prev
          </button>
          <button class="btn btn-outline btn-sm" id="btn-next-page" ${state.page >= state.totalPages - 1 ? 'disabled' : ''}>
            Next <i data-lucide="chevron-right" style="width: 14px; height: 14px;"></i>
          </button>
        </div>
      </div>
    ` : ''}
  `;

  // Attach Table Action Listeners
  container.querySelectorAll('.action-view').forEach(btn => {
    btn.addEventListener('click', () => {
      const id = btn.getAttribute('data-id');
      const med = state.medicines.find(m => String(m.id) === id);
      if (med) openMedicineDetailsModal(med);
    });
  });

  if (isOwner) {
    container.querySelectorAll('.action-edit').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.getAttribute('data-id');
        const med = state.medicines.find(m => String(m.id) === id);
        if (med) openMedicineFormModal(med);
      });
    });

    container.querySelectorAll('.action-toggle').forEach(btn => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-id');
        try {
          const res = await api.toggleMedicineStatus(id);
          toast.success('Status Updated', res.message || 'Medicine status updated.');
          loadMedicines();
        } catch (err) {
          toast.error('Operation Failed', err.message);
        }
      });
    });
  }

  const prevBtn = document.getElementById('btn-prev-page');
  const nextBtn = document.getElementById('btn-next-page');
  if (prevBtn) prevBtn.addEventListener('click', () => { state.page--; loadMedicines(); });
  if (nextBtn) nextBtn.addEventListener('click', () => { state.page++; loadMedicines(); });

  if (window.lucide) window.lucide.createIcons();
}

function openMedicineDetailsModal(m) {
  const placeholder = document.getElementById('medicine-modal-placeholder');
  const isOwner = auth.isOwner();

  placeholder.innerHTML = `
    <div class="med-modal-overlay" id="details-modal-overlay">
      <div class="med-modal">
        <div class="med-modal-header">
          <div class="med-modal-title">
            <i data-lucide="pill" class="text-success"></i>
            <span>${escapeHtml(m.name)}</span>
          </div>
          <button class="btn btn-outline btn-sm" id="btn-close-modal">✕</button>
        </div>
        <div class="med-modal-body">
          <div class="row g-3">
            <div class="col-md-6">
              <label class="form-label text-muted">Generic Name</label>
              <div class="fw-semibold">${escapeHtml(m.genericName || 'None')}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Category</label>
              <div class="fw-semibold">${escapeHtml(m.category || 'Standard')}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Brand & Manufacturer</label>
              <div class="fw-semibold">${escapeHtml(m.brand || '—')} (${escapeHtml(m.manufacturer || '—')})</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Batch Number</label>
              <div class="fw-semibold font-monospace">${escapeHtml(m.batchNumber)}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Manufacturing Date</label>
              <div>${m.manufacturingDate || '—'}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Expiry Date</label>
              <div class="fw-bold ${m.expired ? 'text-danger' : (m.expiringSoon ? 'text-warning' : '')}">${m.expiryDate || '—'}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Selling Price</label>
              <div class="h5 fw-bold text-success mb-0">₹${Number(m.sellingPrice || 0).toFixed(2)}</div>
            </div>
            ${isOwner ? `
              <div class="col-md-6">
                <label class="form-label text-muted">Purchase Cost (Confidential)</label>
                <div class="h5 fw-bold text-dark mb-0">₹${Number(m.purchasePrice || 0).toFixed(2)}</div>
              </div>
            ` : ''}
            <div class="col-md-6">
              <label class="form-label text-muted">Current Stock</label>
              <div class="fw-bold">${m.currentStock} ${escapeHtml(m.unit || 'Units')}</div>
            </div>
            <div class="col-md-6">
              <label class="form-label text-muted">Minimum Reorder Level</label>
              <div>${m.minimumStock} Units</div>
            </div>
            <div class="col-12">
              <label class="form-label text-muted">Description / Notes</label>
              <div class="small p-2 bg-light rounded">${escapeHtml(m.description || 'No notes entered for this medicine.')}</div>
            </div>
          </div>
        </div>
        <div class="med-modal-footer">
          <button class="btn btn-outline" id="btn-modal-dismiss">Close</button>
        </div>
      </div>
    </div>
  `;

  document.getElementById('btn-close-modal').addEventListener('click', closeModal);
  document.getElementById('btn-modal-dismiss').addEventListener('click', closeModal);
  if (window.lucide) window.lucide.createIcons();
}

function openMedicineFormModal(medicine = null) {
  const placeholder = document.getElementById('medicine-modal-placeholder');
  const isEditing = medicine !== null;

  placeholder.innerHTML = `
    <div class="med-modal-overlay" id="form-modal-overlay">
      <div class="med-modal med-modal-lg">
        <div class="med-modal-header">
          <div class="med-modal-title">
            <i data-lucide="${isEditing ? 'edit-3' : 'plus-circle'}" class="text-success"></i>
            <span>${isEditing ? 'Edit Medicine' : 'Add New Medicine'}</span>
          </div>
          <button class="btn btn-outline btn-sm" id="btn-close-modal">✕</button>
        </div>
        <form id="medicine-form">
          <div class="med-modal-body">
            <div class="form-grid">
              <div class="form-group col-span-2">
                <label class="form-label">Medicine Name *</label>
                <input type="text" class="form-input" id="med-name" required placeholder="e.g. Paracetamol 500mg" value="${escapeHtml(medicine?.name || '')}">
              </div>

              <div class="form-group">
                <label class="form-label">Generic Name</label>
                <input type="text" class="form-input" id="med-generic" placeholder="e.g. Acetaminophen" value="${escapeHtml(medicine?.genericName || '')}">
              </div>

              <div class="form-group">
                <label class="form-label">Category</label>
                <select class="form-select" id="med-category">
                  ${['Pain Relief', 'Antibiotic', 'Vitamin', 'Cold & Cough', 'Digestive', 'First Aid', 'Diabetes', 'Allergy', 'Cardiovascular', 'Other'].map(cat => `
                    <option value="${cat}" ${medicine?.category === cat ? 'selected' : ''}>${cat}</option>
                  `).join('')}
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Brand Name</label>
                <input type="text" class="form-input" id="med-brand" placeholder="e.g. Crocin Advance" value="${escapeHtml(medicine?.brand || '')}">
              </div>

              <div class="form-group">
                <label class="form-label">Manufacturer</label>
                <input type="text" class="form-input" id="med-manufacturer" placeholder="e.g. GSK Healthcare" value="${escapeHtml(medicine?.manufacturer || '')}">
              </div>

              <div class="form-group">
                <label class="form-label">Batch Number *</label>
                <input type="text" class="form-input" id="med-batch" required placeholder="e.g. PCM-2024-A1" value="${escapeHtml(medicine?.batchNumber || '')}">
              </div>

              <div class="form-group">
                <label class="form-label">Unit of Measure</label>
                <select class="form-select" id="med-unit">
                  ${['Strip', 'Tablet', 'Bottle', 'Box', 'Injection', 'Sachet', 'Ointment'].map(u => `
                    <option value="${u}" ${medicine?.unit === u ? 'selected' : ''}>${u}</option>
                  `).join('')}
                </select>
              </div>

              <div class="form-group">
                <label class="form-label">Manufacturing Date</label>
                <input type="date" class="form-input" id="med-mfg" value="${medicine?.manufacturingDate || ''}">
              </div>

              <div class="form-group">
                <label class="form-label">Expiry Date *</label>
                <input type="date" class="form-input" id="med-expiry" required value="${medicine?.expiryDate || ''}">
              </div>

              <div class="form-group">
                <label class="form-label">Purchase Price (₹) *</label>
                <input type="number" step="0.01" min="0" class="form-input" id="med-purchase-price" required placeholder="0.00" value="${medicine?.purchasePrice != null ? medicine.purchasePrice : ''}">
              </div>

              <div class="form-group">
                <label class="form-label">Selling Price (₹) *</label>
                <input type="number" step="0.01" min="0" class="form-input" id="med-selling-price" required placeholder="0.00" value="${medicine?.sellingPrice != null ? medicine.sellingPrice : ''}">
              </div>

              <div class="form-group">
                <label class="form-label">Current Stock *</label>
                <input type="number" min="0" class="form-input" id="med-stock" required placeholder="0" value="${medicine?.currentStock != null ? medicine.currentStock : '0'}">
              </div>

              <div class="form-group">
                <label class="form-label">Minimum Stock Alert *</label>
                <input type="number" min="0" class="form-input" id="med-min-stock" required placeholder="10" value="${medicine?.minimumStock != null ? medicine.minimumStock : '10'}">
              </div>

              <div class="form-group col-span-2">
                <label class="form-label">Description / Storage Notes</label>
                <textarea class="form-textarea" id="med-desc" rows="2" placeholder="Storage instructions, precautions, notes...">${escapeHtml(medicine?.description || '')}</textarea>
              </div>
            </div>
          </div>
          <div class="med-modal-footer">
            <button type="button" class="btn btn-outline" id="btn-modal-cancel">Cancel</button>
            <button type="submit" class="btn btn-primary" id="btn-modal-save">
              <i data-lucide="save"></i>
              <span>${isEditing ? 'Save Changes' : 'Create Medicine'}</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  `;

  document.getElementById('btn-close-modal').addEventListener('click', closeModal);
  document.getElementById('btn-modal-cancel').addEventListener('click', closeModal);

  const form = document.getElementById('medicine-form');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const saveBtn = document.getElementById('btn-modal-save');

    // Validation
    const name = document.getElementById('med-name').value.trim();
    const batchNumber = document.getElementById('med-batch').value.trim();
    const expiryDate = document.getElementById('med-expiry').value;
    const mfgDate = document.getElementById('med-mfg').value || null;
    const purchasePrice = parseFloat(document.getElementById('med-purchase-price').value);
    const sellingPrice = parseFloat(document.getElementById('med-selling-price').value);
    const currentStock = parseInt(document.getElementById('med-stock').value, 10);
    const minimumStock = parseInt(document.getElementById('med-min-stock').value, 10);

    if (!name || !batchNumber || !expiryDate) {
      toast.error('Validation Error', 'Please complete all required fields (*)');
      return;
    }

    if (mfgDate && expiryDate && mfgDate > expiryDate) {
      toast.error('Validation Error', 'Manufacturing date cannot be after Expiry date.');
      return;
    }

    if (purchasePrice < 0 || sellingPrice < 0) {
      toast.error('Validation Error', 'Prices must be non-negative.');
      return;
    }

    if (currentStock < 0 || minimumStock < 0) {
      toast.error('Validation Error', 'Stock values cannot be negative.');
      return;
    }

    const payload = {
      name,
      genericName: document.getElementById('med-generic').value.trim(),
      category: document.getElementById('med-category').value,
      brand: document.getElementById('med-brand').value.trim(),
      manufacturer: document.getElementById('med-manufacturer').value.trim(),
      batchNumber,
      unit: document.getElementById('med-unit').value,
      manufacturingDate: mfgDate,
      expiryDate,
      purchasePrice,
      sellingPrice,
      currentStock,
      minimumStock,
      description: document.getElementById('med-desc').value.trim(),
      status: medicine?.status || 'ACTIVE'
    };

    try {
      saveBtn.disabled = true;
      saveBtn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Saving...`;

      if (isEditing) {
        await api.updateMedicine(medicine.id, payload);
        toast.success('Medicine Updated', `"${name}" updated successfully.`);
      } else {
        await api.createMedicine(payload);
        toast.success('Medicine Created', `"${name}" added to catalog.`);
      }

      closeModal();
      loadMedicines();
    } catch (err) {
      toast.error('Save Failed', err.message);
      saveBtn.disabled = false;
      saveBtn.innerHTML = `<i data-lucide="save"></i> <span>${isEditing ? 'Save Changes' : 'Create Medicine'}</span>`;
      if (window.lucide) window.lucide.createIcons();
    }
  });

  if (window.lucide) window.lucide.createIcons();
}

function closeModal() {
  const placeholder = document.getElementById('medicine-modal-placeholder');
  if (placeholder) placeholder.innerHTML = '';
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
