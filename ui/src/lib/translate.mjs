export const FALLBACK_LANGUAGE = "en";

// Languages
import en from "./lang/en.mjs";

import { language } from "./app.mjs";
import {get } from "svelte/store";

// prettier-ignore
const languages = {
    "en": en
};

// ----------------------------------------------------------------
// ----------------------------------------------------------------

let supportedLanguages = [];
for (const lang of Object.values(languages)) {
    supportedLanguages.push({
        name: lang["meta.name"],
        code: lang["meta.code"],
        flag: lang["meta.flag"],
        direction: lang["meta.direction"]
    });
}
export { supportedLanguages };

// ----------------------------------------------------------------
// ----------------------------------------------------------------

import createConsole from "./console-helper.mjs";
const console = createConsole("translate");

// ----------------------------------------------------------------
// ----------------------------------------------------------------

// {placeholder}: text placeholder
// [placeholder]: external localization
// %placeholder%: ui component

export function t(key, opts = {}) {
    return translate(get(language), key, opts, true);
}

export function translate(locale, key, opts = {}, simpleResponse = true) {
    if (!locale) return {};

    let result = key;
    let usedFallback = false;

    if (languages[locale] && languages[locale][key]) {
        result = languages[locale][key];
    }

    const [languageCode] = locale.split("-");
    if (result == key && languages[languageCode] && languages[languageCode][key]) {
        result = languages[languageCode][key];
    }

    if (result == key) {
        result = languages[FALLBACK_LANGUAGE][key] || key;

        if (result != key) {
            if (simpleResponse) {
                console.error(`Missing translation for key: ${key} in locale ${locale}, defaulting to English.`);
            }
            usedFallback = true;
        }
    }

    if (result) {
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
            const tItem = translate(locale, item, {}, true);

            result = result.replace(match, tItem);
        });
    } else {
        result = key;
    }

    if (result == key && simpleResponse) {
        console.warn("Missing translation key:", key, `(${locale})`);
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