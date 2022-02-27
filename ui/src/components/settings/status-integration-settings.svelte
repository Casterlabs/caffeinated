<div class="no-select">
    <div>
        <label class="checkbox">
            Change my Discord status
            <input id="status-integration-discord" type="checkbox" />
        </label>
    </div>
    <br />
    <div>
        <!-- svelte-ignore a11y-label-has-associated-control -->
        <label>
            Status Icon
            <br />
            <div class="select">
                <select id="status-integration-icon">
                    <option value="casterlabs-logo">Casterlabs</option>
                    <option value="casterlabs-pride">Pride</option>
                    <option value="casterlabs-moon">Moonlabs</option>
                    <option value="caffeine-logo">Caffeine</option>
                    <option value="twitch-logo">Twitch</option>
                    <option value="trovo-logo">Trovo</option>
                    <option value="glimesh-logo">Glimesh</option>
                    <option value="brime-logo">Brime</option>
                </select>
            </div>
        </label>
    </div>

    <script type="module">
        import { appStore } from "/js/caffeinated.mjs";
        import { getStatusIcon, setStatusIcon, updateDiscord } from "/js/status-integration/discord.mjs";

        /* ------------ */
        /* Discord status integration enable */
        /* ------------ */
        const statusIntegrationDiscord = document.querySelector("#status-integration-discord");

        statusIntegrationDiscord.checked = appStore.get("status_integration.discord.enabled");

        statusIntegrationDiscord.addEventListener("change", () => {
            appStore.set("status_integration.discord.enabled", statusIntegrationDiscord.checked);
            updateDiscord();
        });

        /* ------------ */
        /* Status icon  */
        /* ------------ */
        const statusIntegrationIcon = document.querySelector("#status-integration-icon");

        statusIntegrationIcon.value = getStatusIcon();

        statusIntegrationIcon.addEventListener("change", () => {
            setStatusIcon(statusIntegrationIcon.value);
        });
    </script>
</div>

<style>
</style>
