<script>
	import { onMount } from 'svelte';

	let videoElement;

	let peer;
	let currentCall;

	onMount(() => {
		Widget.on('init', () => {
			fetch('https://cdn.casterlabs.co/api.json')
				.then((response) => response.json())
				.then((apiInfo) => {
					peer = new Peer({
						host: 'oci-igmsmi.casterlabs.co',
						port: 443,
						secure: true,
						path: '/',
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
							videoElement.srcObject = null;
							currentCall = null;
						});

						call.on('stream', (mediaStream) => {
							console.debug('Video: ' + mediaStream);
							videoElement.srcObject = mediaStream;

							for (const videoTrack of mediaStream.getVideoTracks()) {
								videoTrack.onmute = () => {
									videoElement.style.opacity = 0;
								};
								videoTrack.onunmute = () => {
									videoElement.style.opacity = 1;
								};
							}
						});

						call.answer();
					});

					peer.on('open', (id) => {
						Widget.emit('caller-id', id);
						Widget.emit('aspect-ratio', videoElement.clientHeight / videoElement.clientWidth);
					});
				})
				.catch(location.reload);
		});
	});
</script>

<svelte:head>
	<script src="https://unpkg.com/peerjs@1.5.2/dist/peerjs.js"></script>
</svelte:head>

<!-- svelte-ignore a11y-media-has-caption -->
<video class="w-full h-full object-cover" style:hidden={!currentCall} bind:this={videoElement} />
