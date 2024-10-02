package co.casterlabs.caffeinated.app.chatbot;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Action;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Command;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Shout;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
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
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class AppChatbot extends JavascriptObject {
    public static final char SYMBOL = '!';

    @JavascriptValue(allowSet = false, watchForMutate = false)
    private PreferenceFile<ChatbotPreferences> preferences;

    @SuppressWarnings("deprecation")
    @JavascriptValue(allowSet = false)
    private List<KoiEventType> supportedShoutEvents = Arrays.asList(
        KoiEventType.DONATION,
        KoiEventType.FOLLOW,
        KoiEventType.RAID,
        KoiEventType.SUBSCRIPTION
    );

    private Thread timerThread = new Thread(this::doTimerLoop);
    private int timerIndex;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private long nextMessageAt = -1;

    private @Getter Deque<String> recentReplies = new LinkedList<>();

    @JavascriptSetter("preferences")
    public void setPreferences(@NonNull ChatbotPreferences prefs) {
        this.preferences.set(prefs);
        this.timerThread.interrupt();
    }

    private void doTimerLoop() {
        while (true) {
            int timerIntervalSeconds = this.preferences.get().getTimerIntervalSeconds();
            List<String> timerTexts = this.preferences.get().getTimers();

            try {
                if (timerTexts.isEmpty() || timerIntervalSeconds < 1) {
                    this.nextMessageAt = -1;
                    Thread.sleep(Long.MAX_VALUE);  // Sleep forever (or until interrupted).
                } else {
                    long millisToWait = timerIntervalSeconds * 1000;

                    this.nextMessageAt = System.currentTimeMillis() + millisToWait;
                    Thread.sleep(millisToWait);
                }
            } catch (InterruptedException e) {
                Thread.interrupted(); // Clear.
                continue;
            }

            FastLogger.logStatic(LogLevel.DEBUG, "Doing chat bot tick!");

            // Increments the timer index, and check to make sure we're not overshooting.
            this.timerIndex++;
            if (timerIndex >= timerTexts.size()) {
                this.timerIndex = 0;
            }

            String text = timerTexts.get(this.timerIndex);
            if (text.isEmpty()) continue;

            for (StreamStatusEvent streamStatus : CaffeinatedApp.getInstance().getKoi().getStreamStates().values()) {
                if (!streamStatus.live) return;
                CaffeinatedApp.getInstance().getKoi().sendChat(
                    streamStatus.streamer.platform,
                    text,
                    this.preferences.get().getChatter(),
                    null,
                    false
                );
            }
        }
    }

    public void init() {
        this.preferences = CaffeinatedApp.getInstance().getChatbotPreferences();
        this.timerThread.start();
    }

    public boolean isChatBot(User sender) {
        return sender.username.equalsIgnoreCase("Casterlabs"); // TODO
    }

    public boolean shouldHideFromWidgets(KoiEvent e) {
        switch (e.type()) {
            case RICH_MESSAGE: {
                RichMessageEvent richMessage = (RichMessageEvent) e;
                for (String chatbotToHide : this.preferences.get().getChatbots()) {
                    if (richMessage.sender.username.equalsIgnoreCase(chatbotToHide) ||
                        richMessage.sender.displayname.equalsIgnoreCase(chatbotToHide)) {
                        return true;
                    }
                }

                // Check the replies for this specific message.
                if (this.preferences.get().isHideFromChat()) {
                    if (this.recentReplies.remove(richMessage.raw)) {
                        return true;
                    }
                }

                // Check for !commands or "contains".
                if (this.preferences.get().isHideFromChat()) {
                    for (Command command : this.preferences.get().getCommands()) {
                        // Not filled out, ignore.
                        if (command.getTrigger().isBlank() || command.getResponse().isBlank()) {
                            continue;
                        }

                        // Null means any, so we check if the event's platform matches the target.
                        UserPlatform platform = richMessage.sender.platform;
                        if ((command.getPlatform() != null) && (command.getPlatform() != platform)) {
                            continue;
                        }

                        switch (command.getTriggerType()) {
                            case COMMAND:
                                if (richMessage.raw.trim().startsWith(SYMBOL + command.getTrigger())) {
                                    return true;
                                }

                            case CONTAINS:
                                if (richMessage.raw.contains(command.getTrigger())) {
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
    public void processEventForShout(KoiEvent e) {
        for (Shout shout : this.preferences.get().getShouts()) {
            @SuppressWarnings("deprecation")
            KoiEventType shoutType = shout.getEventType() == KoiEventType.DONATION ? //
                KoiEventType.RICH_MESSAGE : shout.getEventType();

            if (shoutType != e.type() ||
                shout.getResponse().isBlank()) {
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
            if ((shout.getPlatform() != null) && (shout.getPlatform() != platform)) {
                continue;
            }

            switch (shout.getResponseAction()) {
                case EXECUTE: {
                    CaffeinatedApp.getInstance().getScriptingEngines().get("javascript").execute(e, shout.getResponse());
                    break;
                }

                case REPLY_WITH: {
                    String message = shout.getResponse()
                        .replace("%username%", eventSender.displayname);

                    if (!message.contains(eventSender.displayname)) {
                        // Try to always mention the actual user, this is to prevent issues with spam
                        // detection systems.
                        message = String.format("@%s %s", eventSender.displayname, message);
                    }

                    CaffeinatedApp.getInstance().getScriptingEngines().get("javascript").execute(
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
    public void processEventForCommand(RichMessageEvent richMessage) {
        for (Command command : this.preferences.get().getCommands()) {
            // Not filled out, ignore.
            if (command.getTrigger().isBlank() || command.getResponse().isBlank()) {
                continue;
            }

            // Null means any, so we check if the event's platform matches the target.
            UserPlatform platform = richMessage.sender.platform;
            if ((command.getPlatform() != null) && (command.getPlatform() != platform)) {
                continue;
            }

            switch (command.getTriggerType()) {
                case COMMAND:
                    if (!richMessage.raw.trim().startsWith(SYMBOL + command.getTrigger())) {
                        continue;
                    }
                    break;

                case CONTAINS:
                    if (!richMessage.raw.contains(command.getTrigger())) {
                        continue;
                    }
                    break;

                case ALWAYS:
                    if (this.isChatBot(richMessage.sender) || command.getResponseAction() != Action.EXECUTE) {
                        // Prevent infinite loops / dumb behavior.
                        continue;
                    }
                    break;
            }

            switch (command.getResponseAction()) {
                case EXECUTE:
                    CaffeinatedApp.getInstance().getScriptingEngines().get("javascript").execute(richMessage, command.getResponse());
                    break;

                case REPLY_WITH: {
                    String message = command.getResponse();
                    CaffeinatedApp.getInstance().getScriptingEngines().get("javascript").execute(
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
