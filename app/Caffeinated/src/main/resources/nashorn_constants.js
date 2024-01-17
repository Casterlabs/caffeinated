const PlaybackState = {
	PLAYING: 'PLAYING',
	PAUSED: 'PAUSED',
	INACTIVE: 'INACTIVE',
};
Object.freeze(PlaybackState);

const KoiChatter = {
	CLIENT: 'CLIENT',
	PUPPET: 'PUPPET',
	SYSTEM: 'SYSTEM',
};
Object.freeze(KoiChatter);

const KoiPlatform = {
	TWITCH: 'TWITCH',
	TROVO: 'TROVO',
	YOUTUBE: 'YOUTUBE',
	DLIVE: 'DLIVE',
	KICK: 'KICK',
	TIKTOK: 'TIKTOK',
	
	// Other
	CASTERLABS_SYSTEM: 'CASTERLABS_SYSTEM',
	CUSTOM_INTEGRATION: 'CUSTOM_INTEGRATION',
};
Object.freeze(KoiPlatform);