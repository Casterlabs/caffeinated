<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import Container from '../../components/ui/Container.svelte';
	import SlimButton from '../../components/ui/SlimButton.svelte';
	import Button from '../../components/ui/Button.svelte';

	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Settings/Plugins');

	const contexts = st || Caffeinated.plugins.svelte('contexts');

	$: contexts, $contexts && console.debug('Plugins Contexts:', $contexts);
	$: contexts, $contexts && listFiles();

	let files = [];

	function listFiles() {
		Caffeinated.plugins.listFiles().then((v) => (files = v));
	}
</script>

<ul class="space-y-2">
	{#each $contexts || [] as context}
		<li>
			<Container>
				<div class="h-6 flex flex-row items-center">
					<p class="flex-1">
						{#if context.file}
							{context.file}
						{:else}
							<LocalizedText key="page.settings.plugins.internal_plugin" />
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
									window.Caffeinated.plugins.unload(context.id);
								}}
							>
								<LocalizedText key="page.settings.plugins.file.unload" />
							</SlimButton>
						{/if}
					</div>
				</div>
			</Container>
		</li>
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
								window.Caffeinated.plugins.load(file);
							}}
						>
							<LocalizedText key="page.settings.plugins.file.load" />
						</SlimButton>
					</div>
				</div>
			</Container>
		</li>
	{/each}
</ul>

<div class="w-fit mt-6 ml-auto">
	<Button
		on:click={() => {
			window.Caffeinated.plugins.openPluginsDir();
		}}
	>
		<LocalizedText key="page.settings.plugins.open_directory" />
	</Button>
</div>
