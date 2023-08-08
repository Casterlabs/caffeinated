import { writable, get } from 'svelte/store';
import { language } from "$lib/app.mjs";
import createConsole from "./console-helper.mjs";

import BASE_TRANSLATION from "$lib/locale/en-US.json";
const BASE_TRANSLATION_LOCALE = "en-US";

export const currentLocale = writable(null); // "en-US", etc

const console = createConsole("translate");

// ----------------------------------------------------------------
// ----------------------------------------------------------------

// {placeholder}: some text passed to the translator and is replaced
// [placeholder]: grabs a translation key
// %placeholder%: ui components passed to you, example usage can be found in the chatbot
// All keys prefixed with `sr.` are for screen readers.
let currentLang;

import ___SUPPORTED_LANGUAGES from "$lib/locale/_index.json";
export const supportedLanguages = ___SUPPORTED_LANGUAGES;

language.subscribe(async (language) => {
    if (!language) {
        currentLang = null;
        return;
    }

    if (language == get(currentLocale)) return;

    try {

        const newLocale = (await import(`$lib/locale/${language}.json`)).default;
        currentLang = {
            ...BASE_TRANSLATION,
            ...newLocale
        };
        console.debug("Loaded lang:", currentLang);

        currentLocale.set(language);
    } catch (e) {
        console.error("An error occurred whilst loading lang, defaulting to en-US:\n\n", e);
        currentLang = BASE_TRANSLATION;
        currentLocale.set(BASE_TRANSLATION_LOCALE);
    }
})

// ----------------------------------------------------------------
// ----------------------------------------------------------------

export function t(key, opts = {}) {
    return translate(key, opts, true);
}

export function translate(key, opts = {}, simpleResponse = true) {
    if (!key) {
        key = "";
    }

    if (!currentLang) {
        if (simpleResponse) {
            return key;
        } else {
            return {
                result: key,
                usedFallback: true
            };
        }
    };
    let result = currentLang[key] || key;
    let usedFallback = false;

    if (result == key) {
        if (simpleResponse) {
            console.error("Missing translation for key:", key);
        }
        usedFallback = true;
    }

    if (Array.isArray(result)) {
        const arr = result;
        result = arr[Math.floor(Math.random() * arr.length)];
    }

    // Replace placeholders
    (result.match(/{\w+}/g) || []).forEach((match) => {
        const item = match.slice(1, -1);

        if (opts[item] != undefined) {
            result = result.replace(match, opts[item]);
        } else {
            console.warn("Could not find missing option for", item, "in", opts, "for", key);
        }
    });

    // Replace localized placeholders
    (result.match(/\[[\w\.]+\]/g) || []).forEach((match) => {
        const item = match.slice(1, -1);
        const tItem = translate(item, {}, true);

        result = result.replace(match, tItem);
    });

    if (result == key && simpleResponse) {
        console.warn("Missing translation key:", key);
    }

    if (simpleResponse) {
        return result;
    } else {
        return {
            result: result,
            usedFallback: usedFallback
        };
    }
}