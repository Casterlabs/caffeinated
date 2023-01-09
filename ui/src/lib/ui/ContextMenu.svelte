<script>
	import LocalizedText from '../LocalizedText.svelte';
	import FocusListener from '../interaction/FocusListener.svelte';
	import LongPressListener from '../interaction/LongPressListener.svelte';
	import Portal from 'svelte-portal';

	import { tick } from 'svelte';

	let contextMenuOpen = false;
	let contextMenuCoords = [-1000, -1000];
	let contextMenuElement = null;

	$: contextMenuOpen,
		(() => {
			if (contextMenuOpen) return;
			contextMenuCoords = [-1000, -1000];
		})();

	async function doOpen(x, y) {
		if (items.length == 0) return; // Ignore.

		// We need to wait for Svelte to mount our component, because we need it's size.
		// We also don't want to keep the context menu mounted constantly, that'd be a waste.
		// For example, individual chat messages create their own context menu, which'll lead to memory-related suffering.
		contextMenuOpen = true;
		await tick();

		const { clientWidth: documentWidth, clientHeight: documentHeight } = document.documentElement;
		const { clientWidth: menuWidth, clientHeight: menuHeight } = contextMenuElement;

		// Clamp top-left.
		x = Math.max(x, 0);
		y = Math.max(y, 0);

		// Clamp bottom-right.
		x = Math.min(x, documentWidth - menuWidth);
		y = Math.min(y, documentHeight - menuHeight);

		contextMenuCoords = [x, y];
		contextMenuOpen = true;
		contextMenuElement.focus();
		console.debug('Opening context menu at:', contextMenuCoords);
	}

	export let items = [
		{
			type: 'button',
			icon: 'icon/clock',
			text: 'Timeout User',
			onclick() {
				alert('Timeout!');
			}
		},
		{ type: 'divider' },
		{
			type: 'button',
			icon: 'icon/ellipsis-horizontal',
			text: 'No Action',
			color: 'error',
			onclick() {
				return false;
			}
		}
	];
</script>

<LongPressListener
	on:long-press={({ detail }) => {
		const { clientX: x, clientY: y } = detail;
		doOpen(x, y);
	}}
>
	<div
		class="contents"
		on:contextmenu={(e) => {
			e.preventDefault();
			const { clientX: x, clientY: y } = e;
			doOpen(x, y);
		}}
	>
		<slot />
	</div>
</LongPressListener>

{#if contextMenuOpen}
	<Portal target="#context-menu">
		<FocusListener
			on:lostfocus={() => (contextMenuOpen = false)}
			ignoreFocusState={true}
			class="absolute z-[45]"
			style="top: {contextMenuCoords[1]}px; left: {contextMenuCoords[0]}px;"
		>
			<ul
				class="rounded context-menu overflow-hidden py-1 border border-base-5 bg-base-1 text-base-12 shadow-lg"
				style="outline: none;"
				role="menu"
				tabindex="-1"
				aria-hidden={!contextMenuOpen}
				bind:this={contextMenuElement}
				on:keydown={(e) => {
					if (e.code == 'Escape') {
						contextMenuOpen = false;
					}
				}}
			>
				{#each items as item}
					{#if item.hidden}
						<!-- BLANK -->
					{:else if item.type == 'button'}
						{@const { icon, text, onclick, color } = item}

						<li>
							<button
								role="menuitem"
								class="w-full text-left pl-2 pr-2.5 h-8 text-{color} hover:bg-primary-9 hover:text-base-12 flex flex-row items-center"
								on:click={async () => {
									// You can return 'false' to keep the context menu open.
									// Any other value closes it.
									if ((await onclick()) !== false) {
										contextMenuOpen = false;
									}
								}}
							>
								<icon class="mr-1 w-5 h-5" data-icon={icon} />
								<LocalizedText key={text} />
							</button>
						</li>
					{:else if item.type == 'divider'}
						<!-- svelte-ignore a11y-no-redundant-roles -->
						<hr role="separator" class="my-1 border-none h-px bg-base-5" />
					{/if}
				{/each}
			</ul>
		</FocusListener>
	</Portal>
{/if}
