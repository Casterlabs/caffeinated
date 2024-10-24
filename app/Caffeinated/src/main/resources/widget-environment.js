// src/eventHandler.ts
var EventHandler = class {
  #listeners = {};
  #callbackIdCounter = 0;
  on(type, callback) {
    const callbackId = this.#callbackIdCounter++;
    type = type.toLowerCase();
    const callbacks = this.#listeners[type] || {};
    callbacks[callbackId] = callback;
    this.#listeners[type] = callbacks;
    return callbackId;
  }
  once(type, callback) {
    const callbackId = this.#callbackIdCounter++;
    type = type.toLowerCase();
    const callbacks = this.#listeners[type] || {};
    callbacks[callbackId] = (a, b) => {
      delete this.#listeners[type][callbackId];
      callback(a, b);
    };
    this.#listeners[type] = callbacks;
    return callbackId;
  }
  off(type, callbackId) {
    delete this.#listeners[type][callbackId];
  }
  broadcast(type, data = void 0) {
    const wildCardCallbacks = this.#listeners["*"];
    if (wildCardCallbacks) {
      Object.values(wildCardCallbacks).forEach((callback) => {
        try {
          callback(type.toLowerCase(), data);
        } catch (e) {
          console.error("A listener produced an exception: ");
          console.error(e);
        }
      });
    }
    const callbacks = this.#listeners[type.toLowerCase()];
    if (callbacks) {
      Object.values(callbacks).forEach((callback) => {
        try {
          callback(data);
        } catch (e) {
          console.error("A listener produced an exception: ");
          console.error(e);
        }
      });
    }
  }
};

// src/conn.ts
var Conn = class extends EventHandler {
  address;
  connectionId;
  ws;
  constructor(address2) {
    super();
    this.address = address2;
    this.connectionId = null;
  }
  send(type, payload) {
    if (this.isAlive()) {
      this.ws.send(
        JSON.stringify({
          type,
          data: payload
        })
      );
    }
  }
  emit(type, data) {
    this.send("EMISSION", {
      type,
      data
    });
  }
  connect() {
    this.close();
    try {
      this.ws = new WebSocket(this.address);
      this.ws.onerror = (e) => {
        console.debug("[WidgetEnvironment/Conn]", "WS error:", e);
        this.broadcast("close");
      };
      this.ws.onopen = () => {
        console.debug("[WidgetEnvironment/Conn]", "WS open.");
        this.broadcast("open");
      };
      this.ws.onclose = (e) => {
        console.debug("[WidgetEnvironment/Conn]", "WS close:", e.code, e.reason);
        this.broadcast("close");
      };
      this.ws.onmessage = async (raw) => {
        console.debug("[WidgetEnvironment/Conn]", "Received WS message:", raw);
        const payload = JSON.parse(raw.data);
        switch (payload.type) {
          case "ERROR": {
            this.broadcast("error", payload);
            return;
          }
          case "PING": {
            this.send("PONG", {});
            return;
          }
          case "INIT": {
            this.connectionId = payload.data.connectionId;
            this.broadcast("init", payload.data);
            return;
          }
          case "UPDATE": {
            this.broadcast("update");
            return;
          }
          case "EMISSION": {
            this.broadcast("emission", payload.data);
            return;
          }
          case "KOI_STATICS": {
            this.broadcast("koi_statics", payload.data);
            return;
          }
          case "KOI": {
            this.broadcast("koi", payload.data);
            return;
          }
          case "MUSIC": {
            this.broadcast("music", payload.data);
            return;
          }
          case "APP": {
            this.broadcast("app", payload.data);
            return;
          }
          case "LOCALIZE": {
            this.broadcast("localize", payload.data);
            return;
          }
        }
      };
    } catch (e) {
      console.debug("[WidgetEnvironment/Conn]", "WS error:", e);
      this.broadcast("close");
    }
  }
  isAlive() {
    return this.ws && this.ws.readyState == WebSocket.OPEN;
  }
  close() {
    if (this.isAlive()) {
      this.ws.close();
    }
  }
};

// src/globals/App.ts
var events = new EventHandler();
var store = {
  language: "en",
  emojiProvider: "system",
  theme: ["gray", "gray"],
  appearance: "DARK",
  zoom: 1
};
var localeProvider = async (k, knownPlaceholders, knownComponents) => "LOCALE_PRERENDER:" + k;
var GLOBAL = {
  get store() {
    return store;
  },
  on(key, listener) {
    const listenerId = events.on(key, listener);
    if (store[key]) {
      listener(store[key]);
    }
    return [key, listenerId];
  },
  off: events.off,
  get(key) {
    return store[key];
  },
  async localize(key, knownPlaceholders = {}, knownComponents = []) {
    return await localeProvider(key, knownPlaceholders, knownComponents);
  }
};
var App_default = GLOBAL;
var appStyleElement;
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
function init(conn4) {
  if (appStyleElement) return;
  appStyleElement = document.createElement("style");
  appStyleElement.id = "app-style";
  document.head.appendChild(appStyleElement);
  recomputeStyle();
  function mutate(key, value) {
    if (store[key] === value) return;
    store[key] = value;
    events.broadcast(key, value);
    if (["emojiProvider"].includes(key) && appStyleElement) {
      recomputeStyle();
    }
  }
  conn4.on("app", ({ language, emojiProvider, theme, appearance, zoom }) => {
    mutate("language", language);
    mutate("emojiProvider", emojiProvider);
    mutate("theme", theme);
    mutate("appearance", appearance);
    mutate("zoom", zoom);
  });
  let appLocalizationTasks = {};
  conn4.on("localize", ({ nonce, value }) => {
    appLocalizationTasks[nonce](value);
  });
  localeProvider = (key, knownPlaceholders, knownComponents) => {
    return new Promise((resolve) => {
      const nonce = Math.random().toString(28);
      appLocalizationTasks[nonce] = resolve;
      conn4.send("LOCALIZE", {
        nonce,
        key: key || "",
        knownPlaceholders: knownPlaceholders || {},
        knownComponents: knownComponents || []
      });
    });
  };
}

