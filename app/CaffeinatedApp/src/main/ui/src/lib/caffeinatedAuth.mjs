export const SPECIAL_SIGNIN = {};

export const PORTAL_SIGNIN = ["KICK"];

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
	// '',
	// 'CAFFEINE',
	'TWITCH',
	'TROVO',
	// 'GLIMESH',
	// 'BRIME',
	'YOUTUBE',
	'DLIVE',
	// 'THETA',
	'KICK',
	'TIKTOK'
];

export const STREAMING_SERVICES = {
	// CAFFEINE: {
	// 	name: 'Caffeine',
	// 	color: '#0000ff'
	// },
	TWITCH: {
		name: 'Twitch',
		color: '#8838ff'
	},
	TROVO: {
		name: 'Trovo',
		color: '#149b53'
	},
	// GLIMESH: {
	// 	name: 'Glimesh',
	// 	color: '#33528b'
	// },
	// BRIME: {
	// 	name: 'Brime',
	// 	color: 'linear-gradient(45deg, #8439af 15%, #fc3537 65%)'
	// },
	YOUTUBE: {
		name: 'YouTube',
		color: '#ff0000'
	},
	DLIVE: {
		name: 'DLive',
		color: '#ccc121'
	},
	// THETA: {
	// 	name: 'Theta',
	// 	color: '#161a24'
	// },
	KICK: {
		name: 'Kick (BETA)',
		color: '#53fc18'
	},
	TIKTOK: {
		name: 'TikTok (BETA)',
		color: '#e5004e'
	}
};

export const STREAMING_SERVICE_NAMES = {
	// CAFFEINE: 'Caffeine',
	TWITCH: 'Twitch',
	TROVO: 'Trovo',
	// GLIMESH: 'Glimesh',
	// BRIME: 'Brime',
	YOUTUBE: 'YouTube',
	DLIVE: 'DLive',
	// THETA: 'Theta',
	KICK: 'Kick',
	TIKTOK: 'TikTok'
};
