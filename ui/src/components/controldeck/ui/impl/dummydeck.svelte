<script>
    export let deckId;

    import Deckbase from "../deckbase.svelte";
    import DeckKnob from "../deckknob.svelte";

    const WIDTH = 200;
    const HEIGHT = 480;

    let deckName = "DummyDeck"; // TODO use the deck id and load it from the deck instance.

    let knob0Val = 1;
    let knob0Muted = false;
    let knob1Val = 1;
    let knob1Muted = false;
    let knob2Val = 1;
    let knob2Muted = false;
    let knob3Val = 1;
    let knob3Muted = false;

    function readKnobs() {
        console.debug(`Knob0: val=${knob0Val} muted=${knob0Muted}`);
        console.debug(`Knob1: val=${knob1Val} muted=${knob1Muted}`);
        console.debug(`Knob2: val=${knob2Val} muted=${knob2Muted}`);
        console.debug(`Knob3: val=${knob3Val} muted=${knob3Muted}`);
    }
</script>

<Deckbase width={WIDTH} height={HEIGHT} bind:name={deckName}>
    <DeckKnob bind:value={knob0Val} bind:muted={knob0Muted} on:value={readKnobs} />
    <DeckKnob bind:value={knob1Val} bind:muted={knob1Muted} on:value={readKnobs} />
    <DeckKnob bind:value={knob2Val} bind:muted={knob2Muted} on:value={readKnobs} />
    <DeckKnob bind:value={knob3Val} bind:muted={knob3Muted} on:value={readKnobs} />
</Deckbase>

<style>
</style>
