// ============================================================
//  Admin notification bell — shared dropdown panel for admin pages.
//  Expects markup:
//  <div class="topbar-icon" id="adminBell">
//      <i class="fa-solid fa-bell"></i>
//      <span class="bell-count-badge" id="adminBellDot" style="display:none;"></span>
//  </div>
// ============================================================
(function () {
    const ICONS = {
        SYSTEM: ["hint", "bell"],
        REFERRAL: ["green", "handshake"],
        REQUIREMENT: ["blue", "layer-group"],
        SUBSCRIPTION: ["purple", "crown"],
        POINTS: ["amber", "star"],
        CHAT: ["blue", "comment"],
        REVIEW: ["amber", "star"]
    };

    const esc = (s) => window.BnpUtils?.escape ? window.BnpUtils.escape(s) : String(s ?? "");
    const when = (iso) => window.BnpUtils?.dateTime ? window.BnpUtils.dateTime(iso) : "";

    let panel = null;
    let isOpen = false;

    async function refreshBadge() {
        const dot = document.getElementById("adminBellDot");
        if (!dot) return;
        try {
            const count = await window.Api.notifications.unreadCount();
            if (count > 0) {
                dot.textContent = count > 99 ? "99+" : String(count);
                dot.style.display = "flex";
            } else {
                dot.style.display = "none";
            }
        } catch (e) { /* ignore */ }
    }

    function renderPanel(items) {
        const unread = items.filter((n) => !n.readFlag).length;
        const recent = items.slice(0, 6);

        const rows = recent.length
            ? recent.map((n) => {
                const [color, ic] = ICONS[n.type] || ICONS.SYSTEM;
                const isUnread = !n.readFlag;
                return `<div class="admin-bell-item${isUnread ? " unread" : ""}" data-id="${n.id}" data-link="${n.link ? esc(n.link) : ""}">
                    <div class="nt-icon-wrap" style="background:var(--${color}-light);color:var(--${color});">
                        <i class="fa-solid fa-${ic}"></i>
                    </div>
                    <div class="nt-body">
                        <strong>${esc(n.title)}</strong>
                        <p>${esc(n.message)}</p>
                        <span class="nt-time"><i class="fa-regular fa-clock"></i> ${when(n.createdAt)}</span>
                    </div>
                    ${isUnread ? '<span class="nt-dot"></span>' : ""}
                </div>`;
            }).join("")
            : `<div class="admin-bell-empty"><i class="fa-solid fa-bell-slash"></i><p>You're all caught up</p></div>`;

        panel.innerHTML = `
            <div class="admin-bell-head">
                <strong>Notifications</strong>
                ${unread > 0 ? `<button type="button" class="btn btn-ghost btn-sm" id="adminBellMarkAll">Mark all read</button>` : ""}
            </div>
            <div class="admin-bell-list">${rows}</div>
            <a href="../notifications/notifications.html" class="admin-bell-footer">View all notifications</a>
        `;

        panel.querySelectorAll(".admin-bell-item").forEach((row) => {
            row.addEventListener("click", async () => {
                const id = row.dataset.id;
                const link = row.dataset.link;
                if (row.classList.contains("unread")) {
                    try { await window.Api.notifications.markRead(id); } catch (e) { /* ignore */ }
                    row.classList.remove("unread");
                    const dot = row.querySelector(".nt-dot");
                    if (dot) dot.remove();
                    refreshBadge();
                }
                if (link) location.href = link;
            });
        });

        const markAllBtn = panel.querySelector("#adminBellMarkAll");
        if (markAllBtn) {
            markAllBtn.addEventListener("click", async (e) => {
                e.stopPropagation();
                try { await window.Api.notifications.markAllRead(); } catch (e) { /* ignore */ }
                loadPanel();
                refreshBadge();
            });
        }
    }

    async function loadPanel() {
        panel.innerHTML = `<div class="admin-bell-empty"><i class="fa-solid fa-spinner fa-spin"></i></div>`;
        try {
            const items = await window.Api.notifications.list(false);
            renderPanel(items || []);
        } catch (e) {
            panel.innerHTML = `<div class="admin-bell-empty"><p class="muted">Couldn't load notifications.</p></div>`;
        }
    }

    function setOpen(next) {
        isOpen = next;
        panel.classList.toggle("show", isOpen);
        if (isOpen) loadPanel();
    }

    document.addEventListener("DOMContentLoaded", () => {
        const bell = document.getElementById("adminBell");
        if (!bell || !window.BnpStorage?.isAuthenticated()) return;

        panel = document.createElement("div");
        panel.className = "admin-bell-panel";
        panel.id = "adminBellPanel";
        bell.appendChild(panel);

        bell.addEventListener("click", (e) => {
            e.stopPropagation();
            setOpen(!isOpen);
        });
        panel.addEventListener("click", (e) => e.stopPropagation());
        document.addEventListener("click", () => { if (isOpen) setOpen(false); });
        document.addEventListener("keydown", (e) => { if (e.key === "Escape" && isOpen) setOpen(false); });

        refreshBadge();
        setInterval(refreshBadge, 30000);
    });
})();
