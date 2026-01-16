<script lang="ts">
	import { Koi, copyText } from '$lib/app-shim';
	import type { KoiStatics, User, UserPlatform } from '$lib/koi';

	import { IconEye } from '@casterlabs/heroicons-svelte';

	import { onMount } from 'svelte';

	let viewersListByPlatform: Record<string, User[]> = {};
	let viewersCountByPlatform: Record<string, number> = {};

	let viewersList_computed: string[] = $state([]);
	let viewersCount_computed = $state(0);

	Koi.on('koi_statics', (statics: KoiStatics) => {
		const signedInPlatforms = Object.values(statics.userStates).map((state) => state.streamer.platform);

		for (const platform of Object.keys(viewersListByPlatform)) {
			if (!signedInPlatforms.includes(platform as UserPlatform)) {
				delete viewersListByPlatform[platform];
				delete viewersCountByPlatform[platform];
			}
		}

		for (const [platform, viewers] of Object.entries(statics.viewers)) {
			viewersListByPlatform[platform] = viewers;
		}
		for (const [platform, count] of Object.entries(statics.viewerCounts)) {
			viewersCountByPlatform[platform] = count;
		}

		updateViewersList();
	});

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

	onMount(async () => {
		const statics = await Koi.statics();
		Koi.broadcast('koi_statics', statics);
	});
</script>

<!-- svelte-ignore a11y_no_static_element_interactions -->
<div class="relative overflow-y-auto overflow-x-hidden h-full p-1" oncontextmenu={copyViewersList} ondblclick={copyViewersList}>
	<span class="absolute top-1 right-1 text-right">
		<IconEye class="inline-block h-4 w-4 -translate-y-px" />
		<span class="select-auto">{viewersCount_computed}</span>
	</span>

	<ul class="select-auto">
		{#each viewersList_computed as viewer}
			<li>
				{viewer}
			</li>
		{/each}
	</ul>
</div>
