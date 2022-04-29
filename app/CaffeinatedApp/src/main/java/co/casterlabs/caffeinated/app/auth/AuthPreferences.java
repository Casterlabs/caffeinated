package co.casterlabs.caffeinated.app.auth;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class AuthPreferences {
    private @Setter String casterlabsAuthToken;
    private JsonObject tokens = new JsonObject();
    private JsonObject koiTokens = new JsonObject();

    public List<String> getKoiTokenIds() {
        return new ArrayList<>(this.koiTokens.keySet());
    }

    public String addKoiToken(@NonNull String token) {
        String tokenId = UUID.randomUUID().toString();

        this.koiTokens.put(tokenId, token);
        CaffeinatedApp.getInstance().getAuthPreferences().save();

        return tokenId;
    }

    public void addToken(@NonNull String tokenId, @NonNull String token) {
        this.tokens.put(tokenId, token);
        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

}
