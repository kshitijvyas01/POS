/* =================================================================
   MEDIPOINT SETTINGS PAGE VIEW
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   Owner Access Only
   ================================================================= */

import { auth } from '../auth.js';
import { api } from '../api.js';
import { toast } from '../components/toast.js';

export async function renderSettingsPage() {
  const isOwner = auth.isOwner();

  if (!isOwner) {
    return `
      <div class="card-medipoint text-center py-5">
        <div style="width: 64px; height: 64px; border-radius: 50%; background-color: var(--danger-50); color: var(--danger-500); display: inline-flex; align-items: center; justify-content: center; margin-bottom: 1rem;">
          <i data-lucide="shield-alert" style="width: 32px; height: 32px;"></i>
        </div>
        <h3 style="color: var(--slate-900); font-weight: 700;">Access Restricted</h3>
        <p style="color: var(--slate-600); max-width: 480px; margin: 0.5rem auto 1.5rem;">
          Store settings are strictly managed by the <strong>Store Owner</strong>. Pharmacy Workers are not permitted to access or modify financial, tax, or store configurations.
        </p>
        <a href="#dashboard" class="btn-medipoint btn-medipoint-primary">
          <i data-lucide="arrow-left"></i> Return to Dashboard
        </a>
      </div>
    `;
  }

  let settings = {
    storeName: "MediPoint Pharmacy & Healthcare",
    tagline: "Smarter Pharmacy. Better Control.",
    address: "123 Healthcare Avenue, Medical Plaza, Metro City",
    phone: "+91 98765 43210",
    email: "contact@medipointstore.com",
    licenseNumber: "DL-2026-MP994821",
    gstNumber: "27AAAAA0000A1Z5",
    upiId: "medipoint@bank",
    invoicePrefix: "MP-INV-",
    invoiceFooter: "Thank you for choosing MediPoint Pharmacy. Wishing you good health!",
    updatedAt: "System Initialization",
    updatedBy: auth.getUser()?.email || "owner"
  };

  try {
    const res = await api.getSettings();
    if (res && res.data) {
      settings = res.data;
    }
  } catch (e) {
    console.warn("Using local settings state:", e.message);
  }

  return `
    <div class="row g-4">
      <div class="col-12">
        <div class="d-flex justify-content-between align-items-center mb-2">
          <div>
            <h3 style="margin: 0; font-weight: 800; color: var(--slate-900);">Store Configuration & Settings</h3>
            <p style="color: var(--slate-500); font-size: 0.875rem; margin: 0;">Owner control panel for MediPoint branding, payment details, and invoices</p>
          </div>
          <button type="button" id="btn-save-settings" class="btn-medipoint btn-medipoint-primary">
            <i data-lucide="save"></i> Save Settings
          </button>
        </div>
      </div>

      <div class="col-12 col-lg-8">
        <form id="settings-form" class="d-flex flex-column gap-4">

          <!-- Store Information Section -->
          <div class="card-medipoint">
            <div class="d-flex align-items-center gap-2 mb-3 pb-2 border-bottom">
              <i data-lucide="building" style="color: var(--primary-600);"></i>
              <h4 style="margin: 0; font-size: 1.125rem;">Store Profile & Contact</h4>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Store Name</label>
                <input type="text" id="setting-storeName" class="form-control" value="${settings.storeName || ''}" required />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Tagline</label>
                <input type="text" id="setting-tagline" class="form-control" value="${settings.tagline || ''}" required />
              </div>
              <div class="col-12">
                <label class="form-label fw-semibold text-dark small">Physical Address</label>
                <textarea id="setting-address" class="form-control" rows="2">${settings.address || ''}</textarea>
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Phone Number</label>
                <input type="text" id="setting-phone" class="form-control" value="${settings.phone || ''}" required />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Support Email</label>
                <input type="email" id="setting-email" class="form-control" value="${settings.email || ''}" required />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Drug License Number</label>
                <input type="text" id="setting-licenseNumber" class="form-control" value="${settings.licenseNumber || ''}" />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">GSTIN / Tax ID</label>
                <input type="text" id="setting-gstNumber" class="form-control" value="${settings.gstNumber || ''}" placeholder="e.g. 27AAAAA0000A1Z5" />
              </div>
            </div>
          </div>

          <!-- Payment Settings Section -->
          <div class="card-medipoint">
            <div class="d-flex align-items-center gap-2 mb-3 pb-2 border-bottom">
              <i data-lucide="credit-card" style="color: var(--primary-600);"></i>
              <h4 style="margin: 0; font-size: 1.125rem;">Payment Gateway & UPI Integration</h4>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">UPI Virtual Payment Address (VPA)</label>
                <input type="text" id="setting-upiId" class="form-control" value="${settings.upiId || ''}" placeholder="e.g. store@upi" />
              </div>
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Store QR Code</label>
                <div class="input-group">
                  <input type="file" class="form-control" id="setting-qrCodeFile" accept="image/*" />
                </div>
                <div class="form-text small">Store UPI QR image placeholder for POS receipt printing.</div>
              </div>
            </div>
          </div>

          <!-- Invoice Settings Section -->
          <div class="card-medipoint">
            <div class="d-flex align-items-center gap-2 mb-3 pb-2 border-bottom">
              <i data-lucide="file-text" style="color: var(--primary-600);"></i>
              <h4 style="margin: 0; font-size: 1.125rem;">Invoice & Receipt Options</h4>
            </div>

            <div class="row g-3">
              <div class="col-md-6">
                <label class="form-label fw-semibold text-dark small">Invoice Number Prefix</label>
                <input type="text" id="setting-invoicePrefix" class="form-control" value="${settings.invoicePrefix || 'MP-INV-'}" placeholder="MP-INV-" />
              </div>
              <div class="col-12">
                <label class="form-label fw-semibold text-dark small">Invoice Footer Message</label>
                <textarea id="setting-invoiceFooter" class="form-control" rows="2">${settings.invoiceFooter || ''}</textarea>
              </div>
            </div>
          </div>
        </form>
      </div>

      <div class="col-12 col-lg-4">
        <div class="card-medipoint mb-4">
          <h5 class="fw-bold mb-2">Relational Database Configuration</h5>
          <p class="text-muted small">
            Store configurations and metadata are managed via MySQL with transactional JPA persistence and complete audit logging.
          </p>
          <div class="bg-light p-3 rounded border font-monospace small">
            <div>Database: MySQL (SQL)</div>
            <div>Module: Store Settings</div>
            <div>Updated By: ${settings.updatedBy || auth.getUser()?.email}</div>
            <div>Updated At: ${settings.updatedAt || 'Initial Setup'}</div>
          </div>
        </div>

        <div class="card-medipoint">
          <h5 class="fw-bold mb-2">Security Boundary</h5>
          <p class="text-muted small mb-0">
            Enforced at the Spring Boot backend via <code>@PreAuthorize("hasRole('OWNER')")</code>. Any non-owner request is rejected with 403 Forbidden.
          </p>
        </div>
      </div>
    </div>
  `;
}

