<script>
	import createConsole from '$lib/console-helper.mjs';
	import { language } from '$lib/app.mjs';
	import { translate } from '$lib/translate.mjs';
	import { tick } from 'svelte';

	const console = createConsole('LocalizedText');

	const preferences = st || Caffeinated.UI.svelte('preferences');

	export let opts = {};
	export let key;

	export let slotMapping = [];
	let slotContents = {};

	let contents = [];

	async function render(lang) {
		if (!lang) return;

		const { result, usedFallback } = translate(lang, key, opts, false);
		let newContents;

		if (usedFallback) {
			newContents = [
				`<span style="background: red" title="NO TRANSLATION: ${key}">${result}</span>`
			];
		} else if (!result) {
			newContents = [];
		} else {
			newContents = result.split(/(%\w+%)/g);
		}

		for (const [index, value] of newContents.entries()) {
			if (value.startsWith('%')) {
				const slotName = value.slice(1, -1);
				const slotId = 'item' + slotMapping.indexOf(slotName);

				newContents[index] = [slotContents[slotId], null];
			}
		}

		if (slotMapping.length > 0) {
			console.debug('Localized with slots:', key, slotMapping, slotContents, result, newContents);
		}

		let emojiMatchPromises = [];
		for (const content of newContents) {
			if (typeof content == 'string' && typeof window != 'undefined') {
				emojiMatchPromises.push(Caffeinated.emojis.matchAndReturnHTML(content, false));
			} else {
				emojiMatchPromises.push(Promise.resolve(content));
			}
		}

		contents = await Promise.all(emojiMatchPromises);

		await tick();

		for (const content of contents) {
			if (typeof content == 'string') continue;

			const [slotContent, domElement] = content;
			if (!slotContent || !domElement) continue;
			domElement.appendChild(slotContent);
		}
	}

	// Rerender on change
	$: key, render($language);
	$: opts, render($language);
	$: slotContents, render($language);
	$: $preferences, render($language);
	language.subscribe(render);
</script>

<div style="display: none;" aria-hidden="true">
	<!-- We need 10 of these -->
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

<span>
	{#each contents as c}
		{#if typeof c == 'string'}
			{@html c}
		{:else}
			<span bind:this={c[1]} />
		{/if}
	{/each}
</span>
