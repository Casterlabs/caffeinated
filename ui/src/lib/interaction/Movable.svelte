<script>
	import { createEventDispatcher, onMount } from 'svelte';

	const dispatch = createEventDispatcher();

	let positionData = {
		// These are all in percentages
		x: 0.8,
		y: 0.02,
		width: 0.15,
		height: 0.65
	};

	export function getPositionData() {
		return positionData;
	}

	export function setPositionData(x, y, width, height) {
		positionData = {
			x: x || 0.8,
			y: y || 0.02,
			width: width || 0.15,
			height: height || 0.65
		};
	}

	let resizers = {};
	let contentContainer = null;
	let parentElement = null;

	let resizingLocation = null;
	let resizing = false;
	let movable = false;

	let lastMouseX = 0;
	let lastMouseY = 0;
	let mouseX = 0;
	let mouseY = 0;

	let debounceId = 0;

	function onResizeStart(event) {
		event.preventDefault();
		event.stopPropagation();

		resizing = true;

		if (event.touches) {
			lastMouseX = event.touches[0].clientX;
			lastMouseY = event.touches[0].clientY;
		} else {
			lastMouseX = event.clientX;
			lastMouseY = event.clientY;
		}
	}

	function onMoveStart(event) {
		event.preventDefault();
		event.stopPropagation();

		movable = true;

		if (event.touches) {
			lastMouseX = event.touches[0].clientX;
			lastMouseY = event.touches[0].clientY;
		} else {
			lastMouseX = event.clientX;
			lastMouseY = event.clientY;
		}
	}

	function onMove(event) {
		// Not for us.
		if (!resizing && !movable) {
			return;
		}

		let clientX;
		let clientY;

		if (event.touches) {
			clientX = event.touches[0].clientX;
			clientY = event.touches[0].clientY;
		} else {
			clientX = event.clientX;
			clientY = event.clientY;
		}

		const parentWidth = parentElement.offsetWidth;
		const parentHeight = parentElement.offsetHeight;

		if (!mouseX) {
			mouseX = positionData.x * parentWidth;
			mouseY = positionData.y * parentHeight;
		}

		const deltaX = clientX - lastMouseX;
		const deltaY = clientY - lastMouseY;

		mouseX += deltaX;
		mouseY += deltaY;

		lastMouseX = clientX;
		lastMouseY = clientY;

		if (resizing) {
			switch (resizingLocation) {
				case 'se-corner': {
					positionData.height += deltaY / parentHeight;
					positionData.width += deltaX / parentWidth;
					break;
				}

				case 's-edge': {
					positionData.height += deltaY / parentHeight;
					break;
				}

				case 'e-edge': {
					positionData.width += deltaX / parentWidth;
					break;
				}

				case 'n-edge': {
					positionData.y += deltaY / parentHeight;
					positionData.height -= deltaY / parentHeight;
					break;
				}

				case 'w-edge': {
					positionData.x += deltaX / parentWidth;
					positionData.width -= deltaX / parentWidth;
					break;
				}

				case 'sw-corner': {
					positionData.x += deltaX / parentWidth;
					positionData.height += deltaY / parentHeight;
					positionData.width -= deltaX / parentWidth;
					break;
				}

				case 'ne-corner': {
					positionData.y += deltaY / parentHeight;
					positionData.height -= deltaY / parentHeight;
					positionData.width += deltaX / parentWidth;
					break;
				}

				case 'nw-corner': {
					positionData.x += deltaX / parentWidth;
					positionData.y += deltaY / parentHeight;
					positionData.height -= deltaY / parentHeight;
					positionData.width -= deltaX / parentWidth;
					break;
				}
			}
		} else if (movable) {
			positionData.x = mouseX / parentWidth;
			positionData.y = mouseY / parentHeight;
		}

		// Debounce logic
		clearTimeout(debounceId);
		debounceId = setTimeout(() => {
			dispatch('update', positionData);
		}, 100);
	}

	function onEnd() {
		resizing = false;
		movable = false;

		mouseX = null;
		mouseY = null;
	}

	onMount(() => {
		parentElement = contentContainer.parentElement.parentElement;

		for (const [name, element] of Object.entries(resizers)) {
			function handler(event) {
				onResizeStart(event);
				resizingLocation = name;
			}

			element.addEventListener('mousedown', handler);
			element.addEventListener('touchstart', handler);
		}

		contentContainer.addEventListener('mousedown', onMoveStart);
		contentContainer.addEventListener('touchstart', onMoveStart);
		contentContainer.addEventListener('touchstart', console.log);

		document.addEventListener('mousemove', onMove);
		document.addEventListener('touchmove', onMove);

		document.addEventListener('mouseup', onEnd);
		document.addEventListener('touchend', onEnd);
	});
</script>

<div
	id="movable"
	class="relative p-0 m-0 flex flex-wrap justify-center bg-base-5 border border-base-8 rounded-md"
	style="
        left: {positionData.x * 100}%; 
        top: {positionData.y * 100}%; 
        width: {positionData.width * 100}%; 
        height: {positionData.height * 100}%;
    "
>
	<div id="nw-corner" bind:this={resizers['nw-corner']} />
	<div id="n-edge" bind:this={resizers['n-edge']} />
	<div id="ne-corner" bind:this={resizers['ne-corner']} />

	<div id="w-edge" bind:this={resizers['w-edge']} />
	<div id="content" class="relative overflow-auto" bind:this={contentContainer}><slot /></div>
	<div id="e-edge" bind:this={resizers['e-edge']} />

	<div id="sw-corner" bind:this={resizers['sw-corner']} />
	<div id="s-edge" bind:this={resizers['s-edge']} />
	<div id="se-corner" bind:this={resizers['se-corner']} />
</div>

<style>
	/* Size and Positioning */
	#movable {
		--edge-size: 8px;
		pointer-events: all;
	}

	#nw-corner,
	#ne-corner,
	#sw-corner,
	#se-corner {
		width: var(--edge-size);
		height: var(--edge-size);
		/* background-color: red; */
	}

	#n-edge,
	#s-edge {
		width: calc(100% - (var(--edge-size) * 2));
		height: var(--edge-size);
		/* background-color: green; */
	}

	#w-edge,
	#e-edge {
		width: var(--edge-size);
		height: calc(100% - (var(--edge-size) * 2));
		/* background-color: green; */
	}

	#content {
		width: calc(100% - (var(--edge-size) * 2));
		height: calc(100% - (var(--edge-size) * 2));
		/* background-color: rebeccapurple; */
	}

	/* Cursor */
	#content {
		cursor: move;
	}

	#nw-corner {
		cursor: nw-resize;
	}

	#ne-corner {
		cursor: ne-resize;
	}

	#sw-corner {
		cursor: sw-resize;
	}

	#se-corner {
		cursor: se-resize;
	}

	#n-edge {
		cursor: n-resize;
	}

	#s-edge {
		cursor: s-resize;
	}

	#w-edge {
		cursor: w-resize;
	}

	#e-edge {
		cursor: e-resize;
	}
</style>
