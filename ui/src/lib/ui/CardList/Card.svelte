<script>
    import LocalizedText from "../../LocalizedText.svelte";

    import { createEventDispatcher } from "svelte";
    import { goto } from "$app/navigation";

    const dispatch = createEventDispatcher();

    export let icon = null;
    export let text = null;

    export let href = null;

    function onClick() {
        if (href) {
            goto(href);
        } else {
            dispatch("click");
        }
    }
</script>

<button role={href ? "link" : "button"} class="relative flex items-center space-x-3 rounded-lg border border-base-6 bg-base-2 p-5 shadow-sm focus:border-primary-7 focus:outline-none focus:ring-1 focus:ring-primary-7" on:click={onClick}>
    <div class="flex-shrink-0 text-base-12">
        <icon data-icon="icon/{icon}" />
    </div>
    <div class="min-w-0 flex-1">
        <p class="text-sm font-medium text-base-12 text-left">
            <LocalizedText key={text} />
        </p>
    </div>
    <div class="flex-shrink-0 text-base-12">
        <slot />
    </div>
</button>
