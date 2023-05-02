package co.casterlabs.caffeinatedapi;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.Nullable;

import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    public static OkHttpClient client = new OkHttpClient();

    public static Response sendHttpGet(@NonNull String address) throws IOException {
        return sendHttp(null, null, address, null);
    }

    public static Response sendHttp(@NonNull String body, @NonNull String address, @Nullable String mime) throws IOException {
        return sendHttp(RequestBody.create(body.getBytes(StandardCharsets.UTF_8)), "POST", address, mime);
    }

    public static Response sendHttp(@Nullable RequestBody body, @Nullable String type, @NonNull String address, @Nullable String mime) throws IOException {
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

        Request request = builder.build();
        Response response = client.newCall(request).execute();

        return response;
    }

}
