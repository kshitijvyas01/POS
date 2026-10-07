/* =================================================================
   MEDIPOINT APPLICATION INITIALIZATION ENTRY POINT
   Architecture: Spring Boot + Spring Security + JWT + MySQL
   ================================================================= */

import { router } from './router.js';
import { auth } from './auth.js';

document.addEventListener('DOMContentLoaded', async () => {
  console.log("=================================================");
  console.log(" MediPoint Smart Medical Store POS Architecture");
  console.log(" Step 1: MySQL + Spring Boot + JWT Foundation Active");
  console.log("=================================================");

  if (auth.isAuthenticated()) {
    try {
      await auth.verifySession();
    } catch (e) {
      console.warn("Initial session verification note:", e.message);
    }
  }

  router.init();
});
