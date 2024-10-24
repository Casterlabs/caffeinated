import EventHandler from "./eventHandler";

export default class Conn extends EventHandler {
    address: string;
    connectionId: string | null;
    ws: WebSocket;

    constructor(address: string) {
        super();
        this.address = address;
        this.connectionId = null;
    }

    send(type: string, payload: any) {
        if (this.isAlive()) {
            this.ws.send(
                JSON.stringify({
                    type: type,
                    data: payload
                })
            );
        }
    }

    emit(type: string, data: any) {
        this.send("EMISSION", {
            type: type,
            data: data
        });
    }

    connect() {
        this.close();

        try {
            this.ws = new WebSocket(this.address);

            this.ws.onerror = (e) => {
                console.debug("[WidgetEnvironment/Conn]", "WS error:", e)
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
                // console.debug("[WidgetEnvironment/Conn]", "Received WS message:", raw);
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
                            this.broadcast("init", payload.data);
                            return;
                        }

                    case "UPDATE":
                        {
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

                    case "APP":
                        {
                            this.broadcast("app", payload.data);
                            return;
                        }

                    case "LOCALIZE":
                        {
                            this.broadcast("localize", payload.data);
                            return;
                        }
                }
            };
        } catch (e) {
            console.debug("[WidgetEnvironment/Conn]", "WS error:", e)
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