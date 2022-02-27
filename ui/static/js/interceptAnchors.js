
new MutationObserver(() => {
    for (const anchor of document.querySelectorAll("a")) {

        // Intercept all rel="external" anchors and add a click listener.
        if (anchor.getAttribute("rel") == "external") {

            // On click we prevent the navigation and openLink with the href.
            // We use the onclick function rather than 
            // addEventListener("click", ...) because it helps prevent duplicates.
            anchor.onclick = () => {
                window.Bridge.emit("ui:openlink", { link: anchor.href });
                return false;
            };
        }
    }
}).observe(document.body, {
    subtree: true,
    childList: true
});
