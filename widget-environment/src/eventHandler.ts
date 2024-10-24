
export declare type EventHandlerCallback = (data: any | null | undefined) => void;
export declare type EventHandlerCallbackWildcard = (type: string, data: any | null | undefined) => void;
export declare type EventHandlerCallbackEither = EventHandlerCallback | EventHandlerCallbackWildcard;

declare type CallbackMap = { [key: number]: EventHandlerCallbackEither };
declare type ListenersMap = { [key: string]: CallbackMap };

export default class EventHandler {
    #listeners: ListenersMap = {};
    #callbackIdCounter = 0;

    on(type: string, callback: EventHandlerCallbackEither) {
        const callbackId = this.#callbackIdCounter++;
        type = type.toLowerCase();

        const callbacks = this.#listeners[type] || {} as CallbackMap;
        callbacks[callbackId] = callback;
        this.#listeners[type] = callbacks;

        return callbackId;
    }

    once(type: string, callback: EventHandlerCallbackEither) {
        const callbackId = this.#callbackIdCounter++;
        type = type.toLowerCase();

        const callbacks = this.#listeners[type] || {} as CallbackMap;
        callbacks[callbackId] = (a: any, b: any) => {
            delete this.#listeners[type][callbackId];
            callback(a, b);
        };
        this.#listeners[type] = callbacks;

        return callbackId;
    }

    off(type: string, callbackId: number) {
        delete this.#listeners[type][callbackId];
    }

    broadcast(type: string, data: any | null | undefined = undefined) {
        // Broadcast under a wildcard.
        const wildCardCallbacks = this.#listeners["*"];
        if (wildCardCallbacks) {
            Object.values(wildCardCallbacks).forEach((callback) => {
                try {
                    (callback as EventHandlerCallbackWildcard)(type.toLowerCase(), data);
                } catch (e) {
                    console.error("A listener produced an exception: ");
                    console.error(e);
                }
            });
        }

        // Broadcast under type.
        const callbacks = this.#listeners[type.toLowerCase()];
        if (callbacks) {
            Object.values(callbacks).forEach((callback) => {
                try {
                    (callback as EventHandlerCallback)(data);
                } catch (e) {
                    console.error("A listener produced an exception: ");
                    console.error(e);
                }
            });
        }

        // console.debug(`[EventHandler]`, "Processed event:", type, data);
    }
}
