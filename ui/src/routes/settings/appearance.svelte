<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';
	import Switch from '../../components/ui/Switch.svelte';

	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Settings/Appearance');

	const preferences = st || Caffeinated.UI.svelte('preferences');
	const themes = st || Caffeinated.themeManager.svelte('themes');

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
			options={Object.values($themes || {}) //
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
