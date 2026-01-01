import { storify } from '$lib/bridgeHelper';
import Glocale from '@glocale/typescript';
import { writable } from 'svelte/store';

export const glocale: Glocale = new Glocale();
export const rerenderKey = writable(0);

storify(AppLocale, 'current')
	.readable<any>()
	.subscribe(async (locale) => {
		if (!locale) return;

		const fallback = await AppLocale.fallback;

		glocale.use({
			...fallback,
			...locale
		});
		rerenderKey.update((n) => n + 1);
	});

export function lookup(key: string) {
	try {
		return glocale.lookup(key);
	} catch (e) {
		console.error(e);
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
		console.error(e);
		return key;
	}
}
