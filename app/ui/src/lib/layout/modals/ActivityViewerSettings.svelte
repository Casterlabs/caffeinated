<script lang="ts">
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import Modal from '../Modal.svelte';
	import { Input, Select } from '@casterlabs/ui';

	import { type Snippet } from 'svelte';

	type PrefsType = Awaited<(typeof AppConfig)['uiPreferences']>['activityViewerPreferences'];

	interface Props {
		initialPrefs: PrefsType;
		onupdate: (newPrefs: PrefsType) => void;
		onclose: () => void;
	}

	let { initialPrefs, onupdate, onclose }: Props = $props();
</script>

{#snippet prefsItem(title: string, control: Snippet)}
	<li class="py-3">
		<label class="flex items-center justify-between w-full">
			<div class="flex flex-col">
				<p class="text-sm font-medium text-base-12">
					<LocalizedText key={title} />
				</p>
			</div>

			{@render control()}
		</label>
	</li>
{/snippet}

{#snippet prefsItemSwitch(title: string, property: keyof PrefsType)}
	{#snippet control()}
		<Input
			type="checkbox"
			checked={initialPrefs[property] as boolean}
			onchange={(e) => {
				const value = (e.target as HTMLInputElement).checked;
				onupdate({ ...initialPrefs, [property]: value } as PrefsType);
			}}
		/>
	{/snippet}

	{@render prefsItem(title, control)}
{/snippet}

{#snippet prefsItemSelect(title: string, property: keyof PrefsType, options: Record<string, string>)}
	{#snippet control()}
		<Select
			value={initialPrefs[property] as string}
			onchange={(e) => {
				const value = (e.target as HTMLSelectElement).value;
				onupdate({ ...initialPrefs, [property]: value } as PrefsType);
			}}
		>
			{#each Object.entries(options) as [key, label]}
				<option value={key}>
					<LocalizedText key={label} />
				</option>
			{/each}
		</Select>
	{/snippet}

	{@render prefsItem(title, control)}
{/snippet}

{#snippet title()}
	<div class="flex items-center justify-center">
		<LocalizedText key="co.casterlabs.caffeinated.app.docks.activity_feed.preferences.title" />
	</div>
{/snippet}

<Modal {title} {onclose}>
	<ul class="divide-y divide-current text-base-6">
		{@render prefsItemSelect('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by', 'colorBy', {
			THEME: 'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.THEME',
			USER: 'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.USER',
			PLATFORM: 'co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.color_users_by.PLATFORM'
		})}

		{#snippet textSizeControl()}
			<Input
				type="range"
				min={0.1}
				max={2}
				step={0.01}
				value={initialPrefs.textSize as number}
				onchange={(e) => {
					const value = (e.target as HTMLInputElement).valueAsNumber;
					onupdate({ ...initialPrefs, textSize: value } as PrefsType);
				}}
			/>
		{/snippet}
		{@render prefsItem('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.text_size', textSizeControl)}

		{@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_chat_timestamps', 'showTimestamps')}

		{@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_profile_pictures', 'showProfilePictures')}

		{@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_badges', 'showBadges')}

		{@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_platform', 'showPlatform')}

		<!-- {@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_pronouns', 'showPronouns')} -->

		{@render prefsItemSwitch('co.casterlabs.caffeinated.app.docks.chat.viewer.preferences.show_zebra_stripes', 'showZebraStripes')}
	</ul>
</Modal>
