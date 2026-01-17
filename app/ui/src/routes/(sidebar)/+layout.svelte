<script lang="ts">
	import { page } from '$app/state';
	import { storify } from '$lib/bridge-helper';
	import anime from 'animejs';

	import Content from '$lib/layout/Content.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconEllipsisVertical } from '@casterlabs/heroicons-svelte';

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

	const hasUpdate = storify(App, 'hasUpdate').readable<boolean>();

	let isOnDashboard = $derived(page.url.pathname == '/$caffeinated-sdk-root$/dashboard');
	let sidebarVisible = $state(true);
	let sidebarWidthMul = $state(1);

	function toggleSidebar() {
		sidebarVisible = !sidebarVisible;
		anime({
			easing: 'easeOutQuad',
			duration: 200,
			direction: sidebarVisible ? 'normal' : 'reverse',
			update: (anim) => {
				sidebarWidthMul = anim.progress / 100;
			}
		});
	}
</script>

<div
	class="flex flex-row h-full"
	style:--base-sidebar-width="12rem"
	style:--sidebar-width-mul={isOnDashboard ? sidebarWidthMul : 1}
	style:--actual-sidebar-width="calc(var(--base-sidebar-width) * var(--sidebar-width-mul))"
>
	<div class="w-fit h-full flex flex-col overflow-hidden" style:width="var(--actual-sidebar-width)">
		<div
			class="flex flex-grow flex-col overflow-y-auto bg-base-2 pb-4 break-anywhere"
			style:width="var(--base-sidebar-width)"
			style:transform="translateX(calc(var(--base-sidebar-width) * -1 * (1 - var(--sidebar-width-mul))))"
		>
			<nav class="flex flex-1 flex-col divide-y divide-current text-base-6 overflow-y-auto">
				{#if hasUpdate}
					<div class="space-y-1 px-2 py-4" role="listitem">
						<button
							class="group flex flex-col items-center px-3 py-2 border-current font-medium rounded-md bg-primary-10 hover:bg-primary-11 text-base-1 hover:text-base-3 transition"
							onclick={() => {
								// @ts-ignore
								window.saucer.messages.emit(['app:restart']);
							}}
						>
							<h1 class="text-md">
								<LocalizedText key="co.casterlabs.caffeinated.app.ui.sidebar.update_app" />
							</h1>
							<h2 class="text-xs">
								<LocalizedText key="co.casterlabs.caffeinated.app.ui.sidebar.update_app.description" />
							</h2>
						</button>
					</div>
				{/if}

				{#each sections as section}
					<div class="space-y-1 px-2 py-4" role="listitem">
						{#each section as item}
							{@const isSelected = item.useStartsWith ? page.url.pathname.startsWith(item.href) : page.url.pathname == item.href}

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

				<!-- {#if applets.length > 0}
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
			{/if} -->

				<a href="https://docs.casterlabs.co/caffeinated" target="_blank" class="text-primary-11 absolute inset-x-3 bottom-3 text-xs text-center underline">
					<LocalizedText key="co.casterlabs.caffeinated.app.documentation" />
				</a>
			</nav>
		</div>
	</div>

	<Content>
		<!-- svelte-ignore slot_element_deprecated -->
		<slot />
	</Content>

	{#if isOnDashboard}
		<button class="fixed top-1/2 left-[var(--actual-sidebar-width)] -ml-1 -translate-y-1/2 z-90 bg-base-2 py-0.5 rounded-r-md shadow-sm opacity-80" onclick={toggleSidebar}>
			<span class="sr-only">
				{#if sidebarVisible}
					<LocalizedText key="co.casterlabs.caffeinated.app.ui.sidebar.collapse_sidebar" />
				{:else}
					<LocalizedText key="co.casterlabs.caffeinated.app.ui.sidebar.expand_sidebar" />
				{/if}
			</span>
			<IconEllipsisVertical class="-mr-1 w-5 h-5" />
		</button>
	{/if}
</div>
