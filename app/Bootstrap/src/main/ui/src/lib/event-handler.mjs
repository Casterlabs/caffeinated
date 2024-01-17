function EventHandler(console = window.console) {
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

            callbacks[callbackId] = function(data) {
                delete listeners[type][callbackId];
                callback(data);
            };

            listeners[type] = callbacks;

            return callbackId;
        },

        off(type, callbackId) {
            delete listeners[type][callbackId];
        },

        emit(type, data) {
            // Broadcast under a wildcard.
            {
                const wildCardCallbacks = listeners['*'];

                if (wildCardCallbacks) {
                    Object.values(wildCardCallbacks).forEach((callback) => {
                        try {
                            callback(type.toLowerCase(), data);
                        } catch (e) {
                            console.error('[EventHandler]', 'A listener produced an exception: ');
                            console.error('[EventHandler]', e);
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
                            callback(data);
                        } catch (e) {
                            console.error('[EventHandler]', 'A listener produced an exception: ');
                            console.error('[EventHandler]', e);
                        }
                    });
                }
            }

            console.debug('[EventHandler]', type.toLowerCase(), data);
        }
    };
}

export default EventHandler;