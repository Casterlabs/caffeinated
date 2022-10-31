<script>
	import LocalizedText from '../../components/LocalizedText.svelte';
	import Container from '../../components/ui/Container.svelte';
	import SlimButton from '../../components/ui/SlimButton.svelte';
	import Button from '../../components/ui/Button.svelte';
	import SlimSwitch from '../../components/ui/SlimSwitch.svelte';

	import createConsole from '$lib/console-helper.mjs';

	const STREAMING_SERVICES = {
		CAFFEINE: 'Caffeine',
		TWITCH: 'Twitch',
		TROVO: 'Trovo',
		GLIMESH: 'Glimesh',
		BRIME: 'Brime',
		YOUTUBE: 'YouTube (BETA)',
		DLIVE: 'DLive'
	};

	const MUSIC_SERVICES_WITH_ENABLE = ['system', 'pretzel'];
	const MUSIC_SERVICES_WITH_OAUTH = ['spotify'];

	const console = createConsole('Settings/Accounts');

	const activePlayback = st || Caffeinated.music.svelte('activePlayback');
	const musicProviders = st || Caffeinated.music.svelte('providers');
	const authInstances = st || Caffeinated.auth.svelte('authInstances');

	let loading = [];

	$: authInstances,
		Object.values($authInstances || {}).forEach((inst) => {
			const platform = inst.userData?.platform;

			if (loading.includes(platform)) {
				loading.splice(loading.indexOf(platform), 1);
			}
		});

	$: activePlayback, $activePlayback && console.debug('Active Playback:', $activePlayback);
	$: musicProviders, $musicProviders && console.debug('Music Providers:', $musicProviders);
	$: authInstances, $authInstances && console.debug('Auth Instances:', $authInstances);
</script>

<div class="space-y-6">
	<div>
		<h1 class="text-xl font-semibold mb-2">
			<LocalizedText key="page.settings.accounts.streaming_services" />
		</h1>
		<ul class="space-y-2">
			{#each Object.entries(STREAMING_SERVICES) as [platform, platformName]}
				{@const { tokenId, userData } =
					Object.values($authInstances || {}) //
						.filter(({ userData }) => userData?.platform == platform)[0] || {}}
				<!-- {@const isLoading = loading.includes(platform)} -->

				<li>
					<Container>
						<div class="h-8 flex flex-row items-center">
							<icon class="w-5 h-5 -ml-1 mr-1.5" data-icon="service/{platform.toLowerCase()}" />
							<p class="flex-1 flex flex-row items-center">
								{platformName}

								{#if userData}
									<a
										href={userData.link}
										target="_blank"
										class="ml-2 px-2 py-0.5 text-[0.675rem] leading-[1rem] bg-base-4 text-base-11 inline-flex items-center rounded-full font-base underline"
									>
										{userData.displayname}
									</a>
								{/if}
							</p>

							<div class="flex-0">
								{#if loading.includes(platform)}
									...
								{:else if userData}
									<button
										class="px-1.5 py-1 inline-flex items-center rounded bg-red-600 text-base-12 text-xs font-base"
										on:click={() => {
											window.Caffeinated.auth.signout(tokenId);
										}}
									>
										<LocalizedText key="page.settings.accounts.disconnect" />
									</button>
								{:else}
									<button
										class="px-1.5 py-1 inline-flex items-center rounded bg-green-600 text-base-12 text-xs font-base"
										on:click={() => {
											window.Caffeinated.auth.requestOAuthSignin(platform, true, true);
											loading.push(platform);
										}}
									>
										<LocalizedText key="page.settings.accounts.connect" />
									</button>
								{/if}
							</div>
						</div>
					</Container>
				</li>
			{/each}
		</ul>
	</div>

	<div>
		<h1 class="text-xl font-semibold mb-2">
			<LocalizedText key="page.settings.accounts.music_services" />
		</h1>
		<ul class="space-y-2">
			{#each Object.values($musicProviders || {}) as provider}
				<li>
					<Container>
						<div class="h-8 flex flex-row items-center">
							<icon class="w-5 h-5 -ml-1 mr-1.5" data-icon="service/{provider.serviceId}" />
							<p class="flex-1 flex flex-row items-center">
								{provider.serviceName}

								{#if provider.accountName}
									<a
										href={provider.accountLink}
										target="_blank"
										class="ml-2 px-2 py-0.5 text-[0.675rem] leading-[1rem] bg-base-4 text-base-11 inline-flex items-center rounded-full font-base underline"
									>
										{provider.accountName}
									</a>
								{/if}
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
								{:else if MUSIC_SERVICES_WITH_OAUTH.includes(provider.serviceId)}
									{#if provider.isSignedIn}
										<button
											class="px-1.5 py-1 inline-flex items-center rounded bg-red-600 text-base-12 text-xs font-base"
											on:click={() => {
												window.Caffeinated.music.signoutMusicProvider(provider.serviceId);
											}}
										>
											<LocalizedText key="page.settings.accounts.disconnect" />
										</button>
									{:else}
										<a
											class="px-1.5 py-1 inline-flex items-center rounded bg-green-600 text-base-12 text-xs font-base"
											href="/signin/{provider.serviceId}"
										>
											<LocalizedText key="page.settings.accounts.connect" />
										</a>
									{/if}
								{/if}
							</div>
						</div>
					</Container>
				</li>
			{/each}
		</ul>
	</div>
</div>
