package co.casterlabs.autodeploy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import co.casterlabs.rakurai.io.IOUtil;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpUtil {
    private static final OkHttpClient client = new OkHttpClient();

    static {
        new File("tmp").mkdir();
    }

    public static JsonObject sendHttpRequestJson(@NonNull Request.Builder builder) throws IOException {
        String body = sendHttpRequest(builder);

        return Rson.DEFAULT.fromJson(body, JsonObject.class);
    }

    public static String sendHttpRequest(@NonNull Request.Builder builder) throws IOException {
        try (Response response = client.newCall(builder.build()).execute()) {
            return response.body().string();
        }
    }

    public static File sendHttpRequestBytes(@NonNull Request.Builder builder) throws IOException {
        try (Response response = client.newCall(builder.build()).execute()) {
            File tempFile = new File("tmp/" + UUID.randomUUID().toString());

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                try (InputStream in = response.body().byteStream()) {
                    IOUtil.writeInputStreamToOutputStream(in, fos);
                }
            }

            return tempFile;
        }
    }

}
