package co.casterlabs.caffeinated.app.chatbot;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Action;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Command;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Shout;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.sdk.KoiImpl;
import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.koi.api.types.KoiEventType;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.StreamStatusEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.JsonStringUtil;
import lombok.Getter;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@JavascriptObject
public class AppChatbot {
    public static final char SYMBOL = '!';

    @SuppressWarnings("deprecation")
    @JavascriptValue(allowSet = false)
    private static List<KoiEventType> supportedShoutEvents = Arrays.asList(
        KoiEventType.DONATION,
        KoiEventType.FOLLOW,
        KoiEventType.RAID,
        KoiEventType.SUBSCRIPTION
    );

    private static Thread timerThread = new Thread(AppChatbot::doTimerLoop);
    private static int timerIndex;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static long nextMessageAt = -1;

    private static @Getter Deque<String> recentReplies = new LinkedList<>();

    private static void doTimerLoop() {
        while (true) {
            int timerIntervalSeconds = AppConfig.chatbotPreferences.get().timerIntervalSeconds;
            List<String> timerTexts = AppConfig.chatbotPreferences.get().timers;

            try {
                if (timerTexts.isEmpty() || timerIntervalSeconds < 1) {
                    nextMessageAt = -1;
                    Thread.sleep(Long.MAX_VALUE);  // Sleep forever (or until interrupted).
                } else {
                    long millisToWait = timerIntervalSeconds * 1000;

                    nextMessageAt = System.currentTimeMillis() + millisToWait;
                    Thread.sleep(millisToWait);
                }
            } catch (InterruptedException e) {
                Thread.interrupted(); // Clear.
                continue;
            }

            FastLogger.logStatic(LogLevel.DEBUG, "Doing chat bot tick!");

            // Increments the timer index, and check to make sure we're not overshooting.
            timerIndex++;
            if (timerIndex >= timerTexts.size()) {
                timerIndex = 0;
            }

            String text = timerTexts.get(timerIndex);
            if (text.isEmpty()) continue;

            for (StreamStatusEvent streamStatus : KoiImpl.INSTANCE.getStreamStates().values()) {
                if (!streamStatus.live) return;
                KoiImpl.INSTANCE.sendChat(
                    streamStatus.streamer.platform,
                    text,
                    AppConfig.chatbotPreferences.get().chatter,
                    null,
                    false
                );
            }
        }
    }

    public static void init() {
        timerThread.start();
    }

    public static boolean isChatBot(User sender) {
        return sender.username.equalsIgnoreCase("Casterlabs"); // TODO
    }

    public static boolean shouldHideFromWidgets(KoiEvent e) {
        switch (e.type()) {
            case RICH_MESSAGE: {
                RichMessageEvent richMessage = (RichMessageEvent) e;
                for (String chatbotToHide : AppConfig.chatbotPreferences.get().chatbots) {
                    if (richMessage.sender.username.equalsIgnoreCase(chatbotToHide) ||
                        richMessage.sender.displayname.equalsIgnoreCase(chatbotToHide)) {
                        return true;
                    }
                }

                // Check the replies for this specific message.
                if (AppConfig.chatbotPreferences.get().hideFromChat) {
                    if (recentReplies.remove(richMessage.raw)) {
                        return true;
                    }
                }

                // Check for !commands or "contains".
                if (AppConfig.chatbotPreferences.get().hideFromChat) {
                    for (Command command : AppConfig.chatbotPreferences.get().commands) {
                        // Not filled out, ignore.
                        if (command.trigger.isBlank() || command.response.isBlank()) {
                            continue;
                        }

                        // Null means any, so we check if the event's platform matches the target.
                        UserPlatform platform = richMessage.sender.platform;
                        if ((command.platform != null) && (command.platform != platform)) {
                            continue;
                        }

                        switch (command.triggerType) {
                            case COMMAND:
                                if (richMessage.raw.trim().startsWith(SYMBOL + command.trigger)) {
                                    return true;
                                }

                            case CONTAINS:
                                if (richMessage.raw.contains(command.trigger)) {
                                    return true;
                                }

                            case ALWAYS:
                                continue;
                        }
                    }
                }
            }

            default:
                break;
        }

        return false;
    }

