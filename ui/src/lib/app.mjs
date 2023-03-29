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

// TTS
export const SUPPORTED_TTS_VOICES = [];

async function loadTTSVoices() {
	try {
		const response = await (await fetch('https://api.casterlabs.co/v3/tts/voices')).json();
		for (const voice of response.data.voices) {
			SUPPORTED_TTS_VOICES.push(voice);
		}
		console.debug('[App]', 'Loaded TTS voices from api:', SUPPORTED_TTS_VOICES);
	} catch (ignored) {
		setTimeout(loadTTSVoices, 5000); // Retry if an error occurs.
	}
}

if (typeof fetch != 'undefined') loadTTSVoices();

// Misc
export let openLink = async (url) => url;
export function setOpenLink(fn) {
	openLink = fn;
}
