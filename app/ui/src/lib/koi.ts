export const PLATFORM_COLORS: Record<UserPlatform, string> = {
	CAFFEINE: '#0000ff',
	TWITCH: '#8838ff',
	TROVO: '#149b53',
	GLIMESH: '#33528b',
	BRIME: 'linear-gradient(45deg, #8439af 15%, #fc3537 65%)',
	YOUTUBE: '#ff0000',
	DLIVE: '#ccc121',
	TIKTOK: '#e5004e',
	THETA: '#161a24',
	KICK: '#53fc18',
	YOUNOW: '#46d684',
	LIVESPACE: '#fe0070',
	NOICE: '#b914ff',
	LOCO: '#ff5a12',
	FOURTHWALL: '#0042ff',
	X: '#000000',
	RUMBLE: '#8cc736',
	CASTERLABS_SYSTEM: '#ea4c4c',
	CUSTOM_INTEGRATION: '#00aff1'
};

export declare interface KoiStatics {
	history: KoiEvent[];
	viewers: Record<UserPlatform, User[]>;
	viewerCounts: Record<UserPlatform, number>;
	userStates: Record<UserPlatform, UserUpdateEvent>;
	streamStates: Record<UserPlatform, StreamStatusEvent>;
	roomStates: Record<UserPlatform, RoomstateEvent>;
	features: Record<UserPlatform, string[]>;
	connectionStates: Record<UserPlatform, Record<string, ConnectionState>>;
}

/* ------------------------ */
/*         Actions          */
/* ------------------------ */

export const HAS_SENDING: UserPlatform[] = [
	//
	'DLIVE',
	'KICK',
	'TROVO',
	'TWITCH',
	'YOUTUBE'
];

export const HAS_DELETE: UserPlatform[] = [
	//
	'DLIVE',
	'TROVO',
	'TWITCH',
	'YOUTUBE'
];

export const HAS_BAN: UserPlatform[] = [
	//
	'TROVO',
	'TWITCH'
];

export const HAS_TIMEOUT: UserPlatform[] = [
	//
	'TWITCH'
];

/* ------------------------ */
/*   Publishing & Stream    */
/* ------------------------ */

export declare interface PublishingInfo {
	protocol: 'RTMP' | 'WHIP' | 'SRT';
	url: string;
	key: string;
	name: string;
	id: string;
}

export declare type StreamConfigurationFeature =
	| 'TITLE'
	| 'DESCRIPTION'
	| 'CATEGORY'
	| 'LANGUAGE'
	| 'FREEFORM_TAGS'
	| 'FIXED_TAGS'
	| 'THUMBNAIL'
	| 'CONTENT_RATING'
	| 'CLASSIFICATIONS'
	| 'PRIVACY'
	| 'PUBLISHING_BINDING'
	| 'CREATION'
	| 'DELETION';

export declare interface StreamConfiguration {
	title: string | null;
	description: string | null;
	category: string | null;
	language: StreamLanguage | null;
	tags: string[] | null;
	classifications: StreamConfigurationClassification[] | null;
	privacy: StreamPrivacy | null;
	content_rating: StreamContentRating | null;
	thumbnail_url: string | null;
	publishing_id: string | null;

	category_name: string | null; // Doesn't need to be sent to koi.
}

export declare enum StreamPrivacy {
	PUBLIC = 'PUBLIC',
	UNLISTED = 'UNLISTED',
	PRIVATE = 'PRIVATE'
}

export declare enum StreamContentRating {
	FAMILY_FRIENDLY = 'FAMILY_FRIENDLY',
	TEEN = 'TEEN',
	EIGHTEEN_PLUS = 'EIGHTEEN_PLUS'
}

export declare enum StreamConfigurationClassification {
	GAMBLING = 'GAMBLING',
	POLITICS = 'POLITICS',
	PROFANITY = 'PROFANITY',
	SEXUAL = 'SEXUAL',
	VIOLENCE = 'VIOLENCE',
	PAID_ADVERTISEMENT = 'PAID_ADVERTISEMENT',

