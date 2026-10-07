/* =================================================================
   MEDIPOINT LOGIN PAGE VIEW
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   ================================================================= */

import { auth } from '../auth.js';
import { toast } from '../components/toast.js';

export function renderLoginPage() {
  return `
    <div style="min-height: 100vh; display: flex; background-color: #ffffff;">
      <!-- Left Side: MediPoint Healthcare Branding & Architecture Highlights -->
      <div style="flex: 1; background: linear-gradient(135deg, var(--primary-950), var(--primary-900)); color: #ffffff; padding: 4rem; display: flex; flex-direction: column; justify-content: space-between; position: relative; overflow: hidden;" class="d-none d-lg-flex">

        <!-- Decorative Ambient Light Circles -->
        <div style="position: absolute; top: -100px; left: -100px; width: 400px; height: 400px; border-radius: 50%; background: radial-gradient(circle, rgba(16, 185, 129, 0.15), transparent 70%);"></div>
        <div style="position: absolute; bottom: -100px; right: -100px; width: 500px; height: 500px; border-radius: 50%; background: radial-gradient(circle, rgba(5, 150, 105, 0.2), transparent 70%);"></div>

        <!-- Header Branding -->
        <div style="position: relative; z-index: 10;">
          <div style="display: flex; align-items: center; gap: 0.875rem; margin-bottom: 1rem;">
            <div style="width: 48px; height: 48px; border-radius: var(--radius-sm); background: linear-gradient(135deg, var(--primary-500), var(--primary-700)); display: flex; align-items: center; justify-content: center; color: #ffffff; font-weight: 800; box-shadow: var(--shadow-glow);">
              <i data-lucide="cross" style="width: 28px; height: 28px;"></i>
            </div>
            <h1 style="color: #ffffff; font-size: 2rem; font-weight: 800; margin: 0; letter-spacing: -0.03em;">MediPoint</h1>
          </div>
          <p style="color: var(--primary-400); font-weight: 600; font-size: 1.125rem;">"Smarter Pharmacy. Better Control."</p>
        </div>

        <!-- Center Architectural Highlights -->
        <div style="position: relative; z-index: 10; margin: 2rem 0;">
          <div style="background: rgba(255, 255, 255, 0.05); border: 1px solid rgba(255, 255, 255, 0.1); backdrop-filter: blur(12px); border-radius: var(--radius-lg); padding: 2.5rem; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);">
            <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem;">
              <div style="width: 40px; height: 40px; border-radius: 50%; background-color: rgba(16, 185, 129, 0.2); display: flex; align-items: center; justify-content: center; color: var(--primary-400);">
                <i data-lucide="database"></i>
              </div>
              <div>
                <h4 style="color: #ffffff; margin: 0; font-size: 1rem; font-weight: 700;">Relational MySQL Database</h4>
                <p style="color: var(--slate-300); font-size: 0.875rem; margin: 0;">Spring Data JPA & Hibernate relational persistence</p>
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 1rem; margin-bottom: 1.5rem;">
              <div style="width: 40px; height: 40px; border-radius: 50%; background-color: rgba(16, 185, 129, 0.2); display: flex; align-items: center; justify-content: center; color: var(--primary-400);">
                <i data-lucide="shield-check"></i>
              </div>
              <div>
                <h4 style="color: #ffffff; margin: 0; font-size: 1rem; font-weight: 700;">Spring Security 6 & JWT</h4>
                <p style="color: var(--slate-300); font-size: 0.875rem; margin: 0;">BCrypt password encryption & stateless Bearer token authorization</p>
              </div>
            </div>

            <div style="display: flex; align-items: center; gap: 1rem;">
              <div style="width: 40px; height: 40px; border-radius: 50%; background-color: rgba(16, 185, 129, 0.2); display: flex; align-items: center; justify-content: center; color: var(--primary-400);">
                <i data-lucide="lock"></i>
              </div>
              <div>
                <h4 style="color: #ffffff; margin: 0; font-size: 1rem; font-weight: 700;">Strict Role-Based Authorization</h4>
                <p style="color: var(--slate-300); font-size: 0.875rem; margin: 0;">Enforced backend boundaries between Owner and Worker access</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Footer Note -->
        <div style="position: relative; z-index: 10; font-size: 0.8125rem; color: var(--slate-400);">
          © 2026 MediPoint Systems. Enterprise Pharmacy Management Architecture.
        </div>
      </div>

      <!-- Right Side: Clean Login Form -->
      <div style="flex: 1; display: flex; flex-direction: column; justify-content: center; align-items: center; padding: 3rem 2rem; background-color: var(--slate-50);">

        <div style="width: 100%; max-width: 440px;">

          <!-- Mobile Brand Title -->
          <div class="d-lg-none text-center mb-4">
            <div style="width: 48px; height: 48px; border-radius: var(--radius-sm); background: linear-gradient(135deg, var(--primary-600), var(--primary-800)); display: inline-flex; align-items: center; justify-content: center; color: #ffffff; font-weight: 800; margin-bottom: 0.5rem;">
              <i data-lucide="cross"></i>
            </div>
            <h2 style="font-weight: 800; color: var(--slate-900); margin: 0;">MediPoint</h2>
            <p style="color: var(--primary-600); font-weight: 600; font-size: 0.875rem;">Smarter Pharmacy. Better Control.</p>
          </div>

          <div style="margin-bottom: 1.75rem;">
            <h2 style="font-size: 1.75rem; font-weight: 800; color: var(--slate-900); margin-bottom: 0.5rem;">System Login</h2>
            <p style="color: var(--slate-600); font-size: 0.9375rem;">Sign in with your verified MySQL store account</p>
          </div>

          <!-- Quick Account Preset Selector Pills -->
          <div style="background-color: var(--primary-50); border: 1px solid var(--primary-200); border-radius: var(--radius-md); padding: 0.875rem; margin-bottom: 1.5rem;">
            <div style="font-size: 0.75rem; font-weight: 700; color: var(--primary-900); text-transform: uppercase; letter-spacing: 0.05em; margin-bottom: 0.5rem;">
              Seeded Development Accounts
            </div>
            <div style="display: flex; gap: 0.5rem;">
              <button type="button" id="btn-fill-owner" style="flex: 1; padding: 0.45rem; font-size: 0.8125rem; font-weight: 600; border-radius: var(--radius-sm); border: 1px solid var(--primary-600); background-color: var(--primary-600); color: #ffffff; cursor: pointer; transition: all 0.15s;">
                Store Owner
              </button>
              <button type="button" id="btn-fill-worker" style="flex: 1; padding: 0.45rem; font-size: 0.8125rem; font-weight: 600; border-radius: var(--radius-sm); border: 1px solid var(--slate-300); background-color: #ffffff; color: var(--slate-700); cursor: pointer; transition: all 0.15s;">
                Pharmacy Worker
              </button>
            </div>
          </div>

          <!-- Error Alert Container -->
          <div id="login-error-alert" class="alert alert-danger d-none" role="alert" style="font-size: 0.875rem; border-radius: var(--radius-sm);"></div>

          <form id="login-form">
            <div class="mb-3">
              <label for="login-email" class="form-label" style="font-weight: 600; font-size: 0.875rem; color: var(--slate-700);">Email Address</label>
              <div style="position: relative;">
                <input type="email" class="form-control" id="login-email" placeholder="e.g. owner@medipoint.com" required style="padding-left: 2.5rem; height: 46px; border-radius: var(--radius-sm); border-color: var(--slate-300);" />
                <i data-lucide="mail" style="position: absolute; left: 0.875rem; top: 50%; transform: translateY(-50%); color: var(--slate-400); width: 18px;"></i>
              </div>
            </div>

            <div class="mb-3">
              <div class="d-flex justify-content-between align-items-center mb-1">
                <label for="login-password" class="form-label mb-0" style="font-weight: 600; font-size: 0.875rem; color: var(--slate-700);">Password</label>
              </div>
              <div style="position: relative;">
                <input type="password" class="form-control" id="login-password" placeholder="Enter your password" required style="padding-left: 2.5rem; padding-right: 2.5rem; height: 46px; border-radius: var(--radius-sm); border-color: var(--slate-300);" />
                <i data-lucide="lock" style="position: absolute; left: 0.875rem; top: 50%; transform: translateY(-50%); color: var(--slate-400); width: 18px;"></i>
                <button type="button" id="toggle-password-visibility" style="position: absolute; right: 0.875rem; top: 50%; transform: translateY(-50%); background: none; border: none; color: var(--slate-400); cursor: pointer;" title="Toggle Password Visibility">
                  <i data-lucide="eye" id="eye-icon"></i>
                </button>
              </div>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-4">
              <div class="form-check">
                <input class="form-check-input" type="checkbox" id="remember-me" checked />
                <label class="form-check-label" for="remember-me" style="font-size: 0.875rem; color: var(--slate-600);">Remember this device</label>
              </div>
            </div>

            <button type="submit" id="btn-login-submit" class="btn-medipoint btn-medipoint-primary w-100" style="height: 48px; font-size: 1rem;">
              <span id="login-btn-text">Sign In to MediPoint</span>
              <span id="login-btn-spinner" class="spinner-border spinner-border-sm d-none ms-2" role="status" aria-hidden="true"></span>
            </button>
          </form>

        </div>
      </div>
    </div>
  `;
}

