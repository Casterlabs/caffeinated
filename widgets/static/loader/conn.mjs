import EventHandler from "./eventHandler.mjs";

export default class Conn {
    constructor(address) {
        this.address = address;

        this.connectionId = null;
        this.widgetData = null;

        for (const [key, value] of Object.entries(new EventHandler())) {
            this[key] = value;
        }
    }

    send(type, payload) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: type,
                    data: payload
                })
            );
        }
    }

    emit(type, data) {
        this.send("EMISSION", {
            type: type,
            data: data
        });
    }

    connect() {
        this.close();

        try {
            this.ws = new WebSocket(this.address);

            this.ws.onerror = () => {
                this.broadcast("close");
            };

            this.ws.onopen = () => {
                this.broadcast("open");
            };

            this.ws.onclose = () => {
                this.broadcast("close");
            };

            this.ws.onmessage = async(raw) => {
                const payload = JSON.parse(raw.data);

                switch (payload.type) {
                    case "ERROR":
                        {
                            this.broadcast("error", payload);
                            return;
                        }

                    case "PING":
                        {
                            this.send("PONG", {});
                            return;
                        }

                    case "INIT":
                        {
                            this.connectionId = payload.data.connectionId;
                            this.widgetData = payload.data.widget;
                            this.broadcast("init", payload.data);
                            return;
                        }

                    case "UPDATE":
                        {
                            this.widgetData = payload.data.widget;
                            this.broadcast("update");
                            return;
                        }

                    case "EMISSION":
                        {
                            this.broadcast("emission", payload.data);
                            return;
                        }

                    case "KOI_STATICS":
                        {
                            this.broadcast("koi_statics", payload.data);
                            return;
                        }

                    case "KOI":
                        {
                            this.broadcast("koi", payload.data);
                            return;
                        }

                    case "MUSIC":
                        {
                            this.broadcast("music", payload.data);
                            return;
                        }

                    case "APPEARANCE":
                        {
                            this.broadcast("appearance", payload.data);
                            return;
                        }
                }
            };
        } catch (e) {
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
}