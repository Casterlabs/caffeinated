import { FALLBACK_LANGUAGE } from '$lib/translate.mjs';
import {get, writable } from 'svelte/store';

export const language = writable(FALLBACK_LANGUAGE);