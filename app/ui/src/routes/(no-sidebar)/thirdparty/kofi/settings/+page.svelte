<script lang="ts">
	import { type AppSDK } from '$lib/sdk';

	import { Button } from '@casterlabs/ui';
</script>

Copy this URL into your <a class="text-primary-11" href="https://ko-fi.com/manage/webhooks?src=casterlabs" target="_blank"> Ko-fi API Settings Page </a>

<br />

<Button
	on:click={() => {
		const SDK = window as any as AppSDK;
		SDK.Widget.emit('copyUrl');
	}}
>
	Click to copy
</Button>
