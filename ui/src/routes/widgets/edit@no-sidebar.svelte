<script>
	import WidgetPreview from '$lib/WidgetPreview.svelte';
	import CircularButton from '$lib/ui/CircularButton.svelte';
	import FormInput from '$lib/ui/FormInput.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';
	import AspectVar from '$lib/aspect-ratio/AspectVar.svelte';

	import { t } from '$lib/translate.mjs';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount, tick } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Widget Editor');

	let widget;
	let settingsLayout;

	let nameEditorElement;
	let nameEditorTextContent;

	let currentSection;

	function editName() {
		Caffeinated.plugins.renameWidget(widget.id, nameEditorTextContent);
	}

	onMount(() => {
		const id = $page.url.searchParams.get('id');

		Caffeinated.plugins.widgets
			.then((widgets) => {
				// Filter for a widget object with a matching id.
				// This'll return `undefined` if there's no matching result.
				return widgets.filter((w) => w.id == id)[0];
			})
			.then((w) => {
				// If the widget is `undefined`, go back.
				if (!w) {
					goto('/widgets');
					return;
				}

				// All is good.
				widget = w;
				settingsLayout = widget.settingsLayout;
				nameEditorTextContent = widget.name;
				currentSection = widget.settingsLayout.sections[0]?.id;
				console.debug('Widget data:', widget);
			});

		const eventListener = Bridge.on(`widgets:${id}`, (w) => {
			widget = w;

			if (!deepEqual(widget.settingsLayout, settingsLayout)) {
				settingsLayout = widget.settingsLayout; // Re-render the UI.
			}
		});

		return () => {
			Bridge.off(`widgets:${id}`, eventListener);
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

<div class="overflow-x-hidden min-h-full">
	{#if widget}
		<div class="fixed left-2.5 top-2.5">
			<CircularButton title={t('sr.navigation.back')} on:click={() => goto('/widgets')}>
				<span class="sr-only">
					<LocalizedText key="sr.navigation.back" />
				</span>
				<icon class="w-5 h-5" data-icon="icon/arrow-left" />
			</CircularButton>
		</div>

		<div class="fixed right-2.5 top-2.5">
			{#each settingsLayout?.buttons || [] as button}
				<CircularButton
					on:click={() =>
						window.Caffeinated.plugins.clickWidgetSettingsButton(widget.id, button.id)}
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
			<CircularButton
				title={t('sr.page.widgets.copy_link')}
				on:click={() => {
					window.Caffeinated.plugins.copyWidgetUrl(widget.id);
				}}
			>
				<span class="sr-only">
					<LocalizedText key="sr.page.widgets.copy_link" />
				</span>
				<icon class="w-5 h-5" data-icon="icon/document-duplicate" />
			</CircularButton>
			<CircularButton
				title={t('sr.page.widgets.delete')}
				on:click={() => {
					window.Caffeinated.plugins.deleteWidget(widget.id);
					goto('/widgets');
				}}
			>
				<span class="sr-only">
					<LocalizedText key="sr.page.widgets.delete" />
				</span>
				<icon class="w-5 h-5 text-error" data-icon="icon/trash" />
			</CircularButton>
		</div>

		<div class="flex flex-col min-h-full">
			<div class="-mt-1 pb-5 -mx-6 w-screen flex flex-col items-center justify-center">
				<span class="text-lg font-semibold relative">
					<div
						contenteditable
						class="px-1"
						bind:this={nameEditorElement}
						bind:textContent={nameEditorTextContent}
						on:blur={editName}
						on:keypress={(e) => {
							if (e.key === 'Enter') {
								e.preventDefault();
								e.target.blur();
							}
						}}
					/>

					<button
						class="absolute left-full top-1 translate-x-0.5"
						title={t('sr.page.widget.editor.edit_name')}
						on:click={() => {
							const range = document.createRange();
							const sel = window.getSelection();

							// Select all of the text.
							range.setStart(nameEditorElement.childNodes[0], nameEditorTextContent.length);

							// Unselect, moving the caret.
							range.collapse(true);

							// Tell the browser to use our range.
							sel.removeAllRanges();
							sel.addRange(range);

							// Focus the editor.
							nameEditorElement.focus();
						}}
					>
						<span class="sr-only">
							<LocalizedText key="sr.page.widget.editor.edit_name" />
						</span>
						<icon class="w-5 h-5" data-icon="icon/pencil-square" />
					</button>
				</span>

				<span class="text-xs font-thin">
					<LocalizedText key={widget.details.friendlyName} />
				</span>
			</div>

			<div class="border-b border-base-8 -mx-6 w-screen">
				{#if (widget.settingsLayout?.sections || []).length > 1}
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
					</nav>
				{/if}
			</div>

			<ul class="flex-1 block w-full max-w-sm mx-auto mt-2 divide-y divide-current text-base-6">
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
	{/if}
</div>
