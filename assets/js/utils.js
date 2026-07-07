// ============================================================
//  Small shared UI helpers.
// ============================================================
(function () {
    const BnpUtils = {
        /** Lightweight toast notification (no dependency). */
        toast(message, type = "info") {
            let host = document.getElementById("bnp-toast-host");
            if (!host) {
                host = document.createElement("div");
                host.id = "bnp-toast-host";
                host.style.cssText =
                    "position:fixed;top:18px;right:18px;z-index:9999;display:flex;flex-direction:column;gap:10px;max-width:340px;";
                document.body.appendChild(host);
            }
            const colors = { success: "#1e8e3e", error: "#e31e24", info: "#1a1a1a", warning: "#f59e0b" };
            const el = document.createElement("div");
            el.style.cssText =
                `background:${colors[type] || colors.info};color:#fff;padding:12px 16px;border-radius:10px;` +
                "font:500 13.5px/1.4 'Inter',Arial,sans-serif;box-shadow:0 8px 24px rgba(0,0,0,.18);" +
                "opacity:0;transform:translateY(-8px);transition:.2s;";
            el.textContent = message;
            host.appendChild(el);
            requestAnimationFrame(() => { el.style.opacity = "1"; el.style.transform = "none"; });
            setTimeout(() => {
                el.style.opacity = "0";
                el.style.transform = "translateY(-8px)";
                setTimeout(() => el.remove(), 250);
            }, 3200);
        },

        /** Escape user-supplied text before putting it in innerHTML. */
        escape(str) {
            return String(str ?? "").replace(/[&<>"']/g, (c) =>
                ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
        },

        /** Read a query-string parameter. */
        param(name) {
            return new URLSearchParams(location.search).get(name);
        },

        /** Format an ISO datetime into a friendly date. */
        date(iso) {
            if (!iso) return "—";
            const d = new Date(iso);
            if (isNaN(d)) return "—";
            return d.toLocaleDateString(undefined, { day: "2-digit", month: "short", year: "numeric" });
        },

        dateTime(iso) {
            if (!iso) return "—";
            const d = new Date(iso);
            if (isNaN(d)) return "—";
            return d.toLocaleString(undefined, { day: "2-digit", month: "short", hour: "2-digit", minute: "2-digit" });
        },

        money(v) {
            const n = Number(v || 0);
            return "₹" + n.toLocaleString("en-IN");
        },

        /** Toggle a button into a loading state and back. */
        loading(btn, isLoading, busyText) {
            if (!btn) return;
            if (isLoading) {
                btn.dataset.label = btn.innerHTML;
                btn.disabled = true;
                btn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> ${busyText || "Please wait…"}`;
            } else {
                btn.disabled = false;
                if (btn.dataset.label) btn.innerHTML = btn.dataset.label;
            }
        },

        /** Status pill markup shared by referral / requirement / subscription lists. */
        statusBadge(status) {
            const map = {
                ACCEPTED: ["badge-green", "check", "Accepted"],
                COMPLETED: ["badge-green", "check", "Completed"],
                FULFILLED: ["badge-green", "check", "Fulfilled"],
                ACTIVE: ["badge-green", "check", "Active"],
                SUCCESS: ["badge-green", "check", "Success"],
                VERIFIED: ["badge-green", "check", "Verified"],
                OPEN: ["badge-blue", "circle-dot", "Open"],
                IN_PROGRESS: ["badge-blue", "spinner", "In progress"],
                PENDING: ["badge-amber", "clock", "Pending"],
                SENT: ["badge-amber", "paper-plane", "Sent"],
                REJECTED: ["badge-red", "xmark", "Declined"],
                EXPIRED: ["badge-red", "xmark", "Expired"],
                CANCELLED: ["badge-red", "xmark", "Cancelled"],
                CLOSED: ["badge-red", "xmark", "Closed"],
                FAILED: ["badge-red", "xmark", "Failed"]
            };
            const [cls, ic, lbl] = map[status] || ["badge-amber", "circle", status || "—"];
            return `<span class="badge ${cls}"><i class="fa-solid fa-${ic}"></i> ${lbl}</span>`;
        },

        initials(name) {
            return String(name || "U").trim().split(/\s+/).slice(0, 2).map((w) => w[0]).join("").toUpperCase();
        },

        avatar(name) {
            return "https://ui-avatars.com/api/?name=" + encodeURIComponent(name || "User") + "&background=e31e24&color=fff&font-size=0.4";
        },

        /**
         * Resolve a stored file reference to a loadable URL. Server-stored files
         * come back as relative paths like "/uploads/avatars/x.png"; prefix them
         * with the API origin. Absolute http(s) URLs and data URIs pass through.
         */
        fileUrl(u) {
            if (!u) return u;
            if (/^(https?:)?\/\//i.test(u) || /^data:/i.test(u)) return u;
            const base = (window.BNP_CONFIG?.API_BASE_URL || "").replace(/\/$/, "");
            return base + (u.startsWith("/") ? u : "/" + u);
        },

        /** Avatar src that resolves an uploaded file, else falls back to initials art. */
        avatarFor(avatarUrl, name) {
            return avatarUrl ? this.fileUrl(avatarUrl) : this.avatar(name);
        }
    };

    window.BnpUtils = BnpUtils;
})();
