package co.casterlabs.caffeinated.app.scripting;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.jetbrains.annotations.Nullable;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.pluginsdk.Currencies;
import co.casterlabs.caffeinated.pluginsdk.TTS;
import co.casterlabs.caffeinated.pluginsdk.music.MusicPlaybackState;
import co.casterlabs.caffeinated.pluginsdk.scripting.ScriptingEngine;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.KoiChatterType;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.LogUtil;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class JavascriptEngineImpl implements ScriptingEngine {
    private static final Pattern QUOTE_PATTERN = Pattern.compile("([^\\\"]\\S*|\\\".+?\\\") *");
    private static Robot robot;

    private ScriptEngine engine;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }

    public JavascriptEngineImpl() {
        try {
            System.setProperty("nashorn.args", "--language=es6");
            this.engine = new NashornScriptEngineFactory().getScriptEngine();

            this.engine.put("store", CaffeinatedApp.getInstance().getChatbotPreferences().get().getStore());
            this.engine.put("Koi", new KoiScriptHandle());
            this.engine.put("fetch", new FetchScriptHandle());
            this.engine.put("Plugins", new PluginsScriptHandle());
            this.engine.put("Sound", new SoundScriptHandle());
            this.engine.put("Input", new InputScriptHandle());
            this.engine.put("Currencies", new CurrenciesScriptHandle());

            this.engine.eval("function sleep(milliseconds) {return __internal_handle.sleep(milliseconds);}");
            this.engine.eval(String.format("const PLATFORMS = %s;", Rson.DEFAULT.toJson(UserPlatform.values())));
            this.engine.eval(String.format("const KoiPlatform = %s;", Rson.DEFAULT.toJson(Arrays.asList(UserPlatform.values()).stream().collect(Collectors.toMap((p) -> p.name(), (p) -> p.name())))));
            this.engine.eval(String.format("const KoiChatter = %s;", Rson.DEFAULT.toJson(Arrays.asList(KoiChatterType.values()).stream().collect(Collectors.toMap((p) -> p.name(), (p) -> p.name())))));
            this.engine.eval(String.format("const PlaybackState = %s;", Rson.DEFAULT.toJson(Arrays.asList(MusicPlaybackState.values()).stream().collect(Collectors.toMap((p) -> p.name(), (p) -> p.name())))));
            this.engine.eval(
                "function escapeHtml(unsafe) {\r\n"
                    + "    return unsafe\r\n"
                    + "        .replace(/&/g, \"&amp;\")\r\n"
                    + "        .replace(/</g, \"&lt;\")\r\n"
                    + "        .replace(/>/g, \"&gt;\")\r\n"
                    + "        .replace(/\"/g, \"&quot;\")\r\n"
                    + "        .replace(/'/g, \"&#039;\");\r\n"
                    + " }"
            );
        } catch (ScriptException e) {
            FastLogger.logException(e);
            return;
        }

        try {
            this.engine.eval("load('classpath:nashorn_polyfill.js')");
        } catch (Throwable ignored) {}
    }

    @Override
    @SuppressWarnings("deprecation")
    public synchronized Object execute(@Nullable KoiEvent event, @NonNull String scriptToExecute) {
        try {
            List<String> args = new LinkedList<>();
            String rawArgs = "";

            if (event instanceof RichMessageEvent) {
                Matcher matcher = QUOTE_PATTERN.matcher(((RichMessageEvent) event).raw);
                while (matcher.find()) {
                    String arg = matcher.group(1).trim();

                    if (arg.charAt(0) == '"' && arg.charAt(arg.length() - 1) == '"') {
                        args.add(arg.substring(1, arg.length() - 1));
                    } else {
                        args.add(arg);
                    }
                }

                if (!args.isEmpty() && args.get(0).startsWith("!")) {
                    String cmd = args.remove(0);
                    rawArgs = ((RichMessageEvent) event).raw.substring(cmd.length()).trim();
                }
            }

            String[] script = {
                    "(() => {",
                    String.format("const Music = %s;", CaffeinatedApp.getInstance().getMusic().toJson()),
                    String.format(
                        "const ChatBot = {"
                            + "realChatter: %s"
                            + "};",
                        new JsonString(CaffeinatedApp.getInstance().getChatbotPreferences().get().getChatter().name())
                    ),
                    String.format("const event = %s;", Rson.DEFAULT.toJson(event)), // Define the event.
                    String.format("const args = %s;", Rson.DEFAULT.toJson(args)),   // Define a list of arguments, only applicable for RichMessages.
                    String.format("const rawArgs = %s;", new JsonString(rawArgs)),  // ^
                    "",

                    scriptToExecute,
                    "})();"
            };

            Object result = this.engine.eval(String.join("\n", script), this.engine.getContext());
            CaffeinatedApp.getInstance().getChatbotPreferences().save(); // Save any changes to store.
            return result;
        } catch (ScriptException e) {
            String message = LogUtil.parseFormat("An error occurred whilst executing script command:\n%s", e);
            FastLogger.logStatic(LogLevel.WARNING, message);
            CaffeinatedApp.getInstance().getUI().showToast(message, NotificationType.WARNING);
            return null;
        }
    }

    public static class KoiScriptHandle {

        public void sendChat(@Nullable String platform, @NonNull String message, @NonNull String chatter) {
            this.sendChat(platform, message, chatter, null);
        }

        public void sendChat(@Nullable String platform, @NonNull String message, @NonNull String chatter, @Nullable String replyTarget) {
            UserPlatform enumP = platform == null ? null : UserPlatform.valueOf(platform);
            CaffeinatedApp.getInstance().getKoi().sendChat(enumP, message, KoiChatterType.valueOf(chatter), replyTarget, false);
            CaffeinatedApp.getInstance().getChatbot().getRecentReplies().add(message);

            while (CaffeinatedApp.getInstance().getChatbot().getRecentReplies().size() > 50) {
                CaffeinatedApp.getInstance().getChatbot().getRecentReplies().removeFirst();
            }
        }

        public void upvoteChat(@NonNull String platform, @NonNull String messageId) {
            CaffeinatedApp.getInstance().getKoi().upvoteChat(UserPlatform.valueOf(platform), messageId);
        }

        public void deleteChat(@NonNull String platform, @NonNull String messageId) {
            CaffeinatedApp.getInstance().getKoi().deleteChat(UserPlatform.valueOf(platform), messageId, false);
        }

    }

    public static class FetchScriptHandle {

        @SneakyThrows
        public String asText(@NonNull String url) {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                return WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .addHeader("User-Agent", "Casterlabs/Bot")
                        .url(url)
                );
            } else if (url.startsWith("file://")) {
                File file = new File(WebUtil.decodeURIComponent(url.substring("file://".length())));
                byte[] contents = Files.readAllBytes(file.toPath());
                return new String(contents, StandardCharsets.UTF_8);
            } else {
                throw new UnsupportedOperationException("Unsupported protocol: " + url);
            }
        }
    }

    public static class PluginsScriptHandle {

        public Object callServiceMethod(@NonNull String pluginId, @NonNull String serviceId, @NonNull String methodName, @Nullable Object[] args) {
            return CaffeinatedApp.getInstance().getPlugins().callServiceMethod(pluginId, serviceId, methodName, args);
        }
    }

    public static class SoundScriptHandle {

        public void playAudio(@NonNull String audioUrl, Number volume) throws IOException {
            if (volume == null) volume = 1;

            byte[] audioBytes;
            String audioMime;

            if (audioUrl.startsWith("data:")) {
                FastLogger.logStatic(LogLevel.DEBUG, "Playing audio: ??? (data uri)");
                CaffeinatedApp.getInstance().getUI().playAudio(
                    audioUrl,
                    volume.floatValue()
                );
                return;
            } else if (audioUrl.startsWith("http://") || audioUrl.startsWith("https://")) {
                Pair<byte[], String> pair = WebUtil.sendHttpRequestBytesWithMime(
                    new Request.Builder()
                        .addHeader("User-Agent", "Casterlabs/Bot")
                        .url(audioUrl)
                );
                audioBytes = pair.a();
                audioMime = pair.b();
            } else if (audioUrl.startsWith("file://")) {
                File file = new File(WebUtil.decodeURIComponent(audioUrl.substring("file://".length())));
                audioBytes = Files.readAllBytes(file.toPath());
                audioMime = MimeTypes.getMimeForFile(file);
            } else {
                throw new UnsupportedOperationException("Unsupported protocol: " + audioUrl);
            }

            FastLogger.logStatic(LogLevel.DEBUG, "Playing audio: %s", audioMime);
            CaffeinatedApp.getInstance().getUI().playAudio(
                "data:" + audioMime + ";base64," + Base64.getEncoder().encodeToString(audioBytes),
                volume.floatValue()
            );
        }

        public void playTTS(@NonNull String text, String defaultVoice, Number volume) throws IOException {
            if (defaultVoice == null) defaultVoice = "Brian";
            if (volume == null) volume = 1;
            playAudio(TTS.getSpeechAsUrl(defaultVoice, text), volume.floatValue());
        }
    }

    public static class InputScriptHandle {

        @SneakyThrows
        public void keyPress(@NonNull String keyCode) {
            int scanCode = KeyEvent.class.getField(keyCode).getInt(null);

            robot.keyPress(scanCode);
            Thread.sleep(100); // We need delay to trigger successfully.
            robot.keyRelease(scanCode);
        }

        @SneakyThrows
        public void mouseMove(Number pixels, Number degrees, boolean smooth) {
            if (pixels.intValue() < 1) {
                throw new IllegalArgumentException("You must move the mouse by a positive amount of pixels. Did you mean to change degrees?");
            }

            int relativeX = 0;
            int relativeY = 0;

            switch (degrees.intValue() % 360) {
                case 0:
                    relativeY = -1;
                    break;
                case 90:
                    relativeX = 1;
                    break;
                case 180:
                    relativeY = 1;
                    break;
                case 270:
                    relativeX = -1;
                    break;

                default:
                    throw new IllegalArgumentException("The only supported directions are 0, 90, 180, and 270.");
            }

            if (smooth) {
                for (int _i = 0; _i < pixels.intValue(); _i++) {
                    Point currentPos = MouseInfo.getPointerInfo().getLocation();
                    robot.mouseMove(
                        currentPos.x + relativeX,
                        currentPos.y + relativeY
                    );
                    Thread.sleep(2); // Add some delay to "smoothly" move.
                }
            } else {
                Point currentPos = MouseInfo.getPointerInfo().getLocation();
                robot.mouseMove(
                    currentPos.x + (relativeX * pixels.intValue()),
                    currentPos.y + (relativeY * pixels.intValue())
                );
            }
        }

        @SneakyThrows
        public void mouseScroll(Number direction) {
            robot.mouseWheel(direction.intValue());
        }

        @SneakyThrows
        public void mouseClick(Number button) {
            int downMask;

            // Java flips the scroll and right click values. So we have to unflip them.
            switch (button.intValue()) {
                case 2:
                    downMask = InputEvent.BUTTON3_DOWN_MASK; // Right Click.
                    break;
                case 3:
                    downMask = InputEvent.BUTTON2_DOWN_MASK; // Scroll Click.
                    break;
                default:
                    downMask = InputEvent.getMaskForButton(button.intValue());
                    break;
            }

            robot.mousePress(downMask);
            Thread.sleep(50); // We need delay to trigger successfully.
            robot.mouseRelease(downMask);
        }

    }

    public static class InternalScriptHandle {

        @SneakyThrows
        public void sleep(Number milliseconds) {
            Thread.sleep(milliseconds.longValue());
        }

    }

    public static class CurrenciesScriptHandle {

        public String formatCurrency(Number amount, @NonNull String currency) throws InterruptedException, Throwable {
            return Currencies.formatCurrency(amount.doubleValue(), currency).await();
        }

    }

}
