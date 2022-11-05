<script>
	import ThickTextArea from '../ui/ThickTextArea.svelte';
	import Button from '../ui/Button.svelte';
</script>

<div class="flex flex-row space-x-1.5">
	<div class="flex-1">
		<ThickTextArea rows="1" resize={false} />
	</div>

	<div>
		<Button>Send</Button>
	</div>
</div>
