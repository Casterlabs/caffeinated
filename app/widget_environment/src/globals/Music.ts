import Conn from '../conn';
import EventHandler from '../eventHandler';

const events = new EventHandler();
let music: any = {};

const GLOBAL = {
	get providers() {
		return music.providers;
	},

	get activePlayback() {
		return music.activePlayback;
	}
};
const eventsPrototype = Object.getPrototypeOf(events);
const GLOBAL_DYNAMIC = GLOBAL as Record<string, any>;
for (const key of Object.getOwnPropertyNames(eventsPrototype)) {
	if (GLOBAL_DYNAMIC[key]) continue;
	GLOBAL_DYNAMIC[key] = function () {
		return eventsPrototype[key].call(events, ...arguments);
	};
}
export default GLOBAL;

export function init(conn: Conn) {
	conn.on('music', (m: any) => {
		music = m;
		events.broadcast('music', music);
	});
}
