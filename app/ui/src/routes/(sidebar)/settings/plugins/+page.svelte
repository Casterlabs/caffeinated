<script lang="ts">
	import { storify } from '$lib/bridge-helper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { Box, Button } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
	const contexts = storify(AppPlugins, 'contexts').readable<Awaited<typeof AppPlugins.contexts>>();

	const contextsToDisplay = $derived(
		($contexts || [])
			.filter((c) => c.file || $uiPreferences?.enableStupidlyUnsafeSettings) // Hide the internal plugins IF they don't have developer mode enabled.
			.sort((c1, c2) => {
				if (c1.file == c2.file) return 0;
				return c2.file ? -1 : 1; // Put internal plugins at the start.
			})
	);

	let files: string[] = $state([]);

	onMount(() => {
		async function fetchFiles() {
			files = await AppPlugins.listFiles();
		}
		const int = setInterval(fetchFiles, 500);
		fetchFiles();
		return () => clearInterval(int);
	});
</script>

<ul class="space-y-2">
	{#each contextsToDisplay as context}
		<!-- Hide the internal plugins IF they don't have developer mode enabled. -->
		{#if context.file || $uiPreferences?.enableStupidlyUnsafeSettings}
			<li>
				<Box sides={['top', 'bottom', 'left', 'right']}>
					<div class="h-6 flex flex-row items-center">
						<p class="flex-1">
							{#if context.file}
								{context.file}
							{:else}
								<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.internal_plugin" />
							{/if}

							{#if context.pluginIds.length > 0}
								<span class="text-base-11 text-xs">
									&bull;
									{context.pluginIds.join(',')}
								</span>
							{/if}
						</p>
						<div class="flex-0">
							{#if context.file}
								<Button
									class="p-1"
									borderless
									onclick={() => {
										AppPlugins.unload(context.id);
									}}
								>
									<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.file.unload" />
								</Button>
							{/if}
						</div>
					</div>
				</Box>
			</li>
		{/if}
	{/each}

	{#each files as file}
		<li>
			<Box sides={['top', 'bottom', 'left', 'right']}>
				<div class="h-6 flex flex-row items-center">
					<p class="flex-1">
						{file}
					</p>
					<div class="flex-0">
						<Button
							class="p-1"
							borderless
							onclick={() => {
								AppPlugins.load(file);
							}}
						>
							<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.file.load" />
						</Button>
					</div>
				</div>
			</Box>
		</li>
	{/each}
</ul>

<div class="mt-6 flex flex-row items-center">
	<div class="flex-1"></div>

	<Button
		onclick={() => {
			AppPlugins.openPluginsDir();
		}}
	>
		<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.open_directory" />
	</Button>
</div>
