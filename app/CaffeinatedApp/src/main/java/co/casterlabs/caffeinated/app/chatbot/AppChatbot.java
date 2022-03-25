package co.casterlabs.caffeinated.app.chatbot;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Command;
import co.casterlabs.caffeinated.app.chatbot.ChatbotPreferences.Shout;
import co.casterlabs.caffeinated.pluginsdk.koi.Koi;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptSetter;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.types.events.ChatEvent;
import co.casterlabs.koi.api.types.events.DonationEvent;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.KoiEvent;
import co.casterlabs.koi.api.types.events.KoiEventType;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import lombok.NonNull;

public class AppChatbot extends JavascriptObject {
    @JavascriptValue(allowSet = false)
    private PreferenceFile<ChatbotPreferences> preferences;

    @JavascriptValue(allowSet = false)
    private List<KoiEventType> supportedShoutEvents = Arrays.asList(KoiEventType.DONATION, KoiEventType.FOLLOW, KoiEventType.RAID, KoiEventType.SUBSCRIPTION);

    private AsyncTask timerTask;
    private int timerIndex;

    @JavascriptSetter("preferences")
    public void setPreferences(@NonNull ChatbotPreferences prefs) {
        this.preferences.set(prefs);
        this.resetTimerTask();
    }

    private void resetTimerTask() {
        if (this.timerTask != null) this.timerTask.cancel();

        int timerIntervalSeconds = this.preferences.get().getTimerIntervalSeconds();

        if ((timerIntervalSeconds > 0) && !this.preferences.get().getTimers().isEmpty()) {
            this.timerTask = new AsyncTask(() -> {
                while (true) {
                    try {
                        Thread.sleep(timerIntervalSeconds * 1000);
                    } catch (InterruptedException e) {}

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
                            koi.sendChat(platform, text, this.preferences.get().getChatter());
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

    // Accessed from Koi.
    public void processEvent(KoiEvent e) {
        Set<Shout> shouts = this.preferences.get().getShouts();

        for (Shout shout : shouts) {
            if (shout.getEventType() == e.getType()) {
                if (shout.getText().isBlank()) {
                    continue;
                }

                User target = null;

                switch (e.getType()) {
                    case DONATION:
                        target = ((DonationEvent) e).getSender();
                        break;

                    case FOLLOW:
                        target = ((FollowEvent) e).getFollower();
                        break;

                    case RAID:
                        target = ((RaidEvent) e).getHost();
                        break;

                    case SUBSCRIPTION:
                        target = ((SubscriptionEvent) e).getSubscriber();
                        break;

                    default:
                        continue;
                }

                UserPlatform platform = target.getPlatform();

                if (platform == UserPlatform.CASTERLABS_SYSTEM) {
                    platform = CaffeinatedApp.getInstance().getKoi().getFirstSignedInPlatform();
                }

                if ((shout.getPlatform() == null) || (shout.getPlatform() == platform)) {
                    String message = String.format("@%s %s", target.getDisplayname(), shout.getText());

                    CaffeinatedApp.getInstance().getKoi().sendChat(platform, message, this.preferences.get().getChatter());
                    break; // We still want to try to process it as a command.
                }
            }
        }

        if ((e.getType() == KoiEventType.CHAT) || (e.getType() == KoiEventType.DONATION)) {
            ChatEvent chatEvent = (ChatEvent) e;

            for (Command command : this.preferences.get().getCommands()) {
                if (command.getTrigger().isBlank() || command.getResponse().isBlank()) {
                    continue;
                }

                UserPlatform platform = chatEvent.getSender().getPlatform();

                if (platform == UserPlatform.CASTERLABS_SYSTEM) {
                    platform = CaffeinatedApp.getInstance().getKoi().getFirstSignedInPlatform();
                }

                if ((command.getPlatform() == null) || (command.getPlatform() == platform)) {
                    boolean send = false;

                    switch (command.getType()) {
                        case COMMAND:
                            if (chatEvent.getMessage().startsWith('!' + command.getTrigger())) {
                                send = true;
                            }
                            break;

                        case CONTAINS:
                            if (chatEvent.getMessage().contains(command.getTrigger())) {
                                send = true;
                            }
                            break;

                    }

                    if (send) {
                        String message;

                        if (this.preferences.get().isMentionInReply()) {
                            message = String.format("@%s %s", chatEvent.getSender().getDisplayname(), command.getResponse());
                        } else {
                            message = command.getResponse();
                        }

                        CaffeinatedApp.getInstance().getKoi().sendChat(platform, message, this.preferences.get().getChatter());
                        return; // Break the loop.
                    }
                }
            }
        }
    }

}
