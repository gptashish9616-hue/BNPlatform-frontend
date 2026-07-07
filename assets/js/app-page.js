// ============================================================
//  Runs on every authenticated app page: enforces login,
//  fills the sidebar + topbar with the real signed-in user.
//  Include AFTER config/storage/utils/api/guards (and app-shell.js).
// ============================================================
(function () {
    document.addEventListener("DOMContentLoaded", () => {
        // admin pages opt in via <body data-require="admin">
        const requireAdmin = document.body.dataset.require === "admin";
        const ok = requireAdmin ? window.BnpGuards.requireAdmin() : window.BnpGuards.requireAuth();
        if (!ok) return;

        window.BnpGuards.hydrateChrome();

        const user = window.BnpStorage.getUser() || {};
        const first = (user.fullName || "there").split(" ")[0];

        // topbar greeting like "Welcome back, Rahul 👋"
        const greet = document.querySelector(".topbar-title p");
        if (greet && /welcome/i.test(greet.textContent)) {
            greet.textContent = "Welcome back, " + first + " 👋";
        }
        // any element tagged for the user's name / first name
        document.querySelectorAll("[data-user-name]").forEach((el) => (el.textContent = user.fullName || "Member"));
        document.querySelectorAll("[data-user-first]").forEach((el) => (el.textContent = first));
        // topbar avatar(s)
        document.querySelectorAll(".topbar-avatar").forEach((img) => (img.src = window.BnpUtils.avatarFor(user.avatarUrl, user.fullName)));

        // Self-heal a stale cached avatar/name (e.g. a session that started
        // before the login response included avatarUrl) by syncing from the
        // live profile, then re-applying it to the topbar + sidebar.
        Api.users.me().then((me) => {
            window.BnpStorage.setUser({ ...window.BnpStorage.getUser(), fullName: me.fullName, avatarUrl: me.avatarUrl });
            document.querySelectorAll(".topbar-avatar").forEach((img) => (img.src = window.BnpUtils.avatarFor(me.avatarUrl, me.fullName)));
            window.BnpGuards.hydrateChrome();
        }).catch(() => {});

        // ── Bell count badge (unread notifications) ──────────────
        Api.notifications.unreadCount().then(count => {
            if (!count || count <= 0) return;
            const bellLink = document.querySelector('a.topbar-icon[href*="notifications"]');
            if (!bellLink) return;
            const existing = bellLink.querySelector('.bell-count-badge');
            if (existing) { existing.textContent = count > 99 ? "99+" : count; return; }
            const badge = document.createElement('span');
            badge.className = 'bell-count-badge';
            badge.textContent = count > 99 ? "99+" : count;
            bellLink.appendChild(badge);
        }).catch(() => {});
    });
})();