	/**
	 * Only use if the platform DOES NOT make a distinction between DRUGS and
	 * DRINKING.
	 */
	INTOXICATION = 'INTOXICATION',

	/**
	 * Only use if the platform makes the distinction between DRUGS and DRINKING.
	 */
	DRUGS = 'DRUGS',
	/**
	 * Only use if the platform makes the distinction between DRUGS and DRINKING.
	 */
	DRINKING = 'DRINKING'
}

export declare type StreamLanguage = string; // TODO

/* ------------------------ */
/*           User           */
/* ------------------------ */

export declare type UPID = string;

export declare type UserPlatform =
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
	| 'LOCO'
	| 'FOURTHWALL'
	| 'X'
	| 'RUMBLE'
	| 'CASTERLABS_SYSTEM'
	| 'CUSTOM_INTEGRATION';

export declare type UserRole = 'BROADCASTER' | 'SUBSCRIBER' | 'FOLLOWER' | 'MODERATOR' | 'STAFF' | 'VIP' | 'OG';

export declare interface SimpleProfile {
	UPID: UPID;
	id: string;
	channel_id: string;
	platform: UserPlatform;
}

export declare interface User extends SimpleProfile {
	color: string;
	username: string;
	displayname: string;
	bio: string;
	link: string;
	image_link: string;
	followers_count: number;
	subscriber_count: number;

	roles: UserRole[];

	badges: string[];
}

/* ------------------------ */
/*          Event           */
/* ------------------------ */

export declare type KoiEvent =
	| CatchupEvent
	| ChannelPointsEvent
	| ClearChatEvent
	| ConnectionStateEvent
	| FollowEvent
	| LikeEvent
	| PlatformMessageEvent
	| RaidEvent
	| RichMessageEvent
	| RoomstateEvent
	| StreamStatusEvent
	| SubscriptionEvent
	| UserUpdateEvent
	| ViewerCountEvent
	| ViewerJoinLeaveEvent
	| ViewerJoinLeaveEvent
	| ViewerListEvent
	| PurchaseEvent;

export declare type KoiEventType =
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
	| 'PURCHASE'
	| 'CONNECTION_STATE';

declare interface AbstractKoiEvent {
	streamer: SimpleProfile;
	timestamp: string;
	event_type: KoiEventType;
	x_cleared: boolean;
}

export declare interface CatchupEvent extends AbstractKoiEvent {
	event_type: 'CATCHUP';
	events: KoiEvent[];
}

export declare type ChannelPointsRewardEventId = string;
export declare type ChannelPointsRewardId = string;
export declare interface ChannelPointsRewardRedemption {
	id: ChannelPointsRewardId;
	title: string;
	cost: number;
	background_color: string;
	reward_image: string | null;
	default_reward_image: string;
	/**
	 * Null if disabled, see user_input.
	 */
	prompt: string | null;
	/**
	 * Null if disabled, see prompt.
	 */
	user_input: string | null;
}
export declare interface ChannelPointsEvent extends AbstractKoiEvent {
	event_type: 'CHANNEL_POINTS';
	sender: User;
	id: ChannelPointsRewardEventId;
	reward: ChannelPointsRewardRedemption;
}

export declare type ClearChatType = 'ALL' | 'USER';
export declare interface ClearChatEvent extends AbstractKoiEvent {
	event_type: 'CLEARCHAT';
	clear_type: ClearChatType;
	/**
	 * `null` if clearType == 'ALL'.
	 */
	user_upid: UPID | null;
}

export declare type ConnectionState = 'CONNECTED' | 'DISCONNECTED' | 'WAITING';
export declare interface ConnectionStateEvent extends AbstractKoiEvent {
	event_type: 'CONNECTION_STATE';
	states: { [key: string]: ConnectionState };
}

