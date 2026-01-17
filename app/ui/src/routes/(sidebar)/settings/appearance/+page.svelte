<script lang="ts">
	import { modify, storify } from '$lib/bridge-helper';

	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { IconArrowPath } from '@casterlabs/heroicons-svelte';
	import { Button, Input, Select } from '@casterlabs/ui';

	import { type Snippet } from 'svelte';

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
		FOLLOW_SYSTEM: 'co.casterlabs.caffeinated.app.page.settings.appearance.appearance.FOLLOW_SYSTEM',
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

	const uiPreferences = storify(AppConfig, 'uiPreferences').readable<Awaited<typeof AppConfig.uiPreferences>>();
	const themePreferences = storify(AppConfig, 'themePreferences').readable<Awaited<typeof AppConfig.themePreferences>>();
</script>

{#snippet item(langTitle: string, child: Snippet)}
	<label>
		<div class="flex flex-col">
			<p class="text-sm font-medium text-base-12" id="zoominput-label">
				<LocalizedText key={langTitle} />
			</p>
		</div>

		<div class="mt-2">
			{@render child()}
		</div>
	</label>
{/snippet}

{#snippet itemWithDesc(langTitle: string, langDescription: string, child: Snippet)}
	<label class="flex items-center justify-between w-full">
		<div class="flex flex-col">
			<p class="text-sm font-medium text-base-12" id="zoominput-label">
				<LocalizedText key={langTitle} />
			</p>
			<p class="text-sm text-base-11" id="zoominput-description">
				<LocalizedText key={langDescription} />
			</p>
		</div>

		<div class="flex items-center justify-between space-x-1">
			{@render child()}
		</div>
	</label>
{/snippet}

<ul class="divide-y divide-current text-base-6 max-w-sm mx-auto">
	{#if $uiPreferences?.enableAlternateThemes}
		<li class="py-4">
			{#snippet prefsBaseColor()}
				<Select
					class="w-full"
					onchange={(e) => {
						const value = (e.target as HTMLSelectElement).value;
						modify(AppConfig, 'themePreferences', 'baseColor', value);
					}}
				>
					{#each THEME_COLORS as color}
						<option value={color.toLowerCase()} selected={color.toLowerCase() === $themePreferences?.baseColor}>
							{color}
						</option>
					{/each}
				</Select>
			{/snippet}
			{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.base_color', prefsBaseColor)}
		</li>

		<li class="py-4">
			{#snippet prefsPrimaryColor()}
				<Select
					class="w-full"
					onchange={(e) => {
						const value = (e.target as HTMLSelectElement).value;
						modify(AppConfig, 'themePreferences', 'primaryColor', value);
					}}
				>
					{#each THEME_COLORS as color}
						<option value={color.toLowerCase()} selected={color.toLowerCase() === $themePreferences?.primaryColor}>
							{color}
						</option>
					{/each}
				</Select>
			{/snippet}
			{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.primary_color', prefsPrimaryColor)}
		</li>
	{:else}
		<li class="py-4">
			{#snippet prefsTheme()}
				<Select
					class="w-full"
					onchange={(e) => {
						const [newBaseColor, newPrimaryColor] = (e.target as HTMLSelectElement).value.split('/');
						modify(AppConfig, 'themePreferences', 'baseColor', newBaseColor);
						modify(AppConfig, 'themePreferences', 'primaryColor', newPrimaryColor);
					}}
				>
					{#each Object.entries(THEMES) as [k, v]}
						<option value={k} selected={k === `${$themePreferences?.baseColor}/${$themePreferences?.primaryColor}`}>
							{v}
						</option>
					{/each}
				</Select>
			{/snippet}
			{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.theme', prefsTheme)}
		</li>
	{/if}

	<li class="py-4">
		{#snippet prefsAppearance()}
			<Select
				class="w-full"
				onchange={(e) => {
					const value = (e.target as HTMLSelectElement).value;
					modify(AppConfig, 'themePreferences', 'appearance', value);
				}}
			>
				{#each Object.entries(APPEARANCES) as [k, v]}
					<option value={k} selected={k === $themePreferences?.appearance}>
						<LocalizedText key={v} />
					</option>
				{/each}
			</Select>
		{/snippet}
		{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.appearance', prefsAppearance)}
	</li>

	<li class="py-4">
		{#snippet prefsIcon()}
			<Select
				class="w-full"
				onchange={(e) => {
					const value = (e.target as HTMLSelectElement).value;
					modify(AppConfig, 'uiPreferences', 'icon', value);
				}}
			>
				{#each Object.entries(ICONS) as [k, v]}
					<option value={k} selected={k === $uiPreferences?.icon}>
						{v}
					</option>
				{/each}
			</Select>
		{/snippet}
		{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.icon', prefsIcon)}
	</li>

	<li class="py-4">
		{#snippet prefsEmojiProvider()}
			<Select
				class="w-full"
				onchange={(e) => {
					const value = (e.target as HTMLSelectElement).value;
					modify(AppConfig, 'uiPreferences', 'emojiProvider', value);
				}}
			>
				{#each Object.entries(EMOJI_PROVIDERS) as [k, v]}
					<option value={k} selected={k === $uiPreferences?.emojiProvider}>
						<LocalizedText key={v} />
					</option>
				{/each}
			</Select>
		{/snippet}
		{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.emojis', prefsEmojiProvider)}
	</li>

	<li class="py-4">
		{#await AppLocale.available then locales}
			{#snippet prefsLocale()}
				<Select
					class="w-full"
					onchange={(e) => {
						const value = parseFloat((e.target as HTMLSelectElement).value);
						modify(AppConfig, 'uiPreferences', 'language', value);
					}}
				>
					{#each Object.entries(locales) as [k, v]}
						<option value={k} selected={k === $uiPreferences?.language}>{v}</option>
					{/each}
				</Select>
			{/snippet}
			{@render item('co.casterlabs.caffeinated.app.page.settings.appearance.language', prefsLocale)}
		{/await}
	</li>

	<li class="py-4">
		{#snippet prefsZoom()}
			{#if $uiPreferences?.zoom !== 1}
				<Button
					onclick={() => {
						modify(AppConfig, 'uiPreferences', 'zoom', 1);
					}}
				>
					<IconArrowPath class="w-4 h-full" theme="solid" />
					<span class="sr-only">
						<LocalizedText key="co.casterlabs.caffeinated.app.page.settings.appearance.zoom.reset" />
					</span>
				</Button>
			{/if}
			<Input
				type="range"
				min={MIN_ZOOM}
				max={MAX_ZOOM}
				step=".1"
				value={$uiPreferences?.zoom}
				onchange={(e) => {
					const value = parseFloat((e.target as HTMLInputElement).value);
					modify(AppConfig, 'uiPreferences', 'zoom', value);
				}}
			/>
		{/snippet}
		{@render itemWithDesc('co.casterlabs.caffeinated.app.page.settings.appearance.zoom', 'co.casterlabs.caffeinated.app.page.settings.appearance.zoom.description', prefsZoom)}
	</li>

	<li class="py-4">
		{#snippet prefsCloseToTray()}
			<Input
				type="checkbox"
				checked={$uiPreferences?.closeToTray}
				onchange={(e) => {
					const value = (e.target as HTMLInputElement).checked;
					modify(AppConfig, 'uiPreferences', 'closeToTray', value);
				}}
			/>
		{/snippet}
		{@render itemWithDesc(
			'co.casterlabs.caffeinated.app.page.settings.appearance.close_to_tray',
			'co.casterlabs.caffeinated.app.page.settings.appearance.close_to_tray.description',
			prefsCloseToTray
		)}
	</li>
</ul>
