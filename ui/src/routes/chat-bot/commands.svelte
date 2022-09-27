<script>
	import LocalizedText from '../../components/LocalizedText.svelte';

	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Chat Bot/Commands');
	const preferences = st || Caffeinated.chatbot.svelte('preferences');

	$: preferences, $preferences && console.debug('Chat Bot Preferences:', $preferences);

	async function setPreferenceItem(name, value) {
		Caffeinated.chatbot.preferences = {
			...(await Caffeinated.chatbot.preferences),
			[name]: value
		};
	}
</script>

// TODO Chat Bot/Commands
