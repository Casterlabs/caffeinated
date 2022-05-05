<script>
    import Layout from "./layout.svelte";
    import * as Account from "../../account.mjs";

    import LocalizedText from "$lib/components/LocalizedText.svelte";
    import translate from "$lib/translate.mjs";
    import App from "$lib/app.mjs";

    export let account;
    export let currentScreen;

    let emailSent = false;
</script>

<Layout>
    <LocalizedText
        key="casterlabs_account.name"
        opts={{
            name: account.name
        }}
    />
    <br />
    <LocalizedText
        key="casterlabs_account.email"
        opts={{
            email: `${account.email.split("@")[0].substring(0, 1)}*****@${account.email.split("@")[1]}`
        }}
    />
    <br />
    <!-- svelte-ignore a11y-missing-attribute -->
    <LocalizedText key="casterlabs_account.email_verified" slotMapping={["tickbox", "verify"]}>
        <input slot="0" type="checkbox" checked={account.emailVerified} onclick="return false" /><span slot="1">
            {#if !account.emailVerified}
                <a
                    on:click={() => {
                        if (!emailSent) {
                            emailSent = true;
                            Account.sendEmailVerification();

                            setTimeout(() => {
                                emailSent = false;
                            }, 15 * 1000);
                        }
                    }}
                >
                    <LocalizedText key={emailSent ? "casterlabs_account.send_verification_email.sent" : "casterlabs_account.send_verification_email"} />
                </a>
            {/if}
        </span>
    </LocalizedText>
    <br />
    <!-- svelte-ignore a11y-missing-attribute -->
    <LocalizedText key="casterlabs_account.has_casterlabs_plus" slotMapping={["tickbox", "manage"]}>
        <input slot="0" type="checkbox" checked={account.hasCasterlabsPlus} onclick="return false" /><span slot="1">
            <!--{#if account.hasCasterlabsPlus}
                ... manage
            {:else}
                ... learn more
            {/if}-->
        </span>
    </LocalizedText>
    <br />
    <br />
    <br />
    <button
        class="button is-link"
        on:click={() => {
            Account.logout();
            currentScreen = "LOGIN";
        }}
    >
        <LocalizedText key="casterlabs_account.logout" />
    </button>
    <br />
</Layout>
