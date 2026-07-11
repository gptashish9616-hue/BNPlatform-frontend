/* =====================================================================
   BNPlatform — site-wide animation engine (dependency-free)
   ---------------------------------------------------------------------
   Adds the "Netwise"-style motion to every page without touching their
   markup: scroll-reveal with stagger, animated counters, navbar-on-scroll,
   hover lift on cards/buttons, and smooth in-page scrolling.

   Design philosophy: progressive enhancement. All "hidden until revealed"
   CSS is scoped under <html class="anim-ready">, which this script only
   adds when IntersectionObserver is supported. If the script never runs
   (or motion is reduced), the page renders normally — nothing stays hidden.
   ===================================================================== */
(function () {
    "use strict";

    var root = document.documentElement;
    var reduceMotion = window.matchMedia &&
        window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    var supported = "IntersectionObserver" in window;

    /* ---- 1. Inject the stylesheet ----------------------------------- */
    function injectStyles() {
        if (document.getElementById("bnp-anim-styles")) return;
        var css = [
            /* safety net: never let a stray element create a horizontal scroll.
               'clip' (not 'hidden') so it doesn't break the sticky app top-bar. */
            "html,body{overflow-x:clip;}",

            /* site-wide font. 'html body.app' beats dashboard.css's body.app;
               form controls listed so they don't fall back to the UA font.
               (no !important, so Font Awesome icon fonts stay intact) */
            'html body,html body.app{font-family:"Roboto Slab",serif;}',
            'button,input,select,textarea,optgroup{font-family:"Roboto Slab",serif;}',

            /* reveal: only hide things once we know JS + IO are alive */
            "html.anim-ready .bnp-reveal{opacity:0;transform:translateY(34px);",
            "transition:opacity .8s cubic-bezier(.22,.61,.36,1),transform .8s cubic-bezier(.22,.61,.36,1);",
            "will-change:opacity,transform}",
            "html.anim-ready .bnp-reveal.bnp-left{transform:translateX(-44px)}",
            "html.anim-ready .bnp-reveal.bnp-right{transform:translateX(44px)}",
            "html.anim-ready .bnp-reveal.bnp-zoom{transform:scale(.92)}",
            "html.anim-ready .bnp-reveal.bnp-in{opacity:1;transform:none}",

            /* hover lift — cards & feature blocks */
            ".card,[class*='-card']{transition:transform .35s ease,box-shadow .35s ease}",
            ".card:hover,[class*='-card']:hover{transform:translateY(-6px);",
            "box-shadow:0 18px 42px rgba(15,23,42,.12)}",

            /* buttons: subtle rise */
            ".btn{transition:transform .2s ease,box-shadow .2s ease,filter .2s ease}",
            ".btn:hover{transform:translateY(-2px)}",
            ".btn:active{transform:translateY(0)}",

            /* navbar elevates once the page is scrolled */
            "#navbar-container .navbar,nav.navbar,header .navbar{",
            "transition:box-shadow .3s ease,background-color .3s ease,padding .3s ease}",
            /* NOTE: no backdrop-filter/transform/filter here — those make the
               navbar a containing block for its position:fixed offcanvas menu,
               which breaks (hides) the mobile menu after scrolling. */
            ".bnp-nav-scrolled{box-shadow:0 8px 28px rgba(15,23,42,.10)!important;}",

            /* content images: gentle zoom on hover (skip map/leaflet) */
            ".card img,[class*='-card'] img{transition:transform .5s ease}",
            ".card:hover img,[class*='-card']:hover img{transform:scale(1.04)}",

            /* breadcrumb (theme red, self-contained colours) */
            ".bnp-breadcrumb{display:flex;flex-wrap:wrap;align-items:center;gap:8px;",
            "font-size:14px;font-weight:500;line-height:1.4;}",
            ".bnp-breadcrumb a{color:#e31e24;text-decoration:none;display:inline-flex;",
            "align-items:center;gap:6px;}",
            ".bnp-breadcrumb a:hover{text-decoration:underline;}",
            ".bnp-breadcrumb .bnp-sep{color:#9aa3af;font-size:12px;}",
            ".bnp-breadcrumb .bnp-current{color:#6b7280;}",
            ".bnp-breadcrumb--app{margin:-2px 0 20px;}",
            ".bnp-crumb-bar{background:#f7f8fa;border-bottom:1px solid #eceff3;padding:5px 0;}",

            /* shared FAQ accordion (theme red, self-contained) */
            ".bnp-faq{background:#fff;padding:64px 0;border-top:1px solid #eceff3;}",
            ".bnp-faq .bnp-faq__head{text-align:center;max-width:640px;margin:0 auto 36px;padding:0 16px;}",
            ".bnp-faq__eyebrow{display:inline-block;font-size:13px;font-weight:700;letter-spacing:.6px;",
            "text-transform:uppercase;color:#e31e24;margin-bottom:10px;}",
            ".bnp-faq h2{font-size:30px;font-weight:800;color:#1a1a1a;margin:0 0 10px;}",
            ".bnp-faq__sub{font-size:15px;color:#5b6471;margin:0;}",
            ".bnp-faq__list{max-width:820px;margin:0 auto;border:1px solid #e7e9ee;border-radius:16px;",
            "overflow:hidden;background:#fff;}",
            ".bnp-faq__item{border-bottom:1px solid #e7e9ee;}",
            ".bnp-faq__item:last-child{border-bottom:none;}",
            ".bnp-faq__q{width:100%;text-align:left;background:#fff;border:none;display:flex;",
            "justify-content:space-between;align-items:center;gap:16px;padding:20px 24px;font-size:15.5px;",
            "font-weight:600;color:#1a1a1a;cursor:pointer;font-family:inherit;}",
            ".bnp-faq__q:hover{background:#f7f8fa;}",
            ".bnp-faq__arrow{font-size:22px;line-height:1;color:#e31e24;transition:transform .25s ease;flex-shrink:0;}",
            ".bnp-faq__q.open .bnp-faq__arrow{transform:rotate(45deg);}",
            ".bnp-faq__a{display:none;padding:0 24px 20px;font-size:14px;color:#5b6471;line-height:1.75;}",
            ".bnp-faq__a.open{display:block;}",
            "@media(max-width:600px){.bnp-faq{padding:44px 0;}.bnp-faq h2{font-size:24px;}}",

            /* honour reduced-motion */
            "@media (prefers-reduced-motion:reduce){html.anim-ready .bnp-reveal{",
            "opacity:1!important;transform:none!important;transition:none!important}}"
        ].join("");
        var style = document.createElement("style");
        style.id = "bnp-anim-styles";
        style.textContent = css;
        document.head.appendChild(style);

        // site-wide font: load Roboto Slab from Google Fonts once
        if (!document.getElementById("bnp-font")) {
            var fl = document.createElement("link");
            fl.id = "bnp-font";
            fl.rel = "stylesheet";
            fl.href = "https://fonts.googleapis.com/css2?family=Roboto+Slab:wght@300;400;500;600;700;800&display=swap";
            document.head.appendChild(fl);
        }
    }

    /* ---- 2. Elements we must never animate / hide ------------------- */
    var EXCLUDE = "nav,.navbar,#navbar-container,header,#map,.leaflet-container," +
        ".modal,.offcanvas,.dropdown-menu,script,style,[data-no-anim]";

    function excluded(el) {
        return el.closest(EXCLUDE) !== null;
    }

    /* ---- 3. Pick reveal targets and stagger them ------------------- */
    function tagReveals(scope) {
        scope = scope || document.body;
        // Main content blocks on these Bootstrap pages are grid columns and
        // cards; section headings get revealed on their own.
        var candidates = scope.querySelectorAll(
            "[class*='col-'],.card,[class*='-card'],section > .container > h1," +
            "section > .container > h2,section > .container > p.lead,.feature,.step,.row > .card"
        );

        candidates.forEach(function (el) {
            if (el.classList.contains("bnp-reveal") || excluded(el)) return;
            if (el.dataset.no === "anim") return;
            el.classList.add("bnp-reveal");

            // Stagger siblings within the same row/parent for a cascade.
            // NOTE: only fade-UP (translateY) is used. Horizontal slide
            // (translateX) on full-width mobile columns pushes content past
            // the viewport edge and creates a horizontal-scroll bug, so it's
            // intentionally not applied.
            var parent = el.parentElement;
            if (parent) {
                var sibs = Array.prototype.indexOf.call(parent.children, el);
                var delay = Math.min(sibs, 6) * 90; // cap so long lists don't drag
                el.style.transitionDelay = delay + "ms";
            }
            observer.observe(el);
        });
    }

    /* ---- 4. The reveal observer ----------------------------------- */
    var observer = supported ? new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add("bnp-in");
                observer.unobserve(entry.target);
                maybeCount(entry.target);
            }
        });
    }, { threshold: 0.12, rootMargin: "0px 0px -8% 0px" }) : null;

    /* ---- 5. Animated counters ------------------------------------- */
    function findCounters() {
        var els = document.querySelectorAll(
            "[data-count],[class*='counter'],[class*='stat'],[class*='count-up']"
        );
        var list = [];
        els.forEach(function (el) {
            if (el.children.length || excluded(el)) return;
            var txt = (el.dataset.count || el.textContent).trim();
            if (/^\d[\d,]*(\.\d+)?\s*[+%]?\s*[KkMm]?$/.test(txt)) {
                el.dataset.bnpTarget = txt;
                list.push(el);
                observer && observer.observe(el);
            }
        });
        return list;
    }

    function maybeCount(el) {
        if (!el.dataset.bnpTarget || el.dataset.bnpDone) return;
        el.dataset.bnpDone = "1";
        var raw = el.dataset.bnpTarget;
        var suffix = (raw.match(/[+%]?\s*[KkMm]?$/) || [""])[0];
        var grouped = raw.indexOf(",") !== -1;
        var target = parseFloat(raw.replace(/[^\d.]/g, "")) || 0;
        var start = performance.now();
        var dur = 1600;
        function tick(now) {
            var p = Math.min((now - start) / dur, 1);
            var eased = 1 - Math.pow(1 - p, 3); // easeOutCubic
            var val = Math.floor(eased * target);
            el.textContent = (grouped ? val.toLocaleString() : val) + suffix;
            if (p < 1) requestAnimationFrame(tick);
        }
        if (reduceMotion) { el.textContent = raw; return; }
        requestAnimationFrame(tick);
    }

    /* ---- 6. Navbar elevation on scroll ---------------------------- */
    function wireNavbar() {
        function onScroll() {
            var bars = document.querySelectorAll(
                "#navbar-container .navbar,nav.navbar,header .navbar"
            );
            var scrolled = window.scrollY > 12;
            bars.forEach(function (b) {
                b.classList.toggle("bnp-nav-scrolled", scrolled);
            });
        }
        window.addEventListener("scroll", onScroll, { passive: true });
        onScroll();
    }

    /* ---- 7. Smooth in-page anchor scrolling ----------------------- */
    function wireSmoothScroll() {
        document.addEventListener("click", function (e) {
            var a = e.target.closest('a[href^="#"]');
            if (!a) return;
            var id = a.getAttribute("href");
            if (id.length < 2) return;
            var target = document.querySelector(id);
            if (!target) return;
            e.preventDefault();
            target.scrollIntoView({
                behavior: reduceMotion ? "auto" : "smooth",
                block: "start"
            });
        });
    }

    /* ---- 8. Broken-image fallback --------------------------------
       Every avatar on the site loads from the external i.pravatar.cc
       service. If it's blocked / offline / slow, the browser shows a
       broken-image glyph. We catch the load error (capture phase, since
       'error' doesn't bubble) and swap in a self-contained inline SVG
       placeholder so nothing ever looks broken — works with no network
       and also covers images injected later by the shared components. */
    var FALLBACK = "data:image/svg+xml;charset=UTF-8," + encodeURIComponent(
        '<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">' +
        '<rect width="100" height="100" fill="#e7ecf3"/>' +
        '<circle cx="50" cy="38" r="19" fill="#b9c4d4"/>' +
        '<path d="M16 92c0-19 15-31 34-31s34 12 34 31z" fill="#b9c4d4"/></svg>'
    );

    function onImgError(e) {
        var img = e.target;
        if (!img || img.tagName !== "IMG") return;
        if (img.dataset.bnpFallback) return;   // already swapped — avoid loops
        img.dataset.bnpFallback = "1";
        img.src = FALLBACK;
    }

    function wireImageFallback() {
        // capture phase catches the non-bubbling 'error' from any <img>
        document.addEventListener("error", onImgError, true);
        // catch images that already failed before this script ran
        document.querySelectorAll("img").forEach(function (img) {
            if (img.complete && img.naturalWidth === 0 && img.src) onImgError({ target: img });
        });
    }

    /* ---- 9. Breadcrumb navigation --------------------------------
       Builds a "Home › Section › Page" trail from the URL and injects
       it where the page can host it (marketing hero or app top-bar).
       Depth is derived from this script's own src, so the Home link is
       correct even if the site is served from a sub-directory. Pages
       with no suitable host (home page, full-screen auth) are skipped. */
    var LABEL_OVERRIDES = { "ca": "CA", "it": "IT", "faq": "FAQ" };

    function prettify(slug) {
        slug = slug.replace(/\.html?$/i, "");
        if (LABEL_OVERRIDES[slug]) return LABEL_OVERRIDES[slug];
        return slug.replace(/[-_]+/g, " ").replace(/\b\w/g, function (c) {
            return c.toUpperCase();
        });
    }

    function buildBreadcrumbs() {
        // already built, or the page ships its own breadcrumb → don't duplicate
        if (document.querySelector(".bnp-breadcrumb, .breadcrumb")) return;

        // how deep are we? read it from our own <script src>
        var sc = document.querySelector('script[src$="assets/js/animate.js"]');
        var prefix = sc ? sc.getAttribute("src").replace(/assets\/js\/animate\.js$/, "") : "";
        var depth = (prefix.match(/\.\.\//g) || []).length;

        // project-relative path segments (strip any hosting sub-dir)
        var segs = location.pathname.split("/").filter(Boolean).map(decodeURIComponent);
        if (!segs.length) segs = ["index.html"];
        var projectSegs = segs.slice(Math.max(0, segs.length - (depth + 1)));

        var file = projectSegs[projectSegs.length - 1] || "index.html";
        var folders = projectSegs.slice(0, -1);

        // home page itself → nothing to show
        if (folders.length === 0 && /^index\./i.test(file)) return;

        var trail = [{ label: "Home", href: prefix + "index.html", home: true }];
        folders.forEach(function (f) {
            if (f.toLowerCase() === "pages") return;   // internal wrapper folder
            trail.push({ label: prettify(f) });        // section (not linkable)
        });
        trail.push({ label: prettify(file), current: true });

        // render
        var nav = document.createElement("nav");
        nav.className = "bnp-breadcrumb";
        nav.setAttribute("aria-label", "Breadcrumb");
        trail.forEach(function (item, i) {
            if (i > 0) {
                var sep = document.createElement("i");
                sep.className = "fa-solid fa-angle-right bnp-sep";
                nav.appendChild(sep);
            }
            if (item.href) {
                var a = document.createElement("a");
                a.href = item.href;
                if (item.home) {
                    var ic = document.createElement("i");
                    ic.className = "fa-solid fa-house";
                    a.appendChild(ic);
                    a.appendChild(document.createTextNode(" "));
                }
                a.appendChild(document.createTextNode(item.label));
                nav.appendChild(a);
            } else {
                var s = document.createElement("span");
                if (item.current) s.className = "bnp-current";
                s.textContent = item.label;
                nav.appendChild(s);
            }
        });

        // place it at the TOP of the page, per layout
        var placeholder = document.querySelector("[data-breadcrumb],#breadcrumb-container");
        var appContent = document.querySelector(".app-content");
        var navbar = document.getElementById("navbar-container");
        if (placeholder) {
            placeholder.appendChild(nav);
        } else if (appContent) {
            // app pages: top of the content area, under the top-bar
            nav.classList.add("bnp-breadcrumb--app");
            appContent.insertBefore(nav, appContent.firstChild);
        } else if (navbar) {
            // marketing / content pages: full-width bar right under the navbar
            var bar = document.createElement("div");
            bar.className = "bnp-crumb-bar";
            var c = document.createElement("div");
            c.className = "container";
            c.appendChild(nav);
            bar.appendChild(c);
            navbar.insertAdjacentElement("afterend", bar);
        }
        // else: no suitable host (e.g. full-screen auth) — skip silently
    }

    /* ---- 10. FAQ accordion (delegated, works for injected markup) -- */
    function wireFaq() {
        document.addEventListener("click", function (e) {
            var q = e.target.closest(".bnp-faq__q");
            if (!q) return;
            var wasOpen = q.classList.contains("open");
            var list = q.closest(".bnp-faq__list");
            if (list) {   // accordion: close the others in this list first
                list.querySelectorAll(".bnp-faq__q.open, .bnp-faq__a.open")
                    .forEach(function (el) { el.classList.remove("open"); });
            }
            if (!wasOpen) {
                q.classList.add("open");
                var a = q.nextElementSibling;
                if (a) a.classList.add("open");
            }
        });
    }

    /* ---- 11. Boot ------------------------------------------------- */
    function init() {
        injectStyles();
        buildBreadcrumbs();
        wireImageFallback();
        wireNavbar();
        wireSmoothScroll();
        wireFaq();

        if (!supported) return; // page already fully visible — graceful exit
        root.classList.add("anim-ready");
        tagReveals();
        findCounters();

        // Shared navbar/footer are injected asynchronously by include.js —
        // rescan after they land so late content also animates (idempotent).
        window.addEventListener("load", function () { tagReveals(); findCounters(); });
        setTimeout(function () { tagReveals(); findCounters(); }, 700);
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }
})();
