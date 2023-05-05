<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { page } from '$app/stores';
	import { onMount } from 'svelte';

	const sections = [
		[
			{
				href: '/$caffeinated-sdk-root$/dashboard',
				name: 'page.dashboard',
				useStartsWith: false
			},
			{
				href: '/$caffeinated-sdk-root$/chat-bot/commands',
				name: 'page.chat_bot',
				useStartsWith: true
			}
		],
		[
			{
				href: '/$caffeinated-sdk-root$/widgets',
				name: 'page.widgets',
				useStartsWith: true
			},
			{
				href: '/$caffeinated-sdk-root$/docks',
				name: 'page.docks',
				useStartsWith: false
			},
			{
				href: '/$caffeinated-sdk-root$/settings',
				name: 'page.settings',
				useStartsWith: true
			}
		]
	];

	let updateAvailable = false;

	onMount(() => {
		setInterval(async () => {
			updateAvailable = await Caffeinated.hasUpdate();
		}, 2 /*min*/ * 60 * 1000);
	});
</script>

<div class="flex-0 w-[var(--sidebar-width)] h-full flex flex-col">
	<div class="flex flex-grow flex-col overflow-y-auto bg-base-2 pb-4">
		<nav
			class="flex flex-1 flex-col divide-y divide-current text-base-6 overflow-y-auto"
			aria-label="Sidebar"
			role="listbox"
		>
			{#if updateAvailable}
				<div class="space-y-1 px-2 py-4" role="listitem">
					<button
						class="group flex flex-col items-center px-3 py-2 border-current font-medium rounded-md bg-primary-10 hover:bg-primary-11 text-base-1 hover:text-base-3 transition"
						on:click={() => Bridge.emit('app:restart')}
					>
						<h1 class="text-md">
							<LocalizedText key="sidebar.update_app" />
						</h1>
						<h2 class="text-xs">
							<LocalizedText key="sidebar.update_app.description" />
						</h2>
					</button>
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
	:root {
		--sidebar-width: 12rem;
	}
</style>
