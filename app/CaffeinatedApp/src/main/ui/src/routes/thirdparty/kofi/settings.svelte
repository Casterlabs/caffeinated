<script>
	import SlimButton from '$lib/ui/SlimButton.svelte';
</script>

Copy this URL into your <a
	class="text-link underline"
	href="https://ko-fi.com/manage/webhooks?src=casterlabs"
	target="_blank"
>
	Ko-fi API Settings Page
</a>

<SlimButton on:click={() => window.Widget.emit('copyUrl')}>Click to copy</SlimButton>
