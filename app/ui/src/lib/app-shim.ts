import { goto } from '$app/navigation';
import { storify } from './bridge-helper';
import EventHandler from './event-handler';
import type { KoiEvent, MessageId, MessageMetaEvent, MetaId, RichMessageEvent, UPID, User, UserPlatform } from './koi';
import { glocale, rerenderKey } from './locale/locale';
import type { AppSDK } from './sdk';
import { writable } from 'svelte/store';

export const STATUS_COLORS = {
	OPERATIONAL: ['green', 'white'],
	MAJOR_OUTAGE: ['red', 'white'],
	MINOR_OUTAGE: ['orange', 'white'],
	PARTIAL_OUTAGE: ['orange', 'white'],
	DEGRADED_PERFORMANCE: ['yellow', 'black'],
	MAINTENANCE: ['green', 'white']
};

export interface StatusState {
	status: 'OPERATIONAL' | 'MAJOR_OUTAGE' | 'MINOR_OUTAGE' | 'PARTIAL_OUTAGE' | 'DEGRADED_PERFORMANCE' | 'MAINTENANCE';
	activeIncidents: { link: string }[];
}

export const isInApp = typeof saucer !== 'undefined';

export const appStatusStates = writable<StatusState[]>([]);

export const themeBaseColor = writable<string>('gray');
export const themePrimaryColor = writable<string>('gray');

export const themeEffectiveAppearance = writable<string>('DARK');

export const copyText = (text: string) => {
	if (isInApp) {
		Caffeinated.copyText(text, null);
	} else {
		const SDK = window as any as AppSDK;
		SDK.Widget.emit('copyText', text);
	}
};

export const openLink = (link: string) => {
	if (isInApp) {
		Caffeinated.openLink(link);
	} else {
		const SDK = window as any as AppSDK;
		SDK.Widget.emit('openLink', link);
	}
};

abstract class AbstractKoi extends EventHandler {
	public banChatter(target: UPID, user: User): void {
		const platform = target.split(';').pop();
		switch (platform) {
			case 'TROVO':
			case 'TWITCH':
				this.sendChatMessage(target, `/ban ${user.username}`, null);
				break;

			default:
				break;
		}
	}

	public timeoutChatter(target: UPID, user: User): void {
		const platform = target.split(';').pop();
		switch (platform) {
			case 'TWITCH':
				this.sendChatMessage(target, `/timeout ${user.username}`, null);
				break;

			default:
				break;
		}
	}

	public abstract upvoteChat(target: UPID, messageId: MessageId): void;

	public abstract deleteChat(target: UPID, messageId: MessageId): void;

	public abstract sendChatMessage(target: UPID, message: string, replyTarget: MessageId | null): void;

	public abstract authenticated(): Promise<User[]>;

	public abstract history(): Promise<KoiEvent[]>;
}

export let Koi: AbstractKoi;

export function awaitPageLoad() {
	if (isInApp) {
		return Promise.resolve();
	} else {
		const SDK = window as any as AppSDK;
		return new Promise((resolve) => SDK.Widget.on('init', resolve));
	}
}

