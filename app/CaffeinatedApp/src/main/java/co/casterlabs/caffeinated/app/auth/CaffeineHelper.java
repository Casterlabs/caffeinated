package co.casterlabs.caffeinated.app.auth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class CaffeineHelper {
    private static OkHttpClient client = new OkHttpClient();

    /**
     * @implSpec IllegalStateException means MFA prompt.
     */
    static String login(@NonNull String username, @NonNull String password, @Nullable String mfa) throws IOException, IllegalStateException, IllegalArgumentException {
        JsonObject payload = new JsonObject()
            .put(
                "account",
                new JsonObject()
                    .put("username", username)
                    .put("password", password)
            );

        if ((mfa != null) && !mfa.isEmpty()) {
            payload.put(
                "mfa",
                JsonObject.singleton("otp", mfa)
            );
        }

        FastLogger.logStatic(payload);

        String caffeineToken = sendAuth(payload);
        String koiToken = exchangeToken(caffeineToken);

        return koiToken;
    }

    private static String exchangeToken(String caffeineToken) throws IOException {
        try (Response response = sendHttpGet(
            "https://api.casterlabs.co/v2/natsukashii/create?platform=CAFFEINE&token=" + caffeineToken
        )) {
            JsonObject json = Rson.DEFAULT.fromJson(response.body().string(), JsonObject.class);

            return json
                .getObject("data")
                .getString("token");
        }
    }

    private static String sendAuth(JsonObject payload) throws IOException, IllegalStateException, IllegalArgumentException {
        try (Response authResponse = sendHttp(payload.toString(), "https://api.caffeine.tv/v1/account/signin", "application/json")) {
            String body = authResponse.body().string();

            if (authResponse.code() == 401) {
                throw new IllegalStateException(body);
            } else {
                JsonObject json = Rson.DEFAULT.fromJson(body, JsonObject.class);

                FastLogger.logStatic(json);

                if (json.containsKey("next")) {
                    throw new IllegalStateException("MFA");
                } else if (json.containsKey("credentials")) {
                    JsonObject credentials = json.getObject("credentials");

                    return credentials.get("refresh_token").getAsString();
                } else {
                    throw new IllegalArgumentException(body);
                }
            }
        }
    }

    private static Response sendHttpGet(@NonNull String address) throws IOException {
        return sendHttp(null, null, address, null);
    }

    private static Response sendHttp(@NonNull String body, @NonNull String address, @Nullable String mime) throws IOException {
        return sendHttp(RequestBody.create(body.getBytes(StandardCharsets.UTF_8)), "POST", address, mime);
    }

    private static Response sendHttp(@Nullable RequestBody body, @Nullable String type, @NonNull String address, @Nullable String mime) throws IOException {
        Request.Builder builder = new Request.Builder().url(address);

        if ((body != null) && (type != null)) {
            switch (type.toUpperCase()) {
                case "POST": {
                    builder.post(body);
                    break;
                }

                case "PUT": {
                    builder.put(body);
                    break;
                }

                case "PATCH": {
                    builder.patch(body);
                    break;
                }
            }
        }

        if (mime != null) {
            builder.addHeader("Content-Type", mime);
        }

        builder.addHeader("x-client-type", "web");

        Request request = builder.build();
        Response response = client.newCall(request).execute();

        return response;
    }

}
