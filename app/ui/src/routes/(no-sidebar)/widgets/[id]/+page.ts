import type { PageLoad } from './$types';

export const load = (async ({ params }) => {
	return {
		widgetId: params.id
	};
}) satisfies PageLoad;
