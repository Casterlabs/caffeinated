package co.casterlabs.caffeinated.app;

import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class AppPreferences {
    private int conductorPort = 8092; // Caffeinated <1.2 was 8091.
    private String conductorKey = new String(Crypto.generateSecureRandomKey());

    private boolean isNew = true;

    private String developerApiKey = new String(Crypto.generateSecureRandomKey());

}