export declare interface FollowEvent extends AbstractKoiEvent {
	event_type: 'FOLLOW';
	follower: User;
}

export declare interface LikeEvent extends AbstractKoiEvent {
	event_type: 'LIKE';
	/**
	 * May be `null` on platforms that do not tell us who liked.
	 */
	liker: User | null;
	total_likes: number;
}

export declare interface RaidEvent extends AbstractKoiEvent {
	event_type: 'RAID';
	host: User;
	/**
	 * May be 0 on platforms that do not expose this data.
	 */
	viewers: number;
}

export declare interface RoomstateEvent extends AbstractKoiEvent {
	event_type: 'ROOMSTATE';
	is_emote_only: boolean;
	is_subs_only: boolean;
	is_r9k: boolean;
	is_followers_only: boolean;
	is_slowmode: boolean;
}

export declare interface StreamStatusEvent extends AbstractKoiEvent {
	event_type: 'STREAM_STATUS';
	streams: Record<string, StreamConfiguration>;

	// deprecated
	is_live: boolean;
	title: string;
	start_time: string;
	tags: String[];
	category: string;
	contentRating: StreamContentRating;
	thumbnail_url: string;
	language: string;
}

export declare type SubscriptionType = 'SUB' | 'RESUB' | 'SUBGIFT' | 'RESUBGIFT' | 'ANONSUBGIFT' | 'ANONRESUBGIFT';
export declare type SubscriptionLevel = 'UNKNOWN' | 'TWITCH_PRIME' | 'TIER_1' | 'TIER_2' | 'TIER_3' | 'TIER_4' | 'TIER_5';
export declare interface SubscriptionEvent extends AbstractKoiEvent {
	event_type: 'SUBSCRIPTION';
	/**
	 * The user who paid for the subscription.
	 */
	subscriber: User;
	sub_type: SubscriptionType;
	sub_level: SubscriptionLevel;
	/**
	 * Null if not SUBGIFT or RESUBGIFT.
	 */
	gift_recipient: User;
	/**
	 * Note that this is unknowable on some platforms, like TikTok. In that case, it
	 * will always be 1.
	 */
	months_purchased: number;
	/**
	 * Note that this is unknowable on some platforms, like TikTok. In that case, it
	 * will always be 1.
	 */
	months_streak: number;
	/**
	 * Note that this is unknowable on some platforms, like TikTok. In that case, it
	 * will always be 1.
	 */
	months_cumulative: number;
}

export declare interface UserUpdateEvent extends AbstractKoiEvent {
	event_type: 'USER_UPDATE';
	streamer: User;
}

export declare interface ViewerListEvent extends AbstractKoiEvent {
	event_type: 'VIEWER_LIST';
	viewers: User[];
}

export declare interface ViewerJoinLeaveEvent extends AbstractKoiEvent {
	event_type: 'VIEWER_JOIN' | 'VIEWER_LEAVE';
	viewer: User;
}

export declare interface ViewerCountEvent extends AbstractKoiEvent {
	event_type: 'VIEWER_COUNT';
	count: number;
}

export declare interface PurchaseEvent extends AbstractKoiEvent {
	event_type: 'PURCHASE';
	purchaser: User;
	products: ProductInfo[];
	note: string | null;
	currency: string;
	total_amount: number;
}

/* ------------------------ */
/*     Message Events       */
/* ------------------------ */

/**
 * For RP_ACTION, see Twitch's /me command.
 */
export declare type MessageAttribute = 'HIGHLIGHTED' | 'RP_ACTION' | 'FIRST_TIME_CHATTER' | 'ANNOUNCEMENT';

export declare type MessageId = string;
export declare type MetaId = string;

declare interface AbstractMessageMetaKoiEvent extends AbstractKoiEvent {
	meta_id: MetaId;
	is_visible: boolean;
	upvotes: number;
	attributes: MessageAttribute[];
}

