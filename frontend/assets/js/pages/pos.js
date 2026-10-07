/* =================================================================
   MEDIPOINT POINT OF SALE (POS) BILLING MODULE (STEP 2)
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Features: Fast Search, Live Cart, Stock Safety, Real-Time Calculations
   ================================================================= */

import { api } from '../api.js';
import { auth } from '../auth.js';
import { toast } from '../components/toast.js';

let posState = {
  searchQuery: '',
  searchResults: [],
  cart: [], // items: { medicineId, name, batchNumber, unit, sellingPrice, maxStock, quantity, subtotal }
  customer: {
    name: '',
    phone: '',
    email: ''
  },
  discount: 0,
  tax: 0,
  paymentMethod: 'CASH',
  paymentStatus: 'PAID',
  isCheckingOut: false
};

export async function renderPosPage() {
  return `
    <div class="pos-wrapper animate-fade-in">
      <div class="pos-container">
        <!-- LEFT: Medicine Search & Catalog Grid -->
        <div class="pos-left">
          <!-- Search Header -->
          <div class="pos-search-header">
            <i data-lucide="search" class="pos-search-icon"></i>
            <input type="text" id="pos-search-input" class="pos-search-input" placeholder="Scan barcode or type medicine name, generic, brand, batch..." autofocus>
          </div>

          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="small fw-semibold text-muted text-uppercase" id="pos-catalog-status">Available Medicines for Sale</div>
            <span class="badge bg-light text-muted small" id="pos-result-count">Showing quick catalog</span>
          </div>

          <!-- Product Grid -->
          <div class="pos-product-grid" id="pos-product-grid">
            <div class="p-5 text-center text-muted col-span-full">
              <div class="spinner-border text-success spinner-border-sm mb-2"></div>
              <div>Loading pharmacy inventory...</div>
            </div>
          </div>
        </div>

        <!-- RIGHT: Live Billing Cart & Checkout -->
        <div class="pos-right">
          <div class="pos-cart-panel">
            <div class="med-card-header bg-light">
              <div class="fw-bold d-flex align-items-center gap-2">
                <i data-lucide="shopping-cart" class="text-success"></i>
                <span>Active Cart (<span id="cart-items-count">0</span>)</span>
              </div>
              <button class="btn btn-outline btn-sm text-danger" id="btn-clear-cart" title="Clear Cart">
                <i data-lucide="trash-2" style="width: 14px; height: 14px;"></i> Clear
              </button>
            </div>

            <!-- Cart Items List -->
            <div class="pos-cart-items" id="pos-cart-items-list">
              <div class="empty-state p-4 text-center my-auto">
                <div class="empty-state-icon" style="width: 48px; height: 48px;">
                  <i data-lucide="shopping-bag"></i>
                </div>
                <div class="fw-semibold text-dark">Cart is Empty</div>
                <div class="small text-muted">Select medicines from left catalog to add to bill.</div>
              </div>
            </div>

            <!-- Customer Details Accordion/Section -->
            <div class="p-3 border-top border-bottom bg-white">
              <div class="d-flex justify-content-between align-items-center mb-2">
                <span class="small fw-bold text-uppercase text-muted">Customer Information</span>
                <span class="badge bg-light text-muted small">Optional</span>
              </div>
              <div class="row g-2">
                <div class="col-7">
                  <input type="text" class="form-input form-control-sm" id="pos-cust-name" placeholder="Walk-in Customer" value="${escapeHtml(posState.customer.name)}">
                </div>
                <div class="col-5">
                  <input type="tel" class="form-input form-control-sm" id="pos-cust-phone" placeholder="Phone (optional)" value="${escapeHtml(posState.customer.phone)}">
                </div>
              </div>
            </div>

            <!-- Payment & Totals -->
            <div class="pos-bill-summary">
              <div class="bill-row">
                <span>Subtotal</span>
                <span class="fw-semibold" id="bill-subtotal">₹0.00</span>
              </div>
              <div class="bill-row align-items-center">
                <span>Discount (₹)</span>
                <input type="number" min="0" step="1" id="bill-discount-input" class="form-input text-end" style="width: 90px; padding: 0.2rem 0.4rem; height: 28px;" value="${posState.discount}">
              </div>
              <div class="bill-row total">
                <span>Total Payable</span>
                <span id="bill-grand-total">₹0.00</span>
              </div>

              <!-- Payment Method Selector -->
              <div class="mt-3">
                <label class="form-label small mb-1">Payment Method</label>
                <div class="d-flex gap-2">
                  ${['CASH', 'UPI', 'CARD', 'OTHER'].map(pm => `
                    <button type="button" class="btn btn-outline btn-sm flex-grow-1 payment-method-btn ${posState.paymentMethod === pm ? 'btn-primary text-white' : ''}" data-method="${pm}">
                      ${pm}
                    </button>
                  `).join('')}
                </div>
              </div>

              <!-- Checkout Button -->
              <button class="btn btn-primary w-100 mt-3 py-2 fw-bold fs-6 shadow" id="btn-complete-sale">
                <i data-lucide="check-circle-2"></i> COMPLETE SALE
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Receipt / Success Modal Placeholder -->
      <div id="pos-modal-placeholder"></div>
    </div>
  `;
}

