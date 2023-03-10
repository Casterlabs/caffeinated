<script>
	import { onMount } from 'svelte';

	let videoElement;

	let peer;
	let currentCall;

	onMount(() => {
		fetch('https://cdn.casterlabs.co/api.json')
			.then((response) => response.json())
			.then((apiInfo) => {
				peer = new Peer({
					config: {
						iceServers: apiInfo.ice_servers,
						sdpSemantics: 'unified-plan'
					}
				});

				videoElement.addEventListener('loadedmetadata', () => {
					videoElement.style.opacity = 1;
					videoElement.play();
				});

				peer.on('call', (call) => {
					if (currentCall) currentCall.close();

					currentCall = call;
					console.log('Call', call);

					call.on('close', () => {
						console.log('Closed', call);
						currentCall = null;
					});

					currentCall.on('stream', (mediaStream) => {
						console.debug('Video: ' + mediaStream);
						videoElement.srcObject = mediaStream;
					});

					call.answer();
				});

				peer.on('open', (id) => {
					Widget.emit('caller-id', id);
				});
			})
			.catch(location.reload);
	});
</script>

<svelte:head>
	<script src="https://unpkg.com/peerjs@1.3.1/dist/peerjs.js"></script>
</svelte:head>

<!-- svelte-ignore a11y-media-has-caption -->
<video class="w-full h-full" style:hidden={!currentCall} bind:this={videoElement} />
