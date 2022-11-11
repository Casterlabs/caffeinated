<script>
	import CircularButton from '$lib/ui/CircularButton.svelte';
	import FormInput from '$lib/ui/FormInput.svelte';
	import LocalizedText from '$lib/LocalizedText.svelte';

	import { t } from '$lib/translate.mjs';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount, tick } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Widget Editor');

	let widget;

	let nameEditorElement;
	let nameEditorTextContent;

	let currentSection;

	function editName() {}

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
				nameEditorTextContent = widget.name;
				currentSection = widget.settingsLayout.sections[0]?.id;
				console.debug('Widget data:', widget);
			});

		const eventListener = Bridge.on(`widgets:${id}`, (w) => (widget = w));

		return () => {
			Bridge.off(`widgets:${id}`, eventListener);
		};
	});
</script>

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
		<CircularButton
			title={t('sr.navigation.back')}
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
			title={t('sr.navigation.back')}
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

	<ul class="block max-w-sm mx-auto mt-2 divide-y divide-current text-base-6">
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
	</ul>
{/if}
