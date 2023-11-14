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
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.ASKS", "{name} asks {message}")
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.SAYS", "{name} said {message}")
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.LINK", "{name} sent a link.")
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.EMOTES", "{name} sent some emotes.")
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.ATTACHMENT", "{name} sent an attachment.")
            .string("dock.chat.viewer.tts.event_format.RICH_MESSAGE.REPLY", "{name} said, in response to {otherName}, {message}.")

            .string("dock.chat.viewer.tts.event_format.CHANNELPOINTS", "{name} just redeemed {reward}!")

            // These keys are used for both tts and display.
            .function("dock.chat.viewer.tts.event_format.RAID", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                double viewerCount = Double.parseDouble(knownPlaceholders.get("viewers"));

                if (viewerCount < 2) {
                    // The platform didn't tell us how many viewers are participating OR the value
                    // is too small to reasonably say.
                    return "{name_html} just raided the channel!";
                } else {
                    return "{name_html} just raided the channel with {viewers_html} viewers!";
                }
            })

            .string("dock.chat.viewer.tts.event_format.FOLLOW", "{name_html} just followed!")

            .function("dock.chat.viewer.tts.event_format.SUBSCRIPTION", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                // This gets ugly REAL quick.
                double months = Double.parseDouble(knownPlaceholders.get("months"));
                SubscriptionLevel level = SubscriptionLevel.valueOf(knownPlaceholders.get("level"));
                SubscriptionType type = SubscriptionType.valueOf(knownPlaceholders.get("type"));

                switch (type) {
                    case SUB:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just subscribed at Tier 1 for {months_html} months!";
                                case TIER_2:
                                    return "{name_html} just subscribed at Tier 2 for {months_html} months!";
                                case TIER_3:
                                    return "{name_html} just subscribed at Tier 3 for {months_html} months!";
                                case TIER_4:
                                    return "{name_html} just subscribed at Tier 4 for {months_html} months!";
                                case TIER_5:
                                    return "{name_html} just subscribed at Tier 5 for {months_html} months!";
                                case TWITCH_PRIME:
                                    return "{name_html} just subscribed with Twitch Prime for {months_html} months!";
                                case UNKNOWN:
                                    return "{name_html} just subscribed for {months_html} months!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just subscribed at Tier 1!";
                                case TIER_2:
                                    return "{name_html} just subscribed at Tier 2!";
                                case TIER_3:
                                    return "{name_html} just subscribed at Tier 3!";
                                case TIER_4:
                                    return "{name_html} just subscribed at Tier 4!";
                                case TIER_5:
                                    return "{name_html} just subscribed at Tier 5!";
                                case TWITCH_PRIME:
                                    return "{name_html} just subscribed with Twitch Prime!";
                                case UNKNOWN:
                                    return "{name_html} just subscribed!";
                            }
                        }
                        return "{name_html} just subscribed!";

                    case RESUB:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just resubscribed at Tier 1 for {months_html} months!";
                                case TIER_2:
                                    return "{name_html} just resubscribed at Tier 2 for {months_html} months!";
                                case TIER_3:
                                    return "{name_html} just resubscribed at Tier 3 for {months_html} months!";
                                case TIER_4:
                                    return "{name_html} just resubscribed at Tier 4 for {months_html} months!";
                                case TIER_5:
                                    return "{name_html} just resubscribed at Tier 5 for {months_html} months!";
                                case TWITCH_PRIME:
                                    return "{name_html} just resubscribed with Twitch Prime for {months_html} months!";
                                case UNKNOWN:
                                    return "{name_html} just resubscribed for {months_html} months!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just resubscribed at Tier 1!";
                                case TIER_2:
                                    return "{name_html} just resubscribed at Tier 2!";
                                case TIER_3:
                                    return "{name_html} just resubscribed at Tier 3!";
                                case TIER_4:
                                    return "{name_html} just resubscribed at Tier 4!";
                                case TIER_5:
                                    return "{name_html} just resubscribed at Tier 5!";
                                case TWITCH_PRIME:
                                    return "{name_html} just resubscribed with Twitch Prime!";
                                case UNKNOWN:
                                    return "{name_html} just resubscribed!";
                            }
                        }
                        return "{name_html} just resubscribed!";

                    case ANONSUBGIFT: // {name_html} will be "Anonymous".
                    case ANONRESUBGIFT: // {name_html} will be "Anonymous".
                    case SUBGIFT:
                    case RESUBGIFT:
                        if (months > 1) {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Tier 1 subscription!";
                                case TIER_2:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Tier 2 subscription!";
                                case TIER_3:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Tier 3 subscription!";
                                case TIER_4:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Tier 4 subscription!";
                                case TIER_5:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Tier 5 subscription!";
                                case TWITCH_PRIME:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month Twitch Prime subscription!";
                                case UNKNOWN:
                                    return "{name_html} just gifted {recipient_html} a {months_html} month subscription!";
                            }
                        } else {
                            switch (level) {
                                case TIER_1:
                                    return "{name_html} just gifted {recipient_html} a Tier 1 subscription!";
                                case TIER_2:
                                    return "{name_html} just gifted {recipient_html} a Tier 2 subscription!";
                                case TIER_3:
                                    return "{name_html} just gifted {recipient_html} a Tier 3 subscription!";
                                case TIER_4:
                                    return "{name_html} just gifted {recipient_html} a Tier 4 subscription!";
                                case TIER_5:
                                    return "{name_html} just gifted {recipient_html} a Tier 5 subscription!";
                                case TWITCH_PRIME:
                                    return "{name_html} just gifted {recipient_html} a Twitch Prime subscription!";
                                case UNKNOWN:
                                    return "{name_html} just gifted {recipient_html} a subscription!";
                            }
                        }
                        return "{name_html} just gifted {recipient_html} a subscription!";
                }

                return null;
            })
            .build();
    }

}
