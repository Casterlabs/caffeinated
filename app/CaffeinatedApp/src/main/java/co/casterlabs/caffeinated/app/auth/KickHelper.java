package co.casterlabs.caffeinated.app.auth;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

class KickHelper {

    private static String generateState() throws IOException {
        String response = WebUtil.sendHttpRequest(
            new Request.Builder()
                .url(
                    "https://api.auth.casterlabs.co/v1/koi/platforms/KICK/do-auth"
                        + "?clientId=" + CaffeinatedApp.KOI_ID
                        + "&state=X"
                )
        );

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        String next = json.getObject("data").getString("next");
        return next.substring(next.indexOf('=') + 1);
    }

    /**
     * @implSpec IllegalStateException means MFA prompt.
     */
    static String login(@NonNull String email, @NonNull String password, @Nullable String mfa) throws IOException, IllegalStateException, IllegalArgumentException {
        String state = generateState();

        String response = WebUtil.sendHttpRequest(
            new Request.Builder()
                .url(
                    "https://api.auth.casterlabs.co/v1/koi/exchange?state=" + WebUtil.encodeURIComponent(state)
                )
                .post(
                    RequestBody.create(
                        new JsonObject()
                            .put("email", email)
                            .put("password", password)
                            .put("otp", mfa)
                            .toString(true),
                        MediaType.parse("application/json")
                    )
                )
        );

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        if (json.get("data").isJsonNull()) {
            if (json
                .getArray("errors")
                .contains(new JsonString("KICK_2FA_PROMPT"))) {
                FastLogger.logStatic(LogLevel.INFO, "Kick 2FA Prompt.");
                throw new IllegalStateException("MFA");
            } else {
                FastLogger.logStatic(LogLevel.SEVERE, "Error whilst logging in: %s", json);
                throw new IllegalArgumentException();
            }
        }

        return json.getObject("data").getString("token");
    }

}
