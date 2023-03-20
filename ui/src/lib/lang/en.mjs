export default {
	'meta.name': 'English',
	'meta.code': 'en',
	'meta.flag': '🇬🇧',
	'meta.direction': 'ltr',
	'meta.translators': ' ',

	// {placeholder}: some text passed to the translator and is replaced
	// [placeholder]: grabs a translation key
	// %placeholder%: ui components passed to you, example usage can be found in the chatbot

	// All keys prefixed with `sr.` are for screen readers only.
	// They will not be shown to end-users.

	'generic.quote.left': "'",
	'generic.quote.right': "'",

	'app.love': 'Made with ♥ by Casterlabs',
	'app.copyright': 'Copyright © {year} Casterlabs. All rights reserved.',

	'modal.close': 'Close',
	'sr.modal.close': 'Close Modal',

	'sr.navigation.back': 'Go Back',
	'navigation.want_to_go_back': 'Want to go back?',

	show_me_how: 'Show me how',

	'page.dashboard': 'Dashboard',
	'sidebar.update_app': 'Update the app',
	'sidebar.update_app.description': 'Click here to restart and download the latest updates.',

	unsupported_feature: 'Not supported',
	'unsupported_feature.item': 'Not supported by {item}.',

	/* ---- Platforms ---- */
	'platform.CAFFEINE': 'Caffeine',
	'platform.parenthesis.CAFFEINE': '(Caffeine)',
	'platform.TWITCH': 'Twitch',
	'platform.parenthesis.TWITCH': '(Twitch)',
	'platform.TROVO': 'Trovo',
	'platform.parenthesis.TROVO': '(Trovo)',
	'platform.GLIMESH': 'Glimesh',
	'platform.parenthesis.GLIMESH': '(Glimesh)',
	'platform.BRIME': 'Brime',
	'platform.parenthesis.BRIME': '(Brime)',
	'platform.YOUTUBE': 'YouTube',
	'platform.parenthesis.YOUTUBE': '(YouTube)',
	'platform.DLIVE': 'DLive',
	'platform.parenthesis.DLIVE': '(DLive)',
	'platform.THETA': 'Theta',
	'platform.parenthesis.THETA': '(Theta)',
	'platform.KICK': 'Kick',
	'platform.parenthesis.KICK': '(Kick)',
	'platform.TIKTOK': 'TikTok',
	'platform.parenthesis.TIKTOK': '(TikTok)',

	/* ---- Signin ---- */

	'page.signin': 'Sign In',
	'page.signin.welcome': 'Welcome to Casterlabs.',
	'page.signin.choose': 'Choose a platform to get started with.',
	'page.signin.disclaimer':
		'By signing in, you agree to our %terms_of_service% and our %privacy_policy%. Click %here% if you actually read them.',
	'page.signin.disclaimer.terms_of_service': 'Terms of Service',
	'page.signin.disclaimer.privacy_policy': 'Privacy Policy',
	'page.signin.disclaimer.here': 'here',

	/* ---- Widgets & Alerts ---- */

	'page.widgets': 'Widgets & Alerts',
	'page.widgets.info': 'Add these to OBS as Browser sources.',
	'page.widgets.info.show_me_how.modal.title': 'How to create an OBS Browser Source',
	'page.widgets.info.widget_features_not_supported.modal.title': 'Widget not supported',
	'page.widgets.info.widget_features_not_supported.modal.content':
		'This widget requires features that are not supported by {platform}.',
	'page.widgets.info.widget_features_not_supported.modal.content.cancel': 'Cancel',
	'page.widgets.info.widget_features_not_supported.modal.content.create_anyway': 'Create Anyway',
	'sr.page.widgets.create': 'Create a new widget',
	'page.widgets.create.new': '(New)',
	'page.widgets.preview': 'Preview',
	'sr.page.widgets.delete': 'Delete',
	'sr.page.widgets.copy_link': 'Copy Link',
	'page.widgets.create.category.ALERTS': 'Alerts',
	'page.widgets.create.category.LABELS': 'Labels',
	'page.widgets.create.category.INTERACTION': 'Interaction',
	'page.widgets.create.category.GOALS': 'Goals',
	'page.widgets.create.category.OTHER': 'Other',

	'sr.page.widget.editor.edit_name': 'Edit name',
	'page.widget.editor.test_events.tab': 'Test',
	'page.widget.editor.test_events.send_test': 'Send Test',

	/* ---- Docks ---- */

	'page.docks': 'Docks',
	'page.docks.info': 'Add these to OBS as Browser Docks.',
	'page.docks.info.show_me_how.modal.title': 'How to create an OBS Browser Dock',

	/* ---- Chat Bot ---- */

	'page.chat_bot': 'Chat Bot',
	'page.chat_bot.platform.ANY': 'Any Platform',

	'sr.page.chat_bot.remove': 'Remove',

	'page.chat_bot.commands': 'Commands',
	'page.chat_bot.commands.type.COMMAND': 'runs',
	'page.chat_bot.commands.type.CONTAINS': 'mentions',
	'page.chat_bot.commands.format.COMMAND':
		'When someone from %platform% %action% <b>!</b>%action_target%, send: %message%',
	'page.chat_bot.commands.format.CONTAINS':
		'When someone from %platform% %action% <b>[generic.quote.left]</b>%action_target%<b>[generic.quote.right]</b>, reply with: %message%',
	'page.chat_bot.commands.example': 'Checkout casterlabs.co!',
	'sr.page.chat_bot.commands.add': 'Create another command',

	'page.chat_bot.shouts': 'Shouts',
	'page.chat_bot.shouts.format': 'When someone from %platform% %action% say: %message%',
	'page.chat_bot.shouts.DONATION': 'Donates',
	'page.chat_bot.shouts.FOLLOW': 'Follows',
	'page.chat_bot.shouts.RAID': 'Raids',
	'page.chat_bot.shouts.SUBSCRIPTION': 'Subscribes',
	'page.chat_bot.shouts.example': 'Thank you for the follow!',

	'page.chat_bot.timers': 'Timers',
	'page.chat_bot.timers.format': 'Every %seconds% seconds, send one of the following:',
	'page.chat_bot.timers.example': 'I love Casterlabs!',

	'page.chat_bot.settings': 'Settings',
	'page.chat_bot.settings.sender': 'Send messages from:',
	'page.chat_bot.settings.sender.SYSTEM': 'the @Casterlabs account',
	'page.chat_bot.settings.sender.CLIENT': 'your account',
	'page.chat_bot.settings.hide_commands_from_chat': 'Hide commands from chat',
	'page.chat_bot.settings.hide_commands_from_chat.description':
		'Makes Caffeinated hide all !commands and responses from the chat widget.',
	'page.chat_bot.settings.hide_timers_from_chat': 'Hide timers from chat',
	'page.chat_bot.settings.hide_timers_from_chat.description':
		'Makes Caffeinated hide all timer messages from the chat widget.',
	'page.chat_bot.settings.hide_from_chatbots': 'Hide these accounts from chat',
	'page.chat_bot.settings.hide_from_chatbots.description':
		'Hide some additional accounts from your chat widget, such as <pre>@Nightbot</pre>.',
	'sr.page.chat_bot.settings.hide_from_chatbots.add': 'Hide another account',

	/* ---- Settings ---- */

	'page.settings': 'Settings',

	'page.settings.appearance': 'Appearance',
	'page.settings.appearance.theme': 'Theme',
	'page.settings.appearance.theme.base_color': 'Base Color',
	'page.settings.appearance.theme.primary_color': 'Primary Color',
	'page.settings.appearance.appearance': 'Appearance',
	'page.settings.appearance.appearance.LIGHT': 'Light',
	'page.settings.appearance.appearance.DARK': 'Dark',
	'page.settings.appearance.appearance.FOLLOW_SYSTEM': 'Follow System',
	'page.settings.appearance.icon': 'Icon',
	'page.settings.appearance.emojis': 'Emojis 😀',
	'page.settings.appearance.emojis.SYSTEM': 'System',
	'page.settings.appearance.zoom': 'Zoom',
	'page.settings.appearance.zoom.description':
		"Makes Caffeinated's UI larger or smaller. Use with caution.",
	'sr.page.settings.appearance.zoom.reset': 'Reset',
	'page.settings.appearance.language': 'Language',
	'page.settings.appearance.close_to_tray': 'Close button sends to tray',
	'page.settings.appearance.close_to_tray.description':
		'Makes Caffeinated cozy up in the background when you close it.',
	'page.settings.appearance.mikeys_mode': "Mikey's Mode",
	'page.settings.appearance.mikeys_mode.description':
		'<a class="text-link" href="https://twitter.com/Casterlabs/status/1508475284944736268" target="_blank">For those of you who need more time to cook your pockets :^)</a>',

	'page.settings.plugins': 'Plugins',
	'page.settings.plugins.open_directory': 'Open Plugins Folder',
	'page.settings.plugins.internal_plugin': 'Internal Plugin',
	'page.settings.plugins.file.load': 'Load',
	'page.settings.plugins.file.unload': 'Unload',

	'page.settings.accounts': 'Accounts',
	'page.settings.accounts.streaming_services': 'Streaming Services',
	'page.settings.accounts.music_services': 'Music Services',
	'page.settings.accounts.music_services.enable': 'Enable',
	'page.settings.accounts.connect': 'Connect',
	'page.settings.accounts.disconnect': 'Disconnect',

	'page.settings.about': 'About',
	'page.settings.about.enable_unsafe': 'Enable Stupidly Unsafe Settings',
	'page.settings.about.enable_alternate_themes': 'Use Alternate Themes',

	/* ---- Chat ---- */

	'chat.viewer': 'Chat',

	'chat.viewer.send_message': 'Send',
	'chat.viewer.send_message.placeholder': 'Send a message',

	'chat.viewer.preferences.title': 'Chat Preferences',
	'chat.viewer.preferences.show_chat_timestamps': 'Show timestamps',
	'chat.viewer.preferences.show_profile_pictures': 'Show avatars',
	'chat.viewer.preferences.show_badges': 'Show badges',
	'chat.viewer.preferences.show_viewers': 'Show viewer join/leave messages',
	'chat.viewer.preferences.play_ding_on_message': 'Ding when a new message comes in',
	'chat.viewer.preferences.read_messages_out_loud': 'Read messages out loud',
	'chat.viewer.preferences.tts_voice': 'TTS Voice',
	'chat.viewer.preferences.show_platform': "Show the user's platform",

	'chat.viewer.tts.skip': 'Skip message',
	'chat.viewer.tts.event.RICH_MESSAGE.ASKS': '{name} asks "{message}"',
	'chat.viewer.tts.event.RICH_MESSAGE.SAYS': '{name} said "{message}"',
	'chat.viewer.tts.event.RICH_MESSAGE.SENT_A_LINK': '{name} sent a link.',
	'chat.viewer.tts.event.RICH_MESSAGE.SENT_SOME_EMOTES': '{name} sent some emotes.',
	'chat.viewer.tts.event.RICH_MESSAGE.SENT_AN_ATTACHMENT': '{name} sent an attachment.',
	'chat.viewer.tts.event.CHANNELPOINTS': '{name} just redeemed {reward}.',
	'chat.viewer.tts.event.RAID': '{name} just raided with {viewers} viewers!',
	'chat.viewer.tts.event.FOLLOW': '{name} just followed!',
	'chat.viewer.tts.event.SUBSCRIPTION.SUB': '{name} just subscribed for {months} months.',
	'chat.viewer.tts.event.SUBSCRIPTION.RESUB': '{name} just resubscribed for {months} months.',
	'chat.viewer.tts.event.SUBSCRIPTION.SUBGIFT':
		'{gifter} just gifted {recipient} a {months} month subscription.',
	'chat.viewer.tts.event.SUBSCRIPTION.ANONSUBGIFT':
		'Anonymous just gifted {recipient} a {months} month subscription.',

	'chat.viewer.message.CLEARCHAT': 'Chat was cleared',
	'chat.viewer.message.CHANNELPOINTS':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> just redeemed %image%<b>{reward}</b>',
	'chat.viewer.message.RAID':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> just raided with <b>{viewers}</b> viewers',
	'chat.viewer.message.FOLLOW':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> just followed',
	'chat.viewer.message.SUBSCRIPTION.SUB':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> just subscribed for <b>{months}</b> months',
	'chat.viewer.message.SUBSCRIPTION.RESUB':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> just resubscribed for <b>{months}</b> months',
	'chat.viewer.message.SUBSCRIPTION.SUBGIFT':
		'<b>{gifter} <span class="user-platform">{platform}</span></b> just gifted <b>{recipient}</b> a <b>{months}</b> month subscription',
	'chat.viewer.message.SUBSCRIPTION.ANONSUBGIFT':
		'Anonymous just gifted <b>{recipient} <span class="user-platform">{platform}</span></b> a <b>{months}</b> month subscription',
	'chat.viewer.message.VIEWER_JOIN':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> joined',
	'chat.viewer.message.VIEWER_LEAVE':
		'<b>{displayname} <span class="user-platform">{platform}</span></b> left',

	'chat.viewer.action.ban': 'Ban',
	'chat.viewer.action.timeout': 'Timeout',
	'chat.viewer.action.delete_message': 'Delete Message',
	'chat.viewer.action.raid': 'Raid',
	'chat.viewer.action.upvote': 'Upvote',

	/* ---- Channel Info ---- */

	'channel_info.title': 'Title',
	'channel_info.category': 'Category',
	'channel_info.language': 'Language',
	'channel_info.tags': 'Tags',
	'channel_info.tags.add': 'add',
	'channel_info.tags.add.description': 'Add another tag...',
	'channel_info.tags.remove': 'Remove',
	'channel_info.content_rating': 'Content Rating',
	'channel_info.thumbnail': 'Thumbnail',
	'channel_info.thumbnail.select_file': 'Select a file',
	'channel_info.thumbnail.clear': 'Clear',
	'channel_info.update': 'Update',

	/* ---- Dashboard ---- */

	'dashboard.customize.welcomewagon.welcome': 'Welcome!',
	'dashboard.customize.welcomewagon.getstarted': 'Get started by unlocking your dashboard.',
	'dashboard.customize.welcomewagon.next': 'Next, change this panel to something else.',
	'dashboard.customize.welcomewagon.also': 'You can also change the layout in the top right menu.',
	'dashboard.customize.options.none': 'Empty'
};
