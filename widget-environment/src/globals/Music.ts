import Conn from "../conn";
import EventHandler from "../eventHandler";

const events = new EventHandler();
let music: any = {};

const GLOBAL = {
    get providers() {
        return music.providers;
    },

    get activePlayback() {
        return music.activePlayback;
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

export function init(conn: Conn) {
    conn.on("music", (m: any) => {
        music = m;
        events.broadcast("music", music);
    });
}