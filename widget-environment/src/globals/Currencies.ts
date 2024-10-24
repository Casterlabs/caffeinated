async function getCurrencies() {
    const response = await fetch(
        `https://api.casterlabs.co/v3/currencies`
    );
    const json = await response.json();
    return json.data;
}

async function formatCurrency(amount: number, currency: string) {
    const response = await fetch(
        `https://api.casterlabs.co/v3/currencies/format?currency=${encodeURIComponent(currency)}&amount=${encodeURIComponent(amount)}`
    );
    return await response.text();
}

async function convertCurrency(amount: number, from: string, to: string, formatResult = false) {
    const response = await fetch(
        `https://api.casterlabs.co/v3/currencies/convert?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}&amount=${encodeURIComponent(amount)}&formatResult=${encodeURIComponent(formatResult)}`
    );
    return await response.text();
}

export default {
    getCurrencies,
    formatCurrency,
    convertCurrency
};