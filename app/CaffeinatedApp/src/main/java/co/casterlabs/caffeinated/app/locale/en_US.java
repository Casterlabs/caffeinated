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
            // These keys are used for both tts and display.
            .function("docks.chat.viewer.event_format.RAID", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                double viewerCount = Double.parseDouble(knownPlaceholders.get("viewers"));

                if (viewerCount < 2) {
                    // The platform didn't tell us how many viewers are participating OR the value
                    // is too small to reasonably say.
                    return "[co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID.no_viewers]";
                } else {
                    return "[co.casterlabs.caffeinated.app.docks.chat.viewer.event_format.RAID.viewers]";
                }
            })

            .function("docks.chat.viewer.event_format.SUBSCRIPTION", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                // This gets ugly REAL quick.
                double monthsPurchased = Double.parseDouble(knownPlaceholders.get("months_purchased"));
                double monthsStreak = Double.parseDouble(knownPlaceholders.get("months_streak"));
                SubscriptionLevel level = SubscriptionLevel.valueOf(knownPlaceholders.get("level"));
                SubscriptionType type = SubscriptionType.valueOf(knownPlaceholders.get("type"));

                switch (type) {
                    case SUB:
                    case RESUB: {
                        String format = "%name% just subscribed";
                        switch (level) {
                            case TIER_1:
                                format += " at Tier 1";
                                break;
                            case TIER_2:
                                format += " at Tier 2";
                                break;
                            case TIER_3:
                                format += " at Tier 3";
                                break;
                            case TIER_4:
                                format += " at Tier 4";
                                break;
                            case TIER_5:
                                format += " at Tier 5";
                                break;
                            case TWITCH_PRIME:
                                format += " with Twitch Prime";
                                break;
                            case UNKNOWN:
                                break; // No touch.
                        }
                        if (monthsPurchased > 1) {
                            format += " for %months_purchased% months";
                        }
                        if (monthsStreak > 1) {
                            format += "! They have been subscribed for %months_streak% months";
                        }
                        format += "!";
                        return format;
                    }

                    case ANONSUBGIFT: // %name% will be "Anonymous".
                    case ANONRESUBGIFT: // %name% will be "Anonymous".
                    case SUBGIFT:
                    case RESUBGIFT: {
                        String format = "%name% just gifted %recipient%";
                        if (monthsPurchased > 1) {
                            format += " a %months_purchased% month";
                        } else {
                            format += " a";
                        }
                        switch (level) {
                            case TIER_1:
                                format += " Tier 1";
                                break;
                            case TIER_2:
                                format += " Tier 2";
                                break;
                            case TIER_3:
                                format += " Tier 3";
                                break;
                            case TIER_4:
                                format += " Tier 4";
                                break;
                            case TIER_5:
                                format += " Tier 5";
                                break;
                            case TWITCH_PRIME:
                                format += " Twitch Prime";
                                break;
                            case UNKNOWN:
                                break; // No touch.
                        }
                        format += " subscription";
                        if (monthsStreak > 1) {
                            format += "! They have been subscribed for %months_streak% months";
                        }
                        format += "!";
                        return format;
                    }
                }

                return null;
            })
            .build();
    }

}
