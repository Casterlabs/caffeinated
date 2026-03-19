package co.casterlabs.caffeinated.localserver.handlers;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.plugins.CaffeinatedPluginsImpl;
import co.casterlabs.caffeinated.localserver.RequestError;
import co.casterlabs.caffeinated.localserver.RouteHelper;
import co.casterlabs.caffeinated.pluginsdk.CaffeinatedPlugin;
import co.casterlabs.caffeinated.pluginsdk.PluginResource;
import co.casterlabs.rhs.protocol.StandardHttpStatus;
import co.casterlabs.rhs.server.HttpResponse;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;

public class RoutePluginApi implements HttpProvider, RouteHelper {

    @HttpEndpoint(uri = "/api/plugin/:pluginId/resource/:resourceId")
    public HttpResponse onGetPluginResourceRequest(SoraHttpSession session) {
        try {
            if (authorize(session)) {
                String pluginId = session.getUriParameters().get("pluginId");
                String resourceId = session.getUriParameters().get("resourceId");

                CaffeinatedPlugin owningPlugin = CaffeinatedPluginsImpl.INSTANCE.getPluginById(pluginId);

                if (owningPlugin == null) {
                    return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.PLUGIN_NOT_FOUND);
                }

                @Nullable
                PluginResource response = owningPlugin.resolveResource(resourceId);

                if (response == null) {
                    return newErrorResponse(StandardHttpStatus.NOT_FOUND, RequestError.RESOURCE_NOT_FOUND);
                }

                return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, response.data)
                    .setMimeType(response.mimeType);

            } else {
                return newErrorResponse(StandardHttpStatus.UNAUTHORIZED, RequestError.UNAUTHORIZED);
            }
        } catch (Exception e) {
            return newErrorResponse(StandardHttpStatus.INTERNAL_ERROR, RequestError.INTERNAL_ERROR);
        }
    }

}
