<script>
	import Content from '../components/layout/Content.svelte';
	import SideBar from '../components/layout/SideBar.svelte';

	let sideBarInst;
</script>

<div class="flex flex-row h-full">
	<SideBar bind:this={sideBarInst} />
	<Content>
		<slot />
	</Content>
</div>

<button
	class="fixed top-1/2 left-[var(--actual-sidebar-width)] -ml-1 -translate-y-1/2 z-90 bg-base-2 py-0.5 rounded-r-md shadow-sm opacity-80"
	on:click={sideBarInst.toggleSideBar}
>
	<icon class="-mr-1 w-5 h-5" data-icon="icon/ellipsis-vertical" />
</button>
