<script>
	import SelectMenu from '$lib/ui/SelectMenu.svelte';
	import Switch from '$lib/ui/Switch.svelte';

	import { currentLocale, locales } from '$lib/app.mjs';
	import createConsole from '$lib/console-helper.mjs';
	import RangeInput from '$lib/ui/RangeInput.svelte';
	import Button from '$lib/ui/Button.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	const console = createConsole('Settings/Appearance');

	const MIN_ZOOM = 0.45;
	const MAX_ZOOM = 2.75;

	const ICONS = {
		casterlabs: 'Casterlabs',
		pride: 'Pride',
		moonlabs: 'Moonlabs',
		skittles: 'Skittles',
		handdrawn: 'Hand Drawn'
	};
	const EMOJI_PROVIDERS = {
		system: 'co.casterlabs.caffeinated.app.page.settings.appearance.emojis.SYSTEM',
		'noto-emoji': 'Noto Emoji',
		twemoji: 'Twemoji',
		openmoji: 'OpenMoji',
		'toss-face': 'Toss Face'
	};
	const APPEARANCES = {
		FOLLOW_SYSTEM:
			'co.casterlabs.caffeinated.app.page.settings.appearance.appearance.FOLLOW_SYSTEM',
		LIGHT: 'co.casterlabs.caffeinated.app.page.settings.appearance.appearance.LIGHT',
		DARK: 'co.casterlabs.caffeinated.app.page.settings.appearance.appearance.DARK'
	};
	// prettier-ignore
	const THEMES = {
		'mauve/crimson': 'Casterlabs',
		'bronze/gold':   'Bronze',
		'gray/gray':     'Noir',
		'red/teal':      'Cyberpunk',
		'pink/pink':     'Cute <3',
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

	const baseColor = st || window.svelte('Caffeinated.themeManager', 'baseColor');
	const primaryColor = st || window.svelte('Caffeinated.themeManager', 'primaryColor');
	const appearance = st || window.svelte('Caffeinated.themeManager', 'appearance');
	const preferences = st || window.svelte('Caffeinated.UI', 'preferences');

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
				title="co.casterlabs.caffeinated.app.page.settings.appearance.theme.base_color"
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
				title="co.casterlabs.caffeinated.app.page.settings.appearance.theme.primary_color"
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
				title="co.casterlabs.caffeinated.app.page.settings.appearance.theme"
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
			title="co.casterlabs.caffeinated.app.page.settings.appearance.appearance"
			value={$appearance}
			options={APPEARANCES}
			on:value={({ detail: value }) => {
				window.Caffeinated.themeManager.setTheme($baseColor, $primaryColor, value);
			}}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="co.casterlabs.caffeinated.app.page.settings.appearance.icon"
			value={$preferences?.icon}
			options={ICONS}
			on:value={({ detail: value }) => setPreferenceItem('icon', value)}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="co.casterlabs.caffeinated.app.page.settings.appearance.emojis"
			value={$preferences?.emojiProvider}
			options={EMOJI_PROVIDERS}
			on:value={({ detail: value }) => {
				setPreferenceItem('emojiProvider', value);
				location.reload();
			}}
		/>
	</li>
	<li class="py-4">
		<SelectMenu
			title="co.casterlabs.caffeinated.app.page.settings.appearance.language"
			value={$currentLocale}
			options={Object.fromEntries(Object.entries($locales).map(([k, v]) => [k, v.name]))}
			on:value={({ detail: value }) => {
				setPreferenceItem('language', value);
				location.reload();
			}}
		/>
	</li>
	<li class="py-4">
		<div class="flex items-center justify-between w-full">
			<div class="flex flex-col">
				<p class="text-sm font-medium text-base-12" id="zoominput-label">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.appearance.zoom" />
				</p>
				<p class="text-sm text-base-11" id="zoominput-description">
					<LocalizedText
						key="co.casterlabs.caffeinated.app.page.settings.appearance.zoom.description"
					/>
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
							<LocalizedText
								key="co.casterlabs.caffeinated.app.page.settings.appearance.zoom.reset"
							/>
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
	<li class="py-4">
		<Switch
			title="co.casterlabs.caffeinated.app.page.settings.appearance.close_to_tray"
			description="co.casterlabs.caffeinated.app.page.settings.appearance.close_to_tray.description"
			checked={$preferences?.closeToTray}
			on:value={({ detail: value }) => setPreferenceItem('closeToTray', value)}
		/>
	</li>
</ul>
