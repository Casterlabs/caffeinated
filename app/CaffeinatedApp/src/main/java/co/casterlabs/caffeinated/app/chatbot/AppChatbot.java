package co.casterlabs.caffeinated.app.chatbot;

import java.util.Arrays;
import java.util.List;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Command;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Shout;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.TriggerType;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.NonNull;

public class AppChatbot extends JavascriptObject {
    public static final char SYMBOL = '!';

    @JavascriptValue(allowSet = false, watchForMutate = false)
    private PreferenceFile<ChatbotPreferences> preferences;

    @SuppressWarnings("deprecation")
    @JavascriptValue(allowSet = false)
    private List<KoiEventType> supportedShoutEvents = Arrays.asList(
        KoiEventType.DONATION, KoiEventType.FOLLOW,
        KoiEventType.RAID, KoiEventType.SUBSCRIPTION
    );

    private AsyncTask timerTask;
    private int timerIndex;

    @JavascriptSetter("preferences")
    public void setPreferences(@NonNull ChatbotPreferences prefs) {
        this.preferences.set(prefs);
        this.resetTimerTask();
    }

    private void resetTimerTask() {
        if (this.timerTask != null)
            this.timerTask.cancel();

        int timerIntervalSeconds = this.preferences.get().getTimerIntervalSeconds();

        if ((timerIntervalSeconds > 0) && !this.preferences.get().getTimers().isEmpty()) {
            this.timerTask = AsyncTask.create(() -> {
                while (true) {
                    try {
                        Thread.sleep(timerIntervalSeconds * 1000);
                    } catch (InterruptedException e) {
                        return;
                    }

                    List<String> timerTexts = this.preferences.get().getTimers();

                    // Increments the timer index, and check to make sure we're not overshooting.
                    this.timerIndex++;
                    if (timerIndex >= timerTexts.size()) {
                        this.timerIndex = 0;
                    }

                    String text = timerTexts.get(this.timerIndex);

                    if (!text.isEmpty()) {
                        Koi koi = CaffeinatedApp.getInstance().getKoi();

                        for (UserPlatform platform : koi.getSignedInPlatforms()) {
                            koi.sendChat(
                                platform,
                                text,
                                this.preferences.get().getRealChatter(),
                                null,
                                false
                            );
                        }
                    }
                }
            });
        } else {
            this.timerTask = null;
        }
    }

    public void init() {
        this.preferences = CaffeinatedApp.getInstance().getChatbotPreferences();
        this.resetTimerTask();
    }

    public boolean isChatBot(User sender) {
        return sender.getUsername().equalsIgnoreCase("Casterlabs"); // TODO
    }

    // Accessed from Koi.
    public void processEventForShout(KoiEvent e) {
        for (Shout shout : this.preferences.get().getShouts()) {
            @SuppressWarnings("deprecation")
            KoiEventType shoutType = shout.getEventType() == KoiEventType.DONATION ? //
                KoiEventType.RICH_MESSAGE : shout.getEventType();

            if (shoutType != e.getType() ||
                shout.getResponse().isBlank()) {
                continue;
            }

            User eventSender = null;

            switch (e.getType()) {
                case RICH_MESSAGE: {
                    RichMessageEvent richMessage = (RichMessageEvent) e;

                    // Not a donation, skip.
                    if (richMessage.getDonations().isEmpty()) {
                        continue;
                    }

                    eventSender = richMessage.getSender();
                    break;
                }

                case FOLLOW:
                    eventSender = ((FollowEvent) e).getFollower();
                    break;

                case RAID:
                    eventSender = ((RaidEvent) e).getHost();
                    break;

                case SUBSCRIPTION:
                    eventSender = ((SubscriptionEvent) e).getSubscriber();
                    break;

                default:
                    continue;
            }

            // Null means any, so we check if the event's platform matches the target.
            UserPlatform platform = eventSender.getPlatform();
            if ((shout.getPlatform() != null) && (shout.getPlatform() != platform)) {
                continue;
            }

            switch (shout.getResponseAction()) {
                case EXECUTE: {
                    ChatbotScriptEngine.execute(e, shout.getResponse());
                    break;
                }

                case REPLY_WITH: {
                    String message = shout.getResponse()
                        .replace("%username%", eventSender.getDisplayname());

                    if (!message.contains(eventSender.getDisplayname())) {
                        // Try to always mention the actual user, this is to prevent issues with spam
                        // detection systems.
                        message = String.format("@%s %s", eventSender.getDisplayname(), message);
                    }

                    CaffeinatedApp.getInstance().getKoi().sendChat(
                        platform,
                        message,
                        this.preferences.get().getRealChatter(),
                        null,
                        false
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
    public boolean processEventForCommand(RichMessageEvent richMessage) {
        boolean hideable = false;

        for (Command command : this.preferences.get().getCommands()) {
            // Not filled out, ignore.
            if (command.getTrigger().isBlank() || command.getResponse().isBlank()) {
                continue;
            }

            // Null means any, so we check if the event's platform matches the target.
            UserPlatform platform = richMessage.getSender().getPlatform();
            if ((command.getPlatform() == null) || (command.getPlatform() == platform)) {
                boolean actUpon = false;

                switch (command.getTriggerType()) {
                    case COMMAND:
                        if (richMessage.getRaw().trim().startsWith(SYMBOL + command.getTrigger())) {
                            actUpon = true;
                        }
                        break;

                    case CONTAINS:
                        if (richMessage.getRaw().contains(command.getTrigger())) {
                            actUpon = true;
                        }
                        break;

                    case ALWAYS:
                        if (!this.isChatBot(richMessage.getSender())) { // Prevent infinite loops.
                            actUpon = true;
                        }
                        break;
                }

                if (!actUpon) continue;

                switch (command.getResponseAction()) {
                    case EXECUTE:
                        ChatbotScriptEngine.execute(richMessage, command.getResponse());
                        break;

                    case REPLY_WITH: {
                        String message = command.getResponse();
                        CaffeinatedApp.getInstance().getKoi().sendChat(
                            platform,
                            message,
                            this.preferences.get().getRealChatter(),
                            richMessage.getId(),
                            false
                        );
                        break;
                    }
                }

                if (command.getTriggerType() != TriggerType.ALWAYS) {
                    // We don't want to hide messages by accident with ALWAYS, hence the check.
                    hideable = true;
                }
            }
        }

        return hideable;
    }

}
