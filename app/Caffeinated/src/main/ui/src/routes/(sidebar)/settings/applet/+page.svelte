<script>
	import CircularButton from '$lib/ui/CircularButton.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import Button from '$lib/ui/Button.svelte';
	import FormInput from '$lib/ui/FormInput.svelte';
	import WidgetPreview from '$lib/WidgetPreview.svelte';

	import createConsole from '$lib/console-helper.mjs';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount, tick } from 'svelte';

	const console = createConsole('Settings/Applet');

	let widget;

	let settingsLayout;
	let currentSection;
	$: hasTestEvents = widget?.details.testEvents.length > 0;

	onMount(() => {
		const id = $page.url.searchParams.get('id');

		Caffeinated.pluginIntegration.widgets
			.then((widgets) => {
				// Filter for a widget object with a matching id.
				// This'll return `undefined` if there's no matching result.
				return widgets.filter((w) => w.id == id)[0];
			})
			.then((w) => {
				// If the widget is `undefined`, go back.
				if (!w) {
					goto('/$caffeinated-sdk-root$/settings');
					return;
				}

				// All is good.
				widget = w;
				settingsLayout = widget.settingsLayout;
				currentSection = widget.settingsLayout.sections[0]?.id;
				console.debug('Widget data:', widget);
			});

		const eventListener = window.saucer.messages.onMessage(([type, data]) => {
			if (type == `widgets:${id}`) {
				widget = data;
				if (!deepEqual(widget.settingsLayout, settingsLayout)) {
					settingsLayout = widget.settingsLayout; // Re-render the UI.
				}
			}
		});
		return () => {
			window.saucer.messages.off(eventListener);
		};
	});

	function deepEqual(x, y) {
		if (x === y) {
			return true;
		} else if (typeof x == 'object' && x != null && typeof y == 'object' && y != null) {
			if (Object.keys(x).length != Object.keys(y).length) return false;

			for (var prop in x) {
				if (y.hasOwnProperty(prop)) {
					if (!deepEqual(x[prop], y[prop])) return false;
				} else return false;
			}

			return true;
		} else return false;
	}
</script>

{#if settingsLayout}
	<div class="fixed right-2.5 top-2.5">
		{#each settingsLayout?.buttons || [] as button}
			<CircularButton
				on:click={() =>
					window.Caffeinated.pluginIntegration.clickWidgetSettingsButton(widget.id, button.id)}
				title={button.iconTitle}
			>
				{#if button.text}
					<icon class="w-5 h-5 inline-block translate-y-0.5" data-icon="icon/{button.icon}" />
					<span class="text-sm inline-block -translate-y-0.5">{button.text}</span>
				{:else}
					<icon class="w-5 h-5" data-icon="icon/{button.icon}" />
				{/if}
			</CircularButton>
		{/each}
	</div>

	<div class="flex flex-col min-h-full">
		{#if (widget.settingsLayout?.sections || []).length > 1}
			<div class="border-b border-base-8 w-full">
				<nav class="mt-1 -mb-px flex justify-center space-x-8">
					{#each widget.settingsLayout?.sections || [] as section}
						{@const isSelected = currentSection == section.id}
						<button
							class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
							aria-current={isSelected ? 'page' : undefined}
							class:border-b-2={isSelected}
							class:text-primary-11={isSelected}
							on:click={() => {
								currentSection = null;

								// Svelte bug :(
								tick().then(() => (currentSection = section.id));
							}}
						>
							<LocalizedText key={section.name} />
						</button>
					{/each}

					{#if hasTestEvents}
						{@const isSelected = currentSection == '__INTERNAL_TEST'}
						<button
							class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
							aria-current={isSelected ? 'page' : undefined}
							class:border-b-2={isSelected}
							class:text-primary-11={isSelected}
							on:click={() => {
								currentSection = null;

								// Svelte bug :(
								tick().then(() => (currentSection = '__INTERNAL_TEST'));
							}}
						>
							<LocalizedText
								key="co.casterlabs.caffeinated.app.page.widget.editor.test_events.tab"
							/>
						</button>
					{/if}
				</nav>
			</div>
		{/if}

		<ul class="flex-1 block w-full max-w-sm mx-auto mt-2 divide-y divide-current text-base-6">
			{#if currentSection == '__INTERNAL_TEST'}
				{#each widget.details.testEvents as eventType}
					<li class="py-4">
						<span class="text-base-12">
							<div class="flex items-center justify-between w-full">
								<div class="flex flex-col">
									<p class="text-sm font-medium text-base-12">
										<!-- Try to convert the enum to a friendlier name. -->
										{(eventType.substring(0, 1) + eventType.substring(1).toLowerCase()).replace(
											'_',
											' '
										)}
									</p>
								</div>

								<div class="text-right w-40">
									<Button
										on:click={() =>
											window.Caffeinated.pluginIntegration.fireTestEvent(widget.id, eventType)}
									>
										<LocalizedText
											key="co.casterlabs.caffeinated.app.page.widget.editor.test_events.send_test"
										/>
									</Button>
								</div>
							</div>
						</span>
					</li>
				{/each}
			{/if}

			{#key settingsLayout}
				{#each widget.settingsLayout?.sections || [] as section}
					{#if currentSection == section.id}
						{#each section?.items || [] as item}
							<li class="py-4">
								<span class="text-base-12">
									<FormInput {widget} widgetSettingsSection={section} widgetSettingsItem={item} />
								</span>
							</li>
						{/each}
					{/if}
				{/each}
			{/key}
		</ul>
	</div>

	{#if widget.details.showDemo}
		<WidgetPreview {widget} mode="DEMO" ariaHidden={true} />
	{/if}
{:else if widget}
	<iframe
		class="w-full h-full"
		title=""
		src={widget.url.replace(/&mode=\w+/, `&mode=SETTINGS_APPLET`)}
	/>
{/if}
