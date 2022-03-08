
new MutationObserver(() => {
    for (const anchor of document.querySelectorAll("a")) {
        const rel = anchor.getAttribute("rel");

        // Intercept all rel="external" anchors and add a click listener.
        if (rel == "external" || rel == "_blank") {
            const href = anchor.href;
            anchor.href = "#"; // Clear the property incase it tries to open the link.

            // On click we prevent the navigation and openLink with the href.
            // We use the onclick function rather than 
            // addEventListener("click", ...) because it helps prevent duplicates.
            anchor.onclick = () => {
                UI.openLink(href);
                return false;
            };
        }
    }
}).observe(document.body, {
    subtree: true,
    childList: true
});
