function interceptAnchor( /** @type {HTMLAnchorElement} */ element, handler) {
    if (element.target != "_blank" || element.hasAttribute("data-dest")) return;

    element.setAttribute("data-dest", element.href);
    element.setAttribute("title", element.href);
    element.setAttribute("draggable", "false");
    element.href = "#/extern";

    // On click we prevent the navigation and openLink with the href.
    // We use the onclick function rather than
    // addEventListener("click", ...) because it helps prevent duplicates.
    element.onclick = () => {
        handler(element.getAttribute("data-dest"));
        return false;
    };
}

export default function hook(handler) {
    new MutationObserver((records) => {
        for (const record of records) {
            if (record.addedNodes.length > 0) {
                for (const element of record.addedNodes) {
                    if (element.nodeName.toLowerCase() == "a") {
                        interceptAnchor(element, handler);
                        break;
                    }
                }
            }

            // SvelteKit likes to undo all of our hard work. Let's prevent that.
            const element = record.target;
            if (element.nodeName.toLowerCase() == "a") {
                interceptAnchor(element, handler);
            }
        }
    }).observe(document.body, {
        subtree: true,
        attributes: true,
        childList: true
    });

    document.querySelectorAll("a").forEach(interceptAnchor);
}