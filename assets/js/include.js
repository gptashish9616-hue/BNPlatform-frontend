// Loads the shared navbar/footer components into any page that provides the
// #navbar-container / #footer-container placeholders, then highlights the link
// for the current page. Requires the page to be served over http(s) — opening
// the file directly via file:// will block fetch() due to browser CORS rules.

document.addEventListener("DOMContentLoaded", () => {
    loadComponent("#navbar-container", "components/navbar.html").then(() => {
        setActiveNav();
        applyAuthNav();
    });
    injectFaq();
    loadComponent("#footer-container", "components/footer.html");
});

// Logged-in visitors shouldn't see "Login" / "Join Now" on the public site —
// swap them for a single "Go to Dashboard" link instead.
function applyAuthNav() {
    if (!window.BnpStorage?.isAuthenticated?.()) return;

    const loginLink = document.querySelector('#navbar-container a[href*="pages/auth/login.html"]');
    const joinLink = document.querySelector('#navbar-container a[href*="pages/auth/register.html"]');
    const actions = loginLink?.parentElement;
    if (!actions) return;

    const admin = window.BnpStorage.isAdmin?.();
    const dashboardHref = admin ? "pages/dashboard/admin-dashboard.html" : "pages/dashboard/user-dashboard.html";

    loginLink.remove();
    if (joinLink) {
        joinLink.href = dashboardHref;
        joinLink.innerHTML = '<i class="fa-solid fa-gauge-high"></i> Go to Dashboard';
    } else {
        const a = document.createElement("a");
        a.href = dashboardHref;
        a.className = "btn join-btn rounded-pill d-inline-flex align-items-center justify-content-center gap-2";
        a.innerHTML = '<i class="fa-solid fa-gauge-high"></i> Go to Dashboard';
        actions.appendChild(a);
    }
}

// Drop the shared FAQ in just above the footer — but only on pages that
// don't already ship their own FAQ (contact / help / pricing / referral).
function injectFaq() {
    const footer = document.querySelector("#footer-container");
    if (!footer) return;
    if (document.querySelector(".bnp-faq, .bnp-faq__list, .faq-section, .faq-item, .faq-q, .accordion")) return;

    const host = document.createElement("div");
    host.id = "faq-container";
    footer.parentNode.insertBefore(host, footer);
    loadComponent("#faq-container", "components/faq.html");
}

function loadComponent(selector, url) {
    const host = document.querySelector(selector);
    if (!host) return Promise.resolve();

    return fetch(url)
        .then((res) => res.text())
        .then((html) => {
            host.innerHTML = html;
        })
        .catch((err) => {
            console.warn(`Could not load component: ${url}`, err);
        });
}

function setActiveNav() {
    const current = location.pathname.split("/").pop() || "index.html";

    document.querySelectorAll("#navbar-container a[href]").forEach((link) => {
        const href = link.getAttribute("href");
        if (!href || href === "#") return;

        if (href.split("/").pop() === current) {
            link.classList.add("active");

            // If the match is inside a dropdown, light up its toggle too.
            const dropdown = link.closest(".dropdown");
            if (dropdown) {
                const toggle = dropdown.querySelector(".dropdown-toggle");
                if (toggle) toggle.classList.add("active");
            }
        }
    });
}