// src/globals/Widget.ts
var events2 = new EventHandler();
var conn;
var widgetData;
var GLOBAL2 = {
  on(type, handler) {
    if (conn.connectionId && ["init", "update"].includes(type.toLowerCase())) {
      setTimeout(handler, 2);
    }
    events2.on(type, handler);
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
  getSetting(key) {
    return widgetData.settings[key];
  },
  emit(type, data = {}) {
    if (type.startsWith("__internal:")) {
      throw "__internal is a reserved prefix.";
    }
    conn.emit(type, data);
  },
  getResource(resourceId) {
    return new Promise((resolve) => {
      this.once(`__internal:resource_poll:${resourceId}`, resolve);
      conn.emit("__internal:resource_poll", resourceId);
    });
  }
};
var eventsPrototype = Object.getPrototypeOf(events2);
for (const key of Object.getOwnPropertyNames(eventsPrototype)) {
  if (GLOBAL2[key]) continue;
  GLOBAL2[key] = function() {
    return eventsPrototype[key].call(events2, ...arguments);
  };
}
var Widget_default = GLOBAL2;
function init2(c) {
  conn = c;
  conn.on("init", ({ widget }) => {
    widgetData = widget;
  });
  conn.on("update", ({ widget }) => {
    widgetData = widget;
  });
  conn.on("emission", ({ type, data }) => {
    events2.broadcast(type, data);
  });
}

// src/globals/Koi.ts
var events3 = new EventHandler();
var statics = {};
var conn2;
var GLOBAL3 = {
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
  upvoteChat(platform, messageId) {
    conn2.send("KOI", {
      type: "UPVOTE",
      platform,
      messageId
    });
  },
  deleteChat(platform, messageId, isUserGesture = true) {
    conn2.send("KOI", {
      type: "DELETE",
      platform,
      messageId,
      isUserGesture
    });
  },
  sendChat(platform, message, chatter = "CLIENT", replyTarget = null, isUserGesture = true) {
    conn2.send("KOI", {
      type: "MESSAGE",
      platform,
      message,
      chatter,
      replyTarget,
      isUserGesture
    });
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
var eventsPrototype2 = Object.getPrototypeOf(events3);
for (const key of Object.getOwnPropertyNames(eventsPrototype2)) {
  if (GLOBAL3[key]) continue;
  GLOBAL3[key] = function() {
    return eventsPrototype2[key].call(events3, ...arguments);
  };
}
var Koi_default = GLOBAL3;
function init3(c) {
  conn2 = c;
  conn2.on("koi_statics", (s) => {
    statics = s;
    events3.broadcast("koi_statics", statics);
  });
  conn2.on("koi", (event) => {
    events3.broadcast(event.event_type, event);
  });
}

// src/globals/Music.ts
var events4 = new EventHandler();
var music = {};
var GLOBAL4 = {
  get providers() {
    return music.providers;
  },
  get activePlayback() {
    return music.activePlayback;
  }
};
var eventsPrototype3 = Object.getPrototypeOf(events4);
for (const key of Object.getOwnPropertyNames(eventsPrototype3)) {
  if (GLOBAL4[key]) continue;
  GLOBAL4[key] = function() {
    return eventsPrototype3[key].call(events4, ...arguments);
  };
}
var Music_default = GLOBAL4;
function init4(conn4) {
  conn4.on("music", (m) => {
    music = m;
    events4.broadcast("music", music);
  });
}

// src/globals/Currencies.ts
async function getCurrencies() {
  const response = await fetch(
    `https://api.casterlabs.co/v3/currencies`
  );
  const json = await response.json();
  return json.data;
}
async function formatCurrency(amount, currency) {
  const response = await fetch(
    `https://api.casterlabs.co/v3/currencies/format?currency=${encodeURIComponent(currency)}&amount=${encodeURIComponent(amount)}`
  );
  return await response.text();
}
async function convertCurrency(amount, from, to, formatResult = false) {
  const response = await fetch(
    `https://api.casterlabs.co/v3/currencies/convert?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&amount=${encodeURIComponent(amount)}&formatResult=${encodeURIComponent(formatResult)}`
  );
  return await response.text();
}
var Currencies_default = {
  getCurrencies,
  formatCurrency,
  convertCurrency
};

// src/entry.ts
var query = new URLSearchParams(location.search);
var pluginId = query.get("pluginId");
var widgetId = query.get("widgetId");
var authorization = query.get("authorization");
var port = query.get("port") || "8092";
var address = query.get("validAddress") || "localhost";
var widgetMode = (query.get("mode") || "WIDGET").toUpperCase();
var conn3 = new Conn(`ws://${address}:${port}/api/plugin/${pluginId}/widget/${widgetId}/realtime?authorization=${authorization}&mode=${widgetMode}`);
function escapeHtml(unsafe) {
  return unsafe.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}
var openLink = (link) => {
  conn3.send("OPEN_LINK", { link });
};
var globals = { Koi: Koi_default, Widget: Widget_default, Music: Music_default, Currencies: Currencies_default, App: App_default, escapeHtml, openLink };
var inits = [init, init2, init3, init4];
for (const [key, value] of Object.entries(globals)) {
  window[key] = value;
}
for (const init5 of inits) {
  init5(conn3);
}
conn3.on("close", () => {
  setTimeout(() => {
    history.back();
  }, 2500);
});
conn3.on("init", () => {
  conn3.send("READY", {});
});
conn3.connect();
