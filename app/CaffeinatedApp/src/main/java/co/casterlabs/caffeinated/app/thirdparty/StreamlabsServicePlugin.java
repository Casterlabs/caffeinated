package co.casterlabs.caffeinated.app.thirdparty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.builtin.CaffeinatedDefaultPlugin;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.widgets.Widget;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetDetails;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstance;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetInstanceMode;
import co.casterlabs.caffeinated.pluginsdk.widgets.WidgetType;
import co.casterlabs.caffeinated.util.MimeTypes;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.functional.tuples.Pair;
import co.casterlabs.koi.api.listener.KoiEventHandler;
import co.casterlabs.koi.api.types.events.DonationEvent;
import co.casterlabs.koi.api.types.events.FollowEvent;
import co.casterlabs.koi.api.types.events.RaidEvent;
import co.casterlabs.koi.api.types.events.SubscriptionEvent;
import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonBoolean;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;
import okhttp3.RequestBody;

public class StreamlabsServicePlugin extends CaffeinatedPlugin {

    @SuppressWarnings("deprecation")
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

        this.getPlugins().registerWidget(this, StreamlabsUI.DETAILS, StreamlabsUI.class);
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
            .withFriendlyName("Streamlabs Integration");

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
                CaffeinatedApp.getInstance().getAuth().requestOAuthSignin("thirdparty", "streamlabs", false);
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
            this.triggerAlert("follow", e.getFollower().getDisplayname(), "");
        }

        @KoiEventHandler
        public void onSubscription(SubscriptionEvent e) {
            this.triggerAlert("subscription", "", "");
        }

        @SuppressWarnings("deprecation")
        @KoiEventHandler
        public void onDonation(DonationEvent e) {
            this.triggerAlert("donation", e.getSender().getDisplayname(), "");
        }

        @KoiEventHandler
        public void onRaid(RaidEvent e) {
            this.triggerAlert("host", e.getHost().getDisplayname(), "");
        }

    }

    @Override
    public @Nullable Pair<String, String> getResource(String resource) throws IOException {
        // Append `index.html` to the end when required.
        if (!resource.contains(".")) {
            if (resource.endsWith("/")) {
                resource += "index.html";
            } else {
                resource += ".html";
            }
        }

        String mimeType = "application/octet-stream";

        String[] split = resource.split("\\.");
        if (split.length > 1) {
            mimeType = MimeTypes.getMimeForType(split[split.length - 1]);
        }

        resource = "app" + resource; // Load from the app's actual resources.
        this.getLogger().debug("Loading resource: %s", resource);

        try (InputStream in = CaffeinatedDefaultPlugin.class.getClassLoader().getResourceAsStream(resource)) {
            return new Pair<>(
                IOUtil.readInputStreamString(in, StandardCharsets.UTF_8),
                mimeType
            );
        } catch (Exception e) {
            this.getLogger().debug("An error occurred whilst loading resource %s:\n%s", resource, e);
            return new Pair<>("", "text/plain");
        }
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
