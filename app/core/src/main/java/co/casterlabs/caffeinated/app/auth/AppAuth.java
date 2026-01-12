package co.casterlabs.caffeinated.app.auth;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import app.saucer.bridge.JavascriptFunction;
import app.saucer.bridge.JavascriptObject;
import app.saucer.bridge.JavascriptValue;
import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.app.NotificationType;
import co.casterlabs.caffeinated.app.config.AppConfig;
import co.casterlabs.caffeinated.app.koi.KoiImpl;
import co.casterlabs.caffeinated.app.sdk.CaffeinatedImpl;
import co.casterlabs.caffeinated.app.ui.AppUI;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.NonNull;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@JavascriptObject
public class AppAuth {
    private static FastLogger logger = new FastLogger();

    @JavascriptValue(allowSet = false)
    private static final UserPlatform[] AUTHENTICATABLE = {
            UserPlatform.TWITCH,
            UserPlatform.TROVO,
            UserPlatform.YOUTUBE,
            UserPlatform.DLIVE,
            UserPlatform.TIKTOK,
            UserPlatform.KICK,
            UserPlatform.LOCO,
    };

    @JavascriptValue(allowSet = false)
    private static final UserPlatform[] ALL = UserPlatform.values();

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static Map<String, AuthInstance> authInstances = new HashMap<>();

    private static AuthCallback currentAuthCallback;

    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static boolean isKoiAlive = true;

    @Getter
    @JavascriptValue(allowSet = false, watchForMutate = true)
    private static boolean isAuthorized = false;

    static synchronized void checkStatus() {
        boolean isAlive = false;

        for (AuthInstance inst : authInstances.values()) {
            if (inst.isWelcomed()) {
                isAlive = true;
                break;
            }
        }

        if (isKoiAlive != isAlive) {
            if (isAlive) {
                App.notify(
                    "co.casterlabs.caffeinated.app.auth.reconnected",
                    Collections.emptyMap(),
                    NotificationType.INFO
                );
            } else {
                // Show an error to the user.
                App.notify(
                    "co.casterlabs.caffeinated.app.auth.lost_connection",
                    Collections.emptyMap(),
                    NotificationType.ERROR
                );
            }
        }

        isKoiAlive = isAlive;
    }

    public static boolean isSignedIn() {
        return !authInstances.isEmpty();
    }

    public static void init() {
        for (String tokenId : AppConfig.authPreferences.get().getAllTokenIdsByType("koi")) {
            startAuthInstance(tokenId);
        }
    }

    public static void shutdown() {
        cancelSignin();

        for (AuthInstance inst : authInstances.values()) {
            inst.close();
        }
    }

    public static int countPlatform(UserPlatform platform) {
        int count = 0;
        for (AuthInstance inst : authInstances.values()) {
            if ((inst.getUserData() != null) && (inst.getUserData().platform == platform)) {
                count++;
            }
        }
        return count;
    }

    public static void checkAuth() {
        boolean authorized = false;

        for (AuthInstance inst : authInstances.values()) {
            if (inst.isConnected()) {
                authorized = true;
                break;
            }
        }

        if (isAuthorized != authorized) {
            AppUI.navigate(authorized ? "/dashboard" : "/signin");
        }

        isAuthorized = authorized;
    }

    @SuppressWarnings("deprecation")
    public static void updateBridgeData() {
        checkAuth();

        // This is just a temp fix, the real one will come later with some architectural
        // improvements.
        JsonObject platforms = new JsonObject();
        authInstances.forEach((__, v) -> {
            if (v.getUserData() != null) {
                platforms.put(
                    v.getUserData().platform.name(),
                    new JsonObject()
                        .put("userData", Rson.DEFAULT.toJson(v.getUserData()))
                );
            }
        });
        App.emitAppEvent(
            "auth:platforms",
            platforms
        );

        KoiImpl.INSTANCE.updateFromAuth();
    }

