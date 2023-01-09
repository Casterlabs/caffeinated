<script>
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { page } from '$app/stores';

	const sections = [
		[
			{
				href: '/dashboard',
				name: 'page.dashboard',
				useStartsWith: false
			},
			{
				href: '/chat-bot/commands',
				name: 'page.chat_bot',
				useStartsWith: true
			}
		],
		[
			{
				href: '/widgets',
				name: 'page.widgets',
				useStartsWith: true
			},
			{
				href: '/docks',
				name: 'page.docks',
				useStartsWith: false
			},
			{
				href: '/settings/appearance',
				name: 'page.settings',
				useStartsWith: true
			}
		]
	];
</script>

<div class="flex-0 w-[var(--sidebar-width)] h-full flex flex-col">
	<div class="flex flex-grow flex-col overflow-y-auto bg-base-2 pb-4">
		<nav
			class="flex flex-1 flex-col divide-y divide-current text-base-6 overflow-y-auto"
			aria-label="Sidebar"
			role="listbox"
		>
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
