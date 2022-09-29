<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import Container from '../../components/ui/Container.svelte';
	import SlimButton from '../../components/ui/SlimButton.svelte';
	import Button from '../../components/ui/Button.svelte';
	import SlimSwitch from '../../components/ui/SlimSwitch.svelte';

	import createConsole from '$lib/console-helper.mjs';

	const MUSIC_SERVICES_WITH_ENABLE = ['system', 'pretzel'];

	const console = createConsole('Settings/Accounts');

	const activePlayback = st || Caffeinated.music.svelte('activePlayback');
	const musicProviders = st || Caffeinated.music.svelte('providers');

	$: activePlayback, $activePlayback && console.debug('Active Playback:', $activePlayback);
	$: musicProviders, $musicProviders && console.debug('Music Providers:', $musicProviders);
</script>

<div>
	<h1 class="text-xl font-semibold mb-2">
		<LocalizedText key="page.settings.accounts.music_services" />
	</h1>
	<ul class="space-y-2">
		{#each Object.values($musicProviders || {}) as provider}
			<li>
				<Container>
					<div class="h-8 flex flex-row items-center">
						<icon class="h-5 -ml-1 mr-1" data-icon="service/{provider.serviceId}" />
						<p class="flex-1">
							{provider.serviceName}
						</p>

						<div class="flex-0">
							{#if MUSIC_SERVICES_WITH_ENABLE.includes(provider.serviceId)}
								<SlimSwitch
									title="page.settings.accounts.music_services.enable"
									checked={provider.settings.enabled}
									on:value={({ detail: value }) => {
										window.Caffeinated.music.updateMusicProviderSettings(provider.serviceId, {
											enabled: value
										});
									}}
								/>
							{:else}
								...
							{/if}
						</div>
					</div>
				</Container>
			</li>
		{/each}
	</ul>
</div>
