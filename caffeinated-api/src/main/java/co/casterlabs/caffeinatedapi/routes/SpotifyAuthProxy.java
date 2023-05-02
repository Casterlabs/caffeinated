package co.casterlabs.caffeinatedapi.routes;

import co.casterlabs.caffeinatedapi.Config;
import co.casterlabs.caffeinatedapi.HttpUtil;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.AllArgsConstructor;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@AllArgsConstructor
public class SpotifyAuthProxy implements HttpProvider {
    private static final String INTERNAL_ERROR = "{\"data\":null,\"errors\":[\"INTERNAL_ERROR\"]}";
    private static final String CLIENT_ERROR = "{\"data\":null,\"errors\":[\"CLIENT_ERROR\"]}";

    private Config config;

    @HttpEndpoint(uri = "/public/v2/caffeinated/spotify")
    public HttpResponse onValidate(HttpSession session) {
        try {
            String refreshToken = session.getQueryParameters().get("refresh_token");
            String oauthCode = session.getQueryParameters().get("code");

            FormBody.Builder body = new FormBody.Builder()
                .addEncoded("client_id", this.config.getSpotifyClientId())
                .addEncoded("client_secret", this.config.getSpotifySecret())
                .addEncoded("redirect_uri", this.config.getSpotifyRedirectUri());

            if (oauthCode != null) {
                body.addEncoded("grant_type", "authorization_code")
                    .addEncoded("code", oauthCode);
            } else if (refreshToken != null) {
                body.addEncoded("grant_type", "refresh_token")
                    .addEncoded("refresh_token", refreshToken);
            } else {
                return HttpResponse
                    .newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST, CLIENT_ERROR)
                    .setMimeType("application/json");
            }

            Request request = new Request.Builder()
                .url("https://accounts.spotify.com/api/token")
                .post(body.build())
                .build();

            try (Response response = HttpUtil.client.newCall(request).execute()) {
                byte[] bytes = response.body().bytes();
                HttpStatus status = StandardHttpStatus.lookup(response.code());

                return HttpResponse
                    .newFixedLengthResponse(status, bytes)
                    .setMimeType(response.header("Content-Type"));
            }
        } catch (Exception e) {
            FastLogger.logStatic(LogLevel.DEBUG, e);
            return HttpResponse
                .newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST, INTERNAL_ERROR)
                .setMimeType("application/json");
        }
    }

}
