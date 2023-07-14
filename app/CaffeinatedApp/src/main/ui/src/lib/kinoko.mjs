import EventHandler from './event-handler.mjs';

function KinokoV1() {
    const eventHandler = new EventHandler();

    let ws;
    let proxyMode = false;

    const kinoko = {
        on: eventHandler.on,
        once: eventHandler.once,
        off: eventHandler.off,

        isProxyMode() {
            return proxyMode;
        },

        isOpen() {
            return ws && ws.readyState == WebSocket.OPEN;
        },

        send(message, isJson = true) {
            if (this.isOpen()) {
                if (proxyMode) {
                    ws.send(message);
                } else {
                    if (isJson) {
                        ws.send(JSON.stringify(message));
                    } else {
                        ws.send(message);
                    }
                }
            }
        },

        connect(channel, type = 'client', proxy = false) {
            const uri = `wss://api.casterlabs.co/v1/kinoko?channel=${encodeURIComponent(
				channel
			)}&type=${encodeURIComponent(type)}&proxy=${encodeURIComponent(proxy)}`;

            this.disconnect();

            ws = new WebSocket(uri);
            proxyMode = proxy;

            ws.onerror = () => {
                setTimeout(() => {
                    this.connect(channel, type, proxy);
                }, 1000);
            };

            ws.onopen = () => {
                eventHandler.emit('open');
            };

            ws.onclose = () => {
                eventHandler.emit('close');
            };

            ws.onmessage = (message) => {
                const data = message.data;

                switch (data) {
                    case ':ping':
                        {
                            if (!proxyMode) {
                                ws.send(':ping');
                                return;
                            }
                            break;
                        }

                    case ':orphaned':
                        {
                            eventHandler.emit('orphaned');
                            return;
                        }

                    case ':adopted':
                        {
                            eventHandler.emit('adopted');
                            return;
                        }

                    default:
                        {
                            if (proxyMode) {
                                eventHandler.emit('message', { message: data });
                            } else {
                                try {
                                    eventHandler.emit('message', { message: JSON.parse(data) });
                                } catch (ignored) {
                                    eventHandler.emit('message', { message: data });
                                }
                            }
                            return;
                        }
                }
            };
        },

        disconnect() {
            if (this.isOpen()) {
                ws.close();
            }
        }
    };

    Object.freeze(kinoko);

    return kinoko;
}

function generateUnsafePassword(len = 32) {
    const chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz';

    return Array(len)
        .fill(chars)
        .map((x) => {
            return x[Math.floor(Math.random() * x.length)];
        })
        .join('');
}

class AuthCallback {
    constructor(type = 'unknown') {
        this.id = `auth_redirect:${generateUnsafePassword(128)}:${type}`;
        this.cancelled = false;
    }

    cancel() {
        this.cancelled = true;
        this.kinoko && this.kinoko.disconnect();
        this.kinoko = null;
    }

    awaitAuthMessage(timeout = -1) {
        return new Promise((resolve, reject) => {
            this.kinoko = new KinokoV1();

            let fufilled = false;
            const id =
                timeout > 0 ?
                setTimeout(() => {
                    if (!fufilled) {
                        fufilled = true;
                        this.cancel();
                        reject('TOKEN_TIMEOUT');
                    }
                }, timeout) :
                -1;

            this.kinoko.connect(this.id, 'parent');

            this.kinoko.on('close', () => {
                if (!fufilled && !this.cancelled) {
                    reject('CONNECTION_CLOSED');
                }

                clearTimeout(id);
            });

            this.kinoko.on('message', (data) => {
                const message = data.message;

                fufilled = true;

                this.kinoko.disconnect();
                this.kinoko = null;

                if (message === 'NONE') {
                    reject('NO_TOKEN_PROVIDED');
                } else if (message.startsWith('token:')) {
                    const token = message.substring(6);

                    resolve(token);
                } else {
                    reject('TOKEN_MESSAGE_INVALID');
                }
            });
        });
    }

    getStateString() {
        return this.id;
    }
}

export { KinokoV1, AuthCallback };