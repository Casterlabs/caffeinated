package co.casterlabs.caffeinated.app.locale;

import java.util.function.Supplier;

import co.casterlabs.commons.localization.LocaleProvider;
import co.casterlabs.commons.localization.helpers.BuildableLocaleProvider;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionLevel;
import co.casterlabs.koi.api.types.events.SubscriptionEvent.SubscriptionType;

public class tr_TR implements Supplier<LocaleProvider> {

    @Override
    public LocaleProvider get() {
        return new BuildableLocaleProvider.Builder(_Util.loadJson("tr_TR"))
            .function("docks.chat.viewer.event_format.SUBSCRIPTION", (key, externalLookup, knownPlaceholders, knownComponents) -> {
                // This gets ugly REAL quick.
                double monthsPurchased = Double.parseDouble(knownPlaceholders.get("months_purchased"));
                double monthsStreak = Double.parseDouble(knownPlaceholders.get("months_streak"));
                SubscriptionLevel level = SubscriptionLevel.valueOf(knownPlaceholders.get("level"));
                SubscriptionType type = SubscriptionType.valueOf(knownPlaceholders.get("type"));

                switch (type) {
                    case SUB:
                    case RESUB: {
                        String format = "%name% vient de s'abonner";
                        switch (level) {
                            case TIER_1:
                                format += " au Tier 1";
                                break;
                            case TIER_2:
                                format += " au Tier 2";
                                break;
                            case TIER_3:
                                format += " au Tier 3";
                                break;
                            case TIER_4:
                                format += " au Tier 4";
                                break;
                            case TIER_5:
                                format += " at Tier 5";
                                break;
                            case TWITCH_PRIME:
                                format += " avec Twitch Prime";
                                break;
                            case UNKNOWN:
                                break; // No touch.
                        }
                        if (monthsPurchased > 1) {
                            format += " pour %months_purchased% mois";
                        }
                        if (monthsStreak > 1) {
                            format += " ! Ils sont abonnés depuis %months_streak% mois";
                        }
                        format += " !";
                        return format;
                    }

                    case ANONSUBGIFT: // %name% will be "Anonymous".
                    case ANONRESUBGIFT: // %name% will be "Anonymous".
                    case SUBGIFT:
                    case RESUBGIFT: {
                        String format = "%name% vient d'offrir %recipient% un abonnement";
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
                        if (monthsPurchased > 1) {
                            format += " de %months_purchased% mois";
                        }
                        if (monthsStreak > 1) {
                            format += " ! Ils sont abonnés depuis %months_streak% mois";
                        }
                        format += " !";
                        return format;
                    }
                }

                return null;
            })
            .build();
    }

}