export function attachLoginPageEvents() {
  const form = document.getElementById('login-form');
  const emailInput = document.getElementById('login-email');
  const passwordInput = document.getElementById('login-password');
  const toggleBtn = document.getElementById('toggle-password-visibility');
  const errorAlert = document.getElementById('login-error-alert');
  const fillOwnerBtn = document.getElementById('btn-fill-owner');
  const fillWorkerBtn = document.getElementById('btn-fill-worker');
  const btnSubmit = document.getElementById('btn-login-submit');
  const btnText = document.getElementById('login-btn-text');
  const btnSpinner = document.getElementById('login-btn-spinner');

  if (fillOwnerBtn) {
    fillOwnerBtn.addEventListener('click', () => {
      emailInput.value = 'owner@medipoint.com';
      passwordInput.value = 'owner123';
      fillOwnerBtn.style.backgroundColor = 'var(--primary-600)';
      fillOwnerBtn.style.color = '#ffffff';
      if (fillWorkerBtn) {
        fillWorkerBtn.style.backgroundColor = '#ffffff';
        fillWorkerBtn.style.color = 'var(--slate-700)';
      }
      toast.info('Owner Account Selected', 'owner@medipoint.com / owner123');
    });
  }

  if (fillWorkerBtn) {
    fillWorkerBtn.addEventListener('click', () => {
      emailInput.value = 'worker@medipoint.com';
      passwordInput.value = 'worker123';
      fillWorkerBtn.style.backgroundColor = 'var(--primary-600)';
      fillWorkerBtn.style.color = '#ffffff';
      if (fillOwnerBtn) {
        fillOwnerBtn.style.backgroundColor = '#ffffff';
        fillOwnerBtn.style.color = 'var(--slate-700)';
      }
      toast.info('Worker Account Selected', 'worker@medipoint.com / worker123');
    });
  }

  if (toggleBtn) {
    toggleBtn.addEventListener('click', () => {
      const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
      passwordInput.setAttribute('type', type);
      const eyeIcon = document.getElementById('eye-icon');
      if (eyeIcon) {
        eyeIcon.setAttribute('data-lucide', type === 'password' ? 'eye' : 'eye-off');
        if (window.lucide) window.lucide.createIcons();
      }
    });
  }

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      errorAlert.classList.add('d-none');

      const email = emailInput.value.trim();
      const password = passwordInput.value.trim();

      // Client-side Validation
      if (!email || !password) {
        errorAlert.textContent = 'Please enter both email and password.';
        errorAlert.classList.remove('d-none');
        return;
      }

      btnSubmit.disabled = true;
      btnText.textContent = 'Authenticating...';
      btnSpinner.classList.remove('d-none');

      try {
        const res = await auth.login(email, password);
        if (res.success) {
          toast.success('Login Successful', `Welcome back, ${res.user.displayName}!`);
          window.location.hash = '#dashboard';
        }
      } catch (err) {
        // Generic failure message per security guidelines
        const msg = err.message.includes('backend server')
          ? err.message
          : 'Invalid email or password. Please verify your credentials.';
        errorAlert.textContent = msg;
        errorAlert.classList.remove('d-none');
        toast.error('Authentication Error', msg);
      } finally {
        btnSubmit.disabled = false;
        btnText.textContent = 'Sign In to MediPoint';
        btnSpinner.classList.add('d-none');
      }
    });
  }
}
