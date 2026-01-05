import Glocale from '@glocale/typescript';
import { writable } from 'svelte/store';

export const glocale: Glocale = new Glocale();
export const rerenderKey = writable(0);

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
export function render(key: string, args?: Record<string, string>) {
	try {
		return glocale.render(key, args);
	} catch (e) {
		console.warn(e);
		return key;
	}
}
