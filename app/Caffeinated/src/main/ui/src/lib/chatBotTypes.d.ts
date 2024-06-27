/* A list of arguments, so if someone runs "!example 123 abc" this will be ['123', 'abc']. Note that the args are only populated during a chat message, not during a Shout for Subscriptions, Follows, etc. */
declare const args: string[];

/** Exactly the same as args, but not as an pre-split array. */
declare const rawArgs: string;

// The event that triggered this script. Usually a KoiRichMessageEvent, which you can inspect the Java definition here:
// https://github.com/Casterlabs/caffeinated/blob/dev/app/Koi/src/main/java/co/casterlabs/koi/api/types/events/RichMessageEvent.java
// Take note that during Shouts, this will be a SubscriptionEvent, FollowEvent, etc. */
declare const event: KoiEvent;

// Waits the specified amount of time.
declare function sleep(milliseconds: number): void;

// ------------------------------------------------
// Sound
// ------------------------------------------------

declare const Sound: {
	/** Plays the specified audio url using the provided volume (must be 0-1) */
	playAudio(audioUrl: string, volume: number): void;

	/** Plays the specified text as a TTS message using the provided volume (must be 0-1) */
	playTTS(text: string, defaultVoice: string, volume: number): void;
};

// ------------------------------------------------
// Input
// ------------------------------------------------

/**
 * @deprecated Note that using any functions in this may result in a game's anticheat banning you for suspected hacks. You have been warned.
 */
declare const Input: {
	/** Presses the specified key. e.g "VK_A"
	 *  @see https://docs.oracle.com/javase/8/docs/api/java/awt/event/KeyEvent.html#:~:text=Fields-,Modifier%20and%20Type,-Field%20and%20Description
	 */
	keyPress(keycode: string): void;

	/* Moves the mouse by the specified amount of pixels in any direction. */
	mouseMove(pixels: number, degrees: number, smooth: boolean): void;

	/* Simulates the scroll wheel. Use a negative value for up and a positive value for down. */
	mouseScroll(direction: number): void;

	/* Simulates a click. Use 1 for left click, 2 for right, 3 for scroll wheel, 4 for forward, and 5 for backward. */
	mouseClick(button: number): void;
};

// ------------------------------------------------
// store
// ------------------------------------------------

/** The K:V store, you can use this to store information like points or stats. */
declare const store: {
	/** Puts a value in the store. */
	put(key: string, value: any): void;

	/** Returns a value */
	get(key: string): any;

	/** Returns a value, using the default if it's not present */
	getOrDefault(key: string, defaultValue: any): any;

	/** Returns whether or not the store contains a key */
	containsKey(key: string): boolean;
};

// ------------------------------------------------
// fetch
// ------------------------------------------------

declare const fetch: {
	asText(url: string): string;
};

// ------------------------------------------------
// Currencies
// ------------------------------------------------

declare const Currencies: {
	formatCurrency(amount: number, currencyCode: string): string;
};

// ------------------------------------------------
// Plugins
// ------------------------------------------------

declare const Plugins: {
	callServiceMethod(pluginId: string, serviceId: string, methodName: string, args: any[]): object;
};

// ------------------------------------------------
// Music
// ------------------------------------------------

declare const Music: {
	/** Null if there is no song playing. */
	activePlayback: ActivePlayback | null;

	/** Not really useful, feel free to poke around though. */
	providers: any[];
};

declare interface ActivePlayback {
	serviceId: string;
	serviceName: string;
	accountName: string | null;
	accountLink: string | null;
	playbackState: PlaybackState;
	currentTrack: MusicTrack;
}

declare interface MusicTrack {
	title: string;
	album: string | null;
	/**
	 * Can also be base64 data. Be wary.
	 */
	albumArtUrl: string;
	link: string;
	artists: string[];
}

declare const PlaybackState: {
	PLAYING: 'PLAYING';
	PAUSED: 'PAUSED';
	INACTIVE: 'INACTIVE';
};

// ------------------------------------------------
// Koi
// ------------------------------------------------

/** All platform enums. */
declare const PLATFORMS: [];

declare const Koi: {
	sendChat(
		platform: KoiPlatform, // Must be a signed in platform for this to succeed.
		message: string,
		chatter: KoiChatter,
		replyTarget: string | null // The event ID, if you want to do a direct reply.
	): void;

	upvoteChat( // Not supported by all platforms.
		platform: KoiPlatform, // Must be a signed in platform for this to succeed.
		messageId: string // The event ID.
	): void;

	deleteChat( // Not supported by all platforms.
		platform: KoiPlatform, // Must be a signed in platform for this to succeed.
		messageId: string // The event ID.
	): void;

	history: KoiEvent[];

	// This are for the currently signed in accounts.
	viewers: { [key in KoiPlatform]: KoiUser[] };
	userStates: { [key in KoiPlatform]: KoiEvent[] };
	streamStates: { [key in KoiPlatform]: KoiEvent[] };
	roomStates: { [key in KoiPlatform]: KoiEvent[] };
	features: { [key in KoiPlatform]: KoiEvent[] };
};

/** TODO */
declare interface KoiEvent {}
/** TODO */
declare interface KoiUser {}

declare const KoiChatter: {
	/** The streamer's account */
	CLIENT: 'CLIENT';
	/** Unused, currently redirects to CLIENT. */
	PUPPET: 'PUPPET';
	/** The @/casterlabs account. */
	SYSTEM: 'SYSTEM';
};

declare const KoiPlatform: {
	TWITCH: 'TWITCH';
	TROVO: 'TROVO';
	YOUTUBE: 'YOUTUBE';
	DLIVE: 'DLIVE';
	KICK: 'KICK';
	TIKTOK: 'TIKTOK';

	// Other
	CASTERLABS_SYSTEM: 'CASTERLABS_SYSTEM';
	CUSTOM_INTEGRATION: 'CUSTOM_INTEGRATION';
};
