package co.casterlabs.caffeinated.localserver.handlers;

import co.casterlabs.caffeinated.app.App;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.rhs.HttpMethod;
import co.casterlabs.rhs.HttpStatus.StandardHttpStatus;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointData;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointProvider;
import co.casterlabs.rhs.protocol.api.endpoints.HttpEndpoint;
import co.casterlabs.rhs.protocol.http.HttpResponse;
import co.casterlabs.rhs.protocol.http.HttpSession;

public class RouteLocalServer implements EndpointProvider {

    @HttpEndpoint(path = "/", allowedMethods = {
            HttpMethod.GET
    })
    public HttpResponse onIndexRequest(HttpSession session, EndpointData<Void> data) {
        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.TEMPORARY_REDIRECT)
            .header("Location", "https://docs.casterlabs.co/caffeinated/sdk/");
    }

    @HttpEndpoint(path = "/api/test/:specialCode", allowedMethods = {
            HttpMethod.GET
    })
    public HttpResponse onWidgetRealtimeConnectionTest(HttpSession session, EndpointData<Void> data) {
        if (!App.isReady()) {
            // The app hasn't finished bootstrapping yet (plugins/widgets not
            // registered). Answer with a body that won't match the loader's
            // specialCode so it treats this as a failed probe and retries,
            // rather than redirecting to plugin endpoints that would 404
            // (PLUGIN_NOT_FOUND).
            return RouteHelper.addCors(
                HttpResponse
                    .newFixedLengthResponse(StandardHttpStatus.SERVICE_UNAVAILABLE, "NOT_READY")
                    .mime("text/plain")
                    .header("Retry-After", "3")
            );
        }

        return RouteHelper.addCors(
            HttpResponse
                .newFixedLengthResponse(StandardHttpStatus.OK, data.uriParameters().get("specialCode"))
                .mime("text/plain")
        );
    }

    @HttpEndpoint(path = ".*", allowedMethods = {
            HttpMethod.OPTIONS
    })
    public HttpResponse onOptions(HttpSession session, EndpointData<Void> data) {
        return RouteHelper.addCors(
            HttpResponse
                .newFixedLengthResponse(StandardHttpStatus.NO_CONTENT, "")
                .mime("text/plain")
        );
    }

}
