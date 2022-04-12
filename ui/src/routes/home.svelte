<script>
    import StreamConfiguration from "../components/stream-configuration.svelte";

    import { onMount, onDestroy } from "svelte";
    import { setPageProperties } from "./__layout.svelte";

    const unregister = [];

    let accounts = [];
    let streamConfigurationComponent;

    let buildInfo = {};

    let name = "";
    let hourString = 0;

    function parseBridgeData(authInstances) {
        let newAccounts = [];

        // We try to figure out the user's preffered name by tallying up the names.
        let names = {};

        // Parse data from bridge
        for (const inst of Object.values(authInstances)) {
            if (inst.userData) {
                if (names[inst.userData.displayname]) {
                    names[inst.userData.displayname] += 1;
                } else {
                    names[inst.userData.displayname] = 1;
                }

                newAccounts.push(inst);
            } else {
                setTimeout(async () => {
                    parseBridgeData(await Caffeinated.auth.authInstances);
                }, 250);
            }
        }

        accounts = newAccounts.filter((account) => {
            return account.streamConfigurationFeatures && account.streamConfigurationFeatures.length > 0 && account.userData && account.streamData;
        });

        streamConfigurationComponent?.render(accounts);

        {
            // Get the most popular name.
            let mostPopular = "";
            let mostPopularCount = 0;

            for (const [name, count] of Object.entries(names)) {
                if (count > mostPopularCount) {
                    mostPopular = name;
                    mostPopularCount = count;
                }
            }

            name = mostPopular;
        }
    }

    setPageProperties({
        showSideBar: true,
        allowNavigateBackwards: false
    });

    onDestroy(() => {
        for (const un of unregister) {
            try {
                Bridge.off(un[0], un[1]);
            } catch (ignored) {}
        }
    });

    onMount(async () => {
        document.title = "";

        const hours = new Date().getHours();

        if (hours < 12) {
            hourString = "morning";
        } else if (hours < 14) {
            hourString = "day";
        } else if (hours < 18) {
            hourString = "afternoon";
        } else {
            hourString = "evening";
        }

        unregister.push(Caffeinated.auth.mutate("authInstances", parseBridgeData));

        buildInfo = await Caffeinated.buildInfo;
    });
</script>

<div class="has-text-centered welcome-wagon">
    <h1 class="title">
        Good {hourString}{#if name}
            , {name}.
        {:else}
            !
        {/if}
    </h1>
    <h2 class="subtitle">
        {#if buildInfo.isDev}
            (Developer Build)
        {:else if buildInfo.buildChannel && buildInfo.buildChannel != "stable"}
            ({buildInfo.version}-{buildInfo.commit}-{buildInfo.buildChannel})
        {:else}
            &nbsp; <!-- Retains spacing -->
        {/if}
    </h2>
</div>

<!-- <StreamConfiguration bind:this={streamConfigurationComponent} {accounts} /> -->
<style>
    .welcome-wagon {
        margin-top: 1.5em;
        margin-bottom: 2.5em;
    }
</style>
