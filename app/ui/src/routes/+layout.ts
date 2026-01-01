import { dev } from '$app/environment';

export const trailingSlash = 'never';
export const prerender = true;
export const ssr = !dev;
export const csr = dev;
