<script>
	import LocalizedText from '../../components/LocalizedText.svelte';

	const buildInfo = st || Caffeinated.svelte('buildInfo');
	const preferences = st || Caffeinated.UI.svelte('preferences');
	const effectiveTheme = st || Caffeinated.themeManager.svelte('effectiveTheme');

	$: iconColor = $effectiveTheme?.appearance == 'LIGHT' ? 'black' : 'white';

	async function setPreferenceItem(name, value) {
		Caffeinated.UI.updateAppearance({
			...(await Caffeinated.UI.preferences),
			[name]: value
		});
	}
</script>

<ul class="bg-mauve-2 shadow rounded-md border border-mauve-6 divide-y">
	<li class="py-4 flex flex-row space-x-8">
		<div class="w-50 flex-1 flex items-center">
			<img
				src="/images/wordmark/{$preferences?.icon || 'casterlabs'}/{iconColor}.svg"
				class="h-auto w-full"
				alt="Casterlabs"
			/>
		</div>
		<div class="w-50 flex-1 flex flex-col items-left justify-center text-mauve-11">
			<p>Casterlabs-Caffeinated</p>
			<p>
				{#if $buildInfo?.isDev}
					Developer Build
				{:else}
					Version {$buildInfo?.version}-{$buildInfo?.commit} ({$buildInfo?.buildChannel})
				{/if}
			</p>
		</div>
	</li>
	<li class="px-2 py-4 text-mauve-11">
		<LocalizedText key="app.love" />
		<br />
		<LocalizedText key="app.copyright" opts={{ year: new Date().getFullYear() }} />

		<br />
		<br />
		<label class="checkbox select-none cursor-pointer hover:text-mauve-10">
			<LocalizedText key="page.settings.about.enable_unsafe" />
			<input
				type="checkbox"
				checked={$preferences?.enableStupidlyUnsafeSettings}
				on:change={({ target }) => {
					const { checked } = target;
					setPreferenceItem('enableStupidlyUnsafeSettings', checked);
				}}
			/>
		</label>
	</li>
</ul>
