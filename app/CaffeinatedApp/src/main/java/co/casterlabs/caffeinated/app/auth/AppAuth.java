package co.casterlabs.caffeinated.app.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.auth.events.AppAuthCaffeineSigninEvent;
import co.casterlabs.caffeinated.app.auth.events.AppAuthCancelSigninEvent;
import co.casterlabs.caffeinated.app.auth.events.AppAuthEventType;
import co.casterlabs.caffeinated.app.auth.events.AppAuthRequestOAuthSigninEvent;
import co.casterlabs.caffeinated.app.auth.events.AppAuthSignoutEvent;
import co.casterlabs.kaimen.webview.bridge.BridgeValue;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.serialization.JsonParseException;
import lombok.Getter;
import xyz.e3ndr.eventapi.EventHandler;
import xyz.e3ndr.eventapi.listeners.EventListener;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppAuth {
    private static EventHandler<AppAuthEventType> handler = new EventHandler<>();

    private FastLogger logger = new FastLogger();

    private @Getter Map<String, AuthInstance> authInstances = new HashMap<>();
    private AuthCallback currentAuthCallback;

    private @Getter boolean isAuthorized = false;

    private BridgeValue<JsonObject> bridge = new BridgeValue<>("auth");

    public AppAuth() {
        handler.register(this);
    }

    public boolean isSignedIn() {
        return !this.authInstances.isEmpty();
    }

    public void init() {
        CaffeinatedApp.getInstance().getAppBridge().attachValue(this.bridge);
        this.updateBridgeData(); // Populate

        List<String> ids = CaffeinatedApp.getInstance().getAuthPreferences().get().getKoiTokenIds();

        for (String tokenId : ids) {
            this.startAuthInstance(tokenId);
        }
    }

    public void shutdown() {
        this.onCancelSigninEvent(null);

        for (AuthInstance inst : this.authInstances.values()) {
            try {
                inst.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int countPlatform(UserPlatform platform) {
        int count = 0;
        for (AuthInstance inst : this.authInstances.values()) {
            if ((inst.getUserData() != null) && (inst.getUserData().getPlatform() == platform)) {
                count++;
            }
        }
        return count;
    }

    public void checkAuth() {
        boolean authorized = false;

        for (AuthInstance inst : this.authInstances.values()) {
            if (inst.isConnected()) {
                authorized = true;
                break;
            }
        }

        if (this.isAuthorized != authorized) {
            CaffeinatedApp
                .getInstance()
                .getUI()
                .navigate(authorized ? "/home" : "/signin");
        }

        this.isAuthorized = authorized;
    }

    @SuppressWarnings("deprecation")
    public void updateBridgeData() {
        this.checkAuth();

        JsonObject koiAuth = new JsonObject();

        for (AuthInstance inst : this.authInstances.values()) {
            if (inst.getUserData() != null) {
                JsonElement userData = Rson.DEFAULT.toJson(inst.getUserData());
                JsonElement streamData = Rson.DEFAULT.toJson(inst.getStreamData());

                koiAuth.put(
                    inst.getUserData().getPlatform().name(),
                    new JsonObject()
                        .put("tokenId", inst.getTokenId())
                        .put("token", inst.getToken())
                        .put("userData", userData)
                        .put("streamData", streamData)
                );
            }
        }

        JsonObject bridgeData = new JsonObject()
            .put("isAuthorized", this.isAuthorized)
            .put("koiAuth", koiAuth);

        this.bridge.set(bridgeData);

        CaffeinatedApp.getInstance().emitAppEvent("auth:platforms", bridgeData);
        CaffeinatedApp.getInstance().getKoi().updateFromAuth();
    }

    private void startAuthInstance(String tokenId) {
        this.logger.debug("Starting AuthInstance with id: %s", tokenId);
        this.authInstances.put(tokenId, new AuthInstance(tokenId));
    }

    @EventListener
    public void onRequestOAuthSigninEvent(AppAuthRequestOAuthSigninEvent event) {
        try {
            this.logger.info("Signin requested. (%s)", event.getPlatform());

            if (this.currentAuthCallback != null) {
                this.onCancelSigninEvent(null);
            }

            this.currentAuthCallback = this.authorize(event.getPlatform());

            this.currentAuthCallback
                .connect()
                .then((token) -> {
                    this.currentAuthCallback = null;

                    if (token != null) {
                        this.logger.info("Signin completed (%s)", event.getPlatform());

                        if (event.isKoi()) {
                            String tokenId = CaffeinatedApp
                                .getInstance()
                                .getAuthPreferences()
                                .get()
                                .addKoiToken(token);

                            this.startAuthInstance(tokenId);
                        } else {
                            CaffeinatedApp
                                .getInstance()
                                .getAuthPreferences()
                                .get()
                                .addToken(event.getPlatform(), token);

                            CaffeinatedApp.getInstance().emitAppEvent(
                                "auth:completion",
                                new JsonObject()
                                    .put("platform", event.getPlatform())
                                    .put("tokenId", event.getPlatform())
                            );
                        }

                        if (event.isGoBack()) {
                            // Navigate backwards for the signin screen.
                            CaffeinatedApp.getInstance().getUI().goBack();
                        }
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventListener
    public void onCaffeineSigninEvent(AppAuthCaffeineSigninEvent event) {
        String tokenId = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addKoiToken(event.getToken());

        this.startAuthInstance(tokenId);
    }

    @EventListener
    public void onSignoutEvent(AppAuthSignoutEvent event) {
        AuthInstance inst = this.authInstances.remove(event.getTokenId());

        if (inst != null) {
            inst.invalidate();
        }
    }

    @EventListener
    public void onCancelSigninEvent(AppAuthCancelSigninEvent event) {
        this.logger.info("Signin cancelled (?)");
        if (this.currentAuthCallback != null) {
            this.currentAuthCallback.cancel();
            this.currentAuthCallback = null;
        }
    }

    public AuthCallback authorize(String type) throws IOException {
        String oauthLink = CaffeinatedApp.AUTH_URLS.getString(type);

        if (oauthLink == null) {
            throw new IllegalArgumentException("Type '" + type + "' does not have an oauth link associated with it.");
        } else {
            AuthCallback callback = new AuthCallback(type);

            try {
                Desktop
                    .getDesktop()
                    .browse(new URI(oauthLink + "?state=" + callback.getStateString()));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            return callback;
        }
    }

    public static void invokeEvent(JsonObject data, String nestedType) throws InvocationTargetException, JsonParseException {
        handler.call(
            Rson.DEFAULT.fromJson(
                data,
                AppAuthEventType
                    .valueOf(nestedType)
                    .getEventClass()
            )
        );
    }

    public @Nullable AuthInstance getAuthInstance(UserPlatform platform) {
        for (AuthInstance inst : this.authInstances.values()) {
            if ((inst.getUserData() != null) &&
                (inst.getUserData().getPlatform() == platform)) {
                return inst;
            }
        }

        return null;
    }

}
