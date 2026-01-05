<script lang="ts">
	import { storify } from '$lib/bridge-helper';
	import { render } from '$lib/locale/locale';
	import { load } from '../+page';

	import LoadingSpinner from '$lib/layout/LoadingSpinner.svelte';
	import PlatformIcon from '$lib/layout/PlatformIcon.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconInformationCircle, IconXMark } from '@casterlabs/heroicons-svelte';
	import { Box, Button, Input } from '@casterlabs/ui';

	const activePlayback = storify(Music, 'activePlayback').readable<Awaited<typeof Music.activePlayback>>();
	const musicProviders = storify(Music, 'providers').readable<Awaited<typeof Music.providers>>();
	const authInstances = storify(AppAuth, 'authInstances').readable<Awaited<typeof AppAuth.authInstances>>();
	const connectionStates = storify(Koi, 'connectionStates').readable<Awaited<typeof Koi.connectionStates>>();

	const MUSIC_SERVICES_WITH_ENABLE = ['system', 'pretzel'];
	const MUSIC_SERVICES_WITH_OAUTH = ['spotify'];

	$effect(() => {
		Object.values($authInstances || {}).forEach((inst) => {
			const platform = inst.userData?.platform;
			if (loading === platform) {
				loading = null;
			}
		});
	});

	let loading: string | null = $state(null);
</script>

<div class="space-y-6">
	<div>
		<h1 class="text-xl font-semibold mb-2">
			<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.streaming_services" />
		</h1>
		<ul class="space-y-2">
			<!-- Loop over all of the "official" platforms -->
			{#await AppAuth.AUTHENTICATABLE then AUTHENTICATABLE_PLATFORMS}
				{#each AUTHENTICATABLE_PLATFORMS as platform}
					{@const { tokenId, userData } =
						Object.values($authInstances || {}) //
							.filter(({ userData }) => userData?.platform == platform)[0] || {}}
					{@const isLoading = loading === platform || (!userData && Object.keys($authInstances || {}).includes(platform.toLowerCase()))}

					<li>
						<Box sides={['top', 'bottom', 'left', 'right']}>
							<div class="h-8 flex flex-row items-center">
								<PlatformIcon class="text-xl mr-1.5" {platform} color />

								<p class="flex-1 flex flex-row items-center">
									<LocalizedText key="co.casterlabs.caffeinated.app.platform.{platform}" />

									{#if userData}
										{@const connectionState = ($connectionStates || {})[userData.platform] || {}}

										<a
											href={userData.link}
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
												<IconInformationCircle class="translate-x-1 w-3.5 h-3.5" theme="mini" />
											</span>
										{/if}
									{/if}
								</p>

								<div class="flex-0">
									{#if isLoading}
										<Button
											borderless
											class="w-6 h-6 mr-4 group flex items-center justify-center "
											onclick={() => {
												loading = null;
												AppAuth.cancelSignin();
											}}
										>
											<span class="w-6 h-6 group-hover:hidden">
												<LoadingSpinner />
											</span>
											<span class="hidden group-hover:inline">
												<IconXMark theme="mini" />
											</span>
										</Button>
									{:else if userData}
										<button
											class="px-1.5 py-1 inline-flex items-center rounded bg-error text-white text-xs font-base"
											onclick={() => {
												AppAuth.signout(tokenId);
											}}
										>
											<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.disconnect" />
										</button>
									{:else}
										<button
											class="px-1.5 py-1 inline-flex items-center rounded bg-success text-white text-xs font-base"
											disabled={loading !== null}
											onclick={() => {
												loading = platform;
												AppAuth.requestOAuthSignin('koi', platform.toLowerCase(), false, null);
											}}
										>
											<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.connect" />
										</button>
									{/if}
								</div>
							</div>
						</Box>
					</li>
				{/each}

				<!-- Loop over all of the other platforms -->
				<!-- {#each Object.values($authInstances || {}).filter(({ userData }) => !STREAMING_SERVICES[userData?.platform]) as { tokenId, userData }}
				{#if userData}
					<li>
						<Box sides={['top', 'bottom', 'left', 'right']}>
							<div class="h-8 flex flex-row items-center">
								<icon class="w-5 h-5 -ml-1 mr-1.5" data-icon="icon/beaker" />
								<p class="flex-1 flex flex-row items-center">
									TEST / {userData.platform}

									<a
										href={userData.link}
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
						</Box>
					</li>
				{/if}
			{/each} -->
			{/await}
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
					<Box sides={['top', 'bottom', 'left', 'right']}>
						<div class="h-8 flex flex-row items-center">
							<PlatformIcon class="text-lg mr-1.5" platform={provider.serviceId} />

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
									<Input
										type="checkbox"
										title={render('co.casterlabs.caffeinated.app.page.settings.accounts.music_services.enable')}
										checked={provider.settings?.enabled}
										onchange={(e) => {
											const checked = (e.target as HTMLInputElement).checked;
											Music.updateMusicProviderSettings(provider.serviceId, {
												enabled: checked
											});
										}}
									/>
								{:else if MUSIC_SERVICES_WITH_OAUTH.includes(provider.serviceId)}
									{#if provider.isSignedIn}
										<button
											class="px-1.5 py-1 inline-flex items-center rounded bg-error text-white text-xs font-base"
											onclick={() => {
												Music.signoutMusicProvider(provider.serviceId);
											}}
										>
											<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.disconnect" />
										</button>
									{:else}
										<a
											class="px-1.5 py-1 inline-flex items-center rounded bg-success text-white text-xs font-base"
											href="/$caffeinated-sdk-root$/signin/oauth?type=music&platform={provider.serviceId}"
										>
											<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.accounts.connect" />
										</a>
									{/if}
								{/if}
							</div>
						</div>
					</Box>
				</li>
			{/each}
		</ul>
	</div>
</div>
