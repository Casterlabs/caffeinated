<svelte:options accessors />

<script>
	import Movable from '../interaction/Movable.svelte';

	import { createEventDispatcher, onMount } from 'svelte';
	const dispatch = createEventDispatcher();

	let platforms = {};
	let viewersList = [];

	let movable = null;

	export let visible = false;

	function onUpdate(positionData) {
		dispatch('update', positionData);
	}

	export function getPositionData() {
		return movable.getPositionData();
	}

	export function setPositionData(x, y, width, height) {
		movable.setPositionData(x, y, width, height);
	}

	export function onViewersList(e) {
		console.log(e);
		platforms[e.streamer.platform] = e.viewers;

		updateViewersList();
	}

	export function onAuthUpdate(signedInPlatforms) {
		for (const platform of Object.keys(platforms)) {
			if (!signedInPlatforms.includes(platform)) {
				delete platforms[platform];
			}
		}

		updateViewersList();
	}

	function updateViewersList() {
		let list = [];

		for (const viewers of Object.values(platforms)) {
			list.push(...viewers);
		}

		viewersList = list;
	}

	function copyViewersList(e) {
		e.preventDefault();

		const list = [];

		for (const viewer of viewersList) {
			list.push(viewer.displayname);
		}

		dispatch('copy', list.join('\n'));
	}
</script>

<div
	class="pointer-events-none fixed inset-0 z-index-[2000] opacity-60 hover:opacity-80 focus:opacity-80 transition duration-200"
	class:hidden={!visible}
	on:contextmenu={copyViewersList}
	on:dblclick={copyViewersList}
>
	<Movable bind:this={movable} on:update={onUpdate}>
		{#if visible}
			<div class="relative overflow-y-auto overflow-x-hidden h-full">
				<span class="absolute -top-1 right-0 text-right">
					<icon class="inline-block h-4 w-4 translate-y-0.5" data-icon="icon/eye" />
					{viewersList.length}
				</span>

				<ul>
					{#each viewersList as viewer}
						<li>
							{viewer.displayname}
						</li>
					{/each}
				</ul>
			</div>
		{/if}
	</Movable>
</div>
