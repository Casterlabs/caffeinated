import Currencies from "./currencies.mjs";
import Conn from "./conn.mjs";
import EventHandler from "./eventHandler.mjs";

const queryParams = (() => {
    let vars = {};

    location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, (m, key, value) => {
        vars[key] = value;
    });

    return vars;
})();

let currentTheme = { appearance: "FOLLOW_SYSTEM", name: "System", isAuto: true, id: "system" };

const AppContext = {
    ...new EventHandler(),

    get currentTheme() {
        return currentTheme;
    }
};

window.addEventListener("message", (event) => {
    if (typeof event.data == "object" && event.data.call == "theme") {
        currentTheme = event.data.value;
        AppContext.broadcast("theme-update", currentTheme);
    }
}, false);

const { pluginId, widgetId, authorization } = queryParams;
const port = queryParams.port || "8092";
const address = queryParams.address || "127.0.0.1";
const widgetMode = (queryParams.mode || "WIDGET").toUpperCase();

export function deepFreeze(object) {
    const propNames = Object.getOwnPropertyNames(object);

    for (const name of propNames) {
        const value = object[name];

        if (value && typeof value === "object") {
            deepFreeze(value);
        }
    }

    return Object.freeze(object);
}

export function escapeHtml(unsafe) {
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

export function init({ initHandler, disconnectHandler }) {
    if (window.parent) {
        window.parent.postMessage({ call: "init", value: widgetId }, "*");
    }

    const conn = new Conn(`ws://${address}:${port}/api/plugin/${pluginId}/widget/${widgetId}/realtime?authorization=${authorization}&mode=${widgetMode}`);

    // The `Widget` global.
    const widgetInstance = {
        ...new EventHandler(),

        get connectionId() {
            return conn.connectionId;
        },

        get widgetData() {
            return conn.widgetData;
        },

        get mode() {
            return widgetMode;
        },

        getSetting(key) {
            return conn.widgetData.settings[key];
        },

        emit(type, data) {
            conn.emit(type, data);
        }

        // getResource(resourceId) {

        // }
    };

    // The `Koi` global.
    let koi_statics = {};

    const koiInstance = {
        ...new EventHandler(),

        get eventHistory() {
            return koi_statics.history;
        },

        get viewers() {
            return koi_statics.viewers;
        },

        get userStates() {
            return koi_statics.userStates;
        },

        get streamStates() {
            return koi_statics.streamStates;
        },

        get roomStates() {
            return koi_statics.roomStates;
        },

        upvoteChat(platform, messageId) {
            conn.send("KOI", {
                type: "UPVOTE",
                platform: platform,
                messageId: messageId
            });
        },

        deleteChat(platform, messageId) {
            conn.send("KOI", {
                type: "DELETE",
                platform: platform,
                messageId: messageId
            });
        },

        sendChat(platform, message, chatter = "CLIENT") {
            conn.send("KOI", {
                type: "MESSAGE",
                platform: platform,
                message: message,
                chatter: chatter
            });
        },

        getMaxLength(platform) {
            switch (platform) {
                case "CAFFEINE":
                    return 80;

                case "TWITCH":
                    return 500;

                case "TROVO":
                    return 300;

                case "GLIMESH":
                    return 255;

                case "BRIME":
                    return 300;

                default:
                    console.debug(platform);
                    return 100; // ?
            }
        }

        // test(event) {
        //     if (this.isAlive()) {
        //         this.ws.send(JSON.stringify({
        //             type: "TEST",
        //             eventType: event.toUpperCase()
        //         }));
        //     }
        // }
    };

    // The `Music` global.
    let music_data = {};

    const musicInstance = {
        ...new EventHandler(),

        get providers() {
            return music_data.providers;
        },

        get activePlayback() {
            return music_data.activePlayback;
        }
    };

    deepFreeze(widgetInstance);
    deepFreeze(koiInstance);
    deepFreeze(musicInstance);

    // Listen for events on the conn, fire them off, yeah you get the idea.
    conn.on("init", () => {
        if (!initHandler || initHandler({ conn, koiInstance, widgetInstance, musicInstance, Currencies, koi_statics, address, port, pluginId, widgetId, authorization, widgetMode, AppContext })) {
            widgetInstance.broadcast("init");
            koiInstance.broadcast("koi_statics", koi_statics);
            conn.send("READY", {});
        }
    });

    conn.on("update", () => {
        widgetInstance.broadcast("update");
    });

    conn.on("emission", ({ type, data }) => {
        widgetInstance.broadcast(type, data, false);
    });

    conn.on("koi_statics", (statics) => {
        deepFreeze(statics);
        koi_statics = statics;
        koiInstance.broadcast("koi_statics", koi_statics);
    });

    conn.on("koi", (event) => {
        koiInstance.broadcast(event.event_type, event);
    });

    conn.on("music", (music) => {
        deepFreeze(music);
        music_data = music;
        musicInstance.broadcast("music", music_data);
    });

    // We completely reset the widget everytime it loses connection.
    conn.on("close", () => {
        if (!disconnectHandler || disconnectHandler()) {
            setTimeout(() => {
                location.reload();
            }, 2500);
        }
    });

    // Connect.
    conn.connect();

    // Debug
    Object.defineProperty(window, "Koi", {
        value: koiInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Widget", {
        value: widgetInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Music", {
        value: musicInstance,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "escapeHtml", {
        value: escapeHtml,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "Currencies", {
        value: Currencies,
        writable: false,
        configurable: true
    });
    Object.defineProperty(window, "AppContext", {
        value: AppContext,
        writable: false,
        configurable: true
    });
}