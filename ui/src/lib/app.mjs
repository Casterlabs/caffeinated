import { FALLBACK_LANGUAGE } from "./translate.mjs";
import { writable } from "svelte/store";

export const language = writable(FALLBACK_LANGUAGE);

export const baseColor = writable("gray"); // Any of the radix colors
export const primaryColor = writable("gray"); // Any of the radix colors
export const appearance = writable("DARK"); // "LIGHT", "DARK"

export const icon = writable("casterlabs"); // "casterlabs", "moonlabs", "pride", "skittles"
export const iconColor = writable("white"); // "black", "white"
appearance.subscribe((v) => iconColor.set(v == "DARK" ? "white" : "black")); // We derive iconColor from the app's appearance.