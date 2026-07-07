// ============================================================
//  Route guards, logout, and current-user wiring for app pages.
// ============================================================
(function () {
    // Path to the login page, resolved relative to the current page depth.
    function loginPath() {
        const p = location.pathname;
        if (p.includes("/pages/auth/")) return "login.html";
        if (p.includes("/pages/")) return "../auth/login.html";
        return "pages/auth/login.html";
    }

    function dashboardPath() {
        const p = location.pathname;
        const admin = window.BnpStorage?.isAdmin();
        if (p.includes("/pages/")) return admin ? "../dashboard/admin-dashboard.html" : "../dashboard/user-dashboard.html";
        return admin ? "pages/dashboard/admin-dashboard.html" : "pages/dashboard/user-dashboard.html";
    }

    const BnpGuards = {
        loginPath,
        dashboardPath,

        redirectToLogin() {
            location.href = loginPath();
        },

        /** Call at the top of any protected page. Redirects to login if not signed in. */
        requireAuth() {
            if (!window.BnpStorage?.isAuthenticated()) {
                location.replace(loginPath());
                return false;
            }
            return true;
        },

        /** Admin-only pages. */
        requireAdmin() {
            if (!this.requireAuth()) return false;
            if (!window.BnpStorage?.isAdmin()) {
                location.replace(dashboardPath());
                return false;
            }
            return true;
        },

        logout() {
            window.BnpStorage?.clear();
            location.href = loginPath();
        },

        /** Fill the sidebar user block + wire the logout button + live notification badge. */
        async hydrateChrome() {
            const user = window.BnpStorage?.getUser();

            const apply = () => {
                const nameEl = document.querySelector(".sb-user strong");
                const tierEl = document.querySelector(".sb-user small");
                const imgEl = document.querySelector(".sb-user img");
                if (user) {
                    if (nameEl) nameEl.textContent = user.fullName || "Member";
                    if (tierEl) tierEl.textContent = tierLabel(user.role);
                    if (imgEl) imgEl.src = window.BnpUtils.avatarFor(user.avatarUrl, user.fullName);
                }
                document.querySelectorAll(".sb-logout, [data-action='logout']").forEach((el) => {
                    el.addEventListener("click", (e) => { e.preventDefault(); BnpGuards.logout(); });
                });
            };

            // sidebar is injected async by app-shell.js, so retry briefly
            let tries = 0;
            const timer = setInterval(() => {
                if (document.querySelector(".sb-user") || ++tries > 20) {
                    clearInterval(timer);
                    apply();
                }
            }, 100);

            // live unread notification badge
            try {
                const count = await window.Api.notifications.unreadCount();
                const badge = document.getElementById("sbNotifBadge");
                if (badge) badge.textContent = count > 0 ? count : "";
                if (badge && count === 0) badge.style.display = "none";
            } catch (e) { /* ignore */ }
        }
    };

    function tierLabel(role) {
        switch (role) {
            case "SUPER_ADMIN": return "Super Admin";
            case "SUB_ADMIN": return "Admin";
            case "PREMIUM_USER": return "Premium";
            default: return "Free tier";
        }
    }

    window.BnpGuards = BnpGuards;
})();
