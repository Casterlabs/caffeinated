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

const { pluginId, widgetId, authorization } = queryParams;
const port = queryParams.port || "8092";
const address = queryParams.address || "localhost";
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

export function init(initHandler = null) {
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

        // upvote(messageId) {
        //     if (this.isAlive()) {
        //         this.ws.send(JSON.stringify({
        //             type: "UPVOTE",
        //             message_id: messageId
        //         }));
        //     }
        // },

        // sendMessage(message, event = CAFFEINATED.userdata, chatter = "CLIENT") {
        //     if (message.startsWith("/caffeinated")) {
        //         this.broadcast("x_caffeinated_command", { text: message });
        //     } else {
        //         if (!CAFFEINATED.puppetToken) {
        //             chatter = "CLIENT";
        //         }

        //         if (this.isAlive() && event) {
        //             if (event.streamer.platform !== "CAFFEINE") {
        //                 message = message.replace(/\n/gm, " ");
        //             }

        //             this.ws.send(JSON.stringify({
        //                 type: "CHAT",
        //                 message: message.substring(0, this.getMaxLength(event)),
        //                 chatter: chatter
        //             }));
        //         }
        //     }
        // },

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
        if (!initHandler) {
            widgetInstance.broadcast("init");
            koiInstance.broadcast("koi_statics", koi_statics);
            conn.send("READY", {});
        } else {
            initHandler({ conn, koiInstance, widgetInstance, musicInstance, Currencies, koi_statics, address, port, pluginId, widgetId, authorization, widgetMode });
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
        setTimeout(() => {
            location.reload();
        }, 2500);
    });

    // Connect.
    conn.connect();

    // Debug
    Object.defineProperty(window, "Koi", {
        value: koiInstance,
        writable: false,
        configurable: false
    });
    Object.defineProperty(window, "Widget", {
        value: widgetInstance,
        writable: false,
        configurable: false
    });
    Object.defineProperty(window, "Music", {
        value: musicInstance,
        writable: false,
        configurable: false
    });
    Object.defineProperty(window, "escapeHtml", {
        value: escapeHtml,
        writable: false,
        configurable: false
    });
    Object.defineProperty(window, "Currencies", {
        value: Currencies,
        writable: false,
        configurable: false
    });
}