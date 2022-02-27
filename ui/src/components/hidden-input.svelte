<script>
    export let label;
    export let value;

    let showing = false;

    function toggleVisibility() {
        showing = !showing;
    }
</script>

<!-- svelte-ignore a11y-label-has-associated-control -->
<!-- svelte-ignore a11y-missing-attribute -->
<div class="field">
    <label class="label">{label}</label>
    <div class="control" style="position: relative;">
        <input class="input" type={showing ? "input" : "password"} readonly {value} />
        <a class="show-or-hide" on:click={toggleVisibility}>
            {#if showing}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-eye-off"
                >
                    <path
                        d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"
                    />
                    <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
            {:else}
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    class="feather feather-eye"
                >
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle cx="12" cy="12" r="3" />
                </svg>
            {/if}
        </a>
    </div>
</div>

<style>
    input {
        padding-right: 33px !important;
    }

    .show-or-hide {
        position: absolute;
        top: 0;
        right: 0;
        width: 33px;
        height: 40px;
        color: var(--text-color);
    }

    .show-or-hide:hover {
        color: var(--title-bar-faded-color);
    }

    .show-or-hide svg {
        position: absolute;
        margin-top: 1px;
        top: 50%;
        transform: translateY(-50%);
        left: 5px;
        width: 18px;
        height: 18px;
    }
</style>
