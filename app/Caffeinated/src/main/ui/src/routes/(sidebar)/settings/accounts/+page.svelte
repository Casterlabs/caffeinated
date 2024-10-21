<script>
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Container from '$lib/ui/Container.svelte';
	import SlimButton from '$lib/ui/SlimButton.svelte';
	import Button from '$lib/ui/Button.svelte';
	import SlimSwitch from '$lib/ui/SlimSwitch.svelte';
	import LoadingSpinner from '$lib/LoadingSpinner.svelte';

	import { goto } from '$app/navigation';
	import {
		STREAMING_SERVICES,
		SPECIAL_SIGNIN,
		PORTAL_SIGNIN,
		openAuthPortal
	} from '$lib/caffeinatedAuth.mjs';
	import createConsole from '$lib/console-helper.mjs';

	const MUSIC_SERVICES_WITH_ENABLE = ['system', 'pretzel'];
	const MUSIC_SERVICES_WITH_OAUTH = ['spotify'];

	const console = createConsole('Settings/Accounts');

	const activePlayback = st || window.svelte('Caffeinated.music', 'activePlayback');
	const musicProviders = st || window.svelte('Caffeinated.music', 'providers');
	const authInstances = st || window.svelte('Caffeinated.auth', 'authInstances');
	const connectionStates = st || window.svelte('Caffeinated.koi', 'connectionStates');

	let loading = [];

	$: authInstances,
		Object.values($authInstances || {}).forEach((inst) => {
			const platform = inst.userData?.platform;

			if (loading.includes(platform)) {
				loading.splice(loading.indexOf(platform), 1);
				loading = loading;
			}
		});

	$: activePlayback, $activePlayback && console.debug('Active Playback:', $activePlayback);
	$: musicProviders, $musicProviders && console.debug('Music Providers:', $musicProviders);
	$: authInstances, $authInstances && console.debug('Auth Instances:', $authInstances);
	$: connectionStates, $connectionStates && console.debug('Connection states:', $connectionStates);
</script>

