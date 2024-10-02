package co.casterlabs.caffeinated.builtin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.koi.api.types.KoiEvent;

public class Evaluater {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    public static String replace(String input, String prefix, String suffix, KoiEvent event) {
        Matcher matcher = PATTERN.matcher(input);

        // Iterate over matches and print the content between braces
        while (matcher.find()) {
            String key = matcher.group(1);
            String result = execute(event, key);

            input = input.replace("${" + key + "}", prefix + result + suffix);
        }

        return input;
    }

    private static synchronized String execute(KoiEvent event, String scriptToExecute) {
        return String.valueOf(
            Caffeinated.getInstance().getScriptingEngines().get("javascript")
                .execute(event, "return " + scriptToExecute)
        );
    }

}
