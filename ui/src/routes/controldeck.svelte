<script>
    import { onMount } from "svelte";

    import DummyDeck from "../components/controldeck/ui/impl/dummydeck.svelte";

    import { setPageProperties } from "./__layout.svelte";

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: true
    });

    onMount(() => {
        document.title = "Casterlabs Caffeinated - Control Deck";
    });
</script>

<br />
<div class="has-text-centered">
    <DummyDeck deckId="DUMMY000-0000-0000-0000-DUMMYDECK000" />
</div>
