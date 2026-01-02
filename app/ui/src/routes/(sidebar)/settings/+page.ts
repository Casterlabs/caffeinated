import { goto } from '$app/navigation';
import type { PageLoad } from './$types';

export const load = (async () => {
	goto('/$caffeinated-sdk-root$/settings/appearance');
}) satisfies PageLoad;
