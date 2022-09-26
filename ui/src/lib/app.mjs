import { FALLBACK_LANGUAGE } from '$lib/translate.mjs';
import {get, writable } from 'svelte/store';

export const language = writable(FALLBACK_LANGUAGE);
export const icon = writable('casterlabs');
export const iconColor = writable('white'); // "black", "white"