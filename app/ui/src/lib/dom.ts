export function eventListener(event: string, listener: (data: any) => void, target: EventTarget = window) {
	function identity(e: CustomEvent) {
		listener(e.detail);
	}
	return () => {
		//@ts-ignore
		target.addEventListener(event, identity);

		//@ts-ignore
		return () => target.removeEventListener(event, identity);
	};
}

export function fire(event: string, data?: any, target: EventTarget = window) {
	target.dispatchEvent(
		new CustomEvent(event, {
			detail: data
		})
	);
}
