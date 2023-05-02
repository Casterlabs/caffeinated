package co.casterlabs.caffeinatedapi;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinatedapi.routes.SpotifyAuthProxy;
import co.casterlabs.sora.Sora;
import co.casterlabs.sora.api.PluginImplementation;
import co.casterlabs.sora.api.SoraPlugin;
import lombok.NonNull;
import lombok.SneakyThrows;

@PluginImplementation
public class CaffeinatedApi extends SoraPlugin {
    private Config config;

    @SneakyThrows
    @Override
    public void onInit(Sora sora) {
        this.config = Config.load();

        sora.addProvider(this, new SpotifyAuthProxy(this.config));
    }

    @Override
    public void onClose() {}

    @Override
    public @Nullable String getVersion() {
        return null;
    }

    @Override
    public @Nullable String getAuthor() {
        return "Casterlabs";
    }

    @Override
    public @NonNull String getName() {
        return "Caffeinated Api";
    }

    @Override
    public @NonNull String getId() {
        return "co.casterlabs.caffeinatedapi";
    }

}
