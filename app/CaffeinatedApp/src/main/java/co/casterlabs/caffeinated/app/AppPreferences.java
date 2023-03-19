package co.casterlabs.caffeinated.app;

import java.util.concurrent.ThreadLocalRandom;

import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.kaimen.webview.WebviewRenderer;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class AppPreferences {
    private int conductorPort = 8092; // Caffeinated <1.2 was 8091.
    private String conductorKey = new String(Crypto.generateSecureRandomKey());

    private boolean isNew = true;

    private boolean useBetaKoiPath = false;

    private String developerApiKey = new String(Crypto.generateSecureRandomKey());

    private WebviewRenderer[] rendererPreference = {
            WebviewRenderer.WEBKIT,
            WebviewRenderer.CHROMIUM_EMBEDDED_FRAMEWORK
    };

    public int getConductorPort() {
        if (CaffeinatedApp.getInstance().isDev()) {
            // Assign a "random" port when in dev mode. We do this in a method so that when
            // save() is called we don't accidentally write this port to disk.
            return this.conductorPort + ThreadLocalRandom.current().nextInt(10) + 1;
        }

        return this.conductorPort;
    }

}
