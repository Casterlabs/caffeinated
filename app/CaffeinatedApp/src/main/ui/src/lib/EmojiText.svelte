<script>
	import createConsole from '$lib/console-helper.mjs';
	import { emojiProvider, matchAndReturnEmojiHTML } from '$lib/app.mjs';

	const console = createConsole('EmojiText');

	export let sequence;
	let text = sequence;

	async function render() {
		text = await matchAndReturnEmojiHTML(content);
		console.debug('Found emoji:', text);
	}

	// Rerender on change
	$: sequence, render();
	$: $emojiProvider, render();
</script>

{text}
