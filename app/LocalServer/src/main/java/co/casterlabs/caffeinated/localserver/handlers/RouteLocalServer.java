package co.casterlabs.caffeinated.localserver.handlers;

import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

public class RouteLocalServer implements HttpProvider, RouteHelper {

    @HttpEndpoint(uri = "/")
    public HttpResponse onIndexRequest(SoraHttpSession session) {
        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.TEMPORARY_REDIRECT)
            .putHeader("Location", "https://casterlabs.github.io/caffeinated-sdk/");
    }

    @HttpEndpoint(uri = "/api/test/:specialCode")
    public HttpResponse onWidgetRealtimeConnectionTest(SoraHttpSession session) {
        return HttpResponse
            .newFixedLengthResponse(StandardHttpStatus.OK, session.getUriParameters().get("specialCode"))
            .setMimeType("test/plain")
            .putHeader("Access-Control-Allow-Origin", "https://widgets.casterlabs.co");
    }

}
