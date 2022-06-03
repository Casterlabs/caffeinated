package co.casterlabs.caffeinated.app;

import java.util.concurrent.TimeUnit;

import co.casterlabs.caffeinated.util.WebUtil;
import co.casterlabs.kaimen.util.threading.AsyncTask;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.validation.JsonValidate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import okhttp3.Request;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

@Getter
@EqualsAndHashCode
@JsonClass(exposeAll = true)
public class BuildInfo {
    private String author;
    private String version;
    private String commit;
    private String buildChannel;
    private String versionString;
    private boolean isDev = false;

    private boolean isOutOfDate = false;

    @JsonValidate
    private void validate() {
        if (this.version.startsWith("${")) {
            this.buildChannel = "dev";
            this.version = "0.0";
            this.commit = "?";
            this.isDev = true;
        } else {
            new AsyncTask(() -> {
                final String url = String.format("https://cdn.casterlabs.co/dist/%s/commit", this.buildChannel);

                while (true) {
                    try {
                        String remoteCommit = WebUtil.sendHttpRequest(new Request.Builder().url(url)).trim();

                        if (!remoteCommit.equals(this.commit)) {
                            this.isOutOfDate = true;
                            // We're out of date, notify the user.
                            return;
                        }
                    } catch (Exception e) {
                        FastLogger.logException(e);
                    } finally {
                        try {
                            TimeUnit.MINUTES.sleep(5);
                        } catch (InterruptedException ignored) {}
                    }
                }
            });
        }

        this.versionString = String.format("%s-%s-%s", this.version, this.commit, this.buildChannel);
    }

}
