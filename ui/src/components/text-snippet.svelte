<script>
    import { onMount, onDestroy } from "svelte";

    const unregister = [];

    let emojiProvider = "system";

    let slotElement;
    let text;
    let content;

    function render() {
        if (typeof window != "undefined" && emojiProvider != "system") {
            fetch(`https://api.casterlabs.co/v3/emojis/detect?provider=${emojiProvider}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({
                    text: text,
                    responseFormat: "HTML"
                })
            })
                .then((response) => response.text())
                .then((text) => {
                    content = text;
                });
        } else {
            content = null;
        }
    }

    onMount(() => {
        unregister.push(
            Caffeinated.UI.mutate("preferences", (preferences) => {
                console.log(preferences);
                emojiProvider = preferences.emojiProvider;
            })
        );
    });

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    $: slotElement, (text = slotElement?.innerText);

    // Rerender on emoji provider change
    $: emojiProvider, render();
    $: text, render();
</script>

<!-- Yoink the text value from svelte -->
<span bind:this={slotElement} style={content ? "display: none;" : ""}>
    <slot />
</span>

{#if content}
    {@html content}
{/if}
