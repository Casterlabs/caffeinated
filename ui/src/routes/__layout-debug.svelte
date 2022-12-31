<script>
	import CSSIntermediate from '$lib/CSSIntermediate.svelte';

	// Little helper to allow us to access the
	// stores but prevent SSR from erroring out.
	if (typeof global == 'undefined') {
		window.st = false;
	} else {
		global.st = {
			subscribe(callback) {
				callback(null);
				return () => {};
			},
			set() {} // No-OP
		};
	}
</script>

<CSSIntermediate>
	<slot />
</CSSIntermediate>
