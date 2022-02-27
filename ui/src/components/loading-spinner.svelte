<span class="loader" />

<style>
    .loader {
        width: 48px;
        height: 48px;
        border-radius: 50%;
        display: inline-block;
        box-sizing: border-box;
        animation: rotation 1s linear infinite;
    }

    :global(.app-is-dark) .loader {
        border-color: white;
        border-top: 3px solid white;
        border-right: 3px solid transparent;
    }

    :global(.app-is-light) .loader {
        border-color: black;
        border-top: 3px solid black;
        border-right: 3px solid transparent;
    }

    @keyframes rotation {
        0% {
            transform: rotate(0deg);
        }

        100% {
            transform: rotate(360deg);
        }
    }
</style>
