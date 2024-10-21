<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Container from '$lib/ui/Container.svelte';
	import SlimButton from '$lib/ui/SlimButton.svelte';
	import Button from '$lib/ui/Button.svelte';

	import createConsole from '$lib/console-helper.mjs';
	import { onMount } from 'svelte';

	const console = createConsole('Settings/Plugins');

	const preferences = st || window.svelte('Caffeinated.UI', 'preferences');
	const contexts = st || window.svelte('Caffeinated.pluginIntegration', 'contexts');

	$: contexts, $contexts && console.debug('Plugins Contexts:', $contexts);

	let files = [];

	onMount(() => {
		const int = setInterval(async () => {
			files = await Caffeinated.pluginIntegration.listFiles();
		}, 500);

		return () => clearInterval(int);
	});
</script>

<ul class="space-y-2">
	{#each $contexts || [] as context}
		<!-- Hide the internal plugins IF they don't have developer mode enabled. -->
		{#if context.file || $preferences?.enableStupidlyUnsafeSettings}
			<li>
				<Container>
					<div class="h-6 flex flex-row items-center">
						<p class="flex-1">
							{#if context.file}
								{context.file}
							{:else}
								<LocalizedText
									key="co.casterlabs.caffeinated.app.page.settings.plugins.internal_plugin"
								/>
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
								<SlimButton
									on:click={() => {
										window.Caffeinated.pluginIntegration.unload(context.id);
									}}
								>
									<LocalizedText
										key="co.casterlabs.caffeinated.app.page.settings.plugins.file.unload"
									/>
								</SlimButton>
							{/if}
						</div>
					</div>
				</Container>
			</li>
		{/if}
	{/each}

	{#each files as file}
		<li>
			<Container>
				<div class="h-6 flex flex-row items-center">
					<p class="flex-1">
						{file}
					</p>
					<div class="flex-0">
						<SlimButton
							on:click={() => {
								window.Caffeinated.pluginIntegration.load(file);
							}}
						>
							<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.file.load" />
						</SlimButton>
					</div>
				</div>
			</Container>
		</li>
	{/each}
</ul>

<div class="mt-6 flex flex-row items-center">
	<div class="flex-1">
		<LocalizedText key="co.casterlabs.caffeinated.app.sdk_documentation" />
	</div>

	<Button
		on:click={() => {
			window.Caffeinated.pluginIntegration.openPluginsDir();
		}}
	>
		<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.plugins.open_directory" />
	</Button>
</div>
