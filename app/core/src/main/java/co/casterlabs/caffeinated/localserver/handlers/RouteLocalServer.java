package co.casterlabs.caffeinated.localserver.handlers;

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
