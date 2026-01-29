export declare type EventHandlerCallback = (data: any | null | undefined) => void;

export declare type EventHandlerCallbackWildcard = (type: string, data: any | null | undefined) => void;

export declare type EventHandlerCallbackEither = EventHandlerCallback | EventHandlerCallbackWildcard;

export declare type EventInterceptor = (data: any | null | undefined) => any | null | undefined;

declare type CallbackMap = { [key: number]: EventHandlerCallbackEither };
declare type ListenersMap = { [key: string]: CallbackMap };

declare type InterceptorsMap = { [key: string]: EventInterceptor };

declare type OffCallback = () => void;

export default class EventHandler {
	private callbackIdCounter = 0;
	private listeners: ListenersMap = {};
	private interceptors: InterceptorsMap = {};

	/**
	 * Allows you to intercept events before they're broadcast to the listeners.
	 */
	intercept(type: string, callback: EventInterceptor): void {
		type = type.toLowerCase();
		this.interceptors[type] = callback;
	}

	/**
	 * Listens for events until you remove it with `off`.
	 *
	 * @param type The event type you wish to receive events for. Use '*' and a EventHandlerCallbackWildcard to listen for all events.
	 * @param callback Either an EventHandlerCallback or an EventHandlerCallbackWildcard.
	 * @returns A function which you can call to remove the callback.
	 */
	on(type: string, callback: EventHandlerCallbackEither): OffCallback {
		const callbackId = this.callbackIdCounter++;
		type = type.toLowerCase();

		const callbacks = this.listeners[type] || ({} as CallbackMap);
		callbacks[callbackId] = callback;
		this.listeners[type] = callbacks;

		return () => {
			if (!this.listeners[type]) return;
			delete this.listeners[type][callbackId];
		};
	}

	/**
	 * Allows you to receive a single event or optionally remove the listener early.
	 * It's worth noting that the listener will automatically be removed for you after the first event is received.
	 *
	 * @param type The event type you wish to receive events for. Use '*' and a EventHandlerCallbackWildcard to listen for all events.
	 * @param callback Either an EventHandlerCallback or an EventHandlerCallbackWildcard.
	 * @returns A function which you can call to remove the callback.
	 */
	once(type: string, callback: EventHandlerCallbackEither): OffCallback {
		const off = this.on(type, (a: any, b: any) => {
			off();
			callback(a, b);
		});
		return off;
	}

	/**
	 * @param type The event type you wish to broadcast. Wildcard is already handled for you.
	 * @param data The data to pass along with the event.
	 */
	broadcast(type: string, data: any | null | undefined = undefined) {
		// NB: Uncomment for full debug.
		// console.debug(`[EventHandler]`, 'Processing event:', type, data);

		type = type.toLowerCase();
		data = this.interceptors[type] ? this.interceptors[type](data) : data;

		// Broadcast under a wildcard.
		const wildCardCallbacks = this.listeners['*'];
		if (wildCardCallbacks) {
			Object.values(wildCardCallbacks).forEach((callback) => {
				try {
					(callback as EventHandlerCallbackWildcard)(type, data);
				} catch (e) {
					console.error('A listener produced an exception:', e);
				}
			});
		}

		// Broadcast under type.
		const callbacks = this.listeners[type];
		if (callbacks) {
			Object.values(callbacks).forEach((callback) => {
				try {
					(callback as EventHandlerCallback)(data);
				} catch (e) {
					console.error('A listener produced an exception:', e);
				}
			});
		}
	}
}
