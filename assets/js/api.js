// ============================================================
//  Central API client for the BNPlatform Spring Boot backend.
//  Handles base URL, JWT auth header, and the ApiResponse envelope.
//  Exposes `window.Api` with one method per backend module.
// ============================================================
(function () {
    const base = () => (window.BNP_CONFIG?.API_BASE_URL || "").replace(/\/$/, "");

    /**
     * Core request helper.
     * @returns the `data` field of the ApiResponse envelope.
     * @throws Error(message) on network / HTTP / business errors.
     */
    async function request(path, { method = "GET", body, auth = true } = {}) {
        const headers = { Accept: "application/json" };
        if (body !== undefined) headers["Content-Type"] = "application/json";

        const token = window.BnpStorage?.getToken();
        if (auth && token) headers["Authorization"] = "Bearer " + token;

        let res;
        try {
            res = await fetch(base() + path, {
                method,
                headers,
                body: body !== undefined ? JSON.stringify(body) : undefined
            });
        } catch (networkErr) {
            throw new Error("Cannot reach the server. Is the backend running?");
        }

        // 401 → session is invalid/expired; clear and bounce to login
        if (res.status === 401 && auth) {
            window.BnpStorage?.clear();
            if (window.BnpGuards?.redirectToLogin) window.BnpGuards.redirectToLogin();
            throw new Error("Your session has expired. Please sign in again.");
        }

        let json = null;
        try { json = await res.json(); } catch (e) { /* empty body */ }

        if (!res.ok || (json && json.success === false)) {
            throw new Error((json && json.message) || `Request failed (${res.status})`);
        }
        return json ? json.data : null;
    }

    const get = (p) => request(p);
    const post = (p, body, auth = true) => request(p, { method: "POST", body, auth });
    const put = (p, body) => request(p, { method: "PUT", body });
    const del = (p) => request(p, { method: "DELETE" });
    const qs = (obj) => {
        const s = Object.entries(obj || {})
            .filter(([, v]) => v !== undefined && v !== null && v !== "")
            .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
            .join("&");
        return s ? "?" + s : "";
    };

    /**
     * Multipart upload helper. Sends a real file (not JSON) to /api/files and
     * returns the stored file's metadata ({ url, fileName, contentType, size }).
     */
    async function uploadFile(file, category = "misc") {
        const token = window.BnpStorage?.getToken();
        const form = new FormData();
        form.append("file", file);
        form.append("category", category);

        let res;
        try {
            res = await fetch(base() + "/api/files", {
                method: "POST",
                headers: token ? { Authorization: "Bearer " + token } : {},
                body: form
            });
        } catch (networkErr) {
            throw new Error("Cannot reach the server. Is the backend running?");
        }

        if (res.status === 401) {
            window.BnpStorage?.clear();
            if (window.BnpGuards?.redirectToLogin) window.BnpGuards.redirectToLogin();
            throw new Error("Your session has expired. Please sign in again.");
        }

        let json = null;
        try { json = await res.json(); } catch (e) { /* empty body */ }
        if (!res.ok || (json && json.success === false)) {
            throw new Error((json && json.message) || `Upload failed (${res.status})`);
        }
        return json ? json.data : null;
    }

    const Api = {
        request,

        files: {
            upload: uploadFile
        },

        auth: {
            register: (d) => post("/api/auth/register", d, false),
            login: (d) => post("/api/auth/login", d, false),
            forgotPassword: (d) => post("/api/auth/forgot-password", d, false),
            resetPassword: (d) => post("/api/auth/reset-password", d, false),
            adminSetup: (d) => post("/api/auth/admin/setup", d, false)
        },

        users: {
            me: () => get("/api/users/me"),
            updateMe: (d) => put("/api/users/me", d),
            get: (id) => get(`/api/users/${id}`),
            search: (q, city, lat, lng) => get("/api/users/search" + qs({ q, city, lat, lng })),
            nearby: (city) => get("/api/users/nearby" + qs({ city }))
        },

        profile: {
            addresses: () => get("/api/profile/addresses"),
            addAddress: (d) => post("/api/profile/addresses", d),
            updateAddress: (id, d) => put(`/api/profile/addresses/${id}`, d),
            deleteAddress: (id) => del(`/api/profile/addresses/${id}`),
            company: () => get("/api/profile/company"),
            saveCompany: (d) => put("/api/profile/company", d),
            documents: () => get("/api/profile/documents"),
            addDocument: (d) => post("/api/profile/documents", d),
            deleteDocument: (id) => del(`/api/profile/documents/${id}`)
        },

        invites: {
            send: (d) => post("/api/invites", d),
            bulk: (emails, message) => post("/api/invites/bulk", { emails, message }),
            history: () => get("/api/invites/history"),
            stats: () => get("/api/invites/stats"),
            myCode: () => get("/api/invites/my-code")
        },

        referrals: {
            create: (d) => post("/api/referrals", d),
            given: () => get("/api/referrals/given"),
            received: () => get("/api/referrals/received"),
            details: (id) => get(`/api/referrals/${id}`),
            updateStatus: (id, status) => put(`/api/referrals/${id}/status` + qs({ status }))
        },

        requirements: {
            all: () => get("/api/requirements"),
            mine: () => get("/api/requirements/mine"),
            create: (d) => post("/api/requirements", d),
            details: (id) => get(`/api/requirements/${id}`),
            responses: (id) => get(`/api/requirements/${id}/responses`),
            respond: (id, d) => post(`/api/requirements/${id}/responses`, d),
            accept: (id, responseId) => put(`/api/requirements/${id}/responses/${responseId}/accept`),
            fulfill: (id) => put(`/api/requirements/${id}/fulfill`)
        },

        subscription: {
            plans: () => get("/api/plans"),
            subscribe: (planId) => post(`/api/subscriptions/${planId}`),
            current: () => get("/api/subscriptions/current"),
            history: () => get("/api/subscriptions/history"),
            cancel: () => post("/api/subscriptions/cancel"),
            payments: () => get("/api/payments/history")
        },

        chat: {
            start: (otherUserId) => post(`/api/chat/conversations/${otherUserId}`),
            conversations: () => get("/api/chat/conversations"),
            messages: (id) => get(`/api/chat/conversations/${id}/messages`),
            send: (id, content) => post(`/api/chat/conversations/${id}/messages`, { content }),
            deleteMessage: (id, messageId) => del(`/api/chat/conversations/${id}/messages/${messageId}`),
            deleteConversation: (id) => del(`/api/chat/conversations/${id}`)
        },

        groupChat: {
            browsableGroups: () => get("/api/group-chat/groups"),
            myGroups: () => get("/api/group-chat/my-groups"),
            join: (groupId) => post(`/api/group-chat/groups/${groupId}/join`),
            leave: (groupId) => post(`/api/group-chat/groups/${groupId}/leave`),
            messages: (groupId) => get(`/api/group-chat/groups/${groupId}/messages`),
            send: (groupId, content) => post(`/api/group-chat/groups/${groupId}/messages`, { content }),
            deleteMessage: (groupId, messageId) => del(`/api/group-chat/groups/${groupId}/messages/${messageId}`)
        },

        notifications: {
            list: (unreadOnly) => get("/api/notifications" + qs({ unreadOnly })),
            unreadCount: () => get("/api/notifications/unread-count"),
            markRead: (id) => put(`/api/notifications/${id}/read`),
            markAllRead: () => put("/api/notifications/read-all"),
            delete: (id) => del(`/api/notifications/${id}`)
        },

        points: {
            history: () => get("/api/points/history"),
            leaderboard: (city, state) => get("/api/leaderboard" + qs({ city, state }))
        },

        reviews: {
            forUser: (userId) => get(`/api/reviews/user/${userId}`),
            add: (d) => post("/api/reviews", d)
        },

        feed: {
            list: () => get("/api/feed"),
            create: (d) => post("/api/feed", d),
            like: (id) => post(`/api/feed/${id}/like`),
            comments: (id) => get(`/api/feed/${id}/comments`),
            comment: (id, content) => post(`/api/feed/${id}/comments`, { content })
        },

        dashboard: {
            user: () => get("/api/dashboard/user"),
            admin: () => get("/api/admin/dashboard")
        },

        admin: {
            users: () => get("/api/admin/users"),
            user: (id) => get(`/api/admin/users/${id}`),
            updateStatus: (id, status) => put(`/api/admin/users/${id}/status` + qs({ status })),
            updateRole: (id, role) => put(`/api/admin/users/${id}/role` + qs({ role })),
            resetUserData: (id) => post(`/api/admin/users/${id}/reset-data`),
            referrals: () => get("/api/admin/referrals"),
            requirements: () => get("/api/admin/requirements"),
            requirementResponses: (id) => get(`/api/admin/requirements/${id}/responses`),
            updateRequirement: (id, d) => put(`/api/admin/requirements/${id}`, d),
            updateRequirementStatus: (id, status) => put(`/api/admin/requirements/${id}/status` + qs({ status })),
            deleteRequirement: (id) => del(`/api/admin/requirements/${id}`),
            groups: () => get("/api/admin/groups"),
            group: (id) => get(`/api/admin/groups/${id}`),
            createGroup: (d) => post("/api/admin/groups", d),
            updateGroup: (id, d) => put(`/api/admin/groups/${id}`, d),
            deleteGroup: (id) => del(`/api/admin/groups/${id}`),
            updateGroupStatus: (id, status) => put(`/api/admin/groups/${id}/status` + qs({ status })),
            subscriptions: () => get("/api/admin/subscriptions"),
            rewardConfig: () => get("/api/admin/reward-config"),
            updateRewardConfig: (d) => put("/api/admin/reward-config", d)
        }
    };

    window.Api = Api;
})();

