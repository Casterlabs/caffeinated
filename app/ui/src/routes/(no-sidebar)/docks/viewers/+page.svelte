<script lang="ts">
	import { Koi, copyText } from '$lib/app-shim';
	import type { User, ViewerCountEvent, ViewerListEvent } from '$lib/koi';

	import { IconEye } from '@casterlabs/heroicons-svelte';

	let viewersListByPlatform: Record<string, User[]> = {};
	let viewersCountByPlatform: Record<string, number> = {};

	let viewersList_computed: string[] = [];
	let viewersCount_computed = 0;

	Koi.on('VIEWER_LIST', (e: ViewerListEvent) => {
		console.log(e);
		viewersListByPlatform[e.streamer.platform] = e.viewers;

		updateViewersList();
	});

	Koi.on('VIEWERS_COUNT', (e: ViewerCountEvent) => {
		console.log(e);
		viewersCountByPlatform[e.streamer.platform] = e.count;

		updateViewersList();
	});

	// export function onAuthUpdate(signedInPlatforms) {
	// 	for (const platform of Object.keys(viewersListByPlatform)) {
	// 		if (!signedInPlatforms.includes(platform)) {
	// 			delete viewersListByPlatform[platform];
	// 			delete viewersCountByPlatform[platform];
	// 		}
	// 	}

	// 	updateViewersList();
	// }

	function updateViewersList() {
		const wholeList: string[] = [];

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

	function copyViewersList(e: MouseEvent) {
		e.preventDefault();
		copyText(viewersList_computed.join('\n'));
	}
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="relative overflow-y-auto overflow-x-hidden h-full p-1" oncontextmenu={copyViewersList} ondblclick={copyViewersList}>
	<span class="absolute top-1 right-1 text-right">
		<IconEye class="inline-block h-4 w-4 -translate-y-px" />
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
