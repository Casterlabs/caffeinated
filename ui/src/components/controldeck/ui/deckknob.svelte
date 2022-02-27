<script>
    import { createEventDispatcher } from "svelte";

    const dispatch = createEventDispatcher();

    const RING__CIRCLE_CIRCUMFERENCE = 263.893783;

    export let value;
    export let muted;

    function mouseWheel(e) {
        if (e.deltaY < 0 && value < 1) {
            value += 0.05;
        } else if (e.deltaY > 0 && value > 0) {
            value -= 0.05;
        }

        if (value < 0) {
            value = 0;
        } else if (value > 1) {
            value = 1;
        } else {
            value = Math.round(value * 100) / 100;
        }

        sendValue();
    }

    // Transmission

    function sendValue() {
        dispatch("value", {
            value: value
        });
    }

    function toggleMute() {
        muted = !muted;
        sendValue();
    }
</script>

<!-- svelte-ignore a11y-missing-attribute -->
<a class="knob box" data-muted={muted} on:click={toggleMute}>
    <svg class="ring" width="90" height="90" style="transform: translateY(5px) rotate(163deg);">
        <circle class="ring__circle" stroke-width="4" stroke-dashoffset={RING__CIRCLE_CIRCUMFERENCE - (value / 2) * RING__CIRCLE_CIRCUMFERENCE} fill="transparent" r="35" cx="45" cy="45" />
    </svg>
    <input type="number" bind:value min="0" max="1" step=".05" on:change={sendValue()} on:mousewheel={mouseWheel} />
</a>

<style>
    .knob {
        position: relative;
        width: 90px;
        height: 90px;
        display: block;
        box-shadow: 0;
    }

    .ring {
        position: absolute;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .ring .ring__circle {
        stroke-dasharray: 263.893783;
        stroke: rgb(3, 192, 3);
    }

    [data-muted="true"] .ring .ring__circle {
        stroke: rgb(201, 11, 11);
    }

    input {
        position: absolute;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: 50px;
        height: 20px;
        text-align: center;
    }
</style>