// -------------------------------------------------------------
//  Public homepage members grid (used by app.js on index.html).
//  Falls back to demo data when the API isn't reachable.
// -------------------------------------------------------------
function normalizeMembers(data) {
    const list = Array.isArray(data) ? data : (data?.data || data?.members || data?.results || []);
    return list.map((member, index) => {
        const fullName = member.fullName || member.name
            || [member.firstName, member.lastName].filter(Boolean).join(" ")
            || `Member ${index + 1}`;

        // Try every common field path the backend might use for city
        const primaryAddr = Array.isArray(member.addresses)
            ? (member.addresses.find(a => a.isPrimary || a.primary) || member.addresses[0])
            : null;
        const city = (
            member.city ||
            member.businessCity ||
            member.primaryCity ||
            member.address?.city ||
            primaryAddr?.city ||
            member.location?.city ||
            ''
        ).toString().trim();

        const state = (
            member.state ||
            member.businessState ||
            member.primaryState ||
            member.address?.state ||
            primaryAddr?.state ||
            member.location?.state ||
            ''
        ).toString().trim();

        const locationStr = city
            ? `${city}${state ? ', ' + state : ''}`
            : (typeof member.location === 'string' ? member.location : 'India');

        return {
            id: member.id || `member-${index + 1}`,
            name: fullName,
            profession: member.profession || member.industry || member.title || "Professional",
            location: locationStr,
            city: city.toLowerCase(),
            image: (window.BnpUtils?.fileUrl(member.avatarUrl) || member.image || member.avatar) || "https://ui-avatars.com/api/?name=" + encodeURIComponent(fullName) + "&background=e31e24&color=fff",
            verified: Boolean(member.verified ?? member.premium ?? true),
            latitude: member.latitude ?? null,
            longitude: member.longitude ?? null
        };
    });
}

