package co.casterlabs.caffeinated.util;

import java.util.concurrent.ThreadLocalRandom;

public class MiscUtil {

    @SafeVarargs
    public static <T> T random(T... items) {
        return items[ThreadLocalRandom.current().nextInt(items.length)];
    }

}
