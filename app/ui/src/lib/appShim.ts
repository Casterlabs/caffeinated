import { goto } from '$app/navigation';

const isInApp = typeof saucer !== 'undefined';

export function awaitPageLoad() {
	// if (isInApp) {
	return Promise.resolve();
	// } else {
	// 	return new Promise((resolve) => Widget.on('init', resolve));
	// }
}

if (isInApp) {
	// @ts-ignore
	saucer.messages.onMessage(([type, data]: [string, any]) => {
		switch (type) {
			case 'goto': // "Exposes" goto() to the Java side.
				goto(data.path);
				return;
		}
	});
}