    // Accessed from Koi.
    public static void processEventForShout(KoiEvent e) {
        for (Shout shout : AppConfig.chatbotPreferences.get().shouts) {
            @SuppressWarnings("deprecation")
            KoiEventType shoutType = shout.eventType == KoiEventType.DONATION ? //
                KoiEventType.RICH_MESSAGE : shout.eventType;

            if (shoutType != e.type() ||
                shout.response.isBlank()) {
                continue;
            }

            User eventSender = null;

            switch (e.type()) {
                case RICH_MESSAGE: {
                    RichMessageEvent richMessage = (RichMessageEvent) e;

                    // Not a donation, skip.
                    if (richMessage.donations.isEmpty()) {
                        continue;
                    }

                    eventSender = richMessage.sender;
                    break;
                }

                case FOLLOW:
                    eventSender = ((FollowEvent) e).follower;
                    break;

                case RAID:
                    eventSender = ((RaidEvent) e).host;
                    break;

                case SUBSCRIPTION:
                    eventSender = ((SubscriptionEvent) e).subscriber;
                    break;

                default:
                    continue;
            }

            // Null means any, so we check if the event's platform matches the target.
            UserPlatform platform = eventSender.platform;
            if ((shout.platform != null) && (shout.platform != platform)) {
                continue;
            }

            switch (shout.responseAction) {
                case EXECUTE: {
                    CaffeinatedImpl.INSTANCE.getScriptingEngines().get("javascript").execute(e, shout.response);
                    break;
                }

                case REPLY_WITH: {
                    String message = shout.response
                        .replace("%username%", eventSender.displayname);

                    if (!message.contains(eventSender.displayname)) {
                        // Try to always mention the actual user, this is to prevent issues with spam
                        // detection systems.
                        message = String.format("@%s %s", eventSender.displayname, message);
                    }

                    CaffeinatedImpl.INSTANCE.getScriptingEngines().get("javascript").execute(
                        e,
                        String.format(
                            "Koi.sendChat(event.streamer.platform, `%s`, ChatBot.realChatter, event.id);",
                            JsonStringUtil.jsonEscape(message).toString().replace("`", "\\`")
                        )
                    );
                    break;
                }
            }
            return;
        }
    }

    // Accessed from Koi.
    /**
     * @return true if the message should get hidden (assuming the user has the
     *         relevant option enabled)
     */
    public static void processEventForCommand(RichMessageEvent richMessage) {
        for (Command command : AppConfig.chatbotPreferences.get().commands) {
            // Not filled out, ignore.
            if (command.trigger.isBlank() || command.response.isBlank()) {
                continue;
            }

            // Null means any, so we check if the event's platform matches the target.
            UserPlatform platform = richMessage.sender.platform;
            if ((command.platform != null) && (command.platform != platform)) {
                continue;
            }

            switch (command.triggerType) {
                case COMMAND:
                    if (!richMessage.raw.trim().startsWith(SYMBOL + command.trigger)) {
                        continue;
                    }
                    break;

                case CONTAINS:
                    if (!richMessage.raw.contains(command.trigger)) {
                        continue;
                    }
                    break;

                case ALWAYS:
                    if (isChatBot(richMessage.sender) || command.responseAction != Action.EXECUTE) {
                        // Prevent infinite loops / dumb behavior.
                        continue;
                    }
                    break;
            }

            switch (command.responseAction) {
                case EXECUTE:
                    CaffeinatedImpl.INSTANCE.getScriptingEngines().get("javascript").execute(richMessage, command.response);
                    break;

                case REPLY_WITH: {
                    String message = command.response;
                    CaffeinatedImpl.INSTANCE.getScriptingEngines().get("javascript").execute(
                        richMessage,
                        String.format(
                            "Koi.sendChat(event.streamer.platform, `%s`, ChatBot.realChatter, event.id);",
                            JsonStringUtil.jsonEscape(message).toString().replace("`", "\\`")
                        )
                    );
                    break;
                }
            }
        }
    }

}
