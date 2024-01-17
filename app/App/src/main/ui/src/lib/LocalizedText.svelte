<script>
	import createConsole from '$lib/console-helper.mjs';
	import { t, matchAndReturnEmojiHTML } from '$lib/app.mjs';
	import { tick } from 'svelte';

	const console = createConsole('LocalizedText');

	export let key;

	export let opts = {};
	export let slotMapping = [];
	let slotContents = {};

	let contentHash;
	let contents = [];

	async function render() {
		const hash = JSON.stringify({ key, opts, slotMapping });
		if (hash == contentHash) {
			// Avoid re-render.
			return;
		}

		const result = await t(key, opts, slotMapping);
		let newContents = result ? result.split(/(%\w+%)/g) : []; // Split on %placeholders%.

		// Process slot placeholders.
		for (const [index, value] of newContents.entries()) {
			if (value.startsWith('%')) {
				const slotName = value.slice(1, -1);
				const slotId = 'item' + slotMapping.indexOf(slotName);

				newContents[index] = [slotContents[slotId], null];
			}
		}

		// Look for emojis and replace them.
		let emojiMatchPromises = [];
		for (const content of newContents) {
			if (typeof content == 'string' && typeof window != 'undefined') {
				emojiMatchPromises.push(matchAndReturnEmojiHTML(content));
			} else {
				emojiMatchPromises.push(Promise.resolve(content));
			}
		}

		console.debug('Localized with slots:', key, slotMapping, slotContents, result, newContents);

		// Update this instance.
		contents = await Promise.all(emojiMatchPromises);
		contentHash = hash;

		// Wait for the DOM to update after we set `contents`.
		await tick();

		// Mount the slots to the contents.
		for (const content of contents) {
			if (typeof content == 'string') continue;

			const [slotContent, domElement] = content;
			if (!slotContent || !domElement) continue;
			domElement.appendChild(slotContent);
		}
	}

	// Rerender on change
	$: key, render();
	$: opts, render();
	$: slotContents, render();
</script>

<div style="display: none;" aria-hidden="true">
	<!-- TODO Add more as needed. -->
	<span bind:this={slotContents.item0}><slot name="0" /></span>
	<span bind:this={slotContents.item1}><slot name="1" /></span>
	<span bind:this={slotContents.item2}><slot name="2" /></span>
	<span bind:this={slotContents.item3}><slot name="3" /></span>
	<span bind:this={slotContents.item4}><slot name="4" /></span>
	<span bind:this={slotContents.item5}><slot name="5" /></span>
	<span bind:this={slotContents.item6}><slot name="6" /></span>
	<span bind:this={slotContents.item7}><slot name="7" /></span>
	<span bind:this={slotContents.item8}><slot name="8" /></span>
	<span bind:this={slotContents.item9}><slot name="9" /></span>
</div>

<!-- This looks gross, but it's necessary to remove spaces -->
<span style="display: contents;"
	>{#each contents as c}{#if typeof c == 'string'}{@html c}
		{:else}<span bind:this={c[1]} style="display: contents;" />{/if}{/each}</span
>