<div class="space-y-6">
	<div>
		<h1 class="text-xl font-semibold mb-2">
			<LocalizedText
				key="co.casterlabs.caffeinated.app.page.settings.accounts.streaming_services"
			/>
		</h1>
		<ul class="space-y-2">
			<!-- Loop over all of the "official" platforms -->
			{#each Object.entries(STREAMING_SERVICES) as [platform, { name: platformName }]}
				{@const { tokenId, userData } =
					Object.values($authInstances || {}) //
						.filter(({ userData }) => userData?.platform == platform)[0] || {}}
				{@const isLoading =
					loading.includes(platform) ||
					(!userData && Object.keys($authInstances || {}).includes(platform.toLowerCase()))}

				<li>
					<Container>
						<div class="h-8 flex flex-row items-center">
							<icon class="w-5 h-5 -ml-1 mr-1.5" data-icon="service/{platform.toLowerCase()}" />
							<p class="flex-1 flex flex-row items-center">
								{platformName}

								{#if userData}
									{@const connectionState = ($connectionStates || {})[userData.platform] || {}}

									<a
										href="/$caffeinated-sdk-root${userData.link}"
										target="_blank"
										class="ml-2 px-2 py-0.5 text-[0.675rem] leading-[1rem] bg-base-4 text-base-11 inline-flex items-center rounded-full font-base underline"
									>
										{userData.displayname}
									</a>

									{#if Object.entries(connectionState).length > 0}
										<span
											title={Object.entries(connectionState)
												.map(([k, v]) => `${k}: ${v}`)
												.join('\n')}
										>
											<icon class="translate-x-1 w-3.5 h-3.5" data-icon="icon/information-circle" />
										</span>
									{/if}
								{/if}
							</p>

							<div class="flex-0">
								{#if isLoading}
									<div class="w-6 mr-4">
										<LoadingSpinner />
									</div>
								{:else if userData}
									<button
										class="px-1.5 py-1 inline-flex items-center rounded bg-error text-white text-xs font-base"
										on:click={() => {
											window.Caffeinated.auth.signout(tokenId);
										}}
									>
										<LocalizedText
											key="co.casterlabs.caffeinated.app.page.settings.accounts.disconnect"
										/>
									</button>
								{:else}
									<button
										class="px-1.5 py-1 inline-flex items-center rounded bg-success text-white text-xs font-base"
										on:click={() => {
											loading.push(platform);
											loading = loading;

											if (SPECIAL_SIGNIN[platform]) {
												goto(`${SPECIAL_SIGNIN[platform]}?platform=${platform.toLowerCase()}`);
												return;
											}

											if (PORTAL_SIGNIN.includes(platform)) {
												openAuthPortal(platform, false);
												return;
											}

											window.Caffeinated.auth.requestOAuthSignin(
												'koi',
												platform.toLowerCase(),
												false,
												null
											);
										}}
									>
										<LocalizedText
											key="co.casterlabs.caffeinated.app.page.settings.accounts.connect"
										/>
									</button>
								{/if}
							</div>
						</div>
					</Container>
				</li>
			{/each}

			<!-- Loop over all of the other platforms -->
			{#each Object.values($authInstances || {}).filter(({ userData }) => !STREAMING_SERVICES[userData?.platform]) as { tokenId, userData }}
				{#if userData}
					<li>
						<Container>
							<div class="h-8 flex flex-row items-center">
								<icon class="w-5 h-5 -ml-1 mr-1.5" data-icon="icon/beaker" />
								<p class="flex-1 flex flex-row items-center">
									TEST / {userData.platform}

									<a
										href="/$caffeinated-sdk-root${userData.link}"
										target="_blank"
										class="ml-2 px-2 py-0.5 text-[0.675rem] leading-[1rem] bg-base-4 text-base-11 inline-flex items-center rounded-full font-base underline"
									>
										{userData.displayname}
									</a>
								</p>

								<div class="flex-0">
									<button
										class="px-1.5 py-1 inline-flex items-center rounded bg-error text-white text-xs font-base"
										on:click={() => {
											window.Caffeinated.auth.signout(tokenId);
										}}
									>
										<LocalizedText
											key="co.casterlabs.caffeinated.app.page.settings.accounts.disconnect"
										/>
									</button>
								</div>
							</div>
						</Container>
					</li>
				{/if}
			{/each}
		</ul>
	</div>

	<div>
		<h1 class="text-xl font-semibold mb-2">
			<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.music_services" />
		</h1>

		{#if $activePlayback}
			{@const { currentTrack } = $activePlayback}
			<p class="mb-2 flex flex-row items-center">
				<img class="inline-block w-10 h-10 rounded" alt="" src={currentTrack.albumArtUrl} />
				<a href={currentTrack.link} target="_blank" class="ml-2 text-sm">
					{currentTrack.title}
					<br />
					{currentTrack.artists.join(', ')}
				</a>
			</p>
		{/if}

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
										href="/$caffeinated-sdk-root${provider.accountLink}"
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
										title="co.casterlabs.caffeinated.app.page.settings.accounts.music_services.enable"
										checked={provider.settings?.enabled}
										on:value={({ detail: value }) => {
											window.Caffeinated.music.updateMusicProviderSettings(provider.serviceId, {
												enabled: value
											});
										}}
									/>
								{:else if MUSIC_SERVICES_WITH_OAUTH.includes(provider.serviceId)}
									{#if provider.isSignedIn}
										<button
											class="px-1.5 py-1 inline-flex items-center rounded bg-error text-white text-xs font-base"
											on:click={() => {
												window.Caffeinated.music.signoutMusicProvider(provider.serviceId);
											}}
										>
											<LocalizedText
												key="co.casterlabs.caffeinated.app.page.settings.accounts.disconnect"
											/>
										</button>
									{:else}
										<a
											class="px-1.5 py-1 inline-flex items-center rounded bg-success text-white text-xs font-base"
											href="/$caffeinated-sdk-root$/signin/oauth?type=music&platform={provider.serviceId}"
										>
											<LocalizedText
												key="co.casterlabs.caffeinated.app.page.settings.accounts.connect"
											/>
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
