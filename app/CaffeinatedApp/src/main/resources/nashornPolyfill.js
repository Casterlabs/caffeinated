// https://github.com/KhaledElAnsari/Object.values
if (!Object.values) {
	Object.values = function(obj) {
		var allowedTypes = ["[object String]", "[object Object]", "[object Array]", "[object Function]"];
		var objType = Object.prototype.toString.call(obj);
	
		if(obj === null || typeof obj === "undefined") {
			throw new TypeError("Cannot convert undefined or null to object");
		} else if(!~allowedTypes.indexOf(objType)) {
			return [];
		} else {
			// if ES6 is supported
			if (Object.keys) {
				return Object.keys(obj).map(function (key) {
					return obj[key];
				});
			}
			
			var result = [];
			for (var prop in obj) {
				if (obj.hasOwnProperty(prop)) {
					result.push(obj[prop]);
				}
			}
			
			return result;
		}
	};
}

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