    private static void startAuthInstance(String tokenId) {
        AuthInstance existing = authInstances.remove(tokenId);
        if (existing != null) {
            existing.close(); // We don't care enough to invalidate.
        }

        logger.debug("Starting AuthInstance with id: %s", tokenId);
        authInstances.put(tokenId, new AuthInstance(tokenId));
    }

    @SuppressWarnings("deprecation")
    @JavascriptFunction
    public static void requestOAuthSignin(@NonNull String type, @NonNull String platform, boolean shouldNavigateBackwards, @Nullable String tokenId) {
        try {
            final boolean isKoi = type.equalsIgnoreCase("koi");
            final String $tokenId_ptr = tokenId == null ? platform : tokenId;

            logger.info("Signin requested. (%s)", platform);

            if (currentAuthCallback != null) {
                cancelSignin();
            }
            currentAuthCallback = authorize(platform, isKoi);

            currentAuthCallback
                .connect()
                .then((token) -> {
                    currentAuthCallback = null;
                    if (token == null) return;

                    logger.info("Signin completed (%s)", platform);

                    AppConfig.authPreferences
                        .get()
                        .addToken(type, $tokenId_ptr, token);

                    App.emitAppEvent(
                        "auth:completion",
                        new JsonObject()
                            .put("type", type)
                            .put("platform", platform)
                            .put("tokenId", $tokenId_ptr)
                    );

                    if (isKoi) {
                        startAuthInstance($tokenId_ptr);
                    }

                    if (shouldNavigateBackwards) {
                        // Navigate backwards for the signin screen.
                        AppUI.goBack();
                    }
                });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @JavascriptFunction
    public static void signout(@NonNull String tokenId) {
        AuthInstance inst = authInstances.remove(tokenId);

        if (inst != null) {
            inst.invalidate();
        }
    }

    @JavascriptFunction
    public static void cancelSignin() {
        logger.info("Signin cancelled (?)");
        if (currentAuthCallback != null) {
            currentAuthCallback.cancel();
            currentAuthCallback = null;
        }
    }

    @JavascriptFunction
    public static String getPortalUrl(String platform, String state) throws IOException, IllegalStateException, IllegalArgumentException {
        String response = WebUtil.sendHttpRequest(
            new Request.Builder()
                .url(String.format("https://api.auth.casterlabs.co/v1/koi/platforms/%s/do-auth?state=%s&clientId=%s", platform, WebUtil.encodeURIComponent(state), App.KOI_ID))
        );

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        return json.getObject("data").getString("next");
    }

    @JavascriptFunction
    public static void loginPortal(String platform, String koiToken, boolean shouldNavigateBackwards) throws IOException, IllegalStateException, IllegalArgumentException {
        final String tokenId = platform;

        AppConfig.authPreferences
            .get()
            .addToken("koi", tokenId, koiToken);

        startAuthInstance(tokenId);

        if (shouldNavigateBackwards) {
            // Navigate backwards for the signin screen.
            AppUI.goBack();
        }
    }

    private static AuthCallback authorize(String type, boolean isKoi) throws IOException {
        String oauthLink = App.OVERRIDE_AUTH_URLS
            .getOrDefault(type, App.AUTH_URL);

        if (oauthLink == null) {
            throw new IllegalArgumentException("Type '" + type + "' does not have an oauth link associated with it.");
        }

        AuthCallback callback = new AuthCallback(type, isKoi);

        CaffeinatedImpl.INSTANCE.openLink(
            oauthLink +
                "?platform=" + type.toUpperCase() +
                "&clientId=" + App.KOI_ID +
                "&state=" + callback.getStateString()
        );

        return callback;
    }

    public static @Nullable AuthInstance getAuthInstance(UserPlatform platform) {
        for (AuthInstance inst : authInstances.values()) {
            if ((inst.getUserData() != null) &&
                (inst.getUserData().platform == platform)) {
                return inst;
            }
        }

        return null;
    }

}
