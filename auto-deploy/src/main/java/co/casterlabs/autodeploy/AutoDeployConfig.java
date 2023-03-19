package co.casterlabs.autodeploy;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;

@Getter
@JsonClass(exposeAll = true)
public class AutoDeployConfig {
    private String routeToken;
    private String circleToken;
    private String bucketId;
    private String b2Id;
    private String b2Key;

}
