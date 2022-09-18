<script>
	import Content from '../components/layout/Content.svelte';
	import SideBar from '../components/layout/SideBar.svelte';
</script>

<div class="flex flex-row h-full">
	<SideBar />
	<Content>
		<slot />
	</Content>
</div>
