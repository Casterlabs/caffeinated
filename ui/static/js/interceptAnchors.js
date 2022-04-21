new MutationObserver(() => {
    for (const anchor of document.querySelectorAll("a")) {
        // Intercept all rel="external" and target="_blank" anchors and add a click listener.
        if ((anchor.rel == "external" || anchor.target == "_blank") && !anchor.getAttribute("dest")) {
            anchor.setAttribute("dest", anchor.href);
            anchor.href = "#"; // Clear the property incase it tries to open the link.

            // On click we prevent the navigation and openLink with the href.
            // We use the onclick function rather than
            // addEventListener("click", ...) because it helps prevent duplicates.
            anchor.onclick = () => {
                Caffeinated.UI.openLink(anchor.getAttribute("dest"));
                return false;
            };
        }
    }
}).observe(document.body, {
    subtree: true,
    childList: true
});