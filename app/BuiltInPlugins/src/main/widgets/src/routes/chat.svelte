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

	const MAX_EVENTS_DISPLAY = 100;
	const ANIMATION_TIME = 250;

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
	let chatHistory = [];

	const settings = writable({});

	function onEvent(event) {
		switch (event.event_type) {
			case 'META': {
				console.debug('Event:', event);
				const chatElement = chatElements[event.meta_id];

				if (chatElement) {
					chatElement.upvotes = event.upvotes;
					if (!event.is_visible) {
						chatElement.element.remove();
						delete chatElements[event.meta_id];
					}
				}
				return;
			}

			case 'CLEARCHAT': {
				console.debug('Event:', event);
				if (event.user_upid) {
					// Clear by user.
					for (const [key, { component: chatMessage }] of Object.entries(chatElements)) {
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

				for (const [key, { component: chatMessage }] of Object.entries(chatElements)) {
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
				event.reply_target_data = chatElements[event.reply_target || ''] || null;

				const li = document.createElement('li');
				const card = new CardContainer({
					target: li,
					props: {
						event,
						settings
					}
				});
				const comp = new clazz({
					target: li.querySelector('#slot'),
					props: {
						event,
						settings
					}
				});
				li.setAttribute('event-platform', event.streamer.platform);

				if (!Widget.getSetting('platform.platforms').includes(event.streamer.platform)) {
					li.style.display = 'none';
				}

				if (event.event_type == 'RICH_MESSAGE') {
					if (event.donations.length == 0) {
						li.setAttribute('event-type', 'RICH_MESSAGE__REGULAR');
					} else {
						li.setAttribute('event-type', 'RICH_MESSAGE__DONATION');
					}
				} else {
					li.setAttribute('event-type', event.event_type);
				}

				if (event.meta_id) {
					// This event is editable in some way, shape, or form.
					// (so, we must keep track of it)
					chatElements[event.meta_id] = { element: li, component: comp, event: event };
				}

				chatHistory.push({ element: li, component: comp, event: event });
				chatBox.appendChild(li);

				while (chatHistory.length > MAX_EVENTS_DISPLAY) {
					const { element, event } = chatHistory.shift();
					element.remove();
					delete chatElements[event.meta_id];
				}

				if (event.x_is_catchup) return; // No need to animate :)

				const FRAME_RATE = 60; // fps, keep it even.
				const FRAME_INTERVAL = 1000 / FRAME_RATE;
				const TOTAL_FRAME_COUNT = ANIMATION_TIME / FRAME_INTERVAL;

				switch ($settings['message_style.message_style']) {
					case 'Text (Top-down)':
					case 'Text (Bottom-up)': {
						if ($settings['message_style.messages_animation'] == 'None') return; // No need to animate :)

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

						let currentFrame = 0;
						let animTaskId = setInterval(() => {
							currentFrame++;
							if (currentFrame >= TOTAL_FRAME_COUNT) {
								clearInterval(animTaskId);
							}

							const progress = currentFrame / TOTAL_FRAME_COUNT;
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
						}, FRAME_INTERVAL);
						break;
					}

					case 'Text (Sideways)': {
						const direction = $settings['text_style.text_align'];

						switch (direction) {
							case 'Left': {
								card.offsetX = '-100%';
								break;
							}

							case 'Right': {
								card.offsetX = '100%';
								break;
							}
						}

						let currentFrame = 0;
						let animTaskId = setInterval(() => {
							currentFrame++;
							if (currentFrame >= TOTAL_FRAME_COUNT) {
								clearInterval(animTaskId);
							}

							const progress = currentFrame / TOTAL_FRAME_COUNT;
							switch (direction) {
								case 'Left': {
									card.offsetX = `-${(1 - progress) * 100}%`;
									break;
								}

								case 'Right': {
									card.offsetX = `${(1 - progress) * 100}%`;
									break;
								}
							}
						}, FRAME_INTERVAL);
						break;
					}
				}

				return;
			}
		}
	}

	onMount(() => {
		Widget.on('clear', () => {
			chatBox.innerHTML = '';
			chatElements = {};
		});

		Widget.on('update', () => {
			settings.set(Widget.widgetData.settings);
			changeFont(Widget.getSetting('text_style.font'));

			const platforms = Widget.getSetting('platform.platforms');
			for (const li of document.querySelectorAll('#chatbox>li')) {
				if (platforms.includes(li.getAttribute('event-platform'))) {
					li.style.display = 'unset';
				} else {
					li.style.display = 'none';
				}
			}
		});

		Widget.on('init', () => {
			Widget.broadcast('update');
		});

		Koi.on('*', (_, event) => onEvent(event));
		Koi.eventHistory.forEach((event) => {
			onEvent({
				...event,
				x_is_catchup: true
			});
		});
	});
</script>

<ul
	bind:this={chatBox}
	id="chatbox"
	class="absolute flex px-1"
	style:--animation-time={ANIMATION_TIME}
	style:color={$settings['text_style.text_color']}
	style:font-size="{$settings['text_style.font_size']}px"
	style:font-weight={$settings['text_style.font_weight']}
	style:padding="{$settings['message_style.margin']}px"
	style:letter-spacing="{$settings['text_style.letter_spacing']}px"
	class:top-0={$settings['message_style.message_style'] == 'Text (Top-down)'}
	class:flex-col-reverse={$settings['message_style.message_style'] == 'Text (Top-down)'}
	class:flex-col={$settings['message_style.message_style'] == 'Text (Bottom-up)'}
	class:bottom-0={$settings['message_style.message_style'] == 'Text (Bottom-up)'}
	class:flex-row={$settings['message_style.message_style'] == 'Text (Sideways)' &&
		$settings['text_style.text_align'] == 'Right'}
	class:flex-row-reverse={$settings['message_style.message_style'] == 'Text (Sideways)' &&
		$settings['text_style.text_align'] == 'Left'}
	class:w-max={$settings['message_style.message_style'] == 'Text (Sideways)'}
	class:space-x-4={$settings['message_style.message_style'] == 'Text (Sideways)'}
	class:right-0={$settings['message_style.message_style'] == 'Text (Sideways)' &&
		$settings['text_style.text_align'] == 'Right'}
	class:inset-x-0={$settings['message_style.message_style'] != 'Text (Sideways)'}
	class:text-right={$settings['text_style.text_align'] == 'Right'}
	style:--static-username-color={$settings['message_style.username_color.static'] || '#f04f88'}
	class:username-color-platform={$settings['message_style.username_color'] ==
		"Match Platform's Theme"}
	class:username-color-static={$settings['message_style.username_color'] == 'Static Color'}
	class:show-platform-icon={$settings['message_style.show_platform_icon']}
	class:show-RICH_MESSAGE__REGULAR={$settings['events.RICH_MESSAGE.REGULAR']}
	class:show-RICH_MESSAGE__DONATION={$settings['events.RICH_MESSAGE.DONATION']}
	class:show-CHANNEL_POINTS={$settings['events.CHANNEL_POINTS']}
	class:show-FOLLOW={$settings['events.FOLLOW']}
	class:show-SUBSCRIPTION={$settings['events.SUBSCRIPTION']}
	class:show-RAID={$settings['events.RAID']}
	style:-webkit-text-stroke="{$settings['text_style.outline_width'] * 0.1}em {$settings[
		'text_style.outline_color'
	]}"
	style:filter={$settings['text_style.text_shadow'] == -1
		? ''
		: `drop-shadow(0px 0px ${$settings['text_style.text_shadow']}px black)`}
>
	<!---->
</ul>

<style>
	:not(.show-RICH_MESSAGE__REGULAR) :global(li[event-type='RICH_MESSAGE__REGULAR']) {
		display: none;
	}

	:not(.show-RICH_MESSAGE__DONATION) :global(li[event-type='RICH_MESSAGE__DONATION']) {
		display: none;
	}

	:not(.show-CHANNEL_POINTS) :global(li[event-type='CHANNEL_POINTS']) {
		display: none;
	}

	:not(.show-FOLLOW) :global(li[event-type='FOLLOW']) {
		display: none;
	}

	:not(.show-SUBSCRIPTION) :global(li[event-type='SUBSCRIPTION']) {
		display: none;
	}

	:not(.show-RAID) :global(li[event-type='RAID']) {
		display: none;
	}

	:not(.show-platform-icon) :global(.user-platform-icon) {
		display: none;
	}
</style>