export function attachPosEvents() {
  const searchInput = document.getElementById('pos-search-input');
  const clearBtn = document.getElementById('btn-clear-cart');
  const discountInput = document.getElementById('bill-discount-input');
  const checkoutBtn = document.getElementById('btn-complete-sale');
  const custNameInput = document.getElementById('pos-cust-name');
  const custPhoneInput = document.getElementById('pos-cust-phone');

  // Search input with debouncing
  let searchTimer = null;
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      clearTimeout(searchTimer);
      searchTimer = setTimeout(() => {
        posState.searchQuery = e.target.value.trim();
        loadPosCatalog();
      }, 250);
    });
  }

  if (clearBtn) {
    clearBtn.addEventListener('click', () => {
      posState.cart = [];
      updateCartUI();
    });
  }

  if (discountInput) {
    discountInput.addEventListener('input', (e) => {
      const val = parseFloat(e.target.value);
      posState.discount = isNaN(val) || val < 0 ? 0 : val;
      calculateTotals();
    });
  }

  if (custNameInput) {
    custNameInput.addEventListener('input', (e) => {
      posState.customer.name = e.target.value;
    });
  }

  if (custPhoneInput) {
    custPhoneInput.addEventListener('input', (e) => {
      posState.customer.phone = e.target.value;
    });
  }

  document.querySelectorAll('.payment-method-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.payment-method-btn').forEach(b => {
        b.classList.remove('btn-primary', 'text-white');
        b.classList.add('btn-outline');
      });
      btn.classList.remove('btn-outline');
      btn.classList.add('btn-primary', 'text-white');
      posState.paymentMethod = btn.getAttribute('data-method');
    });
  });

  if (checkoutBtn) {
    checkoutBtn.addEventListener('click', handleCheckout);
  }

  loadPosCatalog();
  updateCartUI();
}

