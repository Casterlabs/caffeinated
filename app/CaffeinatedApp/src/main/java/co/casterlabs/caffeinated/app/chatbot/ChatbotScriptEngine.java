package co.casterlabs.caffeinated.app.chatbot;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class ChatbotScriptEngine {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("([^\\\"]\\S*|\\\".+?\\\") *");
    private static ScriptEngine engine;

    static {
        System.setProperty("nashorn.args", "--language=es6");
        engine = new NashornScriptEngineFactory().getScriptEngine();

        engine.put("store", CaffeinatedApp.getInstance().getChatbotPreferences().get().getStore());
        engine.put("__internal_handle", new ScriptHandle());

        try {
            engine.eval(String.format("const PLATFORMS = %s;", Rson.DEFAULT.toJson(UserPlatform.values())));
            engine.eval("load('classpath:nashornPolyfill.js')");
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    public static synchronized void execute(RichMessageEvent event, String scriptToExecute) {
        try {
            List<String> args = new LinkedList<>();
            Matcher matcher = QUOTE_PATTERN.matcher(event.getRaw());
            while (matcher.find()) {
                String arg = matcher.group(1).trim();

                if (arg.charAt(0) == '"' && arg.charAt(arg.length() - 1) == '"') {
                    args.add(arg.substring(1, arg.length() - 1));
                } else {
                    args.add(arg);
                }
            }

            args.remove(0);

            String[] script = {
                    "(() => {",

                    // "Globals"
                    String.format("const Music = %s;", CaffeinatedApp.getInstance().getMusic().toJson()),
                    String.format(
                        "const Koi = {"
                            + "sendChat(platform, message, chatter, replyTarget) {return __internal_handle.koi_sendChat(platform, message, chatter, replyTarget);},"
                            + "upvoteChat(platform, messageId) {return __internal_handle.koi_upvoteChat(platform, messageId);},"
                            + "deleteChat(platform, messageId) {return __internal_handle.koi_deleteChat(platform, messageId);},"
                            + "%s;",
                        CaffeinatedApp.getInstance().getKoi().toJson().toString().substring(1) // Chop off the leading '{'
                    ),
                    "const fetch = {"
                        + "asText(url) {return __internal_handle.fetch_asText(url);},"
                        + "asJson(url) {return JSON.parse(__internal_handle.fetch_asText(url));}"
                        + "};",
                    "",

                    // Per-event.
                    String.format("const event = %s;", Rson.DEFAULT.toJson(event)), // Define the event.
                    String.format("const args = %s;", Rson.DEFAULT.toJson(args)),   // Define a list of arguments.
                    "",

                    scriptToExecute,
                    "})();"
            };

            engine.eval(String.join("\n", script), engine.getContext());
            CaffeinatedApp.getInstance().getChatbotPreferences().save(); // Save any changes to store.
        } catch (ScriptException e) {
            String message = LogUtil.parseFormat("An error occurred whilst executing script command:\n%s", e);
            FastLogger.logStatic(LogLevel.WARNING, message);
            CaffeinatedApp.getInstance().getUI().showToast(message, NotificationType.WARNING);
        }
    }

    public static class ScriptHandle {

        public void koi_sendChat(@NonNull String platform, @NonNull String message, @NonNull String chatter, @Nullable String replyTarget) {
            CaffeinatedApp.getInstance().getKoi().sendChat(UserPlatform.valueOf(platform), message, KoiChatterType.valueOf(chatter), replyTarget, false);
        }

        public void koi_upvoteChat(@NonNull String platform, @NonNull String messageId) {
            CaffeinatedApp.getInstance().getKoi().upvoteChat(UserPlatform.valueOf(platform), messageId);
        }

        public void koi_deleteChat(@NonNull String platform, @NonNull String messageId) {
            CaffeinatedApp.getInstance().getKoi().deleteChat(UserPlatform.valueOf(platform), messageId, false);
        }

        @SneakyThrows
        public String fetch_asText(@NonNull String url) {
            return WebUtil.sendHttpRequest(
                new Request.Builder()
                    .addHeader("User-Agent", "Casterlabs/Bot")
                    .url(url)
            );
        }

    }

}
