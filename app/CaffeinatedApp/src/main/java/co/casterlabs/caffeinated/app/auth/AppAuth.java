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
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.PreferenceFile;
import co.casterlabs.caffeinated.pluginsdk.CasterlabsAccount;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.commons.async.AsyncTask;
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

        String token = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("casterlabs", "casterlabs");
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

        prefs.get().removeToken("casterlabs", "casterlabs");
        prefs.save();
    }

    @JavascriptFunction
    public void loginCasterlabs(final String token) {
        if (this.casterlabsAccount == null) {
            PreferenceFile<AuthPreferences> prefs = CaffeinatedApp.getInstance().getAuthPreferences();

            prefs.get().addToken("casterlabs", "casterlabs", token);
            prefs.save();

            this.clRefreshTask = AsyncTask.create(() -> {
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
                            AsyncTask.create(this::logoutCasterlabs);
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
                CaffeinatedApp.getInstance().notify("Reconnected to Casterlabs successfully.", NotificationType.INFO);
            } else {
                // Show an error to the user.
                CaffeinatedApp.getInstance().notify("Lost connection to Casterlabs, reconnecting.", NotificationType.ERROR);
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

        String casterlabsToken = CaffeinatedApp.getInstance().getAuthPreferences().get().getToken("casterlabs", "casterlabs");
        if (casterlabsToken != null) {
            this.loginCasterlabs(casterlabsToken);
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
                    v.getUserData().getPlatform().name(),
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
    public Map<KoiStreamLanguage, String> getLanguages() {
        return KoiStreamLanguage.LANG;
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public void requestOAuthSignin(@NonNull String type, @NonNull String platform, boolean shouldNavigateBackwards) {
        try {
            final boolean isKoi = type.equalsIgnoreCase("koi");

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
                        .addToken(type, platform, token);

                    CaffeinatedApp.getInstance().emitAppEvent(
                        "auth:completion",
                        new JsonObject()
                            .put("type", type)
                            .put("platform", platform)
                            .put("tokenId", platform)
                    );

                    if (isKoi) {
                        this.startAuthInstance(platform);
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

    /**
     * @implSpec IllegalStateException means MFA prompt.
     */
    @JavascriptFunction
    public void loginCaffeine(@NonNull String username, @NonNull String password, @Nullable String mfa, boolean shouldNavigateBackwards) throws IOException, IllegalStateException, IllegalArgumentException {
        String koiToken = CaffeineHelper.login(username, password, mfa);
        final String tokenId = "caffeine";

        CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addToken("koi", tokenId, koiToken);

        CaffeinatedApp.getInstance().getAnalytics().track("AUTH__" + tokenId, true);
        this.startAuthInstance(tokenId);

        if (shouldNavigateBackwards) {
            // Navigate backwards for the signin screen.
            CaffeinatedApp.getInstance().getUI().goBack();
        }
    }

    @JavascriptFunction
    public void loginKick(@NonNull String username, @NonNull String password, @Nullable String mfa, boolean shouldNavigateBackwards) throws IOException, IllegalStateException, IllegalArgumentException {
        String koiToken = KickHelper.login(username, password, mfa);
        final String tokenId = "kick";

        CaffeinatedApp
            .getInstance()
            .getAuthPreferences()
            .get()
            .addToken("koi", tokenId, koiToken);

        CaffeinatedApp.getInstance().getAnalytics().track("AUTH__" + tokenId, true);
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

        CaffeinatedApp.getInstance().getAnalytics().track("AUTH__" + type, true);

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
