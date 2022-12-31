import { FALLBACK_LANGUAGE } from './translate.mjs';
import { writable } from 'svelte/store';

// Locale
export const language = writable(FALLBACK_LANGUAGE);

// Theme
export const baseColor = writable('gray'); // Any of the radix colors
export const primaryColor = writable('gray'); // Any of the radix colors
export const appearance = writable('DARK'); // "LIGHT", "DARK"

// Misc theme.
export const icon = writable('casterlabs'); // "casterlabs", "moonlabs", "pride", "skittles"
export const iconColor = writable('white'); // "black", "white"
appearance.subscribe((v) => iconColor.set(v == 'DARK' ? 'white' : 'black')); // We derive iconColor from the app's appearance.

// Emojis
export const emojiProvider = writable('system');
export let matchAndReturnEmojiHTML = async (text) => text;
export function setMatchAndReturnEmojiHTML(fn) {
	matchAndReturnEmojiHTML = fn;
}

emojiProvider.subscribe((provider) => {
	if (typeof document == 'undefined') return;

	let appStyleElement = document.querySelector('#app-emoji-style');
	if (!appStyleElement) {
		appStyleElement = document.createElement('style');
		appStyleElement.id = 'app-emoji-style';
		document.head.appendChild(appStyleElement);
	}

	if (provider == 'system') {
		appStyleElement.innerHTML = ``;
	} else {
		appStyleElement.innerHTML = `
            [data-rich-type="emoji"] > [data-emoji-provider="system"] {
                display: none;
            }
            
            [data-rich-type="emoji"] > [data-emoji-provider="${provider}"] {
                display: inline-block !important;
            }
        `;
	}
});
