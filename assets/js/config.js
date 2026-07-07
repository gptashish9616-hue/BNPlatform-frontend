// ============================================================
//  Global frontend config. Change API_BASE_URL when deploying.
// ============================================================
const BNP_CONFIG = {
    // Spring Boot backend. In production set this to your API origin.
    API_BASE_URL: "http://localhost:8080",
};

window.BNP_CONFIG = BNP_CONFIG;

// ---- Config used by the public members grid (app.js), now backend-driven ----
const BNPLATFORM_CONFIG = {
    API_BASE_URL: BNP_CONFIG.API_BASE_URL,
    MEMBERS_ENDPOINT: "/api/public/members",
    FALLBACK_MEMBERS: []   // no dummy data — comes from the database
};

window.BNPLATFORM_CONFIG = BNPLATFORM_CONFIG;
