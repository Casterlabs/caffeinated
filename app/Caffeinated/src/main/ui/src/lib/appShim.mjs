// This file shims the $lib/app runtime data and the Java-IPC/app data.

import { goto } from '$app/navigation';
import { fonts, currencies } from '$lib/misc.mjs';
import { getCurrencies } from '$lib/currencies.mjs';
import { writable } from 'svelte/store';
import createConsole from '$lib/console-helper.mjs';
import * as App from '$lib/app.mjs';

const console = createConsole('appShim');

// Little helper to allow us to access the
// stores but prevent SSR from erroring out.
if (typeof global == 'undefined') {
	window.st = false;
} else {
	global.st = {
		subscribe(callback) {
			callback(null);
			return () => { };
		},
		set() { } // No-OP
	};
}

function setupCommon() {
	getCurrencies().then(currencies.set);
}

function setupApp() {
	// "Exposes" goto() to the Java side.
	window.saucer.messages.onMessage(([type, data]) => {
		if (type == "goto") {
			goto(data.path);
		}
	});

	const writableCache = {};
	window.svelte = (object, field) => {
		if (writableCache[object] && writableCache[object][field]) {
			return writableCache[object][field];
		}

		let root = window;
		try {
			const store = writable(null);

			for (const part of object.split(".")) {
				root = root[part];
			}

			root.onMutate(field, store.set);
			root[field].then(store.set);

			if (!writableCache[object]) {
				writableCache[object] = {};
			}
			writableCache[object][field] = store;

			return store;
		} catch (e) {
			console.debug(e, root, object, field);
			throw "Probably could not find a root, you supplied: " + object;
		}
	}

	App.localeProvider.set(Caffeinated.localize);
	Caffeinated.LOCALES.then(App.locales.set);

	window.addEventListener("message", async (e) => {
		console.debug(e);

		switch (e.origin) {
			case "https://auth.casterlabs.co": {
				// See also: caffeinatedAuth.mjs
				switch (e.data.type) {
					case 'TOKEN': {
						const { shouldGoBack } = JSON.parse(e.data.state);
						await Caffeinated.auth.loginPortal('KICK', e.data.token, shouldGoBack);

						// Fall through and close all portals.
					}

					case 'CLOSE': {
						document
							.querySelectorAll("#koi-auth-portal")
							.forEach((e) => e.remove());
						return;
					}
				}
				return;
			}
		}
	});

	svelte("Caffeinated", 'statusStates').subscribe(App.statusStates.set);
	svelte("Caffeinated.themeManager", 'baseColor').subscribe(App.baseColor.set);
	svelte("Caffeinated.themeManager", 'primaryColor').subscribe(App.primaryColor.set);
	svelte("Caffeinated.themeManager", 'effectiveAppearance').subscribe(App.appearance.set);
	svelte("Caffeinated.UI", 'preferences').subscribe((prefs) => {
		if (!prefs) return;
		const { language, icon, emojiProvider, zoom, uiFont } = prefs;

		App.currentLocale.set(language);
		App.icon.set(icon);
		App.emojiProvider.set(emojiProvider);

		document.documentElement.style.fontSize = `${zoom * 100}%`;
		document.documentElement.style.fontFamily = uiFont;
	});

	Caffeinated.UI.fonts.then(fonts.set);
	App.setOpenLink(Caffeinated.UI.openLink);
	App.setMatchAndReturnEmojiHTML((text) => Caffeinated.emojis.matchAndReturnHTML(text, false));
}

function setupDock() {
	const widgetApp = window.App;

	widgetApp.on('theme', ([baseColor, primaryColor]) => {
		App.baseColor.set(baseColor);
		App.primaryColor.set(primaryColor);
	});

	widgetApp.on('emojiProvider', App.emojiProvider.set);
	widgetApp.on('appearance', App.appearance.set);
	widgetApp.on('language', App.currentLocale.set);
	widgetApp.on('zoom', (zoom) => document.documentElement.style.fontSize = `${zoom * 100}%`);

	App.localeProvider.set(widgetApp.localize);
	App.setOpenLink(window.openLink);


	async function pollStatusApi() {
		try {
			const response = await fetch('https://api.status.casterlabs.co') //
				.then((response) => response.json());

			const stateData = [];
			for (const only of ['Caffeinated']) {
				const state = response.data.stateData[only];
				if (!state || state.status == "OPERATIONAL") continue;
				stateData.push(state);
			}
			App.statusStates.set(stateData);
		} catch (e) {
			console.warn('Error whilst polling status API. Retrying later.', e);
		}
		setTimeout(pollStatusApi, 120 * 1000);
	}
	pollStatusApi();
}

export function init() {
	if (typeof global != 'undefined') {
		// We're in SSR, abort.
		console.debug("Not shimming app (we're in SSR).");
		return;
	}

	console.debug('Setting up common shim.');
	setupCommon();

	if (typeof window.Caffeinated == 'object') {
		console.debug('Shimming with Java-IPC.');
		setupApp();
	} else {
		console.debug('Shimming with Widget-SDK.');
		setupDock();
	}
}

export function awaitPageLoad() {
	if (typeof global != 'undefined') {
		return Promise.resolve(); // We're in SSR.
	}

	if (typeof window.Widget == 'object') {
		return new Promise((resolve) => Widget.on("init", resolve));
	} else {
		return Promise.resolve();
	}
}
