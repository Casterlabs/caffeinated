const CURRENCY_API_BASE = 'https://api.casterlabs.co/v3/currencies';

declare type CurrencyCode = string;

declare interface Currency {
	currencyName: string;
	locale: string;
	currencyCode: CurrencyCode;
}

declare interface Currencies {
	baseCurrency: CurrencyCode;
	/**
	 * Currencies that aren't real but can still be converted from/to. e.g Twitch Bits or TikTok's Coins.
	 */
	psuedoCurrencies: CurrencyCode[];
	currencies: Currency[];
}

export async function getCurrencies() {
	const response = await fetch(CURRENCY_API_BASE);
	const json = await response.json();
	return json.data as Currencies;
}

export async function formatCurrency(amount: number, currency: CurrencyCode) {
	const params = new URLSearchParams();
	params.append('amount', amount.toString());
	params.append('currency', currency);

	const response = await fetch(`${CURRENCY_API_BASE}/format?${params.toString()}`);
	return await response.text();
}

export async function convertCurrency(amount: number, from: CurrencyCode, to: CurrencyCode, formatResult = false) {
	const params = new URLSearchParams();
	params.append('amount', amount.toString());
	params.append('from', from);
	params.append('to', to);
	params.append('formatResult', formatResult ? 'true' : 'false');

	const response = await fetch(`${CURRENCY_API_BASE}/convert?${params.toString()}`);
	const text = await response.text();

	if (formatResult) {
		return text;
	} else {
		return parseFloat(text);
	}
}