if (isInApp) {
	storify(AppConfig, 'uiPreferences')
		.readable<Awaited<typeof AppConfig.uiPreferences>>()
		.subscribe((uiPreferences) => {
			document.documentElement.style.fontSize = `${(uiPreferences?.zoom || 1) * 16}px`;
			document.documentElement.style.fontFamily = uiPreferences?.uiFont || '';
		});

	storify(AppConfig, 'themePreferences')
		.readable<Awaited<typeof AppConfig.themePreferences>>()
		.subscribe((prefs) => {
			if (!prefs) return;
			themeBaseColor.set(prefs.baseColor);
			themePrimaryColor.set(prefs.primaryColor);
		});

	storify(AppThemeManager, 'effectiveAppearance')
		.readable<Awaited<typeof AppThemeManager.effectiveAppearance>>()
		.subscribe((appearance) => {
			if (!appearance) return;
			themeEffectiveAppearance.set(appearance);
		});

	storify(App, 'statusStates')
		.readable<StatusState[]>()
		.subscribe((states: any) => appStatusStates.set(states || []));

	storify(AppLocale, 'current')
		.readable<any>()
		.subscribe(async (locale) => {
			if (!locale) return;

			const fallback = await AppLocale.fallback;

			glocale.use({
				...fallback,
				...locale
			});
			rerenderKey.update((n) => n + 1);
		});

	Koi = new (class extends AbstractKoi {
		public upvoteChat(target: UPID, messageId: MessageId): void {
			const platform = target.split(';').pop() as UserPlatform;
			window.Koi.upvoteChat(platform, messageId);
		}

		public deleteChat(target: UPID, messageId: MessageId): void {
			const platform = target.split(';').pop() as UserPlatform;
			window.Koi.deleteChat(platform, messageId, true);
		}

		public sendChatMessage(target: UPID, message: string, replyTarget: MessageId | null): void {
			const platform = target.split(';').pop() as UserPlatform;
			window.Koi.sendChat(platform, message, 'CLIENT', replyTarget, true);
		}

		public async authenticated(): Promise<User[]> {
			return Object.values(await window.Koi.userStates)
				.map((state) => state?.streamer)
				.filter((user) => !!user);
		}

		public history(): Promise<KoiEvent[]> {
			// @ts-ignore
			return window.Koi.eventHistory;
		}
	})();

	// @ts-ignore
	window.Koi.eventHistory.then((events: KoiEvent[]) => {
		events.forEach((e) => {
			Koi.broadcast(e.event_type, e);
		});
	});

	// @ts-ignore
	saucer.messages.onMessage(([type, data]: [string, any]) => {
		switch (type) {
			case 'goto': // "Exposes" goto() to the Java side.
				goto(data.path);
				return;
			case 'koi:event':
				Koi.broadcast(data.event_type, data as KoiEvent);
				return;
		}
	});
} else {
	// We're a dock!
	const SDK = window as any as AppSDK;
	SDK.App.on('theme', ([baseColor, primaryColor]) => {
		themeBaseColor.set(baseColor);
		themePrimaryColor.set(primaryColor);
	});
	// SDK.App.on('emojiProvider', App.emojiProvider.set);
	// SDK.App.on('language', App.currentLocale.set);
	SDK.App.on('appearance', themeEffectiveAppearance.set);
	SDK.App.on('zoom', (zoom) => (document.documentElement.style.fontSize = `${zoom * 100}%`));

	SDK.Widget.on('locale', ({ current, fallback }: any) => {
		glocale.use({
			...fallback,
			...current
		});
		rerenderKey.update((n) => n + 1);
	});

	Koi = new (class extends AbstractKoi {
		public upvoteChat(target: UPID, messageId: MessageId): void {
			const platform = target.split(';').pop() as UserPlatform;
			SDK.Koi.upvoteChat(platform, messageId);
		}

		public deleteChat(target: UPID, messageId: MessageId): void {
			const platform = target.split(';').pop() as UserPlatform;
			SDK.Koi.deleteChat(platform, messageId, true);
		}

		public sendChatMessage(target: UPID, message: string, replyTarget: MessageId | null): void {
			const platform = target.split(';').pop() as UserPlatform;
			SDK.Koi.sendChat(platform, message, 'CLIENT', replyTarget, true);
		}

		public async authenticated(): Promise<User[]> {
			return Object.values(SDK.Koi.userStates)
				.map((state) => state?.streamer)
				.filter((user) => !!user);
		}

		public async history(): Promise<KoiEvent[]> {
			// @ts-ignore
			return SDK.Koi.eventHistory;
		}
	})();

	SDK.Widget.emit('ready');
}

// We have to keep track of everything in an ordered array so that we can maintain
// a MAX length. We also need to keep track of everything in a map so that we can quickly
// look up a message by its meta_id. speed vs footprint tradeoff :P
const MAX_HISTORICAL_RICH_MESSAGES = 500;
const historicalRichMessages: RichMessageEvent[] = [];
const historicalRichMessagesMappedByMetaId: Record<MetaId, RichMessageEvent> = {};

Koi.intercept('RICH_MESSAGE', (e: RichMessageEvent) => {
	historicalRichMessages.push(e);
	historicalRichMessagesMappedByMetaId[e.meta_id] = e;

	while (historicalRichMessages.length > MAX_HISTORICAL_RICH_MESSAGES) {
		const event = historicalRichMessages.shift();
		if (!event) continue;
		delete historicalRichMessagesMappedByMetaId[event.meta_id];
	}

	if (e.reply_target) {
		e.x_reply_target_data = historicalRichMessagesMappedByMetaId[e.reply_target];
	}

	return e;
});
Koi.intercept('META', (newMeta: MessageMetaEvent) => {
	const eventToEdit = historicalRichMessagesMappedByMetaId[newMeta.meta_id];

	if (eventToEdit) {
		eventToEdit.is_visible = newMeta.is_visible;
		eventToEdit.upvotes = newMeta.upvotes;
	}

	return newMeta;
});
