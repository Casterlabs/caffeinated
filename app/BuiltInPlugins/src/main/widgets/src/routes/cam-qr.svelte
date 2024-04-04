<script>
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';
	import Aspect1by1 from '$lib/aspect-ratio/Aspect1by1.svelte';

	changeFont('Poppins');

	let hidden = true;

	onMount(() => {
		const qrcode = new QRCode('qrcode', {
			text: 'reallylonglinkokthisissoooooooooooooooooolonglikezoowheemamaitslong.',
			width: 200,
			height: 200,
			colorDark: 'black',
			colorLight: 'white'
		});

		function setCode() {
			qrcode.makeCode(`https://studio.casterlabs.co/tools/cam?id=${Widget.getSetting('cam.id')}`);
		}

		setCode();
		Widget.on('init', setCode());
	});
</script>

<svelte:head>
	<script src="https://cdn.rawgit.com/davidshimjs/qrcodejs/gh-pages/qrcode.min.js"></script>
</svelte:head>

<div class="relative mx-auto overflow-hidden" style="width: 200px; height: 200px;">
	<div class:blur-md={hidden} id="qrcode" />

	<button
		class="absolute inset-0 text-white"
		class:opacity-0={!hidden}
		on:click={() => (hidden = !hidden)}
	>
		Click to show QR code
	</button>
</div>
