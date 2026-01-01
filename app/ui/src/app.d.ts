// See https://svelte.dev/docs/kit/types#app.d.ts
// for information about these interfaces

// Auto-generated Saucer Bridge Definitions
// Generated on 2026-01-01T06:44:19.359218700Z

export declare type MutationListenerId = any;
export declare interface MutationObject<M> {
	onMutate(propertyName: M, handler: (newValue: any) => void): MutationListenerId;
	offMutate(id: MutationListenerId): void;
}
export declare type SaucerUrl = string;
export declare type SaucerWindowDecoration = 'NONE' | 'PARTIAL' | 'FULL';
export declare type SaucerBackendType = 'WEBKITGTK' | 'QT6' | 'WEBKIT' | 'WEBVIEW2' | 'CUSTOM';
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
	decorations: Promise<SaucerWindowDecoration> | SaucerWindowDecoration;
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
	decorations: Promise<SaucerWindowDecoration> | SaucerWindowDecoration;
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
	readonly backendType: Promise<SaucerBackendType>;
	readonly systemTarget: Promise<string>;
	readonly archTarget: Promise<string>;
	readonly version: Promise<string>;
}
export declare interface App extends MutationObject<'statusStates' | 'hasUpdate'> {
	readonly isTraySupported: Promise<boolean>;
	readonly clientId: Promise<string>;
	readonly buildInfo: Promise<BuildInfo>;
	readonly isDev: Promise<boolean>;
	readonly statusStates: Promise<StatusState[]>;
	readonly LOCALES: Promise<Record<any, any>>;
	readonly hasUpdate: Promise<boolean>;
	globalTest(arg0: KoiEventType): Promise<void>;
	notify(arg0: string, arg1: Record<any, any>, arg2: NotificationType): Promise<void>;
}
export declare interface AppAuth extends MutationObject<'isAuthorized' | 'authInstances' | 'isKoiAlive'> {
	readonly isAuthorized: Promise<boolean>;
	readonly authInstances: Promise<Record<any, any>>;
	readonly isKoiAlive: Promise<boolean>;
	requestOAuthSignin(arg0: string, arg1: string, arg2: boolean, arg3: string): Promise<void>;
	signout(arg0: string): Promise<void>;
	cancelSignin(): Promise<void>;
	getPortalUrl(arg0: string, arg1: string): Promise<string>;
	loginPortal(arg0: string, arg1: string, arg2: boolean): Promise<void>;
}
export declare interface AppChatbot extends MutationObject<'nextMessageAt'> {
	readonly supportedShoutEvents: Promise<any[]>;
	readonly nextMessageAt: Promise<number>;
}
export declare interface AppConfig extends MutationObject<'chatbotPreferences' | 'uiPreferences' | 'appPreferences' | 'authPreferences' | 'themePreferences'> {
	chatbotPreferences: Promise<ChatbotPreferences> | ChatbotPreferences;
	uiPreferences: Promise<UIPreferences> | UIPreferences;
	appPreferences: Promise<AppPreferences> | AppPreferences;
	authPreferences: Promise<AuthPreferences> | AuthPreferences;
	themePreferences: Promise<ThemePreferences> | ThemePreferences;
	canDoOneTimeEvent(event: string): Promise<boolean>;
}
export declare interface AppPlugins extends MutationObject<'loadedPlugins' | 'contexts'> {
	readonly loadedPlugins: Promise<any[]>;
	readonly creatableWidgets: Promise<any[]>;
	readonly contexts: Promise<any[]>;
	readonly widgets: Promise<any[]>;
	fireTestEvent(arg0: string, arg1: KoiEventType): Promise<void>;
	assignTag(arg0: string, arg1: string): Promise<void>;
	openPopout(arg0: string): Promise<void>;
	deleteWidget(arg0: string): Promise<void>;
	clickWidgetSettingsButton(arg0: string, arg1: string): Promise<void>;
	openPluginsDir(): Promise<void>;
	load(arg0: string): Promise<void>;
	createNewWidget(arg0: string, arg1: string): Promise<string>;
	renameWidget(arg0: string, arg1: string): Promise<void>;
	unload(arg0: string): Promise<void>;
	listFiles(): Promise<any[]>;
	editWidgetSettingsItem(arg0: string, arg1: string, arg2: JsonElement): Promise<void>;
	copyWidgetUrl(arg0: string): Promise<void>;
}
export declare interface AppSounds extends MutationObject<never> {
	playUrl(arg0: string, arg1: number): Promise<void>;
}
export declare interface AppThemeManager extends MutationObject<'effectiveAppearance'> {
	readonly effectiveAppearance: Promise<Appearance>;
}
export declare interface AppUI extends MutationObject<never> {
	readonly fonts: Promise<any[]>;
	showToast(arg0: string, arg1: NotificationType): Promise<void>;
	onUILoaded(): Promise<void>;
	updateDashboard(arg0: DashboardConfig, arg1: boolean): Promise<void>;
}
export declare interface Caffeinated extends MutationObject<never> {
	localize(arg0: string, arg1: JsonObject, arg2: JsonArray): Promise<string>;
	copyText(arg0: string, arg1: string): Promise<void>;
	openLink(arg0: string): Promise<void>;
	getLocale(): Promise<string>;
	getMimeForPath(arg0: string): Promise<string>;
}
export declare interface Emojis extends MutationObject<never> {
	matchAndReturnHTML(arg0: string, arg1: boolean): Promise<string>;
}
export declare interface Koi extends MutationObject<'roomStates' | 'features' | 'viewers' | 'userStates' | 'connectionStates' | 'viewerCounts' | 'streamStates'> {
	readonly roomStates: Promise<Record<any, any>>;
	readonly features: Promise<Record<any, any>>;
	readonly viewers: Promise<Record<any, any>>;
	readonly userStates: Promise<Record<any, any>>;
	readonly eventHistory: Promise<any[]>;
	readonly connectionStates: Promise<Record<any, any>>;
	readonly viewerCounts: Promise<Record<any, any>>;
	readonly streamStates: Promise<Record<any, any>>;
	sendChat(arg0: UserPlatform, arg1: string, arg2: KoiChatterType, arg3: string, arg4: boolean): Promise<void>;
	deleteChat(arg0: UserPlatform, arg1: string, arg2: boolean): Promise<void>;
	upvoteChat(arg0: UserPlatform, arg1: string): Promise<void>;
}
export declare interface Music extends MutationObject<'activePlayback' | 'providers'> {
	readonly activePlayback: Promise<AbstractMusicProvider>;
	readonly providers: Promise<Record<any, any>>;
	signoutMusicProvider(arg0: string): Promise<void>;
	updateMusicProviderSettings(arg0: string, arg1: JsonObject): Promise<void>;
}
export declare interface AppLocale extends MutationObject<'current'> {
	readonly fallback: Promise<any>;
	readonly current: Promise<any>;
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
}

export declare interface UIPreferences {
	emojiProvider: string;
	icon: string;
	theme: string;
	language: string;
	closeToTray: boolean;
	enableStupidlyUnsafeSettings: boolean;
	enableAlternateThemes: boolean;
	zoom: number;
	uiFont: string;
	sidebarClosed: boolean;

	mainDashboard: any; // TODO
	dockDashboard: any;
	chatViewerPreferences: any;
	activityViewerPreferences: any;
}

export declare interface ThemePreferences {
	baseColor: string;
	primaryColor: string;
	appearance: Appearance;
}

export declare interface StatusState {
	status: 'OPERATIONAL' | 'MAJOR_OUTAGE' | 'MINOR_OUTAGE' | 'PARTIAL_OUTAGE' | 'DEGRADED_PERFORMANCE' | 'MAINTENANCE';
	activeIncidents: { link: string }[];
}

export declare type Appearance = 'LIGHT' | 'DARK' | 'SYSTEM';

export declare interface AppPreferences {
	conductorPort: number;
	conductorKey: string;
	developerApiKey: string;
	installationId: string;
	koiUrl: string;
}

export {};
