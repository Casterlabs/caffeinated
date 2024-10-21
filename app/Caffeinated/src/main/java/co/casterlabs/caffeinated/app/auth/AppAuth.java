package co.casterlabs.caffeinated.app.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.types.stream.KoiStreamLanguage;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppAuth extends JavascriptObject {
    private FastLogger logger = new FastLogger();

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private Map<String, AuthInstance> authInstances = new HashMap<>() {
        private static final long serialVersionUID = 7958906556261055481L;

        @Override
        public int hashCode() {
            return Objects.hash(this.values().toArray());
        }
    };

    private AuthCallback currentAuthCallback;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private boolean isKoiAlive = true;

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private boolean isAuthorized = false;

    synchronized void checkStatus() {
        boolean isAlive = false;

        for (AuthInstance inst : this.authInstances.values()) {
            if (inst.isWelcomed()) {
                isAlive = true;
                break;
            }
        }

        if (this.isKoiAlive != isAlive) {
            if (isAlive) {
                CaffeinatedApp.getInstance().notify(
                    "co.casterlabs.caffeinated.app.auth.reconnected",
                    Collections.emptyMap(),
                    NotificationType.INFO
                );
            } else {
                // Show an error to the user.
                CaffeinatedApp.getInstance().notify(
                    "co.casterlabs.caffeinated.app.auth.lost_connection",
                    Collections.emptyMap(),
                    NotificationType.ERROR
                );
            }
        }

        this.isKoiAlive = isAlive;
    }

    public boolean isSignedIn() {
        return !this.authInstances.isEmpty();
    }

    public void init() {
        for (String tokenId : CaffeinatedApp.getInstance().getAuthPreferences().get().getAllTokenIdsByType("koi")) {
            this.startAuthInstance(tokenId);
        }
    }

    public void shutdown() {
        this.cancelSignin();

        for (AuthInstance inst : this.authInstances.values()) {
            inst.close();
        }
    }

    public int countPlatform(UserPlatform platform) {
        int count = 0;
        for (AuthInstance inst : this.authInstances.values()) {
            if ((inst.getUserData() != null) && (inst.getUserData().platform == platform)) {
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
                .navigate(authorized ? "/dashboard" : "/signin");
        }

        this.isAuthorized = authorized;
    }

    @SuppressWarnings("deprecation")
    public void updateBridgeData() {
        this.checkAuth();

        // This is just a temp fix, the real one will come later with some architectural
        // improvements.
        JsonObject platforms = new JsonObject();
        this.authInstances.forEach((__, v) -> {
            if (v.getUserData() != null) {
                platforms.put(
                    v.getUserData().platform.name(),
                    new JsonObject()
                        .put("userData", Rson.DEFAULT.toJson(v.getUserData()))
                );
            }
        });
        CaffeinatedApp.getInstance().emitAppEvent(
            "auth:platforms",
            platforms
        );

        CaffeinatedApp.getInstance().getKoi().updateFromAuth();
    }

    private void startAuthInstance(String tokenId) {
        AuthInstance existing = this.authInstances.remove(tokenId);
        if (existing != null) {
            existing.close(); // We don't care enough to invalidate.
        }

        this.logger.debug("Starting AuthInstance with id: %s", tokenId);
        this.authInstances.put(tokenId, new AuthInstance(tokenId));
    }

    @JavascriptFunction
    public Map<String, String> getLanguages() {
        return KoiStreamLanguage.NAMES;
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public void requestOAuthSignin(@NonNull String type, @NonNull String platform, boolean shouldNavigateBackwards, @Nullable String tokenId) {
        try {
            final boolean isKoi = type.equalsIgnoreCase("koi");
            final String $tokenId_ptr = tokenId == null ? platform : tokenId;

            this.logger.info("Signin requested. (%s)", platform);

            if (this.currentAuthCallback != null) {
                this.cancelSignin();
            }
            this.currentAuthCallback = this.authorize(platform, isKoi);

            this.currentAuthCallback
                .connect()
                .then((token) -> {
                    this.currentAuthCallback = null;
                    if (token == null) return;

                    this.logger.info("Signin completed (%s)", platform);

                    CaffeinatedApp
                        .getInstance()
                        .getAuthPreferences()
                        .get()
                        .addToken(type, $tokenId_ptr, token);

                    CaffeinatedApp.getInstance().emitAppEvent(
                        "auth:completion",
                        new JsonObject()
                            .put("type", type)
                            .put("platform", platform)
                            .put("tokenId", $tokenId_ptr)
                    );

                    if (isKoi) {
                        this.startAuthInstance($tokenId_ptr);
                    }

                    if (shouldNavigateBackwards) {
                        // Navigate backwards for the signin screen.
                        CaffeinatedApp.getInstance().getUI().goBack();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptFunction
    public void signout(@NonNull String tokenId) {
        AuthInstance inst = this.authInstances.remove(tokenId);

        if (inst != null) {
            inst.invalidate();
        }
    }

    @JavascriptFunction
    public void cancelSignin() {
        this.logger.info("Signin cancelled (?)");
        if (this.currentAuthCallback != null) {
            this.currentAuthCallback.cancel();
            this.currentAuthCallback = null;
        }
    }

    @JavascriptFunction
    public String getPortalUrl(String platform, String state) throws IOException, IllegalStateException, IllegalArgumentException {
        String response = WebUtil.sendHttpRequest(
            new Request.Builder()
                .url(String.format("https://api.auth.casterlabs.co/v1/koi/platforms/%s/do-auth?state=%s&clientId=%s", platform, WebUtil.encodeURIComponent(state), CaffeinatedApp.KOI_ID))
        );

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        return json.getObject("data").getString("next");
    }

    @JavascriptFunction
    public void loginPortal(String platform, String koiToken, boolean shouldNavigateBackwards) throws IOException, IllegalStateException, IllegalArgumentException {
        final String tokenId = platform;

        CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addToken("koi", tokenId, koiToken);

        CaffeinatedApp.getInstance().track("AUTH__" + tokenId, true);
        this.startAuthInstance(tokenId);

        if (shouldNavigateBackwards) {
            // Navigate backwards for the signin screen.
            CaffeinatedApp.getInstance().getUI().goBack();
        }
    }

    private AuthCallback authorize(String type, boolean isKoi) throws IOException {
        String oauthLink = CaffeinatedApp.OVERRIDE_AUTH_URLS
            .getOrDefault(type, CaffeinatedApp.AUTH_URL);

        if (oauthLink == null) {
            throw new IllegalArgumentException("Type '" + type + "' does not have an oauth link associated with it.");
        }

        CaffeinatedApp.getInstance().track("AUTH__" + type, true);

        AuthCallback callback = new AuthCallback(type, isKoi);

        CaffeinatedApp
            .getInstance()
            .getUI()
            .openLink(
                oauthLink +
                    "?platform=" + type.toUpperCase() +
                    "&clientId=" + CaffeinatedApp.KOI_ID +
                    "&state=" + callback.getStateString()
            );

        return callback;
    }

    public @Nullable AuthInstance getAuthInstance(UserPlatform platform) {
        for (AuthInstance inst : this.authInstances.values()) {
            if ((inst.getUserData() != null) &&
                (inst.getUserData().platform == platform)) {
                return inst;
            }
        }

        return null;
    }

}
