export async function getCurrencies() {
	const response = await fetch('https://api.casterlabs.co/v3/currencies');

	const json = await response.json();

	return json.data;
}

export async function formatCurrency(amount, currency) {
	const response = await fetch(
		`https://api.casterlabs.co/v3/currencies/format?currency=${encodeURIComponent(
			currency
		)}&amount=${encodeURIComponent(amount)}`
	);

	return await response.text();
}

export async function convertCurrency(amount, from, to, formatResult = false) {
	const response = await fetch(
		`https://api.casterlabs.co/v3/currencies/convert?from=${encodeURIComponent(
			from
		)}&to=${encodeURIComponent(to)}&amount=${encodeURIComponent(
			amount
		)}&formatResult=${encodeURIComponent(formatResult)}`
	);

	return await response.text();
}
