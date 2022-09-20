<script>
	import SelectMenu from '../../components/ui/SelectMenu.svelte';

	const currentTheme = st || Caffeinated.themeManager.svelte('currentTheme');
	const themes = st || Caffeinated.themeManager.svelte('themes');
</script>

<ul class="divide-y divide-gray-200">
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
		<div class="flex flex-col">
			<p class="text-sm font-medium text-gray-900" id="privacy-option-3-label">
				Close button sends to tray
			</p>
			<p class="text-sm text-gray-500" id="privacy-option-3-description">
				Makes Caffeinated cozy up in the background when you close it.
			</p>
		</div>
		<!-- Enabled: "bg-teal-500", Not Enabled: "bg-gray-200" -->
		<button
			type="button"
			class="bg-gray-200 relative ml-4 inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-sky-500 focus:ring-offset-2"
			role="switch"
			aria-checked="true"
			aria-labelledby="privacy-option-3-label"
			aria-describedby="privacy-option-3-description"
		>
			<!-- Enabled: "translate-x-5", Not Enabled: "translate-x-0" -->
			<span
				aria-hidden="true"
				class="translate-x-0 inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
			/>
		</button>
	</li>
	<li class="flex items-center justify-between py-4">
		<div class="flex flex-col">
			<p class="text-sm font-medium text-gray-900" id="privacy-option-4-label">
				<a href="https://twitter.com/Casterlabs/status/1508475284944736268" target="_blank">
					Mikey's Mode
				</a>
			</p>
			<p class="text-sm text-gray-500" id="privacy-option-4-description">
				For those of you who need more time to cook your pockets :^)
			</p>
		</div>
		<!-- Enabled: "bg-teal-500", Not Enabled: "bg-gray-200" -->
		<button
			type="button"
			class="bg-gray-200 relative ml-4 inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-sky-500 focus:ring-offset-2"
			role="switch"
			aria-checked="true"
			aria-labelledby="privacy-option-4-label"
			aria-describedby="privacy-option-4-description"
		>
			<!-- Enabled: "translate-x-5", Not Enabled: "translate-x-0" -->
			<span
				aria-hidden="true"
				class="translate-x-0 inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
			/>
		</button>
	</li>
</ul>
