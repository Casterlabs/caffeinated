import Conn from "./conn";

import App, { init as appInit } from "./globals/App";
import Widget, { init as widgetInit } from "./globals/Widget";
import Koi, { init as koiInit } from "./globals/Koi";
import Music, { init as musicInit } from "./globals/Music";
import Currencies from "./globals/Currencies";

const query = new URLSearchParams(location.search);

const pluginId = query.get("pluginId");
const widgetId = query.get("widgetId");
const authorization = query.get("authorization");
const port = query.get("port") || "8092";
const address = query.get("validAddress") || "localhost";
const widgetMode = (query.get("mode") || "WIDGET").toUpperCase();

const conn = new Conn(`ws://${address}:${port}/api/plugin/${pluginId}/widget/${widgetId}/realtime?authorization=${authorization}&mode=${widgetMode}`);

// The globals.
function escapeHtml(unsafe: string) {
    return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

const openLink = (link: string) => {
    conn.send("OPEN_LINK", { link });
};

const globals = { Koi, Widget, Music, Currencies, App, escapeHtml, openLink };
const inits = [appInit, widgetInit, koiInit, musicInit];

for (const [key, value] of Object.entries(globals)) {
    window[key] = value;
}

for (const init of inits) {
    init(conn);
}

// Navigate back to widgets.casterlabs.co when we disconnect. This effectively resets the widget.
conn.on("close", () => {
    setTimeout(() => {
        history.back();
    }, 2500);
});

conn.on("init", () => {
    conn.send("READY", {});
});

conn.connect();