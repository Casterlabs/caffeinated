package co.casterlabs.caffeinatedapi.routes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import co.casterlabs.caffeinatedapi.Config;
import co.casterlabs.caffeinatedapi.HttpUtil;
import co.casterlabs.rakurai.io.http.HttpStatus;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.rakurai.io.http.server.HttpSession;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.AllArgsConstructor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

@AllArgsConstructor
public class StreamlabsAuthProxy implements HttpProvider {
    private static final String INTERNAL_ERROR = "{\"data\":null,\"errors\":[\"INTERNAL_ERROR\"]}";
    private static final String CLIENT_ERROR = "{\"data\":null,\"errors\":[\"CLIENT_ERROR\"]}";

    private Config config;

    @HttpEndpoint(uri = "/public/v2/caffeinated/streamlabs/auth/redirect")
    public HttpResponse onRedirect(HttpSession session) {
        String state = session.getQueryParameters().get("state");

        final String[] scopes = {
                "alerts.create",
                "alerts.write",
        };

        @SuppressWarnings("deprecation")
        String url = String.format(
            "https://streamlabs.com/api/v2.0/authorize" +
                "?client_id=%s" +
                "&redirect_uri=%s" +
                "&response_type=code" +
                "&scope=%s" +
                "&state=%s",
            URLEncoder.encode(this.config.getStreamlabsClientId()),
            URLEncoder.encode(this.config.getStreamlabsRedirectUri()),
            String.join("%20", scopes),
            state
        );

        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.TEMPORARY_REDIRECT)
            .putHeader("Location", url);

    }

    @HttpEndpoint(uri = "/public/v2/caffeinated/streamlabs/auth/code")
    public HttpResponse onComplete(HttpSession session) {
        try {
            String code = session.getQueryParameters().get("code");

            if (code == null) {
                return HttpResponse
                    .newFixedLengthResponse(StandardHttpStatus.BAD_REQUEST, CLIENT_ERROR)
                    .setMimeType("application/json");
            }

            JsonObject body = new JsonObject()
                .put("client_id", this.config.getStreamlabsClientId())
                .put("client_secret", this.config.getStreamlabsSecret())
                .put("redirect_uri", this.config.getStreamlabsRedirectUri())
                .put("grant_type", "authorization_code")
                .put("code", code);

            Request request = new Request.Builder()
                .url("https://streamlabs.com/api/v2.0/token")
                .post(RequestBody.create(body.toString().getBytes(StandardCharsets.UTF_8), MediaType.parse("application/json")))
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
