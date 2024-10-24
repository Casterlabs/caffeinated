import Conn from "../conn";
import EventHandler from "../eventHandler";

const events = new EventHandler();

let store = {
    language: "en",
    emojiProvider: "system",
    theme: ["gray", "gray"],
    appearance: "DARK",
    zoom: 1
};

let localeProvider = async (k: string, knownPlaceholders: any | null, knownComponents: any | null) => "LOCALE_PRERENDER:" + k;

export const GLOBAL = {
    get store() {
        return store;
    },

    on(key: string, listener: (data: any | null | undefined) => void) {
        const listenerId = events.on(key, listener);

        if (store[key]) {
            listener(store[key]);
        }

        return [key, listenerId];
    },

    off: events.off,

    get(key: string) {
        return store[key];
    },

    async localize(key: string, knownPlaceholders = {}, knownComponents = []) {
        return await localeProvider(key, knownPlaceholders, knownComponents);
    }
};
export default GLOBAL;

let appStyleElement: HTMLStyleElement | null;
function recomputeStyle() {
    if (!appStyleElement) return;
    const { emojiProvider } = store;

    if (emojiProvider == "system") {
        appStyleElement.innerHTML = ``;
    } else {
        appStyleElement.innerHTML = `
        [data-rich-type="emoji"] > [data-emoji-provider="system"] {
            display: none !important;
        }
        
        [data-rich-type="emoji"] > [data-emoji-provider="${emojiProvider}"] {
            display: inline-block !important;
        }
        `;
    }
}

export function init(conn: Conn) {
    if (appStyleElement) return;

    appStyleElement = document.createElement("style");
    appStyleElement.id = "app-style";
    document.head.appendChild(appStyleElement);

    recomputeStyle();

    function mutate(key: string, value: any | null | undefined) {
        if (store[key] === value) return;

        store[key] = value;
        events.broadcast(key, value);

        if (["emojiProvider"].includes(key) && appStyleElement) {
            recomputeStyle();
        }
    }

    conn.on("app", ({ language, emojiProvider, theme, appearance, zoom }) => {
        mutate("language", language);
        mutate("emojiProvider", emojiProvider);
        mutate("theme", theme);
        mutate("appearance", appearance);
        mutate("zoom", zoom);
    });

    let appLocalizationTasks = {};
    conn.on("localize", ({ nonce, value }) => {
        appLocalizationTasks[nonce](value);
    });
    localeProvider = (key, knownPlaceholders, knownComponents) => {
        return new Promise((resolve) => {
            const nonce = Math.random().toString(28);
            appLocalizationTasks[nonce] = resolve;

            conn.send("LOCALIZE", {
                nonce: nonce,
                key: key || '',
                knownPlaceholders: knownPlaceholders || {},
                knownComponents: knownComponents || []
            });
        });
    };
}
