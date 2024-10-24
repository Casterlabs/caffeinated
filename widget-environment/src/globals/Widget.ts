import Conn from "../conn";
import EventHandler, { EventHandlerCallbackEither } from "../eventHandler";

const events = new EventHandler();
let conn: Conn;

let widgetData: any;

const GLOBAL = {
    on(type: string, handler: EventHandlerCallbackEither) {
        if (conn.connectionId && ["init", "update"].includes(type.toLowerCase())) {
            setTimeout(handler, 2); // Execute the handler after a couple of browser ticks.
        }
        events.on(type, handler);
    },

    get connectionId() {
        return conn.connectionId;
    },

    get widgetData() {
        return widgetData;
    },

    get mode() {
        return (new URLSearchParams(location.search).get("MODE") || "WIDGET").toUpperCase();
    },

    getSetting(key: string) {
        return widgetData.settings[key];
    },

    emit(type: string, data = {}) {
        if (type.startsWith("__internal:")) {
            throw "__internal is a reserved prefix.";
        }

        conn.emit(type, data);
    },

    getResource(resourceId: string) {
        return new Promise((resolve) => {
            this.once(`__internal:resource_poll:${resourceId}`, resolve);
            conn.emit("__internal:resource_poll", resourceId);
        });
    }
};
const eventsPrototype = Object.getPrototypeOf(events);
for (const key of Object.getOwnPropertyNames(eventsPrototype)) {
    if (GLOBAL[key]) continue;
    GLOBAL[key] = function () {
        return eventsPrototype[key].call(events, ...arguments);
    };
}
export default GLOBAL;

export function init(c: Conn) {
    conn = c;

    conn.on("init", ({ widget }) => {
        widgetData = widget;
    });
    conn.on("update", ({ widget }) => {
        widgetData = widget;
    });
    conn.on("emission", ({ type, data }) => {
        events.broadcast(type, data);
    });
}
