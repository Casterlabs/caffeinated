<script>
    export let name;
    export let icon;
</script>

<div class="dropdown is-hoverable">
    <div class="dropdown-trigger">
        <button class="button ghost-button" aria-haspopup="true" aria-controls="{name}-dropdown">
            <span class="icon-text">
                <span class="icon is-small">
                    <i data-feather={icon} aria-hidden="true" />
                </span>
                <span>
                    {name}
                </span>
            </span>
            <span class="icon is-small right-chevron">
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-chevron-right"><polyline points="9 18 15 12 9 6" /></svg
                >
            </span>
        </button>
    </div>
    <div class="dropdown-menu" id="{name}-dropdown" role="menu">
        <div class="dropdown-content">
            <slot />
        </div>
    </div>
</div>

<style>
    .dropdown-menu {
        position: absolute;
        left: 100% !important;
        bottom: -2px !important;
        padding-bottom: 0 !important;
        padding-left: 13px;
    }

    .dropdown-content {
        padding-top: 2px;
        padding-bottom: 2px;
    }

    :global(.dropdown-content > .dropdown-item) {
        padding: 0;
    }

    :global(.dropdown-content > .dropdown-item > .button) {
        width: 8rem;
        padding: 0;
        padding-left: 10px;
    }

    :global(.dropdown-divider) {
        margin: 2px;
    }

    .ghost-button {
        padding: 0;
        padding-left: 0.5em;
        width: 11rem;
        justify-content: left;
        border: none;
    }

    .right-chevron {
        position: absolute;
        right: 1em;
    }
</style>
