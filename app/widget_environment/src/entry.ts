import Conn from "./conn";

import GlobalApp, { init as appInit } from "./globals/App";
import GlobalWidget, { init as widgetInit } from "./globals/Widget";
import GlobalKoi, { init as koiInit } from "./globals/Koi";
import GlobalMusic, { init as musicInit } from "./globals/Music";
import GlobalCurrencies from "./globals/Currencies";

const query = new URLSearchParams(location.search);

const pluginId = query.get("pluginId");
const widgetId = query.get("widgetId");
const authorization = query.get("authorization");
const port = query.get("port") || "8092";
const address = query.get("validAddress") || "localhost";
const widgetMode = (query.get("mode") || "WIDGET").toUpperCase();

const conn = new Conn(
  `ws://${address}:${port}/api/plugin/${pluginId}/widget/${widgetId}/realtime?authorization=${authorization}&mode=${widgetMode}`
);

// The globals.
function GlobalEscapeHtml(unsafe: string) {
  return unsafe
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

const GlobalOpenLink = (link: string) => {
  conn.send("OPEN_LINK", { link });
};

const globals = {
  Koi: GlobalKoi,
  Widget: GlobalWidget,
  Music: GlobalMusic,
  Currencies: GlobalCurrencies,
  App: GlobalApp,
  escapeHtml: GlobalEscapeHtml,
  openLink: GlobalOpenLink,
};
const inits = [appInit, widgetInit, koiInit, musicInit];

for (const [key, value] of Object.entries(globals)) {
  window[key] = value;
}

declare global {
  const Koi: typeof GlobalKoi;
  const Widget: typeof GlobalWidget;
  const Music: typeof GlobalMusic;
  const Currencies: typeof GlobalCurrencies;
  const App: typeof GlobalApp;
  function escapeHtml(unsafe: string): string;
  function openLink(link: string): void;
}

for (const init of inits) {
  init(conn);
}

// Navigate back to widgets.casterlabs.co when we disconnect. This effectively resets the widget.
conn.on("close", () => {
  setTimeout(() => {
    location.href = `https://widgets.casterlabs.co/caffeinated/loader.html${location.search}`;
  }, 2500);
});

conn.on("init", () => {
  setTimeout(() => conn.send("READY", {}), 100);
});

conn.connect();
