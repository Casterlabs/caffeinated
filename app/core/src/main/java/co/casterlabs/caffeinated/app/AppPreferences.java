package co.casterlabs.caffeinated.app;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import co.casterlabs.caffeinated.util.Crypto;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.annotating.JsonDeserializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;

@JsonClass(exposeAll = true)
public class AppPreferences {
    private int conductorPort = 8092; // Caffeinated <1.2 was 8091.
    public String conductorKey = new String(Crypto.generateSecureRandomKey());
    public String developerApiKey = new String(Crypto.generateSecureRandomKey());
    public String installationId = UUID.randomUUID().toString();

    public Set<String> oneTimeEvents = new HashSet<>();

    public String koiUrl = "wss://api.casterlabs.co/v2/koi";

    public int conductorPort() {
        if (App.isDev) {
            // Assign a "random" port when in dev mode. We do this in a method so that when
            // save() is called we don't accidentally write this port to disk.
            return this.conductorPort + 1;
        }

        return this.conductorPort;
    }

    @JsonDeserializationMethod("isNew")
    private void $migrate_isNew(JsonElement e) {
        if (!e.getAsBoolean()) {
            this.oneTimeEvents.add("caffeinated.instance.first_time_setup");
        }
    }

}
