// ============================================================
//  Auth page logic — binds the login / register / forgot / reset
//  forms to the backend. Include AFTER config/storage/utils/api/guards.
// ============================================================

// Returns { latitude, longitude } or null if denied/unavailable — used during registration
function getLocationForReg() {
    return new Promise((resolve) => {
        if (!navigator.geolocation) { resolve(null); return; }
        navigator.geolocation.getCurrentPosition(
            (pos) => resolve({ latitude: pos.coords.latitude, longitude: pos.coords.longitude }),
            () => resolve(null),
            { timeout: 10000 }
        );
    });
}

(function () {
    // ---- helpers shared with inline markup (password eye + strength meter) ----
    window.togglePw = function (id, el) {
        const input = document.getElementById(id);
        if (!input) return;
        const show = input.type === "password";
        input.type = show ? "text" : "password";
        el.classList.toggle("fa-eye", !show);
        el.classList.toggle("fa-eye-slash", show);
    };

    window.strength = function () {
        const input = document.getElementById("password") || document.getElementById("newpw");
        if (!input) return;
        const v = input.value;
        let score = 0;
        if (v.length >= 8) score++;
        if (/[A-Z]/.test(v)) score++;
        if (/[0-9]/.test(v)) score++;
        if (/[^A-Za-z0-9]/.test(v)) score++;
        const colors = ["#e2e5ea", "#e31e24", "#f59e0b", "#3b82f6", "#1e8e3e"];
        document.querySelectorAll("#pwStrength span").forEach((s, i) => {
            s.style.background = i < score ? colors[score] : "#e2e5ea";
        });
    };

    // referenced by inline onclick in the forgot/reset markup
    window.resend = function (e) {
        if (e) e.preventDefault();
        const emailEl = document.getElementById("email");
        if (emailEl && emailEl.value) {
            window.Api.auth.forgotPassword({ email: emailEl.value.trim() })
                .then((token) => {
                    const cta = document.querySelector("#sentStep a.auth-btn");
                    if (cta) cta.setAttribute("href", "reset-password.html?token=" + encodeURIComponent(token));
                    window.BnpUtils.toast("A new reset link has been generated.", "success");
                })
                .catch((err) => window.BnpUtils.toast(err.message, "error"));
        } else {
            window.BnpUtils.toast("A new reset link has been sent.", "info");
        }
    };

    function fieldError(inputId, errId, valid) {
        const input = document.getElementById(inputId);
        if (input) input.closest(".auth-input")?.classList.toggle("invalid", !valid);
        const err = document.getElementById(errId);
        if (err) err.style.display = valid ? "none" : "block";
        return valid;
    }

    function dashRedirect() {
        const admin = window.BnpStorage.isAdmin();
        location.href = admin ? "../dashboard/admin-dashboard.html" : "../dashboard/user-dashboard.html";
    }

    const emailOk = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim());

    // ---------------- LOGIN ----------------
    function bindLogin(form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("email");
            const pw = document.getElementById("password");
            let ok = fieldError("email", "emailErr", emailOk(email.value));
            ok = fieldError("password", "pwErr", pw.value.length > 0) && ok;
            if (!ok) return;

            const btn = form.querySelector("button[type=submit]");
            window.BnpUtils.loading(btn, true, "Signing in…");
            try {
                const auth = await window.Api.auth.login({ email: email.value.trim(), password: pw.value });
                window.BnpStorage.setSession(auth);
                window.BnpUtils.toast("Welcome back, " + auth.fullName + "!", "success");
                dashRedirect();
            } catch (err) {
                window.BnpUtils.loading(btn, false);
                window.BnpUtils.toast(err.message, "error");
            }
        });
    }

    // ---------------- REGISTER ----------------
    function bindRegister(form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const name = document.getElementById("name");
            const email = document.getElementById("email");
            const phone = document.getElementById("phone");
            const pw = document.getElementById("password");
            const terms = document.getElementById("terms");

            let ok = fieldError("name", "nameErr", name.value.trim().length > 1);
            ok = fieldError("email", "emailErr", emailOk(email.value)) && ok;
            ok = fieldError("phone", "phoneErr", phone.value.replace(/\D/g, "").length >= 10) && ok;
            ok = fieldError("password", "pwErr", pw.value.length >= 8) && ok;
            const termsOk = terms.checked;
            const termsErr = document.getElementById("termsErr");
            if (termsErr) termsErr.style.display = termsOk ? "none" : "block";
            ok = ok && termsOk;
            if (!ok) return;

            const btn = form.querySelector("button[type=submit]");
            window.BnpUtils.loading(btn, true, "Getting location…");
            const loc = await getLocationForReg();
            window.BnpUtils.loading(btn, true, "Creating account…");
            try {
                const auth = await window.Api.auth.register({
                    fullName: name.value.trim(),
                    email: email.value.trim(),
                    phone: phone.value.trim(),
                    password: pw.value,
                    latitude: loc ? loc.latitude : undefined,
                    longitude: loc ? loc.longitude : undefined,
                    referralCode: window.BnpUtils.param("ref") || undefined
                });
                window.BnpStorage.setSession(auth);
                window.BnpUtils.toast("Account created. Welcome!", "success");
                dashRedirect();
            } catch (err) {
                window.BnpUtils.loading(btn, false);
                window.BnpUtils.toast(err.message, "error");
            }
        });
    }

    // ---------------- FORGOT PASSWORD ----------------
    function bindForgot(form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const email = document.getElementById("email");
            if (!fieldError("email", "emailErr", emailOk(email.value))) return;

            const btn = form.querySelector("button[type=submit]");
            window.BnpUtils.loading(btn, true, "Sending…");
            try {
                const token = await window.Api.auth.forgotPassword({ email: email.value.trim() });
                document.getElementById("sentEmail").textContent = email.value.trim();
                document.getElementById("requestStep").style.display = "none";
                document.getElementById("sentStep").style.display = "block";

                // No email service yet → expose the reset link so the flow is usable.
                const link = "reset-password.html?token=" + encodeURIComponent(token);
                const cta = document.querySelector("#sentStep a.auth-btn");
                if (cta) cta.setAttribute("href", link);
            } catch (err) {
                window.BnpUtils.loading(btn, false);
                window.BnpUtils.toast(err.message, "error");
            }
        });
    }

    // ---------------- RESET PASSWORD ----------------
    function bindReset(form) {
        const token = window.BnpUtils.param("token") || "";
        const tokenField = document.getElementById("resetToken");
        if (tokenField && token) tokenField.value = token;

        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const pw = document.getElementById("newpw");
            const cpw = document.getElementById("confirmpw");
            const tok = (tokenField ? tokenField.value : token).trim();

            let ok = fieldError("resetToken", "otpErr", tok.length > 0);
            ok = fieldError("newpw", "pwErr", pw.value.length >= 8) && ok;
            ok = fieldError("confirmpw", "cpwErr", pw.value === cpw.value && pw.value.length >= 8) && ok;
            if (!ok) return;

            const btn = form.querySelector("button[type=submit]");
            window.BnpUtils.loading(btn, true, "Resetting…");
            try {
                await window.Api.auth.resetPassword({ token: tok, newPassword: pw.value });
                document.getElementById("resetStep").style.display = "none";
                document.getElementById("doneStep").style.display = "block";
            } catch (err) {
                window.BnpUtils.loading(btn, false);
                window.BnpUtils.toast(err.message, "error");
            }
        });
    }

    // ---------------- ADMIN SETUP ----------------
    function bindAdminSetup(form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const name = document.getElementById("name");
            const email = document.getElementById("email");
            const phone = document.getElementById("phone");
            const pw = document.getElementById("password");
            const setupKey = document.getElementById("setupKey");

            let ok = fieldError("name", "nameErr", name.value.trim().length > 1);
            ok = fieldError("email", "emailErr", emailOk(email.value)) && ok;
            ok = fieldError("phone", "phoneErr", phone.value.replace(/\D/g, "").length >= 10) && ok;
            ok = fieldError("password", "pwErr", pw.value.length >= 8) && ok;
            ok = fieldError("setupKey", "setupKeyErr", setupKey.value.trim().length > 0) && ok;
            if (!ok) return;

            const btn = form.querySelector("button[type=submit]");
            window.BnpUtils.loading(btn, true, "Setting up…");
            try {
                const auth = await window.Api.auth.adminSetup({
                    fullName: name.value.trim(),
                    email: email.value.trim(),
                    phone: phone.value.trim(),
                    password: pw.value,
                    setupKey: setupKey.value.trim()
                });
                window.BnpStorage.setSession(auth);
                window.BnpUtils.toast("Super Admin account created. Welcome!", "success");
                location.href = "../dashboard/admin-dashboard.html";
            } catch (err) {
                window.BnpUtils.loading(btn, false);
                window.BnpUtils.toast(err.message, "error");
            }
        });
    }

    document.addEventListener("DOMContentLoaded", () => {
        // already signed in? skip the auth screens
        if (window.BnpStorage.isAuthenticated() &&
            /login|register|admin-setup/.test(location.pathname.split("/").pop())) {
            dashRedirect();
            return;
        }
        const login = document.getElementById("loginForm");
        const reg = document.getElementById("regForm");
        const forgot = document.getElementById("forgotForm");
        const reset = document.getElementById("resetForm");
        const adminSetup = document.getElementById("adminSetupForm");
        if (login) bindLogin(login);
        if (reg) bindRegister(reg);
        if (forgot) bindForgot(forgot);
        if (reset) bindReset(reset);
        if (adminSetup) bindAdminSetup(adminSetup);
    });
})();
