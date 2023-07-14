<script>
	import { createEventDispatcher } from 'svelte';

	const dispatch = createEventDispatcher();

	let viewersListByPlatform = {};
	let viewersCountByPlatform = {};

	let viewersList_computed = [];
	let viewersCount_computed = 0;

	export function onViewersList(e) {
		console.log(e);
		viewersListByPlatform[e.streamer.platform] = e.viewers;

		updateViewersList();
	}

	export function onViewersCount(e) {
		console.log(e);
		viewersCountByPlatform[e.streamer.platform] = e.count;

		updateViewersList();
	}

	export function onAuthUpdate(signedInPlatforms) {
		for (const platform of Object.keys(viewersListByPlatform)) {
			if (!signedInPlatforms.includes(platform)) {
				delete viewersListByPlatform[platform];
				delete viewersCountByPlatform[platform];
			}
		}

		updateViewersList();
	}

	function updateViewersList() {
		const wholeList = [];

		for (const viewers of Object.values(viewersListByPlatform)) {
			viewers.forEach((v) => wholeList.push(v.displayname));
		}

		viewersList_computed = wholeList;

		let wholeCount = 0;

		for (const count of Object.values(viewersCountByPlatform)) {
			wholeCount += count;
		}

		viewersCount_computed = wholeCount;
	}

	function copyViewersList(e) {
		e.preventDefault();
		dispatch('copy', viewersList_computed.join('\n'));
	}
</script>

<div
	class="relative overflow-y-auto overflow-x-hidden h-full p-1"
	on:contextmenu={copyViewersList}
	on:dblclick={copyViewersList}
>
	<span class="absolute top-1 right-1 text-right">
		<icon class="inline-block h-4 w-4 translate-y-0.5" data-icon="icon/eye" />
		{viewersCount_computed}
	</span>

	<ul>
		{#each viewersList_computed as viewer}
			<li>
				{viewer}
			</li>
		{/each}
	</ul>
</div>
