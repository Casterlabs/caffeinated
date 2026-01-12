// Auto-generated Saucer Bridge Definitions
// Generated on 2026-01-03T12:58:16.522346Z

export declare type MutationListenerId = any;
export declare interface MutationObject<M> {
	onMutate(propertyName: M, handler: (newValue: any) => void): MutationListenerId;
	offMutate(id: MutationListenerId): void;
}
export declare type SaucerUrl = string;
export declare interface SaucerColor {
	r: number;
	g: number;
	b: number;
	a: number;
}
export declare interface SaucerSize {
	width: number;
	height: number;
}
export declare interface SaucerPosition {
	x: number;
	y: number;
}
export declare interface SaucerRectangle {
	x: number;
	y: number;
	width: number;
	height: number;
}
export declare interface SaucerScreen {
	name: string;
	size: SaucerSize;
	position: SaucerPosition;
}

export declare interface saucer_webview_window extends MutationObject<never> {
	backgroundColor: Promise<SaucerColor> | SaucerColor;
	resizable: Promise<boolean> | boolean;
	alwaysOnTop: Promise<boolean> | boolean;
	maximized: Promise<boolean> | boolean;
	readonly screen: Promise<SaucerScreen>;
	maxSize: Promise<SaucerSize> | SaucerSize;
	decorations: Promise<'NONE' | 'PARTIAL' | 'FULL'> | 'NONE' | 'PARTIAL' | 'FULL';
	readonly isVisible: Promise<boolean>;
	title: Promise<string> | string;
	minimized: Promise<boolean> | boolean;
	readonly isFocused: Promise<boolean>;
	size: Promise<SaucerSize> | SaucerSize;
	fullscreen: Promise<boolean> | boolean;
	minSize: Promise<SaucerSize> | SaucerSize;
	position: Promise<SaucerPosition> | SaucerPosition;
	clickThrough: Promise<boolean> | boolean;
	hide(): Promise<void>;
	show(): Promise<void>;
	destroy(): Promise<void>;
	focus(): Promise<void>;
}

