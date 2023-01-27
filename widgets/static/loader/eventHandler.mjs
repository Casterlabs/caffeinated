
export default function EventHandler() {
    let listeners = {};
    let callbackIdCounter = 0;

    return {
        on(type, callback) {
            const callbackId = callbackIdCounter++;

            type = type.toLowerCase();

            let callbacks = listeners[type] || {};

            callbacks[callbackId] = callback;

            listeners[type] = callbacks;

            return callbackId;
        },

        once(type, callback) {
            const callbackId = callbackIdCounter++;

            type = type.toLowerCase();

            let callbacks = listeners[type] || {};

            callbacks[callbackId] = function (data) {
                delete listeners[type][callbackId];
                callback(data);
            };

            listeners[type] = callbacks;

            return callbackId;
        },

        off(type, callbackId) {
            delete listeners[type][callbackId];
        },

        broadcast(type, data, clone = true) {
            // Broadcast under a wildcard.
            {
                const wildCardCallbacks = listeners["*"];

                if (wildCardCallbacks) {
                    Object.values(wildCardCallbacks).forEach((callback) => {
                        try {
                            if (clone) {
                                callback(type.toLowerCase(), Object.assign({}, data));
                            } else {
                                callback(type.toLowerCase(), data);
                            }
                        } catch (e) {
                            console.error("A listener produced an exception: ");
                            console.error(e);
                        }
                    });
                }
            }

            // Broadcast under type.
            {
                const callbacks = listeners[type.toLowerCase()];

                if (callbacks) {
                    Object.values(callbacks).forEach((callback) => {
                        try {
                            if (clone) {
                                callback(Object.assign({}, data));
                            } else {
                                callback(data);
                            }
                        } catch (e) {
                            console.error("A listener produced an exception: ");
                            console.error(e);
                        }
                    });
                }
            }

            // console.debug(`[EventHandler]`, "Processed event:", type, data);
        }
    };
}