export declare interface MessageMetaEvent extends AbstractMessageMetaKoiEvent {
	event_type: 'META';
}

declare interface AbstractRichMessageEvent extends AbstractMessageMetaKoiEvent {
	sender: User;
	fragments: ChatFragment[];
	donations: Donation[];
	attachments: Attachment[];
	milestones: Milestone[];
	id: MessageId;
	reply_target: MetaId | null;
	raw: string;
	html: string;
}

export declare interface RichMessageEvent extends AbstractRichMessageEvent {
	event_type: 'RICH_MESSAGE';

	/**
	 * This field is computed and not actually sent by the backend.
	 */
	x_reply_target_data: RichMessageEvent | null;
}

export declare interface PlatformMessageEvent extends AbstractRichMessageEvent {
	event_type: 'PLATFORM_MESSAGE';

	/**
	 * This field is computed and not actually sent by the backend.
	 */
	x_reply_target_data: RichMessageEvent | null;
}

export declare type DonationType =
	| 'CASTERLABS_TEST'
	| 'OTHER' // Never exclude other!
	| 'CAFFEINE_PROP'
	| 'TWITCH_BITS'
	| 'TROVO_SPELL'
	| 'YOUTUBE_SUPER_CHAT'
	| 'YOUTUBE_SUPER_STICKER'
	| 'DLIVE_SUPER_CHAT'
	| 'DLIVE_LEMON'
	| 'TIKTOK_COINS';
export declare interface Donation {
	type: DonationType;
	name: string;
	currency: string;
	amount: number;
	/**
	 * This is the amount of the item sent. For example, Caffeine props can be sent
	 * multiple times in a single message. For the true value of a donation,
	 * multiply count by the amount.
	 */
	count: number;
	image: string;
}

export declare type AttachmentType = 'IMAGE' | 'INTERACTIVE';
export declare interface AttachmentContent {
	src: string;
	alt: string;
}
export declare interface Attachment {
	type: AttachmentType;
	content: AttachmentContent;
	html: string;
	donation: Donation | null;
}

export declare type MilestoneType = 'WATCH_STREAK_DAYS';
export declare interface Milestone {
	type: MilestoneType;

	/**
	 * Context dependent.
	 *
	 * @implSpec <code>WATCH_STREAK_DAYS</code>: this is the number of days in the
	 *           streak.
	 */
	amount: number;
}

export declare type ChatFragment = TextChatFragment | EmoteChatFragment | EmojiChatFragment | MentionChatFragment | LinkChatFragment;
export declare type ChatFragmentType = 'TEXT' | 'EMOTE' | 'EMOJI' | 'MENTION' | 'LINK';

declare interface AbstractChatFragment {
	type: ChatFragmentType;
	raw: string;
	html: string;
}

export declare interface TextChatFragment extends AbstractChatFragment {
	type: 'TEXT';
	/**
	 * Same as raw.
	 */
	text: string;
}

export declare interface EmoteChatFragment extends AbstractChatFragment {
	type: 'EMOTE';
	emoteName: string;
	imageLink: string;
	/**
	 * Some emotes are purely for augmenting another emote. For instance, adding a
	 * rain effect on a Sad Pepe. This is already handled for you in the rendered
	 * html.
	 */
	isZeroWidth: boolean;
	provider: string | null;
	donation: Donation | null;
}

export declare interface EmojiChatFragment extends AbstractChatFragment {
	type: 'EMOJI';
	variation: any; // Not gonna document this yet. Kindof useless.
}

export declare interface MentionChatFragment extends AbstractChatFragment {
	type: 'MENTION';
	mentioned: User;
}

export declare interface LinkChatFragment extends AbstractChatFragment {
	type: 'LINK';
	url: string;
}

/* ------------------------ */
/*           Misc           */
/* ------------------------ */

export declare interface ProductInfo {
	name: string;
	image: string | null;
}
