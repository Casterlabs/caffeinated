import type { PageLoad } from './$types';

export const load = (async ({ params }) => {
	return {
		platform: params.platform,
		type: params.type
	};
}) satisfies PageLoad;
