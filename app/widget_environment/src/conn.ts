import EventHandler from "./eventHandler";

export default class Conn extends EventHandler {
    address: string;
    connectionId: string | null;
    ws: WebSocket;

    /**
     * Messages that were sent before the WebSocket was open are buffered here so
     * that they are not silently dropped. They are flushed once the socket
     * opens (see #connect). This is the client-side counterpart to the server's
     * `pendingEmissions` buffer in RealtimeWidgetListener: without it, any
     * emission fired before `onopen` — most notably the single, unretried
     * `init` emission that drives the dock's `init -> ready` handshake — is
     * lost, leaving the dock as a black window until a refresh re-runs the race.
     */
    private pendingSends: { type: string; payload: any }[] = [];

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
        } else {
            this.pendingSends.push({ type, payload });
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
                this.flushPending();
                this.broadcast("open");
            };

            this.ws.onclose = (e) => {
                console.debug("[WidgetEnvironment/Conn]", "WS close:", e.code, e.reason);
                this.pendingSends = [];
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
                            this.broadcast("update", payload.data);
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
            this.pendingSends = [];
            this.broadcast("close");
        }
    }

    /**
     * Drains any messages that were queued before the socket opened, delivering
     * them in arrival order. Called from `ws.onopen`; also safe to call from
     * `onmessage`/`onerror` paths if a buffered send was pending when the
     * socket became usable.
     */
    private flushPending() {
        if (!this.ws || this.pendingSends.length === 0) {
            return;
        }

        const pending = this.pendingSends;
        this.pendingSends = [];

        for (const { type, payload } of pending) {
            try {
                this.ws.send(
                    JSON.stringify({
                        type: type,
                        data: payload
                    })
                );
            } catch (e) {
                console.debug("[WidgetEnvironment/Conn]", "Failed to flush a pending send:", e);
            }
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