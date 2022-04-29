<div class="container is-max-desktop">
    <div class="box">
        <slot />
    </div>
</div>

<style>
    .box {
        margin: auto;
        margin-top: 100px;
        max-width: 400px;
    }

    :global(.footer) {
        display: none;
    }
</style>
