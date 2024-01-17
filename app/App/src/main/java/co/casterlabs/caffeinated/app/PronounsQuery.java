package co.casterlabs.caffeinated.app;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import co.casterlabs.koi.api.types.user.Pronouns;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonArray;
import co.casterlabs.rakurai.json.element.JsonObject;
import co.casterlabs.yen.Cache;
import co.casterlabs.yen.Cacheable;
import co.casterlabs.yen.impl.MemoryBackedCache;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class PronounsQuery {
    public static final OkHttpClient client = new OkHttpClient();

    private static Cache<PronounCacheItem> pronounsCache = new MemoryBackedCache<>(TimeUnit.MINUTES.toMillis(30), 100000);

    public static Pronouns get(User user) {
        switch (user.getPlatform()) {
            case TWITCH:
                if (pronounsCache.has(user.getUPID())) { // bug.
                    return pronounsCache.get(user.getUPID()).pronouns;
                } else {
                    JsonObject response = jsonRequest(String.format("https://pronoundb.org/api/v2/lookup?platform=twitch&ids=%s", user.getId()));
                    if (!response.containsKey(user.getId())) {
                        pronounsCache.submit(new PronounCacheItem(user.getUPID(), Pronouns.UNKNOWN));
                        return Pronouns.UNKNOWN;
                    }

                    JsonArray set = response.getObject(user.getId()).getObject("sets").getArray("en");
                    if (set.isEmpty()) {
                        pronounsCache.submit(new PronounCacheItem(user.getUPID(), Pronouns.UNKNOWN));
                        return Pronouns.UNKNOWN;
                    }

                    Pronouns pronouns = Pronouns.valueOf(set.getString(0).toUpperCase());

                    pronounsCache.submit(new PronounCacheItem(user.getUPID(), pronouns));
                    return pronouns;
                }

            default:
                return Pronouns.UNKNOWN;
        }
    }

    @SneakyThrows
    private static JsonObject jsonRequest(@NonNull String url) {
        try (Response response = client.newCall(
            new Request.Builder()
                .url(url)
                .build()
        ).execute()) {
            String content = new String(response.body().bytes(), StandardCharsets.UTF_8);
            FastLogger.logStatic(LogLevel.DEBUG, "Pronouns result for %s:\n%s", url, content);
            return Rson.DEFAULT.fromJson(content, JsonObject.class);
        }
    }

    @NoArgsConstructor
    @AllArgsConstructor
    private static class PronounCacheItem implements Cacheable {
        public String upid;
        public Pronouns pronouns;

        @Override
        public String id() {
            return this.upid;
        }

    }

}
