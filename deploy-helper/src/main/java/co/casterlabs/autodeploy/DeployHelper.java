package co.casterlabs.autodeploy;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.backblaze.b2.client.B2StorageClient;
import com.backblaze.b2.client.B2StorageClientFactory;
import com.backblaze.b2.client.contentSources.B2ByteArrayContentSource;
import com.backblaze.b2.client.contentSources.B2ContentTypes;
import com.backblaze.b2.client.contentSources.B2FileContentSource;
import com.backblaze.b2.client.exceptions.B2Exception;
import com.backblaze.b2.client.structures.B2UploadFileRequest;

import co.casterlabs.commons.async.Promise;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class DeployHelper {
    private static final String[] CAFFEINATED_ARTIFACTS = {
            "Windows-amd64-nojre.zip",
            "macOS-amd64-nojre.zip",
            "Linux-amd64-nojre.zip",
            "Windows-amd64.zip",
            "macOS-amd64.zip",
            "Linux-amd64.zip"
    };

    private static final FastLogger logger = new FastLogger();

    public static void main(String[] args) {
        String bucketId = System.getenv("BB_BUCKET_ID");

        B2StorageClient b2 = B2StorageClientFactory
            .createDefaultFactory()
            .create(System.getenv("BB_CLIENT_ID"), System.getenv("BB_CLIENT_KEY"), "AutoDeploy");

        String commitShortHash = System.getenv("CIRCLE_SHA1").substring(0, 7);
        String branch = System.getenv("CIRCLE_BRANCH");
        int jobNumber = Integer.parseInt(System.getenv("CIRCLE_BUILD_NUM"));

        logger.info("Incoming build: %s (%s) (#%d)", commitShortHash, branch, jobNumber);

        // Start all of the tasks in parallel.
        List<Promise<Void>> tasks = new LinkedList<>();

        for (String artifact : CAFFEINATED_ARTIFACTS) {
            Promise<Void> promise = new Promise<>(() -> {
                File artifactFile = new File("./dist/artifacts/" + artifact);

                logger.info("Uploading build artifact: %s (%d bytes)", artifactFile, artifactFile.length());

                b2.uploadSmallFile(
                    B2UploadFileRequest
                        .builder(
                            bucketId,
                            String.format("dist/%s/%s", branch, artifact),
                            B2ContentTypes.APPLICATION_OCTET,
                            B2FileContentSource.build(artifactFile)
                        )
                        .build()
                );

                return null; // aVoid
            });
            tasks.add(promise);
        }

        boolean anySucceeded = false;
        boolean anyFailed = false;

        // Wait for each and every one to complete, catching the error if any.
        for (Promise<Void> task : tasks) {
            try {
                task.await();
                anySucceeded = true;
            } catch (Throwable t) {
                anyFailed = true;
                logger.severe("An error occurred whilst uploading file to backblaze:\n%s", t);
            }
        }

        if (!anySucceeded) {
            logger.warn("No upload succeeded, not uploading commit hash.");
            return;
        }

        if (anyFailed) {
            logger.warn("Some uploads failed, proceeding with commit hash upload anyway.");
        }

        try {
            b2.uploadSmallFile(
                B2UploadFileRequest
                    .builder(
                        bucketId,
                        String.format("dist/%s/commit", branch),
                        B2ContentTypes.TEXT_PLAIN, B2ByteArrayContentSource.build(commitShortHash.getBytes())
                    )
                    .build()
            );

            logger.info("Done!");
        } catch (B2Exception e) {
            logger.severe("An error occurred whilst uploading commit hash to backblaze:\n%s", e);
        }
    }

}
