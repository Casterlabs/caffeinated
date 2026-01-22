<script lang="ts">
	import { goto } from '$app/navigation';
	import { deepEqual } from '$lib/bridge-helper';

	import WidgetPreview from '$lib/layout/WidgetPreview.svelte';
	import WidgetSettingsLayout from '$lib/layout/widget-settings/WidgetSettingsLayout.svelte';
	import LocalizedText from '$lib/locale/LocalizedText.svelte';
	import { ICONS, IconArrowLeft, IconDocumentDuplicate, IconPencilSquare, IconTrash } from '@casterlabs/heroicons-svelte';
	import { Button } from '@casterlabs/ui';

	import { onMount } from 'svelte';

	let widget: null | Awaited<typeof AppPlugins.widgets>[0] = $state(null);
	let settingsLayout: null | Awaited<typeof AppPlugins.widgets>[0]['settingsLayout'] = $state(null);
	let currentSection: string | null = $state(null);

	let nameEditorTextContent = $state('');
	let nameEditorElement: HTMLDivElement | null = $state(null);

	function editName() {
		AppPlugins.renameWidget(widget!.id, nameEditorTextContent);
	}

	onMount(async () => {
		const id = new URLSearchParams(location.search).get('id');

		widget = await AppPlugins.widgets.then((widgets) => {
			// Filter for a widget object with a matching id.
			// This'll return `undefined` if there's no matching result.
			return widgets.filter((w) => w.id == id)[0];
		});

		// If the widget is `undefined`, go back.
		if (!widget) {
			goto('/$caffeinated-sdk-root$/widgets');
			return;
		}

		settingsLayout = widget.settingsLayout;
		nameEditorTextContent = widget.name;
		currentSection = widget.settingsLayout.sections[0]?.id;
	});

	onMount(() => {
		const id = new URLSearchParams(location.search).get('id');

		// @ts-ignore
		const eventListener = window.saucer.messages.onMessage(([type, newWidget]) => {
			if (type != `widgets:${id}`) return;

			widget = newWidget;
			if (!deepEqual(newWidget.settingsLayout, settingsLayout)) {
				console.debug('Updating settings layout UI for widget settings because the layout changed.');
				settingsLayout = newWidget.settingsLayout; // Re-render the UI.
			}
		});
		return () => {
			// @ts-ignore
			window.saucer.messages.off(eventListener);
		};
	});
</script>

<div class="overflow-x-hidden min-h-full">
	{#if widget}
		<div class="fixed left-2.5 top-2.5">
			<Button onclick={() => goto('/$caffeinated-sdk-root$/widgets')}>
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.ui.navigation.go_back" />
				</span>
				<IconArrowLeft class="w-5 h-5" theme="outline" />
			</Button>
		</div>

		<div class="fixed right-2.5 top-2.5">
			{#each settingsLayout?.buttons || [] as button (button.id)}
				<Button class="inline-flex items-center" onclick={() => AppPlugins.clickWidgetSettingsButton(widget!.id, button.id)}>
					{@const Icon = ICONS[button.icon as 'academic-cap'] as (typeof ICONS)['academic-cap']}
					{#if button.text}
						<Icon class="w-5 h-5" theme="outline" />
						<span class="text-sm ml-1">{button.text}</span>
					{:else}
						<Icon class="w-5 h-5" theme="outline" />
					{/if}
				</Button>
			{/each}

			<Button onclick={() => AppPlugins.copyWidgetUrl(widget!.id)}>
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.copy_link" />
				</span>
				<IconDocumentDuplicate class="w-5 h-5" theme="outline" />
			</Button>

			<Button
				onclick={() => {
					AppPlugins.deleteWidget(widget!.id);
					goto('/$caffeinated-sdk-root$/widgets');
				}}
			>
				<span class="sr-only">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.widgets.delete" />
				</span>
				<IconTrash class="w-5 h-5 text-error" theme="solid" />
			</Button>
		</div>

		<div class="flex flex-col min-h-full">
			<div class="-mt-1 pb-5 -mx-6 w-screen flex flex-col items-center justify-center">
				<span class="text-lg font-semibold relative">
					<div
						role="textbox"
						tabindex="0"
						contenteditable
						class="px-1"
						bind:this={nameEditorElement}
						bind:textContent={nameEditorTextContent}
						onblur={editName}
						onkeypress={(e) => {
							if (e.key === 'Enter') {
								e.preventDefault();
								nameEditorElement?.blur();
							}
						}}
					></div>

					<button
						class="absolute left-full top-1 translate-x-0.5"
						onclick={() => {
							const range = document.createRange();
							const sel = window.getSelection();
							if (!sel) return;

							// Select all of the text.
							range.setStart(nameEditorElement!.childNodes[0], nameEditorTextContent.length);

							// Unselect, moving the caret.
							range.collapse(true);

							// Tell the browser to use our range.
							sel.removeAllRanges();
							sel.addRange(range);

							// Focus the editor.
							nameEditorElement!.focus();
						}}
					>
						<span class="sr-only">
							<LocalizedText key="co.casterlabs.caffeinated.app.page.widget.editor.edit_name" />
						</span>
						<IconPencilSquare class="w-5 h-5" theme="solid" />
					</button>
				</span>

				<span class="text-xs font-thin">
					<LocalizedText key={widget.details.friendlyName} />
				</span>
			</div>

			<div class="border-b border-base-8 -mx-6 w-screen">
				{#if (widget.settingsLayout?.sections || []).length > 1}
					<nav class="mt-1 -mb-px flex justify-center space-x-8">
						{#each widget.settingsLayout?.sections || [] as section (section.id)}
							{@const isSelected = currentSection == section.id}
							<button
								class="border-current whitespace-nowrap pb-4 px-1 font-medium text-sm"
								aria-current={isSelected ? 'page' : undefined}
								class:border-b-2={isSelected}
								class:text-primary-11={isSelected}
								onclick={() => (currentSection = section.id)}
							>
								<LocalizedText key={section.name} />
							</button>
						{/each}
					</nav>
				{/if}
			</div>

			{#each settingsLayout?.sections || [] as section (section.id)}
				{#if currentSection == section.id}
					<WidgetSettingsLayout {widget} settingsSection={section} />
				{/if}
			{/each}
		</div>

		{#if widget.details.showDemo}
			<WidgetPreview {widget} mode="DEMO" ariaHidden={true} />
		{/if}

		{#if widget.details.testEvents.length > 0}
			<div class="max-w-md mx-auto mt-6 mb-8">
				<h1 class="font-semibold text-xl">
					<LocalizedText key="co.casterlabs.caffeinated.app.page.widget.editor.test_events.send_test" />
				</h1>

				<div class="w-full mt-1 pt-1 select-none">
					{#each widget.details.testEvents as eventType}
						<Button onclick={() => AppPlugins.fireTestEvent(widget!.id, eventType as any)}>
							<!-- Try to convert the enum to a friendlier name. -->
							{(eventType.substring(0, 1) + eventType.substring(1).toLowerCase()).replace('_', ' ')}
						</Button>
					{/each}
				</div>
			</div>
		{/if}
	{/if}
</div>
