// Canonical list of all profession/category values used across the app.
// Any page that shows a profession/category picker should read from here
// instead of hardcoding its own list, so every list stays in sync.
window.BnpProfessions = {
    TOP: ["IT", "Finance", "Legal", "Design", "Marketing", "Healthcare"],
    MORE: [
        "Architecture", "Education", "Real Estate", "Consulting", "Retail", "Manufacturing",
        "Construction", "Photography", "Events", "Hospitality", "Transportation", "Insurance",
        "Engineering", "HR & Recruitment", "Media & PR"
    ],
    OTHER: [
        "Accounting", "Agriculture", "Automotive", "Beauty & Wellness", "Coaching", "E-commerce",
        "Electronics", "Fashion", "Food & Beverage", "Government", "Interior Design", "Jewellery",
        "NGO & Social", "Pharma", "Sports", "Travel & Tourism", "Textiles", "Security", "Logistics", "Energy"
    ]
};

BnpProfessions.ALL = [...BnpProfessions.TOP, ...BnpProfessions.MORE, ...BnpProfessions.OTHER];

// Fills a <select> element with one <option> per category.
// opts.placeholder, if given, is inserted as a leading empty-value option.
BnpProfessions.fillSelect = function (selectEl, opts) {
    opts = opts || {};
    if (!selectEl) return;
    selectEl.innerHTML = "";
    if (opts.placeholder !== undefined) {
        const o = document.createElement("option");
        o.value = "";
        o.textContent = opts.placeholder;
        selectEl.appendChild(o);
    }
    BnpProfessions.ALL.forEach(cat => {
        const o = document.createElement("option");
        o.value = cat;
        o.textContent = cat;
        selectEl.appendChild(o);
    });
    if (opts.extraOptions) {
        opts.extraOptions.forEach(text => {
            const o = document.createElement("option");
            o.value = text;
            o.textContent = text;
            selectEl.appendChild(o);
        });
    }
};
