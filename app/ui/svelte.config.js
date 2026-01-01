import adapter from '@sveltejs/adapter-static';
import { vitePreprocess } from '@sveltejs/vite-plugin-svelte';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	// Consult https://svelte.dev/docs/kit/integrations
	// for more information about preprocessors
	preprocess: vitePreprocess(),

	kit: {
		adapter: adapter({
			assets: './target/classes/co/casterlabs/caffeinated/app/ui/html',
			pages: './target/classes/co/casterlabs/caffeinated/app/ui/html',
			fallback: 'index.html'
		}),
		files: {
			lib: 'src/lib'
		},
		paths: {
			base: '/$caffeinated-sdk-root$'
		}
	}
};

export default config;
