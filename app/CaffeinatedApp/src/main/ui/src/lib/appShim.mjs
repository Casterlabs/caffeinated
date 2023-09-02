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
			return () => {};
		},
		set() {} // No-OP
	};
}

function setupCommon() {
	getCurrencies().then(currencies.set);
}

function setupApp() {
	// "Exposes" goto() to the Java side.
	Bridge.on('goto', ({ path }) => goto(path));

	Caffeinated.themeManager.svelte('baseColor').subscribe(App.baseColor.set);
	Caffeinated.themeManager.svelte('primaryColor').subscribe(App.primaryColor.set);
	Caffeinated.themeManager.svelte('effectiveAppearance').subscribe(App.appearance.set);
	Caffeinated.UI.svelte('preferences').subscribe((prefs) => {
		if (!prefs) return;
		const { language, icon, emojiProvider, zoom } = prefs;

		App.language.set(language);
		App.icon.set(icon);
		App.emojiProvider.set(emojiProvider);

		document.documentElement.style.fontSize = `${zoom * 100}%`;
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
	widgetApp.on('language', App.language.set);
}

export default function () {
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
