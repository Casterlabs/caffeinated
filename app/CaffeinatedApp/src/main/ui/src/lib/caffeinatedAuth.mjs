export const SPECIAL_SIGNIN = {};

export const PORTAL_SIGNIN = ["KICK", "NOICE", "LIVESPACE"];

// See also: appShim.mjs
export async function openAuthPortal(platform, shouldGoBack) {
	const portalLink = await Caffeinated.auth.getPortalUrl(platform, JSON.stringify({ shouldGoBack }));

	const frame = document.createElement("iframe");
	frame.src = portalLink;
	frame.id = "koi-auth-portal";
	frame.style = "position: absolute; top: 0; left: 0; bottom: 0; right: 0; width: 100vw; height: 100vh; z-index: 10000;"
	document.body.insertBefore(frame, document.body.firstChild);
}

export const ALL_STREAMING_SERVICES = [
	'TWITCH',
	'TROVO',
	'YOUTUBE',
	'DLIVE',
	'KICK',
	'TIKTOK',
	// 'NOICE',
	// 'LIVESPACE',
	// 'X',
	// 'FACEBOOK_GAMING'
];

export const STREAMING_SERVICES = {
	TWITCH: {
		name: 'Twitch',
		color: '#8838ff'
	},
	TROVO: {
		name: 'Trovo',
		color: '#149b53'
	},
	YOUTUBE: {
		name: 'YouTube',
		color: '#ff0000'
	},
	DLIVE: {
		name: 'DLive',
		color: '#ccc121'
	},
	KICK: {
		name: 'Kick (BETA)',
		color: '#53fc18'
	},
	TIKTOK: {
		name: 'TikTok (BETA)',
		color: '#e5004e'
	},
	// NOICE: {
	// 	name: 'Noice',
	// 	color: 'linear-gradient(45deg, #5231f9 6%, #6c2cfc 32%, #bb12fc 68%, #cf0bf9 77%, #ef00f5 94%)'
	// },
	// LIVESPACE: {
	// 	name: 'LiveSpace',
	// 	color: '#ff0070'
	// },
	// X: {
	// 	name: '𝕏',
	// 	color: '#000000'
	// },
	// FACEBOOK_GAMING: {
	// 	name: 'Facebook Gaming',
	// 	color: '#0866FF'
	// },
};

export const STREAMING_SERVICE_NAMES = {
	TWITCH: 'Twitch',
	TROVO: 'Trovo',
	YOUTUBE: 'YouTube',
	DLIVE: 'DLive',
	KICK: 'Kick',
	TIKTOK: 'TikTok',
	// NOICE: 'Noice',
	// LIVESPACE: 'LiveSpace',
	// X: '𝕏',
	// FACEBOOK_GAMING: 'Facebook Gaming'
};
