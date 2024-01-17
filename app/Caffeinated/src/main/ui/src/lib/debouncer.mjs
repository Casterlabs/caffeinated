export default class Debouncer {
    constructor(timeout = 500) {
        this.id = -1;
        this.timeout = timeout;
    }

    debounce(fun) {
        clearTimeout(this.id);
        this.id = setTimeout(fun, this.timeout);
    }
}