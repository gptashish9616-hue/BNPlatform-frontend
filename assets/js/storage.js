// ============================================================
//  Local storage helpers — JWT token + cached current user.
// ============================================================
(function () {
    const TOKEN_KEY = "bnp_token";
    const USER_KEY = "bnp_user";

    const BnpStorage = {
        setToken(token) {
            if (token) localStorage.setItem(TOKEN_KEY, token);
        },
        getToken() {
            return localStorage.getItem(TOKEN_KEY);
        },
        setUser(user) {
            if (user) localStorage.setItem(USER_KEY, JSON.stringify(user));
        },
        getUser() {
            try {
                return JSON.parse(localStorage.getItem(USER_KEY));
            } catch (e) {
                return null;
            }
        },
        /** Persist the AuthResponse returned by /api/auth/login|register. */
        setSession(auth) {
            if (!auth) return;
            this.setToken(auth.token);
            this.setUser({
                userId: auth.userId,
                fullName: auth.fullName,
                email: auth.email,
                role: auth.role,
                avatarUrl: auth.avatarUrl
            });
        },
        clear() {
            localStorage.removeItem(TOKEN_KEY);
            localStorage.removeItem(USER_KEY);
        },
        isAuthenticated() {
            return Boolean(this.getToken());
        },
        isAdmin() {
            const u = this.getUser();
            return u && (u.role === "SUPER_ADMIN" || u.role === "SUB_ADMIN");
        },
        isPremium() {
            const u = this.getUser();
            return u && (u.role === "PREMIUM_USER" || u.role === "SUPER_ADMIN" || u.role === "SUB_ADMIN");
        },

        // ── "New item" tracking (admin lists: referrals, requirements, …) ──
        // Purely client-side — marks which list items an admin has already
        // opened, so newly-arrived ones can be highlighted until viewed.
        _seenKey(scope) {
            return "bnp_seen_" + scope;
        },
        /** First time a scope is seen, baseline every current id as "seen"
         *  so pre-existing items don't all show up as new. */
        initSeenBaseline(scope, ids) {
            const key = this._seenKey(scope);
            if (localStorage.getItem(key) === null) {
                localStorage.setItem(key, JSON.stringify(ids));
            }
        },
        isSeen(scope, id) {
            try {
                const raw = localStorage.getItem(this._seenKey(scope));
                if (raw === null) return true;
                return JSON.parse(raw).includes(id);
            } catch (e) {
                return true;
            }
        },
        markSeen(scope, id) {
            const key = this._seenKey(scope);
            let ids = [];
            try { ids = JSON.parse(localStorage.getItem(key)) || []; } catch (e) { ids = []; }
            if (!ids.includes(id)) {
                ids.push(id);
                localStorage.setItem(key, JSON.stringify(ids));
            }
        }
    };

    window.BnpStorage = BnpStorage;
})();
