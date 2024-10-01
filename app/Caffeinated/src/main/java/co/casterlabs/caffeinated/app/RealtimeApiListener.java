package co.casterlabs.caffeinated.app;

import java.io.IOException;

import co.casterlabs.koi.api.types.KoiEvent;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;

public interface RealtimeApiListener {

    public void onKoiEvent(@NonNull KoiEvent event) throws IOException;

    public void onKoiStaticsUpdate(@NonNull JsonObject statics) throws IOException;

    public void onMusicUpdate(@NonNull JsonObject music) throws IOException;

    public void onAppearanceUpdate(@NonNull JsonObject preferences) throws IOException;

}
