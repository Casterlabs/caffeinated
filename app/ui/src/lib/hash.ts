export function simpleHash(str: string) {
	if (str.length == 0) return '0';

	let hash = 0;
	for (let i = 0; i < str.length; i++) {
		const char = str.charCodeAt(i);
		hash = (hash << 5) - hash + char; // hash * 31 + char
		hash = hash & hash; // Convert to a 32-bit integer
	}
	return (hash >>> 0).toString(); // Convert to unsigned and return as a string
}

export function hashCode(obj: any) {
	const stableString = JSON.stringify(obj, Object.keys(obj).sort());
	return simpleHash(stableString);
}
