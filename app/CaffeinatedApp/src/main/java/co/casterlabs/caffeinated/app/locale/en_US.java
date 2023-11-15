package co.casterlabs.caffeinated.app.locale;

import java.util.function.Supplier;

import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.commons.localization.helpers.BuildableLocaleProvider;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionLevel;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionType;

public class en_US implements Supplier<LocaleProvider> {

    @Override
    public LocaleProvider get() {
        return new BuildableLocaleProvider.Builder(_Util.loadJson("en_US"))
            .Rfunction("platform.\\w+.parenthesis", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                // Basically returns "([platform.TWITCH])".
                String[] parts = key.split("\\.");
                return "([co.casterlabs.caffeinated.app.platform." + parts[parts.length - 2] + "])";
            })
            .function("unsupported_feature.item", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                return "Not supported by [co.casterlabs.caffeinated.app.platform." + knownPlaceholders.get("item") + "].";
            })

            // We do these here for simplicity.
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.ASKS", "%name% asks {message}")
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.SAYS", "%name% said {message}")
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.LINK", "%name% sent a link.")
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.EMOTES", "%name% sent some emotes.")
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.ATTACHMENT", "%name% sent an attachment.")
            .string("docks.chat.viewer.tts.event_format.RICH_MESSAGE.REPLY", "%name% said, in response to {otherName}, {message}.")

            // These keys are used for both tts and display.
            .string("docks.chat.viewer.event_format.CHANNEL_POINTS", "%name% just redeemed %reward%!")
            .string("docks.chat.viewer.event_format.CLEAR_CHAT", "Chat was cleared.")
            .string("docks.chat.viewer.event_format.VIEWER_JOIN", "%name% joined.")
            .string("docks.chat.viewer.event_format.VIEWER_LEAVE", "%name% left.")

            .function("docks.chat.viewer.event_format.RAID", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                double viewerCount = Double.parseDouble(knownPlaceholders.get("viewers"));

                if (viewerCount < 2) {
                    // The platform didn't tell us how many viewers are participating OR the value
                    // is too small to reasonably say.
                    return "%name% just raided the channel!";
                } else {
                    return "%name% just raided the channel with %viewers% viewers!";
                }
            })

            .string("docks.chat.viewer.event_format.FOLLOW", "%name% just followed!")

            .function("docks.chat.viewer.event_format.SUBSCRIPTION", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                // This gets ugly REAL quick.
                double months = Double.parseDouble(knownPlaceholders.get("months"));
                SubscriptionLevel level = SubscriptionLevel.valueOf(knownPlaceholders.get("level"));
                SubscriptionType type = SubscriptionType.valueOf(knownPlaceholders.get("type"));

                switch (type) {
                    case SUB:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just subscribed at Tier 1 for %months% months!";
                                case TIER_2:
                                    return "%name% just subscribed at Tier 2 for %months% months!";
                                case TIER_3:
                                    return "%name% just subscribed at Tier 3 for %months% months!";
                                case TIER_4:
                                    return "%name% just subscribed at Tier 4 for %months% months!";
                                case TIER_5:
                                    return "%name% just subscribed at Tier 5 for %months% months!";
                                case TWITCH_PRIME:
                                    return "%name% just subscribed with Twitch Prime for %months% months!";
                                case UNKNOWN:
                                    return "%name% just subscribed for %months% months!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just subscribed at Tier 1!";
                                case TIER_2:
                                    return "%name% just subscribed at Tier 2!";
                                case TIER_3:
                                    return "%name% just subscribed at Tier 3!";
                                case TIER_4:
                                    return "%name% just subscribed at Tier 4!";
                                case TIER_5:
                                    return "%name% just subscribed at Tier 5!";
                                case TWITCH_PRIME:
                                    return "%name% just subscribed with Twitch Prime!";
                                case UNKNOWN:
                                    return "%name% just subscribed!";
                            }
                        }
                        return "%name% just subscribed!";

                    case RESUB:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just resubscribed at Tier 1 for %months% months!";
                                case TIER_2:
                                    return "%name% just resubscribed at Tier 2 for %months% months!";
                                case TIER_3:
                                    return "%name% just resubscribed at Tier 3 for %months% months!";
                                case TIER_4:
                                    return "%name% just resubscribed at Tier 4 for %months% months!";
                                case TIER_5:
                                    return "%name% just resubscribed at Tier 5 for %months% months!";
                                case TWITCH_PRIME:
                                    return "%name% just resubscribed with Twitch Prime for %months% months!";
                                case UNKNOWN:
                                    return "%name% just resubscribed for %months% months!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just resubscribed at Tier 1!";
                                case TIER_2:
                                    return "%name% just resubscribed at Tier 2!";
                                case TIER_3:
                                    return "%name% just resubscribed at Tier 3!";
                                case TIER_4:
                                    return "%name% just resubscribed at Tier 4!";
                                case TIER_5:
                                    return "%name% just resubscribed at Tier 5!";
                                case TWITCH_PRIME:
                                    return "%name% just resubscribed with Twitch Prime!";
                                case UNKNOWN:
                                    return "%name% just resubscribed!";
                            }
                        }
                        return "%name% just resubscribed!";

                    case ANONSUBGIFT: // %name% will be "Anonymous".
                    case ANONRESUBGIFT: // %name% will be "Anonymous".
                    case SUBGIFT:
                    case RESUBGIFT:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just gifted %recipient% a %months% month Tier 1 subscription!";
                                case TIER_2:
                                    return "%name% just gifted %recipient% a %months% month Tier 2 subscription!";
                                case TIER_3:
                                    return "%name% just gifted %recipient% a %months% month Tier 3 subscription!";
                                case TIER_4:
                                    return "%name% just gifted %recipient% a %months% month Tier 4 subscription!";
                                case TIER_5:
                                    return "%name% just gifted %recipient% a %months% month Tier 5 subscription!";
                                case TWITCH_PRIME:
                                    return "%name% just gifted %recipient% a %months% month Twitch Prime subscription!";
                                case UNKNOWN:
                                    return "%name% just gifted %recipient% a %months% month subscription!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "%name% just gifted %recipient% a Tier 1 subscription!";
                                case TIER_2:
                                    return "%name% just gifted %recipient% a Tier 2 subscription!";
                                case TIER_3:
                                    return "%name% just gifted %recipient% a Tier 3 subscription!";
                                case TIER_4:
                                    return "%name% just gifted %recipient% a Tier 4 subscription!";
                                case TIER_5:
                                    return "%name% just gifted %recipient% a Tier 5 subscription!";
                                case TWITCH_PRIME:
                                    return "%name% just gifted %recipient% a Twitch Prime subscription!";
                                case UNKNOWN:
                                    return "%name% just gifted %recipient% a subscription!";
                            }
                        }
                        return "%name% just gifted %recipient% a subscription!";
                }

                return null;
            })
            .build();
    }

}
