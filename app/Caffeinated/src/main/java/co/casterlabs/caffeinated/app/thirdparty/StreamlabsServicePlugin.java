package co.casterlabs.caffeinated.app.thirdparty;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.ui.UIDocksPlugin;
import co.casterlabs.caffeinated.pluginsdk.Caffeinated;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.RichMessageEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonBoolean;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.RequestBody;

@SuppressWarnings("deprecation")
public class StreamlabsServicePlugin extends CaffeinatedPlugin {

    @Override
    public void onInit() {
        CaffeinatedApp
            .getInstance()
            .onAppEvent("auth:completion", (JsonObject data) -> {
                if (data.getString("type").equals("thirdparty") &&
                    data.getString("platform").equals("streamlabs")) {
                    this.getLogger().info("Completing OAuth.");
                    this.completeOAuth();
                }
            });

        Caffeinated.getInstance().getPlugins().registerWidget(this, StreamlabsUI.DETAILS, StreamlabsUI.class);
    }

    @Override
    public void onClose() {}

    @Override
    public @NonNull String getName() {
        return "Streamlabs Integration";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.thirdparty.streamlabs";
    }

    public static class StreamlabsUI extends Widget {
        public static final WidgetDetails DETAILS = new WidgetDetails()
            .withNamespace("co.casterlabs.thirdparty.streamlabs.settings")
            .withType(WidgetType.SETTINGS_APPLET)
            .withFriendlyName("Streamlabs Alerts");

        @Override
        public @NonNull String getWidgetBasePath(WidgetInstanceMode mode) {
            return "/thirdparty/streamlabs/settings";
        }

        @SneakyThrows
        private void triggerAlert(String type, String username, String userMessage) {
            String accessToken = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("thirdparty", "streamlabs");
            if (accessToken == null) return;

            WebUtil.sendHttpRequest(
                new Request.Builder()
                    .header("Authorization", "Bearer " + accessToken)
                    .url(
                        String.format(
                            "https://streamlabs.com/api/v2.0/alerts?type=%s&message=%s&user_message=%s",
                            WebUtil.encodeURIComponent(type),
                            WebUtil.encodeURIComponent(username),
                            WebUtil.encodeURIComponent(userMessage)
                        )
                    )
                    .post(RequestBody.create(new byte[0]))
            );
        }

        @Override
        public void onNewInstance(@NonNull WidgetInstance instance) {
            try {
                String accessToken = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("thirdparty", "streamlabs");
                instance.emit("hasToken", new JsonBoolean(accessToken != null));
            } catch (IOException ignored) {}

            instance.on("hasToken", () -> {
                String accessToken = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("thirdparty", "streamlabs");

                try {
                    instance.emit("hasToken", new JsonBoolean(accessToken != null));
                } catch (IOException ignored) {}
            });

            instance.on("setSettings", (e) -> {
                this.setSettings(e.getAsObject());
            });

            instance.on("signin", () -> {
                CaffeinatedApp.getInstance().getAuth().requestOAuthSignin("thirdparty", "streamlabs", false, null);
            });

            instance.on("signout", () -> {
                CaffeinatedApp.getInstance().getAuthPreferences().get().removeToken("thirdparty", "streamlabs");
                CaffeinatedApp.getInstance().getAuthPreferences().save();
                try {
                    instance.emit("hasToken", new JsonBoolean(false));
                } catch (IOException ignored) {}
            });

            instance.on("test", () -> {
                this.triggerAlert("follow", "Casterlabs", "");
            });
        }

        @KoiEventHandler
        public void onFollow(FollowEvent e) {
            this.triggerAlert("follow", e.follower.displayname, "");
        }

        @KoiEventHandler
        public void onSubscription(SubscriptionEvent e) {
            this.triggerAlert("subscription", "", "");
        }

        @KoiEventHandler
        public void onDonation(RichMessageEvent e) {
            if (e.donations.isEmpty()) return;
            this.triggerAlert("donation", e.sender.displayname, "");
        }

        @KoiEventHandler
        public void onRaid(RaidEvent e) {
            this.triggerAlert("host", e.host.displayname, "");
        }

    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        return UIDocksPlugin.resolveUIFile(resource);
    }

    private void completeOAuth() {
        String code = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("thirdparty", "streamlabs");

        try {
            JsonObject response = Rson.DEFAULT.fromJson(
                WebUtil.sendHttpRequest(
                    new Request.Builder()
                        .url("https://api.casterlabs.co/v2/caffeinated/streamlabs/auth/code?code=" + code)
                ), JsonObject.class
            );

            String accessToken = response.getString("access_token");

            // Update the auth file.
            CaffeinatedApp.getInstance().getAuthPreferences().get().addToken("thirdparty", "streamlabs", accessToken);
            CaffeinatedApp.getInstance().getAuthPreferences().save();
            this.getLogger().info("OAuth Completed, enabling now.");

            for (Widget w : this.getWidgets()) {
                w.broadcastToAll("hasToken", new JsonBoolean(true));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