// Fetches one page of the public member directory. Backend caps page size at 100,
// so however many members exist (100, 10000, ...) this always stays a light request —
// callers page through with `page`/`size` instead of asking for everything at once.
function getMembersPage(page = 0, size = 24) {
    const config = window.BNPLATFORM_CONFIG || {};
    const baseUrl = (config.API_BASE_URL || "").replace(/\/$/, "");
    const endpoint = (config.MEMBERS_ENDPOINT || "/api/users/search").replace(/^\//, "");
    const url = `${baseUrl ? `${baseUrl}/${endpoint}` : `/${endpoint}`}?page=${page}&size=${size}`;

    return fetch(url, { headers: { Accept: "application/json" } })
        .then((response) => {
            if (!response.ok) throw new Error(`Request failed with status ${response.status}`);
            return response.json();
        })
        .then((body) => {
            const payload = body?.data || {};
            const rawList = Array.isArray(payload) ? payload : (payload.members || []);
            return {
                members: normalizeMembers(rawList),
                page: payload.page ?? page,
                size: payload.size ?? size,
                totalElements: payload.totalElements ?? rawList.length,
                totalPages: payload.totalPages ?? 1,
                hasNext: payload.hasNext ?? false
            };
        })
        .catch((error) => {
            console.warn("Members API not available, using fallback member data.", error);
            const fallback = normalizeMembers(config.FALLBACK_MEMBERS || []);
            return { members: fallback, page: 0, size: fallback.length, totalElements: fallback.length, totalPages: 1, hasNext: false };
        });
}

// Convenience wrapper for callers that just want a flat list (e.g. the homepage teaser).
function getMembers() {
    return getMembersPage(0, 100).then((result) => result.members);
}