export function attachSettingsEvents() {
  const btnSave = document.getElementById('btn-save-settings');
  if (btnSave) {
    btnSave.addEventListener('click', async () => {
      const payload = {
        storeName: document.getElementById('setting-storeName')?.value,
        tagline: document.getElementById('setting-tagline')?.value,
        address: document.getElementById('setting-address')?.value,
        phone: document.getElementById('setting-phone')?.value,
        email: document.getElementById('setting-email')?.value,
        licenseNumber: document.getElementById('setting-licenseNumber')?.value,
        gstNumber: document.getElementById('setting-gstNumber')?.value,
        upiId: document.getElementById('setting-upiId')?.value,
        invoicePrefix: document.getElementById('setting-invoicePrefix')?.value,
        invoiceFooter: document.getElementById('setting-invoiceFooter')?.value
      };

      try {
        btnSave.disabled = true;
        btnSave.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Saving...`;

        await api.updateSettings(payload);
        toast.success('Settings Saved', 'Store configuration updated successfully in backend.');
      } catch (err) {
        toast.error('Save Failed', err.message || 'Unable to update store settings.');
      } finally {
        btnSave.disabled = false;
        btnSave.innerHTML = `<i data-lucide="save"></i> Save Settings`;
        if (window.lucide) window.lucide.createIcons();
      }
    });
  }
}
