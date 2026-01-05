export default class Debouncer {
	id: ReturnType<typeof setTimeout>;
	timeout: number;

	constructor(timeout = 500) {
		this.id = -1;
		this.timeout = timeout;
	}

	debounce(fun: () => void) {
		clearTimeout(this.id);
		this.id = setTimeout(fun, this.timeout);
	}
}
