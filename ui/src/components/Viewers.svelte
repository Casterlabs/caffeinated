<script>
	import { onMount } from 'svelte';

	let platforms = {};
	let viewersList = [];

	let movable = null;

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

		Caffeinated.copyText(list.join('\n'), 'Copied the viewer list to your clipboard');
	}

	onMount(() => {
		const eventListener = Bridge.on('koi:event', (event) => {
			if (event.event_type == 'VIEWER_LIST') {
				onViewersList(event);
			}
		});

		Caffeinated.koi.viewers.then((all) => {
			for (const [platform, viewers] of Object.entries(all)) {
				onViewersList({
					streamer: {
						platform
					},
					viewers
				});
			}
		});

		return () => {
			Bridge.off('koi:event', eventListener);
		};
	});
</script>

<div
	class="relative overflow-y-auto overflow-x-hidden h-full p-1"
	on:contextmenu={copyViewersList}
	on:dblclick={copyViewersList}
>
	<span class="absolute top-1 right-1 text-right">
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
