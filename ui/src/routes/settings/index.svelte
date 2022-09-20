<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';
	import Switch from '../../components/ui/Switch.svelte';

	const currentTheme = st || Caffeinated.themeManager.svelte('currentTheme');
	const themes = st || Caffeinated.themeManager.svelte('themes');
</script>

<ul class="divide-y divide-current text-mauve-6">
	<li class="flex items-center justify-between py-4">
		<div class="col-span-6 w-full">
			<SelectMenu
				title="Theme"
				value={$currentTheme?.id}
				options={Object.values($themes || {}) //
					.reduce((obj, curr) => ({ ...obj, [curr.id]: curr.name }), {})}
				on:value={async ({ detail: newTheme }) => {
					window.Caffeinated.UI.updateAppearance({
						...(await window.Caffeinated.UI.preferences),
						theme: newTheme
					});
				}}
			/>
		</div>
	</li>
	<li class="flex items-center justify-between py-4">
		<Switch
			title="Close button sends to tray"
			description="Makes Caffeinated cozy up in the background when you close it."
			checked={true}
		/>
	</li>
	<li class="flex items-center justify-between py-4">
		<Switch
			title="Mikey's Mode"
			description={`
				<a
					class="text-link"
					href="https://twitter.com/Casterlabs/status/1508475284944736268"
					target="_blank"
				>
					For those of you who need more time to cook your pockets :^)
				</a>
			`}
			checked={true}
		/>
	</li>
</ul>
