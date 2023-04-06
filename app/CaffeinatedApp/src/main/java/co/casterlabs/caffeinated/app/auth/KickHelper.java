package co.casterlabs.caffeinated.app.auth;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.rakurai.json.element.JsonString;
import lombok.NonNull;
import okhttp3.Request;

public class KickHelper {

    /**
     * @implSpec IllegalStateException means MFA prompt.
     */
    static String login(@NonNull String username, @NonNull String password, @Nullable String mfa) throws IOException, IllegalStateException, IllegalArgumentException {
        String response = WebUtil.sendHttpRequest(
            new Request.Builder()
                .url(
                    "https://api.casterlabs.co/v2/natsukashii/create"
                        + "?platform=KICK"
                        + "&token=X"
                        + "&email=" + WebUtil.encodeURIComponent(username)
                        + "&password=" + WebUtil.encodeURIComponent(password)
                        + "&otp=" + (mfa == null ? "null" : WebUtil.encodeURIComponent(mfa))
                )
        );

        JsonObject json = Rson.DEFAULT.fromJson(response, JsonObject.class);

        if (json
            .getArray("errors")
            .contains(new JsonString("KICK_2FA_PROMPT"))) {
            throw new IllegalStateException("MFA");
        } else if (json.get("data").isJsonNull()) {
            throw new IllegalArgumentException();
        }

        return json
            .getObject("data")
            .getString("token");
    }

}
