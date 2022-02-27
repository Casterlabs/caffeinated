<script>
    export let width;
    export let height;
    export let name;
</script>

<div class="base box" style="width: {width}px; height: {height}px; margin-left: 15px;">
    <span class="name">{name}</span>
    <div class="slot">
        <slot />
    </div>
</div>

<style>
    .base {
        position: relative;
    }

    .name {
        position: absolute;
        left: 8px;
        top: 6px;
        font-size: 0.75em;
        z-index: 20;
    }

    .slot {
        margin-top: 10px;
    }
</style>
