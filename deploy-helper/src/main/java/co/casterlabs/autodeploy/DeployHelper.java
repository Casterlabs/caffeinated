package co.casterlabs.autodeploy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    public static void main(String[] args) throws FileNotFoundException, IOException {
//        JsonObject githubEvent = Rson.DEFAULT.fromJson(
//            IOUtil.readString(new FileInputStream(args[0] /* github event */)),
//            JsonObject.class
//        );

        if (!System.getenv().containsKey("BB_BUCKET_ID")) {
            logger.fatal("Could not find BB_BUCKET_ID in the current env, aborting.");
            System.exit(1);
        }

        if (!System.getenv().containsKey("BB_CLIENT_ID")) {
            logger.fatal("Could not find BB_CLIENT_ID in the current env, aborting.");
            System.exit(1);
        }

        if (!System.getenv().containsKey("BB_CLIENT_KEY")) {
            logger.fatal("Could not find BB_CLIENT_KEY in the current env, aborting.");
            System.exit(1);
        }

        if (!System.getenv().containsKey("GITHUB_SHA")) {
            logger.fatal("Could not find GITHUB_SHA in the current env, aborting.");
            System.exit(1);
        }

        if (!System.getenv().containsKey("GITHUB_REF_NAME")) {
            logger.fatal("Could not find GITHUB_REF_NAME in the current env, aborting.");
            System.exit(1);
        }

        String bucketId = System.getenv("BB_BUCKET_ID");

        B2StorageClient b2 = B2StorageClientFactory
            .createDefaultFactory()
            .create(System.getenv("BB_CLIENT_ID"), System.getenv("BB_CLIENT_KEY"), "AutoDeploy");

        String commitShortHash = System.getenv("GITHUB_SHA").substring(0, 7);
        String branch = System.getenv("DEPLOY_CHANNEL");

        logger.info("Deploying build: %s (%s)", commitShortHash, branch);

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

        if (branch.equals("stable")) {
            // We need to also deploy to the beta branch to keep them up-to-date.
            for (String artifact : CAFFEINATED_ARTIFACTS) {
                Promise<Void> promise = new Promise<>(() -> {
                    File artifactFile = new File("./dist/artifacts/" + artifact);

                    logger.info("Uploading build artifact: %s (%d bytes)", artifactFile, artifactFile.length());

                    b2.uploadSmallFile(
                        B2UploadFileRequest
                            .builder(
                                bucketId,
                                String.format("dist/beta/%s", artifact),
                                B2ContentTypes.APPLICATION_OCTET,
                                B2FileContentSource.build(artifactFile)
                            )
                            .build()
                    );

                    return null; // aVoid
                });
                tasks.add(promise);
            }
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
            System.exit(1);
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
            System.exit(1);
        }
    }

}
