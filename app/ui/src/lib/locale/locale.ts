import Glocale from '@glocale/typescript';
import { type Writable, writable } from 'svelte/store';

export const glocale: Glocale = new Glocale();
export const rerenderKey = writable(0);

const renderStoreCache: Record<string, Writable<string>> = {};

export function lookup(key: string) {
	try {
		return glocale.lookup(key);
	} catch (e) {
		console.warn(e);
		return [
			{
				type: 'RAW',
				content: key,
				render(gl: Glocale, args: Record<string, string>) {
					return key;
				}
			}
		];
	}
}

/**
 * A utility method for localizing a key that requires no arguments.
 * @returns A store that mutates when the underlying locale does.
 */
export function renderStore(key: string) {
	let store = renderStoreCache[key];
	if (!store) {
		store = writable<string>('');
		rerenderKey.subscribe(() => store.set(render(key)));
		renderStoreCache[key] = store;
	}
	return store;
}

export function render(key: string, args?: Record<string, string>) {
	try {
		return glocale.render(key, args);
	} catch (e) {
		console.warn(e);
		return key;
	}
}
