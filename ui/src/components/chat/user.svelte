<script>
    export let userData;

    function prettifyString(str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
</script>

<span>
    <a href={userData.link} rel="external" style="user-select: none;" target="_blank">
        <img src={userData.image_link} class="inline-image profile-picture" alt={userData.displayname} />
        <span title={prettifyString(userData.platform)} style="color: {userData.color}">
            {userData.displayname}

            <span class="user-platform">
                ({prettifyString(userData.platform)})
            </span>
        </span></a
    ><span style="user-select: none;">
        {#each userData.badges as badge}
            <img src={badge} class="inline-image badge" alt="" title={badge.name || ""} />
        {/each}
    </span>
</span>

<style>
    .inline-image {
        height: 1.3em;
        vertical-align: text-bottom;
        image-rendering: pixelated;
    }

    .profile-picture {
        display: none;
        border-radius: 50%;
    }

    :global(.show-profile-pictures) .profile-picture {
        display: inline-block;
    }

    .user-platform {
        display: none;
    }

    :global(.is-multi-platform) .user-platform {
        display: inline-block;
    }

    .badge {
        display: none;
    }

    :global(.show-badges) .badge {
        display: inline-block;
    }

    .badge:not(:first-child) {
        margin-left: 0.5ch;
    }
</style>
