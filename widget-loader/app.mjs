import EventHandler from "./eventHandler.mjs";

const eventHandler = new EventHandler();

let store = {
    language: "en",
    emojiProvider: "system",
    theme: ["gray", "gray"],
    appearance: "DARK"
};

let appStyleElement;

function recomputeStyle() {
    const { emojiProvider } = store;

    if (emojiProvider == "system") {
        appStyleElement.innerHTML = ``;
    } else {
        appStyleElement.innerHTML = `
        [data-rich-type="emoji"] > [data-emoji-provider="system"] {
            display: none;
        }
        
        [data-rich-type="emoji"] > [data-emoji-provider="${emojiProvider}"] {
            display: inline-block !important;
        }
        `;
    }
}

function init(doc = document) {
    if (appStyleElement) return;

    appStyleElement = doc.createElement("style");
    appStyleElement.id = "app-style";
    doc.head.appendChild(appStyleElement);
    recomputeStyle();
}

export default {
    init,

    get store() {
        return store;
    },

    on(key, listener) {
        const listenerId = eventHandler.on(key, listener);

        if (store[key]) {
            listener(store[key]);
        }

        return [key, listenerId];
    },

    off: eventHandler.off,

    get(key) {
        return store[key];
    },

    mutate(key, value) {
        if (store[key] === value) return;

        store[key] = value;
        eventHandler.broadcast(key, value);

        if (["emojiProvider"].includes(key) && appStyleElement) {
            recomputeStyle();
        }
    }
};