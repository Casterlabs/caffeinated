import Conn from "../conn";
import EventHandler from "../eventHandler";

const events = new EventHandler();
let statics: any = {};
let conn: Conn;

const GLOBAL = {
    get eventHistory() {
        return statics.history;
    },

    get viewers() {
        return statics.viewers;
    },

    get viewerCounts() {
        return statics.viewerCounts;
    },

    get userStates() {
        return statics.userStates;
    },

    get streamStates() {
        return statics.streamStates;
    },

    get roomStates() {
        return statics.roomStates;
    },

    get features() {
        return statics.features;
    },

    upvoteChat(platform: string, messageId: any) {
        conn.send("KOI", {
            type: "UPVOTE",
            platform: platform,
            messageId: messageId
        });
    },

    deleteChat(platform: string, messageId: any, isUserGesture = true) {
        conn.send("KOI", {
            type: "DELETE",
            platform: platform,
            messageId: messageId,
            isUserGesture: isUserGesture
        });
    },

    sendChat(platform: string, message: any, chatter = "CLIENT", replyTarget = null, isUserGesture = true) {
        conn.send("KOI", {
            type: "MESSAGE",
            platform: platform,
            message: message,
            chatter: chatter,
            replyTarget: replyTarget,
            isUserGesture: isUserGesture
        });
    },

    // test(event) {
    //     if (this.isAlive()) {
    //         this.ws.send(JSON.stringify({
    //             type: "TEST",
    //             eventType: event.toUpperCase()
    //         }));
    //     }
    // }
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

    conn.on("koi_statics", (s: any) => {
        statics = s;
        events.broadcast("koi_statics", statics);
    });
    conn.on("koi", (event: any) => {
        events.broadcast(event.event_type, event);
    });
}