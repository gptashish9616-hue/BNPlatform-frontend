document.addEventListener("DOMContentLoaded", () => {
    const navbarContainer = document.getElementById("navbar-container");

    if (navbarContainer) {
        fetch("components/navbar.html")
            .then((response) => response.text())
            .then((data) => {
                navbarContainer.innerHTML = data;
            })
            .catch((error) => {
                console.warn("Navbar component could not be loaded.", error);
            });
    }

    const membersGrid = document.getElementById("members-grid");
    const loadingText = document.getElementById("members-loading-text");

    if (membersGrid) {
        membersGrid.innerHTML = `
            <div class="col-12 text-center text-muted py-3">Loading members…</div>
        `;

        getMembers()
            .then((members) => renderMembers(membersGrid, members, loadingText))
            .catch((error) => {
                console.error("Unable to render members section.", error);

                if (loadingText) {
                    loadingText.textContent = "Unable to load members right now.";
                }
            });
    }
});

function renderMembers(membersGrid, members, loadingText) {
    const items = Array.isArray(members) ? members.slice(0, 8) : [];

    if (!items.length) {
        membersGrid.innerHTML = `
            <div class="col-12 text-center text-muted py-3">No members available right now.</div>
        `;

        if (loadingText) {
            loadingText.textContent = "No members available right now.";
        }

        return;
    }

    membersGrid.innerHTML = items.map((member) => `
        <div class="col-lg-3 col-md-6">
            <article class="member-card h-100">
                <img src="${member.image}" alt="${member.name}" onerror="this.onerror=null;this.src='https://ui-avatars.com/api/?name=${encodeURIComponent(member.name)}&background=0d6efd&color=fff'">
                <h5>
                    ${member.name}
                    ${member.verified ? '<i class="fa-solid fa-circle-check text-primary"></i>' : ""}
                </h5>
                <p>${member.profession}</p>
                <small><i class="fa-solid fa-location-dot"></i> ${member.location}</small><br><br>
                <a href="#" class="btn btn-outline-primary rounded-pill px-4">View Profile</a>
            </article>
        </div>
    `).join("");

    if (loadingText) {
        loadingText.textContent = `Showing ${items.length} professionals from the backend data feed.`;
    }
}
