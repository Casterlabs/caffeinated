const DEBOUNCE_TIMEOUT = 500;

export default class Debouncer {
    constructor() {
        this.id = -1;
    }

    debounce(fun) {
        clearTimeout(this.id);
        this.id = setTimeout(fun, DEBOUNCE_TIMEOUT);
    }
}