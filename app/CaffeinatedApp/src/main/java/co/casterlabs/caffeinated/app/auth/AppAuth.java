package co.casterlabs.caffeinated.app.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppAuth extends JavascriptObject {
    private FastLogger logger = new FastLogger();

    private @Getter Map<String, AuthInstance> authInstances = new HashMap<>();
    private AuthCallback currentAuthCallback;

    @Getter
    @JavascriptValue
    private boolean isAuthorized = false;

    public boolean isSignedIn() {
        return !this.authInstances.isEmpty();
    }

    public void init() {
        List<String> ids = CaffeinatedApp.getInstance().getAuthPreferences().get().getKoiTokenIds();

        for (String tokenId : ids) {
            this.startAuthInstance(tokenId);
        }
    }

    public void shutdown() {
        this.cancelSignin();

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
        CaffeinatedApp.getInstance().getKoi().updateFromAuth();
    }

    private void startAuthInstance(String tokenId) {
        this.logger.debug("Starting AuthInstance with id: %s", tokenId);
        this.authInstances.put(tokenId, new AuthInstance(tokenId));
    }

    @JavascriptFunction
    public void requestOAuthSignin(@NonNull String platform, boolean isKoi, boolean shouldNavigateBackwards) {
        try {
            this.logger.info("Signin requested. (%s)", platform);

            if (this.currentAuthCallback != null) {
                this.cancelSignin();
            }

            this.currentAuthCallback = this.authorize(platform);

            this.currentAuthCallback
                .connect()
                .then((token) -> {
                    this.currentAuthCallback = null;

                    if (token != null) {
                        this.logger.info("Signin completed (%s)", platform);

                        if (isKoi) {
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
                                .addToken(platform, token);

                            CaffeinatedApp.getInstance().emitAppEvent(
                                "auth:completion",
                                new JsonObject()
                                    .put("platform", platform)
                                    .put("tokenId", platform)
                            );
                        }

                        if (shouldNavigateBackwards) {
                            // Navigate backwards for the signin screen.
                            CaffeinatedApp.getInstance().getUI().goBack();
                        }
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptFunction
    public void caffeineSignin(@NonNull String caffeineToken) {
        String tokenId = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addKoiToken(caffeineToken);

        this.startAuthInstance(tokenId);
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
