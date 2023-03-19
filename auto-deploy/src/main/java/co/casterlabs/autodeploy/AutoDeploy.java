package co.casterlabs.autodeploy;

import java.io.File;

import org.jetbrains.annotations.Nullable;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import com.backblaze.b2.client.contentSources.B2ByteArrayContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2UploadFileRequest;

import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.Request;

@PluginImplementation
public class AutoDeploy extends SoraPlugin {
    private B2StorageClient b2;
    private String bucketId;
    private String circleToken;

    @SneakyThrows
    @Override
    public void onInit(Sora sora) {
        AutoDeployConfig config = FileUtil.read(new File("plugins/AutoDeploy/config.json"), AutoDeployConfig.class);

        this.bucketId = config.getBucketId();
        this.circleToken = config.getCircleToken();

        this.b2 = B2StorageClientFactory
            .createDefaultFactory()
            .create(config.getB2Id(), config.getB2Key(), "AutoDeploy");

        sora.addHttpProvider(this, new RouteCircleCI(config.getRouteToken(), this));
    }

    @SneakyThrows
    public void deploy(JsonObject body) {
        String workflowId = body.getObject("workflow").getString("id");
        String commitShortHash = body.getObject("pipeline").getObject("vcs").getString("revision").substring(0, 7);
        String branch = body.getObject("pipeline").getObject("vcs").getString("branch");

        JsonObject workflowInfo = HttpUtil.sendHttpRequestJson(
            new Request.Builder()
                .url(String.format("https://circleci.com/api/v2/workflow/%s/job", workflowId))
                .header("Authorization", "Basic " + circleToken)
        );

        int jobNumber = -1;

        for (JsonElement e : workflowInfo.getArray("items")) {
            JsonObject job = (JsonObject) e;

            if (job.getString("name").equals("build-caffeinated")) {
                jobNumber = job.getNumber("job_number").intValue();
                break;
            }
        }

        assert jobNumber != -1;

        this.getLogger().info("(%d) Incoming build: %s (%s)", jobNumber, commitShortHash, branch);

        JsonArray artifacts = HttpUtil.sendHttpRequestJson(
            new Request.Builder()
                .url(String.format("https://circleci.com/api/v2/project/gh/Casterlabs/Casterlabs/%d/artifacts", jobNumber))
                .header("Authorization", "Basic " + circleToken)
        ).getArray("items");

        for (JsonElement e : artifacts) {
            JsonObject artifact = (JsonObject) e;

            String file = artifact.getString("path");
            File tempFile = HttpUtil.sendHttpRequestBytes(new Request.Builder().url(artifact.getString("url")));

            this.getLogger().info("(%d) Uploading build artifact: %s (%d bytes)", jobNumber, file, tempFile.length());

            this.upload(
                String.format("dist/%s/%s", branch, file),
                tempFile
            );

            tempFile.delete();
        }

        this.upload(
            String.format("dist/%s/commit", branch),
            commitShortHash
        );

        this.getLogger().info("(%d) Done!", jobNumber);
    }

    private void upload(String file, File tempFile) throws B2Exception {
        this.b2.uploadSmallFile(
            B2UploadFileRequest
                .builder(this.bucketId, file, B2ContentTypes.APPLICATION_OCTET, B2FileContentSource.build(tempFile))
                .build()
        );
    }

    private void upload(String file, String content) throws B2Exception {
        this.b2.uploadSmallFile(
            B2UploadFileRequest
                .builder(this.bucketId, file, B2ContentTypes.TEXT_PLAIN, B2ByteArrayContentSource.build(content.getBytes()))
                .build()
        );
    }

    @Override
    public void onClose() {
        this.b2.close();
    }

    @Override
    public @Nullable String getVersion() {
        return null;
    }

    @Override
    public @Nullable String getAuthor() {
        return "Casterlabs-Hidden";
    }

    @Override
    public @NonNull String getName() {
        return "AutoDeploy";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.autodeploy";
    }

}
