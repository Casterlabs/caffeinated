<svelte:options accessors />

<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import anime from 'animejs';

	const sections = [
		[
			{
				href: '/$caffeinated-sdk-root$/dashboard',
				name: 'co.casterlabs.caffeinated.app.page.dashboard',
				useStartsWith: false
			},
			{
				href: '/$caffeinated-sdk-root$/chat-bot',
				name: 'co.casterlabs.caffeinated.app.page.chat_bot',
				useStartsWith: true
			}
		],
		[
			{
				href: '/$caffeinated-sdk-root$/widgets',
				name: 'co.casterlabs.caffeinated.app.page.widgets',
				useStartsWith: true
			},
			{
				href: '/$caffeinated-sdk-root$/docks',
				name: 'co.casterlabs.caffeinated.app.page.docks',
				useStartsWith: false
			},
			{
				href: '/$caffeinated-sdk-root$/settings',
				name: 'co.casterlabs.caffeinated.app.page.settings',
				useStartsWith: true
			}
		]
	];

	let applets = [];
	let updateAvailable = false;
	export let sidebarVisible = true;

	export function toggleSideBar() {
		sidebarVisible = !sidebarVisible;
		setPreferenceItem('sidebarClosed', !sidebarVisible);

		anime({
			easing: 'easeOutQuad',
			duration: 200,
			direction: sidebarVisible ? 'normal' : 'reverse',
			update: (anim) => {
				document.body.style.setProperty('--sidebar-width-mul', anim.progress);
			}
		});
	}

	async function setPreferenceItem(name, value) {
		Caffeinated.UI.updateAppearance({
			...(await Caffeinated.UI.preferences),
			[name]: value
		});
	}

	onMount(() => {
		window.toggleSideBar = toggleSideBar;

		Caffeinated.pluginIntegration.widgets.then((widgets) => {
			applets = widgets.filter((w) => w.details.type == 'APPLET');
		});

		Caffeinated.UI.preferences.then((prefs) => {
			if (prefs.sidebarClosed) {
				document.body.style.setProperty('--sidebar-width-mul', 0);
				sidebarVisible = false;
			}
		});

		setInterval(
			async () => {
				updateAvailable = await Caffeinated.hasUpdate();
			},
			2 /*min*/ * 60 * 1000
		);
	});
</script>

<div class="flex-0 w-[var(--actual-sidebar-width)] h-full flex flex-col overflow-hidden">
	<div
		class="w-[var(--base-sidebar-width)] flex flex-grow flex-col overflow-y-auto bg-base-2 pb-4"
		style="transform: translateX(calc(-1% * calc(100 - var(--sidebar-width-mul, 100)))"
	>
		<!-- svelte-ignore a11y-no-noninteractive-element-to-interactive-role -->
		<nav
			class="flex flex-1 flex-col divide-y divide-current text-base-6 overflow-y-auto"
			aria-label="Sidebar"
			role="listbox"
		>
			{#if updateAvailable}
				<div class="space-y-1 px-2 py-4" role="listitem">
					<button
						class="group flex flex-col items-center px-3 py-2 border-current font-medium rounded-md bg-primary-10 hover:bg-primary-11 text-base-1 hover:text-base-3 transition"
						on:click={() => window.saucer.messages.emit(['app:restart'])}
					>
						<h1 class="text-md">
							<LocalizedText key="co.casterlabs.caffeinated.app.ui.sidebar.update_app" />
						</h1>
						<h2 class="text-xs">
							<LocalizedText
								key="co.casterlabs.caffeinated.app.ui.sidebar.update_app.description"
							/>
						</h2>
					</button>
				</div>
			{/if}

			{#if applets.length > 0}
				<div class="space-y-1 px-2 py-4" role="listitem">
					{#each applets as applet}
						{@const href = `/$caffeinated-sdk-root$/applet?id=${applet.id}`}
						{@const isSelected = $page.url.pathname == href}

						<a
							{href}
							class="group flex items-center px-3 py-2 text-sm leading-6 border-current transition font-medium rounded-md"
							aria-current={isSelected ? 'page' : undefined}
							class:hover:bg-base-4={!isSelected}
							class:bg-base-5={isSelected}
						>
							<span class="text-base-12">
								<LocalizedText key={applet.details.friendlyName} />
							</span>
						</a>
					{/each}
				</div>
			{/if}

			{#each sections as section}
				<div class="space-y-1 px-2 py-4" role="listitem">
					{#each section as item}
						{@const isSelected = item.useStartsWith
							? $page.url.pathname.startsWith(item.href)
							: $page.url.pathname == item.href}

						<a
							href={item.href}
							class="group flex items-center px-3 py-2 text-sm leading-6 border-current transition font-medium rounded-md"
							aria-current={isSelected ? 'page' : undefined}
							class:hover:bg-base-4={!isSelected}
							class:bg-base-5={isSelected}
						>
							<span class="text-base-12">
								<LocalizedText key={item.name} />
							</span>
						</a>
					{/each}
				</div>
			{/each}
		</nav>
	</div>
</div>

<style>
	:global(body) {
		--base-sidebar-width: 12rem;
		--actual-sidebar-width: calc(
			var(--base-sidebar-width) * calc(var(--sidebar-width-mul, 100) / 100)
		);
	}
</style>
