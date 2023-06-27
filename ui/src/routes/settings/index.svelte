<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';
	import Switch from '$lib/ui/Switch.svelte';

	import { supportedLanguages } from '$lib/translate.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import RangeInput from '$lib/ui/RangeInput.svelte';
	import Button from '$lib/ui/Button.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	const console = createConsole('Settings/Appearance');

	const MIN_ZOOM = 0.45;
	const MAX_ZOOM = 1.5;

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
		openmoji: 'OpenMoji',
		'toss-face': 'Toss Face'
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

	const THEME_COLORS = [
		'Gray',
		'Mauve',
		'Slate',
		'Sage',
		'Olive',
		'Sand',
		'Tomato',
		'Red',
		'Crimson',
		'Pink',
		'Plum',
		'Purple',
		'Violet',
		'Indigo',
		'Blue',
		'Cyan',
		'Teal',
		'Green',
		'Grass',
		'Brown',
		'Orange',
		'Sky',
		'Mint',
		'Lime',
		'Yellow',
		'Amber',
		'Gold',
		'Bronze'
	];

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
	{#if $preferences?.enableAlternateThemes}
		<li class="py-4">
			<SelectMenu
				title="page.settings.appearance.theme.base_color"
				value={$baseColor}
				options={THEME_COLORS.reduce(
					(arr, color) => ({ ...arr, [color.toLowerCase()]: color }),
					{}
				)}
				on:value={({ detail: value }) => {
					window.Caffeinated.themeManager.setTheme(value, $primaryColor, $appearance);
				}}
			/>
		</li>
		<li class="py-4">
			<SelectMenu
				title="page.settings.appearance.theme.primary_color"
				value={$primaryColor}
				options={THEME_COLORS.reduce(
					(arr, color) => ({ ...arr, [color.toLowerCase()]: color }),
					{}
				)}
				on:value={({ detail: value }) => {
					window.Caffeinated.themeManager.setTheme($baseColor, value, $appearance);
				}}
			/>
		</li>
	{:else}
		<li class="py-4">
			<SelectMenu
				title="page.settings.appearance.theme"
				value="{$baseColor}/{$primaryColor}"
				options={THEMES}
				on:value={({ detail: value }) => {
					const [newBaseColor, newPrimaryColor] = value.split('/');
					window.Caffeinated.themeManager.setTheme(newBaseColor, newPrimaryColor, $appearance);
				}}
			/>
		</li>
	{/if}
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
	<li class="py-4">
		<div class="flex items-center justify-between w-full">
			<div class="flex flex-col">
				<p class="text-sm font-medium text-base-12" id="zoominput-label">
					<LocalizedText key="page.settings.appearance.zoom" />
				</p>
				<p class="text-sm text-base-11" id="zoominput-description">
					<LocalizedText key="page.settings.appearance.zoom.description" />
				</p>
			</div>

			<span aria-labelledby="zoominput-label" aria-describedby="zoominput-description">
				<span
					class="inline-block text-base-12 translate-y-1.5 mr-1.5"
					class:opacity-0={$preferences?.zoom == 1}
				>
					<Button on:click={() => setPreferenceItem('zoom', 1)}>
						<icon class="w-4 h-full" data-icon="icon/x-mark" />
						<span class="sr-only">
							<LocalizedText key="sr.page.settings.appearance.zoom.reset" />
						</span>
					</Button>
				</span>
				<RangeInput
					min={MIN_ZOOM}
					max={MAX_ZOOM}
					step=".1"
					value={$preferences?.zoom}
					on:value={({ detail: value }) => setPreferenceItem('zoom', value)}
				/>
			</span>
		</div>
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
</ul>
