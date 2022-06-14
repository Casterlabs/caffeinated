package co.casterlabs.caffeinated.app.auth;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.app.ui.UIBackgroundColor;
import co.casterlabs.caffeinated.pluginsdk.CasterlabsAccount;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.koi.api.stream.KoiStreamLanguage;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.TypeToken;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request;
import okhttp3.RequestBody;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

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

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private CasterlabsAccount casterlabsAccount;

    private AsyncTask clRefreshTask;

    @JavascriptFunction
    public byte[] sendAuthorizedApiRequestBytes(String endpoint, @Nullable JsonObject body) throws IOException {
        Request.Builder request = new Request.Builder();

        request.url("https://api.casterlabs.co" + endpoint);

        if (body != null) {
            request
                .post(
                    RequestBody.create(body.toString().getBytes())
                )
                .header("Content-Type", "application/json");
        }

        String token = CaffeinatedApp.getInstance().getAuthPreferences().get().getCasterlabsAuthToken();

        if (token != null) {
            request.header("Authorization", "Bearer " + token);
        }

        return WebUtil.sendHttpRequestBytes(request);
    }

    @JavascriptFunction
    public JsonObject sendAuthorizedApiRequest(String endpoint, @Nullable JsonObject body) throws IOException, CasterlabsApiException {
        JsonObject response = Rson.DEFAULT.fromJson(
            new String(this.sendAuthorizedApiRequestBytes(endpoint, body)),
            JsonObject.class
        );

        if (response.containsKey("__note")) {
            FastLogger.logStatic(LogLevel.INFO, "Api note (%s): %s", endpoint, response.getString("__note"));
        }

        if (response.get("data").isJsonNull()) {
            List<String> errors = Rson.DEFAULT.fromJson(response.get("errors"), new TypeToken<List<String>>() {
            });

            if (errors.contains("UNAUTHORIZED")) {
                this.logoutCasterlabs();
            }

            throw new CasterlabsApiException(errors);
        } else {
            return response.getObject("data");
        }
    }

    @JavascriptFunction
    public void logoutCasterlabs() {
        if (this.clRefreshTask != null) {
            this.clRefreshTask.cancel();
            this.clRefreshTask = null;
        }

        this.casterlabsAccount = null;

        PreferenceFile<AuthPreferences> prefs = CaffeinatedApp.getInstance().getAuthPreferences();

        prefs.get().setCasterlabsAuthToken(null);
        prefs.save();
    }

    @JavascriptFunction
    public void loginCasterlabs(final String token) {
        if (this.casterlabsAccount == null) {
            PreferenceFile<AuthPreferences> prefs = CaffeinatedApp.getInstance().getAuthPreferences();

            prefs.get().setCasterlabsAuthToken(token);
            prefs.save();

            this.clRefreshTask = new AsyncTask(() -> {
                while (true) {
                    try {
                        JsonObject response = this.sendAuthorizedApiRequest("/v3/account", null);

                        this.casterlabsAccount = Rson.DEFAULT.fromJson(response, CasterlabsAccount.class);

                        // TODO
                        try {
                            TimeUnit.SECONDS.sleep(60);
                        } catch (InterruptedException ignored) {}

                        continue;
                    } catch (CasterlabsApiException e) {
                        if (e.getErrors().contains("UNAUTHORIZED")) {
                            new AsyncTask(this::logoutCasterlabs);
                        }
                        // Fall through
                    } catch (IOException e) {
                        // Fall through
                    }

                    // An error occurred, try again quickly.
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException ignored) {}
                }
            });
        }
    }

    void checkStatus() {
        boolean isAlive = false;

        if (this.authInstances.isEmpty()) {
            isAlive = true; // Sure.
            return;
        } else {
            for (AuthInstance inst : this.authInstances.values()) {
                if (inst.isConnected()) {
                    isAlive = true;
                    return;
                }
            }
        }

        if (this.isKoiAlive != isAlive) {
            if (!isAlive) {
                // Show an error to the user.
                CaffeinatedApp.getInstance().getUI().showToast("Lost connection to Casterlabs, reconnecting.", UIBackgroundColor.DANGER);
            }
        }

        this.isKoiAlive = true;
    }

    public boolean isSignedIn() {
        return !this.authInstances.isEmpty();
    }

    public void init() {
        List<String> ids = CaffeinatedApp.getInstance().getAuthPreferences().get().getKoiTokenIds();

        for (String tokenId : ids) {
            this.startAuthInstance(tokenId);
        }

        String token = CaffeinatedApp.getInstance().getAuthPreferences().get().getCasterlabsAuthToken();

        if (token != null) {
            this.loginCasterlabs(token);
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
    public Map<KoiStreamLanguage, String> getLanguages() {
        return KoiStreamLanguage.LANG;
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public void requestOAuthSignin(@NonNull String platform, boolean isKoi, boolean shouldNavigateBackwards) {
        try {
            this.logger.info("Signin requested. (%s)", platform);

            if (this.currentAuthCallback != null) {
                this.cancelSignin();
            }

            this.currentAuthCallback = this.authorize(platform, isKoi);

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

    /**
     * @implSpec IllegalStateException means MFA prompt.
     */
    @JavascriptFunction
    public void loginCaffeine(@NonNull String username, @NonNull String password, @Nullable String mfa, boolean shouldNavigateBackwards) throws IOException, IllegalStateException, IllegalArgumentException {
        String koiToken = CaffeineHelper.login(username, password, mfa);

        String tokenId = CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addKoiToken(koiToken);

        this.startAuthInstance(tokenId);

        if (shouldNavigateBackwards) {
            // Navigate backwards for the signin screen.
            CaffeinatedApp.getInstance().getUI().goBack();
        }
    }

    private AuthCallback authorize(String type, boolean isKoi) throws IOException {
        String oauthLink = CaffeinatedApp.AUTH_URLS.getString(type);

        if (oauthLink == null) {
            throw new IllegalArgumentException("Type '" + type + "' does not have an oauth link associated with it.");
        } else {
            AuthCallback callback = new AuthCallback(type, isKoi);

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
