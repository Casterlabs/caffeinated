<script>
    import StreamConfiguration from "../components/stream-configuration/index.svelte";

    import LocalizedText from "$lib/components/LocalizedText.svelte";

    import { onMount, onDestroy } from "svelte";
    import { setPageProperties } from "./__layout.svelte";

    const unregister = [];

    let accounts = [];
    let streamConfigurationComponent;

    let buildInfo = {};

    let name = "";
    let hourLang = 0;

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

        accounts = newAccounts;
        streamConfigurationComponent?.render(accounts);
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
            hourLang = "ui.greeting.good_morning";
        } else if (hours < 14) {
            hourLang = "ui.greeting.good_day";
        } else if (hours < 18) {
            hourLang = "ui.greeting.good_afternoon";
        } else {
            hourLang = "ui.greeting.good_evening";
        }

        unregister.push(Caffeinated.auth.mutate("authInstances", parseBridgeData));

        buildInfo = await Caffeinated.buildInfo;
    });
</script>

<div class="has-text-centered welcome-wagon">
    <h1 class="title">
        <LocalizedText key={hourLang} opts={{ name }} />
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

<StreamConfiguration bind:this={streamConfigurationComponent} {accounts} />

<style>
    .welcome-wagon {
        margin-top: 1.5em;
        margin-bottom: 2.5em;
    }
</style>
