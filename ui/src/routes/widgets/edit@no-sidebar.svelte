<script>
	import CircularButton from '../../components/ui/CircularButton.svelte';
	import LocalizedText from '../../components/LocalizedText.svelte';

	import { t } from '$lib/translate.mjs';
	import { goto } from '$app/navigation';
	import { page } from '$app/stores';
	import { onMount } from 'svelte';
	import createConsole from '$lib/console-helper.mjs';

	const console = createConsole('Widget Editor');

	let widget;

	let nameEditorElement;
	let nameEditorTextContent;

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
				console.debug('Widget data:', widget);
			});
	});
</script>

{#if widget}
	<div class="fixed left-2.5 top-2.5" title={t('sr.navigation.back')}>
		<CircularButton on:click={() => goto('/widgets')}>
			<span class="sr-only">
				<LocalizedText key="sr.navigation.back" />
			</span>
			<icon class="w-5 h-5" data-icon="icon/arrow-left" />
		</CircularButton>
	</div>

	<div
		class="-mt-1 pb-5 -mx-6 border-b border-mauve-6 w-screen flex flex-col items-center justify-center"
	>
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
{/if}