async function loadPosCatalog() {
  const grid = document.getElementById('pos-product-grid');
  const resultBadge = document.getElementById('pos-result-count');
  if (!grid) return;

  try {
    const res = await api.searchPosMedicines(posState.searchQuery);
    posState.searchResults = res.data || [];

    if (resultBadge) {
      resultBadge.textContent = `${posState.searchResults.length} medicines available`;
    }

    renderCatalogGrid(grid);
  } catch (err) {
    console.error("Failed to load POS catalog:", err);
    grid.innerHTML = `
      <div class="col-span-full p-4 text-center text-danger">
        <i data-lucide="alert-circle" class="mb-1"></i>
        <div>Failed to search medicines: ${escapeHtml(err.message)}</div>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
  }
}

function renderCatalogGrid(grid) {
  if (posState.searchResults.length === 0) {
    grid.innerHTML = `
      <div class="col-span-full p-5 text-center text-muted">
        <i data-lucide="package-search" style="width: 48px; height: 48px; margin-bottom: 0.5rem; opacity: 0.5;"></i>
        <div class="fw-semibold">No medicines found for sale</div>
        <div class="small">Try searching by generic name, brand, or batch.</div>
      </div>
    `;
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const cards = posState.searchResults.map(m => {
    const isOut = m.currentStock <= 0;
    const isExp = m.expired;
    const isExpSoon = m.expiringSoon;
    const isDisabled = isOut || isExp;

    return `
      <div class="pos-product-card ${isDisabled ? 'opacity-50' : ''}" data-id="${m.id}">
        <div>
          <div class="d-flex justify-content-between align-items-start mb-1">
            <span class="badge bg-light text-secondary small">${escapeHtml(m.category || 'Medicine')}</span>
            <span class="badge ${isOut ? 'bg-danger text-white' : 'bg-success text-white'} small">
              ${m.currentStock} ${escapeHtml(m.unit || '')}
            </span>
          </div>
          <div class="fw-bold text-dark fs-6">${escapeHtml(m.name)}</div>
          <div class="small text-muted">${escapeHtml(m.genericName || '—')}</div>
          <div class="small text-muted font-monospace" style="font-size: 0.75rem;">Batch: ${escapeHtml(m.batchNumber)}</div>
          <div class="small ${isExp ? 'text-danger fw-bold' : (isExpSoon ? 'text-warning' : 'text-muted')}" style="font-size: 0.75rem;">
            Exp: ${m.expiryDate || '—'}
          </div>
        </div>

        <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
          <div class="fw-bold text-success fs-5">₹${Number(m.sellingPrice || 0).toFixed(2)}</div>
          <button class="btn btn-primary btn-sm btn-add-to-cart" data-id="${m.id}" ${isDisabled ? 'disabled' : ''}>
            <i data-lucide="plus" style="width: 14px; height: 14px;"></i> Add
          </button>
        </div>
      </div>
    `;
  }).join('');

  grid.innerHTML = cards;

  grid.querySelectorAll('.pos-product-card').forEach(card => {
    card.addEventListener('click', (e) => {
      if (e.target.closest('button')) return; // handled by button
      const id = card.getAttribute('data-id');
      const med = posState.searchResults.find(m => String(m.id) === id);
      if (med && med.currentStock > 0 && !med.expired) {
        addToCart(med);
      }
    });
  });

  grid.querySelectorAll('.btn-add-to-cart').forEach(btn => {
    btn.addEventListener('click', (e) => {
      e.stopPropagation();
      const id = btn.getAttribute('data-id');
      const med = posState.searchResults.find(m => String(m.id) === id);
      if (med) addToCart(med);
    });
  });

  if (window.lucide) window.lucide.createIcons();
}

function addToCart(med) {
  const existing = posState.cart.find(item => item.medicineId === med.id);
  if (existing) {
    if (existing.quantity >= med.currentStock) {
      toast.warning('Stock Limit Reached', `Only ${med.currentStock} units available for ${med.name}.`);
      return;
    }
    existing.quantity += 1;
    existing.subtotal = existing.quantity * existing.sellingPrice;
  } else {
    posState.cart.push({
      medicineId: med.id,
      name: med.name,
      batchNumber: med.batchNumber,
      unit: med.unit,
      sellingPrice: Number(med.sellingPrice),
      maxStock: med.currentStock,
      quantity: 1,
      subtotal: Number(med.sellingPrice)
    });
  }

  updateCartUI();
}

function updateCartUI() {
  const container = document.getElementById('pos-cart-items-list');
  const countBadge = document.getElementById('cart-items-count');
  if (!container) return;

  const totalItemsCount = posState.cart.reduce((sum, item) => sum + item.quantity, 0);
  if (countBadge) countBadge.textContent = totalItemsCount;

  if (posState.cart.length === 0) {
    container.innerHTML = `
      <div class="empty-state p-4 text-center my-auto">
        <div class="empty-state-icon" style="width: 48px; height: 48px;">
          <i data-lucide="shopping-bag"></i>
        </div>
        <div class="fw-semibold text-dark">Cart is Empty</div>
        <div class="small text-muted">Select medicines from left catalog to add to bill.</div>
      </div>
    `;
    calculateTotals();
    if (window.lucide) window.lucide.createIcons();
    return;
  }

  const rows = posState.cart.map((item, idx) => `
    <div class="pos-cart-item">
      <div class="flex-grow-1" style="min-width: 0;">
        <div class="fw-bold text-dark text-truncate small">${escapeHtml(item.name)}</div>
        <div class="text-muted small" style="font-size: 0.72rem;">
          Batch: ${escapeHtml(item.batchNumber)} • ₹${item.sellingPrice.toFixed(2)}
        </div>
      </div>

      <div class="pos-qty-control">
        <button class="pos-qty-btn btn-qty-dec" data-idx="${idx}">-</button>
        <input type="number" min="1" max="${item.maxStock}" class="pos-qty-input inp-qty" data-idx="${idx}" value="${item.quantity}">
        <button class="pos-qty-btn btn-qty-inc" data-idx="${idx}">+</button>
      </div>

      <div class="text-end" style="min-width: 60px;">
        <div class="fw-bold text-success small">₹${item.subtotal.toFixed(2)}</div>
        <button class="btn btn-link p-0 text-danger text-decoration-none small btn-remove-item" data-idx="${idx}">✕</button>
      </div>
    </div>
  `).join('');

  container.innerHTML = rows;

  // Listeners for quantity controls
  container.querySelectorAll('.btn-qty-dec').forEach(btn => {
    btn.addEventListener('click', () => {
      const idx = parseInt(btn.getAttribute('data-idx'), 10);
      if (posState.cart[idx].quantity > 1) {
        posState.cart[idx].quantity--;
        posState.cart[idx].subtotal = posState.cart[idx].quantity * posState.cart[idx].sellingPrice;
        updateCartUI();
      } else {
        posState.cart.splice(idx, 1);
        updateCartUI();
      }
    });
  });

  container.querySelectorAll('.btn-qty-inc').forEach(btn => {
    btn.addEventListener('click', () => {
      const idx = parseInt(btn.getAttribute('data-idx'), 10);
      const item = posState.cart[idx];
      if (item.quantity < item.maxStock) {
        item.quantity++;
        item.subtotal = item.quantity * item.sellingPrice;
        updateCartUI();
      } else {
        toast.warning('Max Stock', `Cannot exceed available stock of ${item.maxStock}.`);
      }
    });
  });

  container.querySelectorAll('.inp-qty').forEach(inp => {
    inp.addEventListener('change', () => {
      const idx = parseInt(inp.getAttribute('data-idx'), 10);
      let val = parseInt(inp.value, 10);
      const item = posState.cart[idx];
      if (isNaN(val) || val <= 0) val = 1;
      if (val > item.maxStock) {
        val = item.maxStock;
        toast.warning('Stock Limit', `Adjusted to maximum available stock: ${item.maxStock}.`);
      }
      item.quantity = val;
      item.subtotal = item.quantity * item.sellingPrice;
      updateCartUI();
    });
  });

  container.querySelectorAll('.btn-remove-item').forEach(btn => {
    btn.addEventListener('click', () => {
      const idx = parseInt(btn.getAttribute('data-idx'), 10);
      posState.cart.splice(idx, 1);
      updateCartUI();
    });
  });

  calculateTotals();
  if (window.lucide) window.lucide.createIcons();
}

function calculateTotals() {
  const subtotal = posState.cart.reduce((sum, item) => sum + item.subtotal, 0);
  const discount = Math.min(posState.discount, subtotal);
  const total = Math.max(0, subtotal - discount + posState.tax);

  const subEl = document.getElementById('bill-subtotal');
  const totEl = document.getElementById('bill-grand-total');

  if (subEl) subEl.textContent = `₹${subtotal.toFixed(2)}`;
  if (totEl) totEl.textContent = `₹${total.toFixed(2)}`;
}

async function handleCheckout() {
  if (posState.cart.length === 0) {
    toast.error('Empty Cart', 'Please add at least one medicine to the cart before checking out.');
    return;
  }

  const checkoutBtn = document.getElementById('btn-complete-sale');
  try {
    checkoutBtn.disabled = true;
    checkoutBtn.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Processing Sale...`;

    const payload = {
      customerName: posState.customer.name.trim() || 'Walk-in Customer',
      customerPhone: posState.customer.phone.trim() || null,
      customerEmail: posState.customer.email.trim() || null,
      discount: posState.discount,
      tax: posState.tax,
      paymentMethod: posState.paymentMethod,
      paymentStatus: posState.paymentStatus,
      items: posState.cart.map(item => ({
        medicineId: item.medicineId,
        quantity: item.quantity
      }))
    };

    const res = await api.checkoutSale(payload);
    const sale = res.data;

    toast.success('Sale Completed', `Invoice ${sale.invoiceNumber} created successfully.`);

    // Show Invoice / Receipt Modal
    showReceiptModal(sale);

    // Reset Cart & Customer
    posState.cart = [];
    posState.customer = { name: '', phone: '', email: '' };
    posState.discount = 0;
    const discInput = document.getElementById('bill-discount-input');
    if (discInput) discInput.value = 0;
    const nameInput = document.getElementById('pos-cust-name');
    if (nameInput) nameInput.value = '';
    const phoneInput = document.getElementById('pos-cust-phone');
    if (phoneInput) phoneInput.value = '';

    updateCartUI();
    loadPosCatalog(); // Refresh available stocks
  } catch (err) {
    console.error("Checkout failed:", err);
    toast.error('Checkout Failed', err.message);
  } finally {
    checkoutBtn.disabled = false;
    checkoutBtn.innerHTML = `<i data-lucide="check-circle-2"></i> COMPLETE SALE`;
    if (window.lucide) window.lucide.createIcons();
  }
}

