package co.casterlabs.caffeinated.localserver.handlers;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.plugins.CaffeinatedPluginsImpl;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginResource;
import co.casterlabs.rhs.HttpMethod;
import co.casterlabs.rhs.HttpStatus.StandardHttpStatus;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointData;
import co.casterlabs.rhs.protocol.api.endpoints.EndpointProvider;
import co.casterlabs.rhs.protocol.api.endpoints.HttpEndpoint;
import co.casterlabs.rhs.protocol.http.HttpResponse;
import co.casterlabs.rhs.protocol.http.HttpSession;

public class RoutePluginApi implements EndpointProvider {

    @HttpEndpoint(path = "/api/plugin/:pluginId/resource/:resourceId", allowedMethods = {
            HttpMethod.GET
    })
    public HttpResponse onGetPluginResourceRequest(HttpSession session, EndpointData<Void> data) {
        try {
            if (!RouteHelper.authorize(session)) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }

            String pluginId = data.uriParameters().get("pluginId");
            String resourceId = data.uriParameters().get("resourceId");

            CaffeinatedPlugin owningPlugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);

            if (owningPlugin == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
            }

            @Nullable
            PluginResource response = owningPlugin.resolveResource(resourceId);

            if (response == null) {
                return RouteHelper.newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
            }

            return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, response.data)
                .mime(response.mimeType);
        } catch (Exception e) {
            return RouteHelper.newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
