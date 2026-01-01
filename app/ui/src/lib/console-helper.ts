export default function createConsole(name: string) {
	const consoleInstance = {
		log(...args: any[]) {
			console.log(...[`[${name}]`, ...args]);
		},

		warn(...args: any[]) {
			console.warn(...[`[${name}]`, ...args]);
		},

		error(...args: any[]) {
			console.error(...[`[${name}]`, ...args]);
		},

		info(...args: any[]) {
			console.info(...[`[${name}]`, ...args]);
		},

		debug(...args: any[]) {
			console.debug(...[`[${name}]`, ...args]);
		}
	};

	Object.freeze(consoleInstance);

	return consoleInstance;
}