function showReceiptModal(sale) {
  const placeholder = document.getElementById('pos-modal-placeholder');
  const isOwner = auth.isOwner();

  const itemRows = (sale.items || []).map(item => `
    <tr>
      <td>
        <div class="fw-bold">${escapeHtml(item.medicineName)}</div>
        <div class="small text-muted font-monospace">Batch: ${escapeHtml(item.batchNumber)}</div>
      </td>
      <td class="text-center">${item.quantity}</td>
      <td class="text-end">₹${Number(item.sellingPrice).toFixed(2)}</td>
      <td class="text-end fw-semibold">₹${Number(item.subtotal).toFixed(2)}</td>
    </tr>
  `).join('');

  placeholder.innerHTML = `
    <div class="med-modal-overlay">
      <div class="med-modal" style="max-width: 520px;">
        <div class="med-modal-header bg-success text-white">
          <div class="med-modal-title text-white">
            <i data-lucide="check-check"></i>
            <span>Sale Completed Successfully</span>
          </div>
          <button class="btn btn-sm btn-outline text-white border-white" id="btn-close-receipt">✕</button>
        </div>

        <div class="med-modal-body p-4" id="printable-receipt">
          <div class="text-center mb-3">
            <h4 class="fw-bold mb-0" style="color: var(--primary-800);">MediPoint Pharmacy</h4>
            <div class="small text-muted">Smarter Pharmacy. Better Control.</div>
            <div class="small text-muted mt-1">Tax Invoice • Retail Sale</div>
          </div>

          <div class="d-flex justify-content-between small text-muted border-top border-bottom py-2 my-2">
            <div>
              <div><strong>Invoice:</strong> <span class="font-monospace">${escapeHtml(sale.invoiceNumber)}</span></div>
              <div><strong>Date:</strong> ${formatDate(sale.saleDate)}</div>
            </div>
            <div class="text-end">
              <div><strong>Customer:</strong> ${escapeHtml(sale.customerName || 'Walk-in')}</div>
              <div><strong>Billed By:</strong> ${escapeHtml(sale.workerName || 'Cashier')}</div>
            </div>
          </div>

          <table class="table table-sm mt-3" style="font-size: 0.85rem;">
            <thead>
              <tr class="border-bottom">
                <th>Item</th>
                <th class="text-center">Qty</th>
                <th class="text-end">Price</th>
                <th class="text-end">Total</th>
              </tr>
            </thead>
            <tbody>
              ${itemRows}
            </tbody>
          </table>

          <div class="border-top pt-2">
            <div class="d-flex justify-content-between small">
              <span>Subtotal:</span>
              <span>₹${Number(sale.subtotal).toFixed(2)}</span>
            </div>
            ${sale.discount > 0 ? `
              <div class="d-flex justify-content-between small text-danger">
                <span>Discount:</span>
                <span>-₹${Number(sale.discount).toFixed(2)}</span>
              </div>
            ` : ''}
            <div class="d-flex justify-content-between fw-bold fs-5 mt-2 text-success border-top pt-2">
              <span>Grand Total:</span>
              <span>₹${Number(sale.totalAmount).toFixed(2)}</span>
            </div>
            <div class="d-flex justify-content-between small text-muted mt-1">
              <span>Payment: ${sale.paymentMethod}</span>
              <span class="badge bg-success text-white">${sale.paymentStatus}</span>
            </div>
          </div>

          <div class="text-center small text-muted mt-4 border-top pt-2">
            <div>Thank you for choosing MediPoint Pharmacy!</div>
            <div>Medicines once sold cannot be returned without original batch verification.</div>
          </div>
        </div>

        <div class="med-modal-footer">
          <button class="btn btn-outline" id="btn-print-receipt">
            <i data-lucide="printer"></i> Print Invoice
          </button>
          <button class="btn btn-primary" id="btn-new-sale">
            <i data-lucide="plus"></i> New Sale
          </button>
        </div>
      </div>
    </div>
  `;

  const closeReceipt = () => { placeholder.innerHTML = ''; };
  document.getElementById('btn-close-receipt').addEventListener('click', closeReceipt);
  document.getElementById('btn-new-sale').addEventListener('click', closeReceipt);

  document.getElementById('btn-print-receipt').addEventListener('click', () => {
    window.print();
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
