// This file shims the $lib/app runtime data and the Java-IPC/app data.

import { goto } from '$app/navigation';
import { fonts, currencies } from '$lib/misc.mjs';
import { getCurrencies } from '$lib/currencies.mjs';
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
	Bridge.on('goto', ({ path }) => goto(path));

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

	Caffeinated.themeManager.__stores.svelte('baseColor').subscribe(App.baseColor.set);
	Caffeinated.themeManager.__stores.svelte('primaryColor').subscribe(App.primaryColor.set);
	Caffeinated.themeManager.__stores.svelte('effectiveAppearance').subscribe(App.appearance.set);
	Caffeinated.UI.__stores.svelte('preferences').subscribe((prefs) => {
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
