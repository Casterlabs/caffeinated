package co.casterlabs.caffeinated.app.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.ToString;

@ToString
@JsonClass(exposeAll = true)
public class AuthPreferences {
    private Map<String, JsonObject> tokensMap = new HashMap<>();

    public List<String> getAllTokensByType(String type) {
        type = type.toLowerCase();

        if (!this.tokensMap.containsKey(type)) {
            return Collections.emptyList();
        }

        return this.tokensMap.get(type).values()
            .stream()
            .map((e) -> e.getAsString())
            .collect(Collectors.toList());
    }

    public List<String> getAllTokenIdsByType(String type) {
        type = type.toLowerCase();

        if (!this.tokensMap.containsKey(type)) {
            return Collections.emptyList();
        }

        return new ArrayList<>(this.tokensMap.get(type).keySet());
    }

    public @Nullable String getToken(@NonNull String type, @NonNull String id) {
        type = type.toLowerCase();

        JsonObject tokenStore = this.tokensMap.get(type);
        if (tokenStore == null) return null;

        if (tokenStore.containsKey(id)) {
            return tokenStore.getString(id);
        } else {
            return null;
        }
    }

    public void addToken(@NonNull String type, @NonNull String id, @NonNull String token) {
        type = type.toLowerCase();

        JsonObject tokenStore = this.tokensMap.get(type);
        if (tokenStore == null) {
            // Create it if it doesn't exist.
            tokenStore = new JsonObject();
            this.tokensMap.put(type, tokenStore);
        }

        tokenStore.put(id, token);
        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

    public void removeToken(@NonNull String type, @NonNull String id) {
        type = type.toLowerCase();

        JsonObject tokenStore = this.tokensMap.get(type);
        if (tokenStore == null) return;

        tokenStore.remove(id);
        CaffeinatedApp.getInstance().getAuthPreferences().save();
    }

}
