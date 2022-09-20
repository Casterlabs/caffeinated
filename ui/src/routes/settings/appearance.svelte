<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';
	import Switch from '../../components/ui/Switch.svelte';

	import { supportedLanguages } from '$lib/translate.mjs';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Settings/Appearance');

	const ICONS = {
		casterlabs: 'Casterlabs',
		pride: 'Pride',
		moonlabs: 'Moonlabs',
		skittles: 'Skittles'
	};
	const EMOJI_PROVIDERS = {
		system: 'page.settings.appearance.emojis.SYSTEM',
		'noto-emoji': 'Noto Emoji',
		twemoji: 'Twemoji',
		openmoji: 'OpenMoji'
	};
	const THEMES = st || Caffeinated.themeManager.svelte('themes');

	const preferences = st || Caffeinated.UI.svelte('preferences');

	$: preferences, $preferences && console.debug('UI Preferences:', $preferences);

	async function setPreferenceItem(name, value) {
		Caffeinated.UI.updateAppearance({
			...(await Caffeinated.UI.preferences),
			[name]: value
		});
	}
</script>

<ul class="divide-y divide-current text-mauve-6">
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.theme"
			value={$preferences?.theme}
			options={Object.values($THEMES || {}) //
				.reduce(
					(obj, curr) => ({
						...obj,
						[curr.id]: `page.settings.appearance.theme.${curr.id}`
					}),
					{}
				)}
			on:value={({ detail: value }) => setPreferenceItem('theme', value)}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.icon"
			value={$preferences?.icon}
			options={ICONS}
			on:value={({ detail: value }) => setPreferenceItem('icon', value)}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.emojis"
			value={$preferences?.emojiProvider}
			options={EMOJI_PROVIDERS}
			on:value={({ detail: value }) => setPreferenceItem('emojiProvider', value)}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.language"
			value={$preferences?.language}
			options={supportedLanguages.reduce(
				(obj, curr) => ({
					...obj,
					[curr.code]: `${curr.name}`
				}),
				{}
			)}
			on:value={({ detail: value }) => setPreferenceItem('language', value)}
		/>
	</li>
	<li class="py-4">
		<Switch
			title="page.settings.appearance.close_to_tray"
			description="page.settings.appearance.close_to_tray.description"
			checked={$preferences?.closeToTray}
			on:value={({ detail: value }) => setPreferenceItem('closeToTray', value)}
		/>
	</li>
	<li class="py-4">
		<Switch
			title="page.settings.appearance.mikeys_mode"
			description="page.settings.appearance.mikeys_mode.description"
			checked={$preferences?.mikeysMode}
			on:value={({ detail: value }) => setPreferenceItem('mikeysMode', value)}
		/>
	</li>
</ul>
