<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';
	import Switch from '$lib/ui/Switch.svelte';

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
	const APPEARANCES = {
		FOLLOW_SYSTEM: 'page.settings.appearance.appearance.FOLLOW_SYSTEM',
		LIGHT: 'page.settings.appearance.appearance.LIGHT',
		DARK: 'page.settings.appearance.appearance.DARK'
	};
	// prettier-ignore
	const THEMES = {
		'mauve/crimson': 'Casterlabs',
		'bronze/gold':   'Bronze',
		'gray/gray':     'Noir'
	};
	// prettier-ignore
	const PAC_THEMES = {
		'mauve/red':   '赤ベイ',
		'mauve/pink':  'ピンキー',
		'slate/blue':  '青助',
		'sand/orange': '愚図た',
		'sand/yellow': 'パックマン'
	};

	const baseColor = st || Caffeinated.themeManager.svelte('baseColor');
	const primaryColor = st || Caffeinated.themeManager.svelte('primaryColor');
	const appearance = st || Caffeinated.themeManager.svelte('appearance');
	const preferences = st || Caffeinated.UI.svelte('preferences');

	$: preferences, $preferences && console.debug('UI Preferences:', $preferences);

	async function setPreferenceItem(name, value) {
		Caffeinated.UI.updateAppearance({
			...(await Caffeinated.UI.preferences),
			[name]: value
		});
	}
</script>

<ul class="divide-y divide-current text-base-6">
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.theme"
			value="{$baseColor}/{$primaryColor}"
			options={$preferences?.enableAlternateThemes ? PAC_THEMES : THEMES}
			on:value={({ detail: value }) => {
				const [newBaseColor, newPrimaryColor] = value.split('/');
				window.Caffeinated.themeManager.setTheme(newBaseColor, newPrimaryColor, $appearance);
			}}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="page.settings.appearance.appearance"
			value={$appearance}
			options={APPEARANCES}
			on:value={({ detail: value }) => {
				window.Caffeinated.themeManager.setTheme($baseColor, $primaryColor, value);
			}}
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
	<!-- <li class="py-4">
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
	</li> -->
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
