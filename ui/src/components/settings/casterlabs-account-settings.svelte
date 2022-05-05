<script>
    import LoadingScreen from "./cl-account/loading.svelte";
    import MeScreen from "./cl-account/me.svelte";
    import LoginScreen from "./cl-account/login.svelte";
    import RegisterScreen from "./cl-account/register.svelte";
    import ResetPasswordScreen from "./cl-account/resetpassword.svelte";

    import * as Account from "../account.mjs";
    import { onMount } from "svelte";

    let account = null;
    let currentScreen = "LOADING";

    onMount(async () => {
        account = await Account.getMyAccount();

        if (account) {
            console.log(account);
            currentScreen = "ME";
        } else {
            currentScreen = "LOGIN";
        }
    });
</script>

{#if currentScreen == "LOADING"}
    <LoadingScreen />
{:else if currentScreen == "ME"}
    <MeScreen bind:account bind:currentScreen />
{:else if currentScreen == "LOGIN"}
    <LoginScreen bind:account bind:currentScreen />
{:else if currentScreen == "REGISTER"}
    <RegisterScreen bind:account bind:currentScreen />
{:else if currentScreen == "RESET_PASSWORD"}
    <ResetPasswordScreen bind:currentScreen />
{/if}
