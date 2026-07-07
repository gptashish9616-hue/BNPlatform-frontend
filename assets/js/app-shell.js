// Injects the shared user sidebar into #sidebar-container on every app page,
// highlights the current page, and wires the mobile open/close toggle.
// All app pages sit at pages/<section>/, so the component path is ../../.

document.addEventListener("DOMContentLoaded", () => {
    const host = document.getElementById("sidebar-container");
    if (!host) {
        initSidebarBehaviour();
        return;
    }

    // admin pages set data-shell="admin" to load the admin sidebar instead.
    // Shared pages (like notifications.html) don't set it, so fall back to
    // the logged-in user's role so admins get their own sidebar there too.
    const isAdmin = host.dataset.shell === "admin" || window.BnpStorage?.isAdmin();
    const file = isAdmin ? "admin-sidebar.html" : "sidebar.html";

    fetch("../../components/" + file)
        .then((res) => res.text())
        .then((html) => {
            host.innerHTML = html;
            setActiveLink();
            initSidebarBehaviour();
            updateReqBadge();
            updateMsgBadge();
            restoreSidebarScroll();
        })
        .catch((err) => console.warn("Could not load sidebar.", err));
});

function setActiveLink() {
    const current = location.pathname.split("/").pop() || "user-dashboard.html";
    document.querySelectorAll(".sb-link[data-page]").forEach((link) => {
        if (link.dataset.page === current) link.classList.add("active");
    });
}

// The sidebar is plain fetched markup re-inserted on every page, so its
// scroll position always starts at 0 — clicking a link further down the
// list made the sidebar appear to "jump" back to the top on the next page.
// Persist the scroll offset across navigations so it stays put instead.
const SIDEBAR_SCROLL_KEY = "bnp_sb_scroll";

function restoreSidebarScroll() {
    const nav = document.querySelector(".sb-nav");
    if (!nav) return;

    const activeLink = nav.querySelector(".sb-link.active");
    const saved = sessionStorage.getItem(SIDEBAR_SCROLL_KEY);
    if (saved !== null) {
        nav.scrollTop = parseInt(saved, 10) || 0;
    } else if (activeLink) {
        // first visit this session: bring the current page's link into view
        activeLink.scrollIntoView({ block: "nearest" });
    }

    nav.addEventListener("scroll", () => {
        sessionStorage.setItem(SIDEBAR_SCROLL_KEY, nav.scrollTop);
    }, { passive: true });
}

function updateReqBadge() {
    try {
        const me = window.BnpStorage?.getUser();
        const uid = me?.userId || "g";
        const notifs = JSON.parse(localStorage.getItem("bnp_rn_" + uid) || "[]");
        const unread = notifs.filter(function(n) { return !n.read; }).length;
        const badge = document.getElementById("sbReqBadge");
        if (!badge) return;
        if (unread > 0) {
            badge.textContent = unread <= 3 ? String(unread) : "more";
            badge.style.display = "";
        } else {
            badge.style.display = "none";
        }
    } catch (e) {}
}
window.updateReqBadge = updateReqBadge;

function updateMsgBadge() {
    try {
        const unread = parseInt(localStorage.getItem("bnp_msg_unread") || "0", 10);
        const badge = document.getElementById("sbMsgBadge");
        if (!badge) return;
        if (unread > 0) {
            badge.textContent = unread > 99 ? "99+" : String(unread);
            badge.style.display = "";
        } else {
            badge.style.display = "none";
        }
    } catch (e) {}
}
window.updateMsgBadge = updateMsgBadge;

function initSidebarBehaviour() {
    // expose toggle for inline onclick handlers
    window.toggleSidebar = function () {
        const sb = document.getElementById("appSidebar");
        const backdrop = document.getElementById("sbBackdrop");
        if (!sb) return;
        const open = sb.classList.toggle("open");
        if (backdrop) backdrop.classList.toggle("show", open);
        document.body.style.overflow = open ? "hidden" : "";
    };
}