export declare interface saucer_webview extends MutationObject<never> {
	readonly window: saucer_webview_window;
	devtoolsVisible: Promise<boolean> | boolean;
	backgroundColor: Promise<SaucerColor> | SaucerColor;
	contextMenu: Promise<boolean> | boolean;
	bounds: Promise<SaucerRectangle> | SaucerRectangle;
	readonly title: Promise<string>;
	forceDark: Promise<boolean> | boolean;
	url: Promise<SaucerUrl> | SaucerUrl;
	reload(): Promise<void>;
	forward(): Promise<void>;
	destroy(): Promise<void>;
	back(): Promise<void>;
}
export declare interface saucer_window extends MutationObject<never> {
	backgroundColor: Promise<SaucerColor> | SaucerColor;
	resizable: Promise<boolean> | boolean;
	alwaysOnTop: Promise<boolean> | boolean;
	maximized: Promise<boolean> | boolean;
	readonly screen: Promise<SaucerScreen>;
	maxSize: Promise<SaucerSize> | SaucerSize;
	decorations: Promise<'NONE' | 'PARTIAL' | 'FULL'> | 'NONE' | 'PARTIAL' | 'FULL';
	readonly isVisible: Promise<boolean>;
	title: Promise<string> | string;
	minimized: Promise<boolean> | boolean;
	readonly isFocused: Promise<boolean>;
	size: Promise<SaucerSize> | SaucerSize;
	fullscreen: Promise<boolean> | boolean;
	minSize: Promise<SaucerSize> | SaucerSize;
	position: Promise<SaucerPosition> | SaucerPosition;
	clickThrough: Promise<boolean> | boolean;
	hide(): Promise<void>;
	show(): Promise<void>;
	destroy(): Promise<void>;
	focus(): Promise<void>;
}
export declare interface saucer_app extends MutationObject<never> {
	readonly screens: Promise<SaucerScreen[]>;
	readonly backendType: Promise<'WEBKITGTK' | 'QT6' | 'WEBKIT' | 'WEBVIEW2' | 'CUSTOM'>;
	readonly systemTarget: Promise<string>;
	readonly archTarget: Promise<string>;
	readonly version: Promise<string>;
}
export declare interface App extends MutationObject<'hasUpdate' | 'statusStates'> {
	readonly isTraySupported: Promise<boolean>;
	readonly clientId: Promise<string>;
	readonly hasUpdate: Promise<boolean>;
	readonly buildInfo: Promise<{ versionString: string; author: string; isDev: boolean; commit: string; buildChannel: string; version: string }>;
	readonly isDev: Promise<boolean>;
	readonly statusStates: Promise<any[]>;
	readonly LOCALES: Promise<Record<string, Record<string, any>>>;
	globalTest(
		arg0:
			| 'FOLLOW'
			| 'SUBSCRIPTION'
			| 'USER_UPDATE'
			| 'STREAM_STATUS'
			| 'META'
			| 'VIEWER_JOIN'
			| 'VIEWER_LEAVE'
			| 'VIEWER_LIST'
			| 'VIEWER_COUNT'
			| 'RAID'
			| 'CHANNEL_POINTS'
			| 'CATCHUP'
			| 'CLEARCHAT'
			| 'ROOMSTATE'
			| 'PLATFORM_MESSAGE'
			| 'RICH_MESSAGE'
			| 'LIKE'
			| 'CONNECTION_STATE'
			| 'DONATION'
			| 'CHAT'
	): Promise<void>;
	notify(arg0: string, arg0: Record<string, string>, arg0: 'ERROR' | 'WARNING' | 'INFO' | 'NONE'): Promise<void>;
}
export declare interface AppAuth extends MutationObject<'isAuthorized' | 'authInstances' | 'isKoiAlive'> {
	readonly ALL: Promise<
		| 'CAFFEINE'
		| 'TWITCH'
		| 'TROVO'
		| 'GLIMESH'
		| 'BRIME'
		| 'YOUTUBE'
		| 'DLIVE'
		| 'TIKTOK'
		| 'THETA'
		| 'KICK'
		| 'YOUNOW'
		| 'LIVESPACE'
		| 'NOICE'
		| 'X'
		| 'RUMBLE'
		| 'LOCO'
		| 'CASTERLABS_SYSTEM'
		| 'CUSTOM_INTEGRATION'[]
	>;
	readonly AUTHENTICATABLE: Promise<
		| 'CAFFEINE'
		| 'TWITCH'
		| 'TROVO'
		| 'GLIMESH'
		| 'BRIME'
		| 'YOUTUBE'
		| 'DLIVE'
		| 'TIKTOK'
		| 'THETA'
		| 'KICK'
		| 'YOUNOW'
		| 'LIVESPACE'
		| 'NOICE'
		| 'X'
		| 'RUMBLE'
		| 'LOCO'
		| 'CASTERLABS_SYSTEM'
		| 'CUSTOM_INTEGRATION'[]
	>;
	readonly isAuthorized: Promise<boolean>;
	readonly authInstances: Promise<
		Record<
			string,
			{
				viewers: {
					image_link: string;
					color: string;
					roles: 'BROADCASTER' | 'SUBSCRIBER' | 'FOLLOWER' | 'MODERATOR' | 'STAFF' | 'VIP' | 'OG'[];
					link: string;
					bio: string;
					platform:
						| 'CAFFEINE'
						| 'TWITCH'
						| 'TROVO'
						| 'GLIMESH'
						| 'BRIME'
						| 'YOUTUBE'
						| 'DLIVE'
						| 'TIKTOK'
						| 'THETA'
						| 'KICK'
						| 'YOUNOW'
						| 'LIVESPACE'
						| 'NOICE'
						| 'X'
						| 'RUMBLE'
						| 'LOCO'
						| 'CASTERLABS_SYSTEM'
						| 'CUSTOM_INTEGRATION';
					badges: any;
					UPID: string;
					subscriber_count: number;
					displayname: string;
					followers_count: number;
					pronouns: 'HE' | 'SHE' | 'IT' | 'THEY' | 'ANY' | 'ASK' | 'AVOID' | 'OTHER';
					id: string;
					channel_id: string;
					username: string;
				}[];
				userData: {
					image_link: string;
					color: string;
					roles: 'BROADCASTER' | 'SUBSCRIBER' | 'FOLLOWER' | 'MODERATOR' | 'STAFF' | 'VIP' | 'OG'[];
					link: string;
					bio: string;
					platform:
						| 'CAFFEINE'
						| 'TWITCH'
						| 'TROVO'
						| 'GLIMESH'
						| 'BRIME'
						| 'YOUTUBE'
						| 'DLIVE'
						| 'TIKTOK'
						| 'THETA'
						| 'KICK'
						| 'YOUNOW'
						| 'LIVESPACE'
						| 'NOICE'
						| 'X'
						| 'RUMBLE'
						| 'LOCO'
						| 'CASTERLABS_SYSTEM'
						| 'CUSTOM_INTEGRATION';
					badges: any;
					UPID: string;
					subscriber_count: number;
					displayname: string;
					followers_count: number;
					pronouns: 'HE' | 'SHE' | 'IT' | 'THEY' | 'ANY' | 'ASK' | 'AVOID' | 'OTHER';
					id: string;
					channel_id: string;
					username: string;
				};
				tokenId: string;
				streamData: {
					start_time: {};
					streamer: {
						UPID: string;
						id: string;
						channel_id: string;
						extraMetadata: Record<string, any>;
						platform:
							| 'CAFFEINE'
							| 'TWITCH'
							| 'TROVO'
							| 'GLIMESH'
							| 'BRIME'
							| 'YOUTUBE'
							| 'DLIVE'
							| 'TIKTOK'
							| 'THETA'
							| 'KICK'
							| 'YOUNOW'
							| 'LIVESPACE'
							| 'NOICE'
							| 'X'
							| 'RUMBLE'
							| 'LOCO'
							| 'CASTERLABS_SYSTEM'
							| 'CUSTOM_INTEGRATION';
					};
					is_live: boolean;
					content_rating: 'FAMILY_FRIENDLY' | 'TEEN' | 'EIGHTEEN_PLUS';
					language:
						| 'AB'
						| 'AA'
						| 'AF'
						| 'SQ'
						| 'AM'
						| 'AR'
						| 'HY'
						| 'AS'
						| 'AY'
						| 'AZ'
						| 'BA'
						| 'EU'
						| 'BN'
						| 'DZ'
						| 'BH'
						| 'BI'
						| 'BR'
						| 'BG'
						| 'MY'
						| 'BE'
						| 'KM'
						| 'CA'
						| 'ZH'
						| 'CO'
						| 'HR'
						| 'CS'
						| 'DA'
						| 'NL'
						| 'EN'
						| 'EO'
						| 'ET'
						| 'FO'
						| 'FJ'
						| 'FI'
						| 'FR'
						| 'FY'
						| 'GD'
						| 'GL'
						| 'KA'
						| 'DE'
						| 'EL'
						| 'KL'
						| 'GN'
						| 'GU'
						| 'HA'
						| 'IW'
						| 'HI'
						| 'HU'
						| 'IS'
						| 'IN'
						| 'IA'
						| 'IE'
						| 'IK'
						| 'GA'
						| 'IT'
						| 'JA'
						| 'JW'
						| 'KN'
						| 'KS'
						| 'KK'
						| 'RW'
						| 'KY'
						| 'RN'
						| 'KO'
						| 'KU'
						| 'LO'
						| 'LA'
						| 'LV'
						| 'LN'
						| 'LT'
						| 'MK'
						| 'MG'
						| 'MS'
						| 'ML'
						| 'MT'
						| 'MI'
						| 'MR'
						| 'MO'
						| 'MN'
						| 'NA'
						| 'NE'
						| 'NO'
						| 'OC'
						| 'OR'
						| 'OM'
						| 'PS'
						| 'FA'
						| 'PL'
						| 'PT'
						| 'PA'
						| 'QU'
						| 'RM'
						| 'RO'
						| 'RU'
						| 'SM'
						| 'SG'
						| 'SA'
						| 'SR'
						| 'SH'
						| 'ST'
						| 'TN'
						| 'SN'
						| 'SD'
						| 'SI'
						| 'SS'
						| 'SK'
						| 'SL'
						| 'SO'
						| 'ES'
						| 'SU'
						| 'SW'
						| 'SV'
						| 'TL'
						| 'TG'
						| 'TA'
						| 'TT'
						| 'TE'
						| 'TH'
						| 'BO'
						| 'TI'
						| 'TO'
						| 'TS'
						| 'TR'
						| 'TK'
						| 'TW'
						| 'UK'
						| 'UR'
						| 'UZ'
						| 'VI'
						| 'VO'
						| 'CY'
						| 'WO'
						| 'XH'
						| 'JI'
						| 'YO'
						| 'ZU'
						| 'OTHER';
					title: string;
					category: string;
					thumbnail_url: string;
					tags: string[];
					timestamp: {};
				};
				roomstate: {
					room_id: string;
					streamer: {
						UPID: string;
						id: string;
						channel_id: string;
						extraMetadata: Record<string, any>;
						platform:
							| 'CAFFEINE'
							| 'TWITCH'
							| 'TROVO'
							| 'GLIMESH'
							| 'BRIME'
							| 'YOUTUBE'
							| 'DLIVE'
							| 'TIKTOK'
							| 'THETA'
							| 'KICK'
							| 'YOUNOW'
							| 'LIVESPACE'
							| 'NOICE'
							| 'X'
							| 'RUMBLE'
							| 'LOCO'
							| 'CASTERLABS_SYSTEM'
							| 'CUSTOM_INTEGRATION';
					};
					roomstate: { is_followers_only: boolean; is_r9k: boolean; is_subs_only: boolean; is_emote_only: boolean; is_slowmode: boolean };
					timestamp: {};
				};
				token: string;
			}
		>
	>;
	readonly isKoiAlive: Promise<boolean>;
	requestOAuthSignin(arg0: string, arg0: string, arg0: boolean, arg0: string | null): Promise<void>;
	signout(arg0: string): Promise<void>;
	cancelSignin(): Promise<void>;
	loginPortal(arg0: string, arg0: string, arg0: boolean): Promise<void>;
	getPortalUrl(arg0: string, arg0: string): Promise<string>;
}
export declare interface AppChatbot extends MutationObject<'nextMessageAt'> {
	readonly supportedShoutEvents: Promise<
		| 'FOLLOW'
		| 'SUBSCRIPTION'
		| 'USER_UPDATE'
		| 'STREAM_STATUS'
		| 'META'
		| 'VIEWER_JOIN'
		| 'VIEWER_LEAVE'
		| 'VIEWER_LIST'
		| 'VIEWER_COUNT'
		| 'RAID'
		| 'CHANNEL_POINTS'
		| 'CATCHUP'
		| 'CLEARCHAT'
		| 'ROOMSTATE'
		| 'PLATFORM_MESSAGE'
		| 'RICH_MESSAGE'
		| 'LIKE'
		| 'CONNECTION_STATE'
		| 'DONATION'
		| 'CHAT'[]
	>;
	readonly nextMessageAt: Promise<number>;
}
export declare interface AppConfig extends MutationObject<'chatbotPreferences' | 'uiPreferences' | 'appPreferences' | 'themePreferences'> {
	chatbotPreferences:
		| Promise<{
				timerIntervalSeconds: number;
				shouts: {
					eventType:
						| 'FOLLOW'
						| 'SUBSCRIPTION'
						| 'USER_UPDATE'
						| 'STREAM_STATUS'
						| 'META'
						| 'VIEWER_JOIN'
						| 'VIEWER_LEAVE'
						| 'VIEWER_LIST'
						| 'VIEWER_COUNT'
						| 'RAID'
						| 'CHANNEL_POINTS'
						| 'CATCHUP'
						| 'CLEARCHAT'
						| 'ROOMSTATE'
						| 'PLATFORM_MESSAGE'
						| 'RICH_MESSAGE'
						| 'LIKE'
						| 'CONNECTION_STATE'
						| 'DONATION'
						| 'CHAT';
					text: any;
					responseAction: 'REPLY_WITH' | 'EXECUTE';
					response: string;
					platform: any;
				}[];
				timers: string[];
				chatbots: string[];
				chatter: 'CLIENT' | 'SYSTEM';
				store: Record<string, any>;
				commands: { triggerType: 'COMMAND' | 'CONTAINS' | 'ALWAYS'; trigger: string; type: any; responseAction: 'REPLY_WITH' | 'EXECUTE'; response: string; platform: any }[];
				hideFromChat: boolean;
		  }>
		| {
				timerIntervalSeconds: number;
				shouts: {
					eventType:
						| 'FOLLOW'
						| 'SUBSCRIPTION'
						| 'USER_UPDATE'
						| 'STREAM_STATUS'
						| 'META'
						| 'VIEWER_JOIN'
						| 'VIEWER_LEAVE'
						| 'VIEWER_LIST'
						| 'VIEWER_COUNT'
						| 'RAID'
						| 'CHANNEL_POINTS'
						| 'CATCHUP'
						| 'CLEARCHAT'
						| 'ROOMSTATE'
						| 'PLATFORM_MESSAGE'
						| 'RICH_MESSAGE'
						| 'LIKE'
						| 'CONNECTION_STATE'
						| 'DONATION'
						| 'CHAT';
					text: any;
					responseAction: 'REPLY_WITH' | 'EXECUTE';
					response: string;
					platform: any;
				}[];
				timers: string[];
				chatbots: string[];
				chatter: 'CLIENT' | 'SYSTEM';
				store: Record<string, any>;
				commands: { triggerType: 'COMMAND' | 'CONTAINS' | 'ALWAYS'; trigger: string; type: any; responseAction: 'REPLY_WITH' | 'EXECUTE'; response: string; platform: any }[];
				hideFromChat: boolean;
		  };
	uiPreferences:
		| Promise<{
				emojiProvider: string;
				chatViewerPreferences: {
					showTimestamps: boolean;
					showProfilePictures: boolean;
					showBadges: boolean;
					showPlatform: boolean;
					showPronouns: boolean;
					showActivities: boolean;
					showViewers: boolean;
					showZebraStripes: boolean;
					textSize: number;
					colorBy: string;
					playDingOnMessage: boolean;
					readMessagesAloud: boolean;
					ttsVoice: string;
					ttsOrDingVolume: number;
					inputBoxPreferences: Record<string, any>;
				};
				sidebarClosed: boolean;
				icon: string;
				mainDashboard: { h: number[]; contents: Record<string, string>; v: number[] };
				language: string;
				zoom: number;
				enableStupidlyUnsafeSettings: boolean;
				enableAlternateThemes: boolean;
				activityViewerPreferences: {
					showTimestamps: boolean;
					showProfilePictures: boolean;
					showBadges: boolean;
					showPlatform: boolean;
					showPronouns: boolean;
					showZebraStripes: boolean;
					textSize: number;
					colorBy: string;
				};
				uiFont: string;
				dockDashboard: { h: number[]; contents: Record<string, string>; v: number[] };
				closeToTray: boolean;
				theme: string;
		  }>
		| {
				emojiProvider: string;
				chatViewerPreferences: {
					showTimestamps: boolean;
					showProfilePictures: boolean;
					showBadges: boolean;
					showPlatform: boolean;
					showPronouns: boolean;
					showActivities: boolean;
					showViewers: boolean;
					showZebraStripes: boolean;
					textSize: number;
					colorBy: string;
					playDingOnMessage: boolean;
					readMessagesAloud: boolean;
					ttsVoice: string;
					ttsOrDingVolume: number;
					inputBoxPreferences: Record<string, any>;
				};
				sidebarClosed: boolean;
				icon: string;
				mainDashboard: { h: number[]; contents: Record<string, string>; v: number[] };
				language: string;
				zoom: number;
				enableStupidlyUnsafeSettings: boolean;
				enableAlternateThemes: boolean;
				activityViewerPreferences: {
					showTimestamps: boolean;
					showProfilePictures: boolean;
					showBadges: boolean;
					showPlatform: boolean;
					showPronouns: boolean;
					showZebraStripes: boolean;
					textSize: number;
					colorBy: string;
				};
				uiFont: string;
				dockDashboard: { h: number[]; contents: Record<string, string>; v: number[] };
				closeToTray: boolean;
				theme: string;
		  };
	appPreferences:
		| Promise<{ oneTimeEvents: string[]; conductorPort: number; conductorKey: string; developerApiKey: string; koiUrl: string; installationId: string; isNew: any }>
		| { oneTimeEvents: string[]; conductorPort: number; conductorKey: string; developerApiKey: string; koiUrl: string; installationId: string; isNew: any };
	themePreferences:
		| Promise<{ primaryColor: string; appearance: 'FOLLOW_SYSTEM' | 'LIGHT' | 'DARK'; baseColor: string }>
		| { primaryColor: string; appearance: 'FOLLOW_SYSTEM' | 'LIGHT' | 'DARK'; baseColor: string };
	/** write-only */
	authPreferences: { tokensMap: Record<string, Record<string, any>> };
	canDoOneTimeEvent(arg0: string): Promise<boolean>;
}
export declare interface AppLocale extends MutationObject<'current' | 'fallback'> {
	readonly current: Promise<Record<string, any>>;
	readonly available: Promise<Record<string, string>>;
	readonly fallback: Promise<Record<string, any>>;
}
export declare interface AppPlugins extends MutationObject<'loadedPlugins' | 'contexts'> {
	readonly loadedPlugins: Promise<{}[]>;
	readonly creatableWidgets: Promise<
		{
			requiredFeatures: (
				| 'UPDATE_STREAM_INFO'
				| 'STREAM_INFO'
				| 'PUBLISHING_INFO'
				| 'UPDATE_ROOM_STATE'
				| 'CHAT_BOT_LINKING'
				| 'CHANNEL_POINTS'
				| 'HYPE_TRAIN'
				| 'STREAM_KEY'
				| 'ADVERTISEMENTS'
				| 'DONATION_ALERT'
				| 'FOLLOWER_ALERT'
				| 'SUBSCRIPTION_ALERT'
				| 'RAID_ALERT'
				| 'FOLLOWER_COUNT'
				| 'SUBSCRIBER_COUNT'
				| 'CHAT'
				| 'STREAM_STATUS'
				| 'ROOMSTATE'
				| 'VIEWERS_LIST'
				| 'VIEWERS_COUNT'
				| 'VIEWERS_PRESENCE'
				| 'MESSAGE_UPVOTE'
				| 'MESSAGE_REACTION'
				| 'MESSAGE_DELETION'
				| 'CHAT_SEND_MESSAGE'
				| 'CHAT_SEND_COMMAND'
			)[];
			showDemo: boolean;
			testEvents:
				| 'FOLLOW'
				| 'SUBSCRIPTION'
				| 'USER_UPDATE'
				| 'STREAM_STATUS'
				| 'META'
				| 'VIEWER_JOIN'
				| 'VIEWER_LEAVE'
				| 'VIEWER_LIST'
				| 'VIEWER_COUNT'
				| 'RAID'
				| 'CHANNEL_POINTS'
				| 'CATCHUP'
				| 'CLEARCHAT'
				| 'ROOMSTATE'
				| 'PLATFORM_MESSAGE'
				| 'RICH_MESSAGE'
				| 'LIKE'
				| 'CONNECTION_STATE'
				| 'DONATION'
				| 'CHAT'[];
			namespace: string;
			icon: string;
			localeBase: string;
			demoAspectRatio: number;
			category: 'ALERTS' | 'LABELS' | 'INTERACTION' | 'GOALS' | 'OTHER';
			type: 'WIDGET' | 'DOCK' | 'APPLET' | 'SETTINGS_APPLET';
			friendlyName: string;
		}[]
	>;
	readonly contexts: Promise<{ pluginIds: string[]; file: {}; pluginType: 'PLUGIN' | 'INTERNAL' | 'STORE_ASSET'; id: string; hasSucceeded: boolean }[]>;
	readonly widgets: Promise<
		{
			settings: Record<string, any>;
			settingsLayout: {
				buttons: { iconTitle: string; icon: string; id: string; text: string }[];
				sections: {
					name: string;
					id: string;
					items: {
						name: string;
						id: string;
						type: 'CHECKBOX' | 'COLOR' | 'NUMBER' | 'DROPDOWN' | 'TEXT' | 'TEXTAREA' | 'CODE' | 'PASSWORD' | 'CURRENCY' | 'FONT' | 'RANGE' | 'FILE' | 'PLATFORM_DROPDOWN';
						extraData: Record<string, any>;
					}[];
				}[];
				allowWidgetPreview: boolean;
			};
			namespace: string;
			name: string;
			details: {
				requiredFeatures:
					| 'UPDATE_STREAM_INFO'
					| 'STREAM_INFO'
					| 'PUBLISHING_INFO'
					| 'UPDATE_ROOM_STATE'
					| 'CHAT_BOT_LINKING'
					| 'CHANNEL_POINTS'
					| 'HYPE_TRAIN'
					| 'STREAM_KEY'
					| 'ADVERTISEMENTS'
					| 'DONATION_ALERT'
					| 'FOLLOWER_ALERT'
					| 'SUBSCRIPTION_ALERT'
					| 'RAID_ALERT'
					| 'FOLLOWER_COUNT'
					| 'SUBSCRIBER_COUNT'
					| 'CHAT'
					| 'STREAM_STATUS'
					| 'ROOMSTATE'
					| 'VIEWERS_LIST'
					| 'VIEWERS_COUNT'
					| 'VIEWERS_PRESENCE'
					| 'MESSAGE_UPVOTE'
					| 'MESSAGE_REACTION'
					| 'MESSAGE_DELETION'
					| 'CHAT_SEND_MESSAGE'
					| 'CHAT_SEND_COMMAND'[];
				showDemo: boolean;
				testEvents:
					| 'FOLLOW'
					| 'SUBSCRIPTION'
					| 'USER_UPDATE'
					| 'STREAM_STATUS'
					| 'META'
					| 'VIEWER_JOIN'
					| 'VIEWER_LEAVE'
					| 'VIEWER_LIST'
					| 'VIEWER_COUNT'
					| 'RAID'
					| 'CHANNEL_POINTS'
					| 'CATCHUP'
					| 'CLEARCHAT'
					| 'ROOMSTATE'
					| 'PLATFORM_MESSAGE'
					| 'RICH_MESSAGE'
					| 'LIKE'
					| 'CONNECTION_STATE'
					| 'DONATION'
					| 'CHAT'[];
				namespace: string;
				icon: string;
				localeBase: string;
				demoAspectRatio: number;
				category: 'ALERTS' | 'LABELS' | 'INTERACTION' | 'GOALS' | 'OTHER';
				type: 'WIDGET' | 'DOCK' | 'APPLET' | 'SETTINGS_APPLET';
				friendlyName: string;
			};
			id: string;
			tag: string;
			url: string;
		}[]
	>;
	fireTestEvent(
		arg0: string,
		arg0:
			| 'FOLLOW'
			| 'SUBSCRIPTION'
			| 'USER_UPDATE'
			| 'STREAM_STATUS'
			| 'META'
			| 'VIEWER_JOIN'
			| 'VIEWER_LEAVE'
			| 'VIEWER_LIST'
			| 'VIEWER_COUNT'
			| 'RAID'
			| 'CHANNEL_POINTS'
			| 'CATCHUP'
			| 'CLEARCHAT'
			| 'ROOMSTATE'
			| 'PLATFORM_MESSAGE'
			| 'RICH_MESSAGE'
			| 'LIKE'
			| 'CONNECTION_STATE'
			| 'DONATION'
			| 'CHAT'
	): Promise<void>;
	assignTag(arg0: string, arg0: string): Promise<void>;
	openPopout(arg0: string): Promise<void>;
	deleteWidget(arg0: string): Promise<void>;
	openPluginsDir(): Promise<void>;
	clickWidgetSettingsButton(arg0: string, arg0: string): Promise<void>;
	load(arg0: string): Promise<void>;
	createNewWidget(arg0: string, arg0: string): Promise<string>;
	renameWidget(arg0: string, arg0: string): Promise<void>;
	unload(arg0: string): Promise<void>;
	listFiles(): Promise<string[]>;
	copyWidgetUrl(arg0: string): Promise<void>;
	editWidgetSettingsItem(arg0: string, arg0: string, arg0: any): Promise<void>;
}
export declare interface AppSounds extends MutationObject<never> {
	playUrl(arg0: string, arg0: number): Promise<void>;
}
export declare interface AppThemeManager extends MutationObject<'effectiveAppearance'> {
	readonly effectiveAppearance: Promise<'FOLLOW_SYSTEM' | 'LIGHT' | 'DARK'>;
}
export declare interface AppUI extends MutationObject<never> {
	readonly fonts: Promise<string[]>;
	showToast(arg0: string, arg0: 'ERROR' | 'WARNING' | 'INFO' | 'NONE'): Promise<void>;
	onUILoaded(): Promise<void>;
	updateDashboard(arg0: { h: number[]; contents: Record<string, string>; v: number[] }, arg0: boolean): Promise<void>;
}
export declare interface Caffeinated extends MutationObject<never> {
	copyText(arg0: string, arg0: string | null): Promise<void>;
	openLink(arg0: string): Promise<void>;
	getLocale(): Promise<string>;
	getMimeForPath(arg0: string): Promise<string>;
}
export declare interface Emojis extends MutationObject<never> {
	matchAndReturnHTML(arg0: string, arg0: boolean): Promise<string>;
}
export declare interface Koi extends MutationObject<'roomStates' | 'features' | 'viewers' | 'userStates' | 'connectionStates' | 'viewerCounts' | 'streamStates'> {
	readonly roomStates: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			{
				room_id: string;
				streamer: {
					UPID: string;
					id: string;
					channel_id: string;
					extraMetadata: Record<string, any>;
					platform:
						| 'CAFFEINE'
						| 'TWITCH'
						| 'TROVO'
						| 'GLIMESH'
						| 'BRIME'
						| 'YOUTUBE'
						| 'DLIVE'
						| 'TIKTOK'
						| 'THETA'
						| 'KICK'
						| 'YOUNOW'
						| 'LIVESPACE'
						| 'NOICE'
						| 'X'
						| 'RUMBLE'
						| 'LOCO'
						| 'CASTERLABS_SYSTEM'
						| 'CUSTOM_INTEGRATION';
				};
				roomstate: { is_followers_only: boolean; is_r9k: boolean; is_subs_only: boolean; is_emote_only: boolean; is_slowmode: boolean };
				timestamp: {};
			}
		>
	>;
	readonly features: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			any
		>
	>;
	readonly viewers: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			any
		>
	>;
	readonly userStates: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			{ streamer: any; timestamp: {} }
		>
	>;
	readonly connectionStates: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			any
		>
	>;
	readonly viewerCounts: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			number
		>
	>;
	readonly streamStates: Promise<
		Record<
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
			{
				start_time: {};
				streamer: {
					UPID: string;
					id: string;
					channel_id: string;
					extraMetadata: Record<string, any>;
					platform:
						| 'CAFFEINE'
						| 'TWITCH'
						| 'TROVO'
						| 'GLIMESH'
						| 'BRIME'
						| 'YOUTUBE'
						| 'DLIVE'
						| 'TIKTOK'
						| 'THETA'
						| 'KICK'
						| 'YOUNOW'
						| 'LIVESPACE'
						| 'NOICE'
						| 'X'
						| 'RUMBLE'
						| 'LOCO'
						| 'CASTERLABS_SYSTEM'
						| 'CUSTOM_INTEGRATION';
				};
				is_live: boolean;
				content_rating: 'FAMILY_FRIENDLY' | 'TEEN' | 'EIGHTEEN_PLUS';
				language:
					| 'AB'
					| 'AA'
					| 'AF'
					| 'SQ'
					| 'AM'
					| 'AR'
					| 'HY'
					| 'AS'
					| 'AY'
					| 'AZ'
					| 'BA'
					| 'EU'
					| 'BN'
					| 'DZ'
					| 'BH'
					| 'BI'
					| 'BR'
					| 'BG'
					| 'MY'
					| 'BE'
					| 'KM'
					| 'CA'
					| 'ZH'
					| 'CO'
					| 'HR'
					| 'CS'
					| 'DA'
					| 'NL'
					| 'EN'
					| 'EO'
					| 'ET'
					| 'FO'
					| 'FJ'
					| 'FI'
					| 'FR'
					| 'FY'
					| 'GD'
					| 'GL'
					| 'KA'
					| 'DE'
					| 'EL'
					| 'KL'
					| 'GN'
					| 'GU'
					| 'HA'
					| 'IW'
					| 'HI'
					| 'HU'
					| 'IS'
					| 'IN'
					| 'IA'
					| 'IE'
					| 'IK'
					| 'GA'
					| 'IT'
					| 'JA'
					| 'JW'
					| 'KN'
					| 'KS'
					| 'KK'
					| 'RW'
					| 'KY'
					| 'RN'
					| 'KO'
					| 'KU'
					| 'LO'
					| 'LA'
					| 'LV'
					| 'LN'
					| 'LT'
					| 'MK'
					| 'MG'
					| 'MS'
					| 'ML'
					| 'MT'
					| 'MI'
					| 'MR'
					| 'MO'
					| 'MN'
					| 'NA'
					| 'NE'
					| 'NO'
					| 'OC'
					| 'OR'
					| 'OM'
					| 'PS'
					| 'FA'
					| 'PL'
					| 'PT'
					| 'PA'
					| 'QU'
					| 'RM'
					| 'RO'
					| 'RU'
					| 'SM'
					| 'SG'
					| 'SA'
					| 'SR'
					| 'SH'
					| 'ST'
					| 'TN'
					| 'SN'
					| 'SD'
					| 'SI'
					| 'SS'
					| 'SK'
					| 'SL'
					| 'SO'
					| 'ES'
					| 'SU'
					| 'SW'
					| 'SV'
					| 'TL'
					| 'TG'
					| 'TA'
					| 'TT'
					| 'TE'
					| 'TH'
					| 'BO'
					| 'TI'
					| 'TO'
					| 'TS'
					| 'TR'
					| 'TK'
					| 'TW'
					| 'UK'
					| 'UR'
					| 'UZ'
					| 'VI'
					| 'VO'
					| 'CY'
					| 'WO'
					| 'XH'
					| 'JI'
					| 'YO'
					| 'ZU'
					| 'OTHER';
				title: string;
				category: string;
				thumbnail_url: string;
				tags: string[];
				timestamp: {};
			}
		>
	>;
	sendChat(
		arg0:
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
		arg0: string,
		arg0: 'CLIENT' | 'SYSTEM',
		arg0: string | null,
		arg0: boolean
	): Promise<void>;
	getEventHistory(beforeTimestamp: number): Promise<
		{
			streamer: {
				UPID: string;
				id: string;
				channel_id: string;
				extraMetadata: Record<string, any>;
				platform:
					| 'CAFFEINE'
					| 'TWITCH'
					| 'TROVO'
					| 'GLIMESH'
					| 'BRIME'
					| 'YOUTUBE'
					| 'DLIVE'
					| 'TIKTOK'
					| 'THETA'
					| 'KICK'
					| 'YOUNOW'
					| 'LIVESPACE'
					| 'NOICE'
					| 'X'
					| 'RUMBLE'
					| 'LOCO'
					| 'CASTERLABS_SYSTEM'
					| 'CUSTOM_INTEGRATION';
			};
			timestamp: {};
		}[]
	>;
	deleteChat(
		arg0:
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
		arg0: string,
		arg0: boolean
	): Promise<void>;
	upvoteChat(
		arg0:
			| 'CAFFEINE'
			| 'TWITCH'
			| 'TROVO'
			| 'GLIMESH'
			| 'BRIME'
			| 'YOUTUBE'
			| 'DLIVE'
			| 'TIKTOK'
			| 'THETA'
			| 'KICK'
			| 'YOUNOW'
			| 'LIVESPACE'
			| 'NOICE'
			| 'X'
			| 'RUMBLE'
			| 'LOCO'
			| 'CASTERLABS_SYSTEM'
			| 'CUSTOM_INTEGRATION',
		arg0: string
	): Promise<void>;
}
export declare interface Music extends MutationObject<'activePlayback' | 'providers'> {
	readonly activePlayback: Promise<{
		settings: any;
		currentTrack: { link: string; title: string; artists: string[]; albumArtUrl: string; album: string };
		accountName: string;
		isSignedIn: boolean;
		playbackState: 'PLAYING' | 'PAUSED' | 'INACTIVE';
		settingsClass: {};
		accountLink: string;
		serviceName: string;
		serviceId: string;
	}>;
	readonly providers: Promise<Record<string, any>>;
	signoutMusicProvider(arg0: string): Promise<void>;
	updateMusicProviderSettings(arg0: string, arg0: Record<string, any>): Promise<void>;
}

declare global {
	const App: App;
	const AppAuth: AppAuth;
	const AppChatbot: AppChatbot;
	const AppConfig: AppConfig;
	const AppLocale: AppLocale;
	const AppPlugins: AppPlugins;
	const AppSounds: AppSounds;
	const AppThemeManager: AppThemeManager;
	const AppUI: AppUI;
	const Caffeinated: Caffeinated;
	const Emojis: Emojis;
	const Koi: Koi;
	const Music: Music;
	const saucer: { window: saucer_window; webview: saucer_webview; app: saucer_app };
	interface Window {
		readonly App: App;
		readonly AppAuth: AppAuth;
		readonly AppChatbot: AppChatbot;
		readonly AppConfig: AppConfig;
		readonly AppLocale: AppLocale;
		readonly AppPlugins: AppPlugins;
		readonly AppSounds: AppSounds;
		readonly AppThemeManager: AppThemeManager;
		readonly AppUI: AppUI;
		readonly Caffeinated: Caffeinated;
		readonly Emojis: Emojis;
		readonly Koi: Koi;
		readonly Music: Music;
		readonly saucer: { window: saucer_window; webview: saucer_webview; app: saucer_app };
	}
}
