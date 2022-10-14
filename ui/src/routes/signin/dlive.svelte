<script>
    import { setPageProperties } from "../__layout.svelte";
    import { onMount } from "svelte";

    import OAuthSignin from "../../components/platforms/oauth-signin.svelte";

    setPageProperties({
        showSideBar: false,
        allowNavigateBackwards: false
    });

    onMount(() => {
        document.title = "";
    });
</script>

<div id="signin-container">
    <OAuthSignin platform="caffeinated_dlive" isKoi={true} />
</div>
