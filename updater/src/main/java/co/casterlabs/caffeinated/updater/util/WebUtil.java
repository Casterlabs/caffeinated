package co.casterlabs.caffeinated.updater.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WebUtil {
    private static final OkHttpClient client = new OkHttpClient();

    public static Response sendRawHttpRequest(@NonNull Request.Builder builder) throws IOException {
        Response response = client.newCall(builder.build()).execute();

        return response;
    }

    public static String sendHttpRequest(@NonNull Request.Builder builder) throws IOException {
        try (Response response = sendRawHttpRequest(builder)) {
            return response.body().string();
        }
    }

    public static byte[] sendHttpRequestBytes(@NonNull Request.Builder builder) throws IOException {
        try (Response response = sendRawHttpRequest(builder)) {
            return response.body().bytes();
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
