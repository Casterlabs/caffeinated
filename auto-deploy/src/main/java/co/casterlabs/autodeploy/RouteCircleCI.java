package co.casterlabs.autodeploy;

import co.casterlabs.rakurai.io.http.HttpMethod;
import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.api.http.HttpProvider;
import co.casterlabs.sora.api.http.SoraHttpSession;
import co.casterlabs.sora.api.http.annotations.HttpEndpoint;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@AllArgsConstructor
public class RouteCircleCI implements HttpProvider {
    private String routeToken;
    private AutoDeploy autoDeploy;

    @SneakyThrows
    @HttpEndpoint(uri = "/public/v2/services/autodeploy/:routeToken", allowedMethods = {
            HttpMethod.POST
    })
    public HttpResponse onWorkflowComplete(SoraHttpSession session) {
        if (session.getUriParameters().get("routeToken").equals(this.routeToken)) {
            JsonObject body = (JsonObject) session.getRequestBodyJson(Rson.DEFAULT);

            if (body.getString("type").equals("workflow-completed") &&
                body.getObject("workflow").getString("status").equals("success")) {
                new Thread(() -> {
                    autoDeploy.deploy(body);
                }).start();
            }
        }

        return HttpResponse.newFixedLengthResponse(StandardHttpStatus.OK, "OK");
    }

}
