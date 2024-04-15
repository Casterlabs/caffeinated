package co.casterlabs.caffeinated.builtin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class Evaluater {
    private static final Pattern PATTERN = Pattern.compile("\\$\\{(.+?)\\}");
    private static ScriptEngine engine;

    static {
        System.setProperty("nashorn.args", "--language=es6");
        engine = new NashornScriptEngineFactory().getScriptEngine();

        try {
            engine.put("__internal_handle", new ScriptHandle());
            engine.eval(String.format("const PLATFORMS = %s;", Rson.DEFAULT.toJson(UserPlatform.values())));
            engine.eval(
                "const fetch = {"
                    + "asText(url) {return __internal_handle.fetch_asText(url);},"
                    + "asJson(url) {return JSON.parse(__internal_handle.fetch_asText(url));}"
                    + "};"
            );
            engine.eval(
                "const Plugins = {"
                    + "callServiceMethod(pluginId, serviceId, methodName, args) {return __internal_handle.callServiceMethod(pluginId, serviceId, methodName, args);}"
                    + "};"
            );
            engine.eval(
                "const Currencies = {"
                    + "formatCurrency(amount, currency) {return __internal_handle.currencies_formatCurrency(amount, currency);}"
                    + "};"
            );
            engine.eval(
                "let event = null;"
            );
            engine.eval("load('classpath:nashorn_constants.js')");
            engine.eval("load('classpath:nashorn_polyfill.js')");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

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
        try {
            engine.eval(String.format("event = %s;", Rson.DEFAULT.toJson(event)), engine.getContext());  // Define the event.
            return String.valueOf(engine.eval(scriptToExecute, engine.getContext()));
        } catch (ScriptException e) {
            String message = LogUtil.parseFormat("An error occurred whilst executing script command:\n%s", e);
            FastLogger.logStatic(LogLevel.WARNING, message);
            try {
                Object ui = ReflectionLib.invokeMethod(Caffeinated.getInstance(), "getUI");
                ReflectionLib.invokeMethod(ui, "showToast", message, Class.forName("co.casterlabs.caffeinated.app.NotificationType").getEnumConstants()[1]);
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            return "internalerror";
        }
    }

    public static class ScriptHandle {

        @SneakyThrows
        public String fetch_asText(@NonNull String url) {
            return WebUtil.sendHttpRequest(
                new Request.Builder()
                    .addHeader("User-Agent", "Casterlabs/Bot")
                    .url(url)
            );
        }

        public Object callServiceMethod(@NonNull String pluginId, @NonNull String serviceId, @NonNull String methodName, @Nullable Object[] args) {
            return Caffeinated.getInstance().getPlugins().callServiceMethod(pluginId, serviceId, methodName, args);
        }

        public String currencies_formatCurrency(Number amount, @NonNull String currency) throws InterruptedException, Throwable {
            return Currencies.formatCurrency(amount.doubleValue(), currency).await();
        }

    }

}
