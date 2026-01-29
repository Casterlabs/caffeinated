export function animate(duration, callback) {
	const startedAt = performance.now();
	let isAnimating = true;

	function update(progress = (performance.now() - startedAt) / duration) {
		if (progress > 1) progress = 1;
		callback(progress);
		if (isAnimating) {
			requestAnimationFrame(() => update());
		}
	}
	update(0); // kickstart

	setTimeout(() => {
		isAnimating = false;
		update(1);
	}, duration);
}
