<div class="aspect-container">
    <div>
        <slot />
    </div>
</div>

<style>
    .aspect-container {
        position: relative;
        padding-bottom: 100%;
        overflow: hidden;
    }
    .aspect-container > div {
        position: absolute;
        top: 0;
        left: 0;
        height: 100%;
        width: 100%;
    }
</style>
