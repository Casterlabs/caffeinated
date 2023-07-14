import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
	kit: {
		trailingSlash: 'never',
		prerender: { default: true },
		adapter: adapter({
			assets: '../../../target/classes/co/casterlabs/caffeinated/builtin/html',
			pages: '../../../target/classes/co/casterlabs/caffeinated/builtin/html',
			fallback: '__fallback.html'
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
