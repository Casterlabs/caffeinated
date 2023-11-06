package co.casterlabs.caffeinated.app.locale;

import co.casterlabs.commons.localization.LocaleProvider;
import lombok.SneakyThrows;

public class _LocaleLoader {

    @SuppressWarnings("deprecation")
    @SneakyThrows
    public static LocaleProvider load(String locale) {
        switch (locale) {

            case "en-US":
            default:
                return en_US.class.newInstance().get();
        }
    }

}
