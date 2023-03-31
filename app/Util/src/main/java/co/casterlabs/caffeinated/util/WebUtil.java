package co.casterlabs.caffeinated.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import co.casterlabs.commons.functional.tuples.Pair;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebUtil {
    public static final OkHttpClient client = new OkHttpClient.Builder()
        .addNetworkInterceptor(
            (chain) -> chain.proceed(
                chain // OkHttp bug.
                    .request()
                    .newBuilder()
                    .removeHeader("Accept-Encoding")
                    .build()
            )
        )
        .build();

    public static String sendHttpRequest(@NonNull Request.Builder builder) throws IOException {
        return new String(sendHttpRequestBytes(builder), StandardCharsets.UTF_8);
    }

    public static byte[] sendHttpRequestBytes(@NonNull Request.Builder builder) throws IOException {
        return sendHttpRequestBytesWithMime(builder).a();
    }

    public static Pair<byte[], String> sendHttpRequestBytesWithMime(@NonNull Request.Builder builder) throws IOException {
        try (Response response = client.newCall(builder.build()).execute()) {
            byte[] content = response.body().bytes();
            String mimeType = response.header("Content-Type", "application/octet-stream");

            return new Pair<>(content, mimeType);
        }
    }

    public static String escapeHtml(@NonNull String str) {
        return str
            .codePoints()
            .mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ? "&#" + c + ";" : new String(Character.toChars(c)))
            .collect(Collectors.joining());
    }

    @SneakyThrows
    public static String decodeURIComponent(@NonNull String s) {
        return URLDecoder.decode(s, "UTF-8");
    }

    @SneakyThrows
    public static String encodeURIComponent(@NonNull String s) {
        return URLEncoder.encode(s, "UTF-8")
            .replaceAll("\\+", "%20")
            .replaceAll("\\%21", "!")
            .replaceAll("\\%27", "'")
            .replaceAll("\\%28", "(")
            .replaceAll("\\%29", ")")
            .replaceAll("\\%7E", "~");
    }

}
