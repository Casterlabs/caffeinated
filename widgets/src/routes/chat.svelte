<script>
	import CardContainer from '$lib/chat/CardContainer.svelte';

	import ChannelPointsMessage from '$lib/chat/ChannelPointsMessage.svelte';
	import SubscriptionMessage from '$lib/chat/SubscriptionMessage.svelte';
	import RichMessage from '$lib/chat/RichMessage.svelte';
	import FollowMessage from '$lib/chat/FollowMessage.svelte';
	import RaidMessage from '$lib/chat/RaidMessage.svelte';

	import { writable } from 'svelte/store';
	import { onMount } from 'svelte';
	import changeFont from '$lib/changeFont.mjs';

	const ANIMATION_TIME = 1000;

	// prettier-ignore
	const EVENT_CLASSES = {
		CHANNEL_POINTS:   ChannelPointsMessage,
		SUBSCRIPTION:     SubscriptionMessage,
		RICH_MESSAGE:     RichMessage,
		FOLLOW:           FollowMessage,
		RAID:             RaidMessage,
	};

	/** @type {HTMLElement} */
	let chatBox;
	let chatElements = {};

	const settings = writable({});
	let chatBoxOffsetYpx = 0;

	function onEvent(event) {
		switch (event.event_type) {
			case 'META': {
				console.debug('Event:', event);
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.upvotes = event.upvotes;
					if (!event.is_visible) {
						chatMessage.remove();
						delete chatElements[event.meta_id];
					}
				}
				return;
			}

			case 'CLEARCHAT': {
				console.debug('Event:', event);
				if (event.user_upid) {
					// Clear by user.
					for (const [key, chatMessage] of Object.entries(chatElements)) {
						const koiEvent = chatMessage.koiEvent;

						if (koiEvent.sender && koiEvent.sender.UPID == event.user_upid) {
							chatMessage.remove();
							delete chatElements[key];
						}
					}
					return;
				}

				// Clear all.
				const now = Date.now();

				for (const [key, chatMessage] of Object.entries(chatElements)) {
					if (chatMessage.timestamp < now) {
						chatMessage.remove();
						delete chatElements[key];
					}
				}
				return;
			}

			default: {
				const clazz = EVENT_CLASSES[event.event_type];
				if (!clazz) return;

				console.debug('Event:', event);

				const li = document.createElement('li');
				const card = new CardContainer({
					target: li,
					props: {
						settings
					}
				});
				const message = new clazz({
					target: li.querySelector('#slot'),
					props: {
						event,
						settings
					}
				});

				if (event.meta_id) {
					// This event is editable in some way, shape, or form.
					// (so, we must keep track of it)
					chatElements[event.meta_id] = message;
				}

				chatBox.appendChild(li);

				if ($settings['message_style.message_animation'] == 'None') return; // No need to animate :)

				const FRAME_RATE = 100; // fps, keep it even.
				const FRAME_INTERVAL = 1000 / FRAME_RATE;
				const TOTAL_FRAME_COUNT = ANIMATION_TIME / FRAME_INTERVAL;

				const isTopDown = $settings['message_style.message_direction'] == 'Top-down';
				const direction = $settings['message_style.slide_direction'];

				switch (direction) {
					case 'From the left': {
						card.offsetX = '-100%';
						break;
					}

					case 'From the right': {
						card.offsetX = '100%';
						break;
					}
				}

				const chatBoxOffsetAmount = li.offsetHeight;

				if (isTopDown) {
					chatBoxOffsetYpx -= chatBoxOffsetAmount;
				} else {
					chatBoxOffsetYpx += chatBoxOffsetAmount;
				}

				let currentFrame = 0;
				let animTaskId = setInterval(() => {
					const progress = currentFrame / TOTAL_FRAME_COUNT;

					if (progress >= 1) {
						clearInterval(animTaskId);
					}

					if (isTopDown) {
						chatBoxOffsetYpx += chatBoxOffsetAmount / TOTAL_FRAME_COUNT; // 1 "step"
					} else {
						chatBoxOffsetYpx -= chatBoxOffsetAmount / TOTAL_FRAME_COUNT; // 1 "step"
					}

					switch (direction) {
						case 'From the left': {
							card.offsetX = `-${(1 - progress) * 100}%`;
							break;
						}

						case 'From the right': {
							card.offsetX = `${(1 - progress) * 100}%`;
							break;
						}
					}

					currentFrame++;
				}, FRAME_INTERVAL);

				return;
			}
		}
	}

	onMount(() => {
		Widget.on('init', () => {
			Widget.broadcast('update');
			Koi.on('*', (_, event) => onEvent(event));
			Koi.eventHistory.forEach(onEvent);
		});

		Widget.on('clear', () => {
			chatBox.innerHTML = '';
			chatElements = {};
		});

		Widget.on('update', () => {
			settings.set(Widget.widgetData.settings);
			changeFont(Widget.getSetting('text_style.font'));
		});
	});
</script>

<ul
	bind:this={chatBox}
	id="chatbox"
	style:--animation-time={ANIMATION_TIME}
	style:--y-offset="{chatBoxOffsetYpx}px"
	style:color={$settings['text_style.text_color']}
	style:font-size="{$settings['text_style.font_size']}px"
	class="absolute inset-x-0 flex flex-col px-1"
	class:top-0={$settings['message_style.message_direction'] == 'Top-down'}
	class:flex-col-reverse={$settings['message_style.message_direction'] == 'Top-down'}
	class:bottom-0={$settings['message_style.message_direction'] == 'Bottom-up'}
	class:text-right={$settings['text_style.text_align'] == 'Right'}
>
	<!---->
</ul>

<style>
	#chatbox {
		transform: translateY(var(--y-offset));
	}
</style>
