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
				title="page.settings.appearance.theme"
				value={$currentTheme?.id}
				options={Object.values($themes || {}) //
					.reduce(
						(obj, curr) => ({
							...obj,
							[curr.id]: `page.settings.appearance.theme.${curr.name}`
						}),
						{}
					)}
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
			title="page.settings.appearance.close_button_tray"
			description="page.settings.appearance.close_button_tray.description"
			checked={true}
		/>
	</li>
	<li class="flex items-center justify-between py-4">
		<Switch
			title="page.settings.appearance.mikeys_mode"
			description="page.settings.appearance.mikeys_mode.description"
			checked={true}
		/>
	</li>
</ul>
