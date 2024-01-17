package co.casterlabs.caffeinated.bootstrap;

import co.casterlabs.caffeinated.app.music_integration.impl.InternalMusicProvider;
import co.casterlabs.caffeinated.bootstrap.SystemPlaybackMusicProvider.SystemPlaybackSettings;
import co.casterlabs.rakurai.json.annotating.JsonClass;

public abstract class SystemPlaybackMusicProvider extends InternalMusicProvider<SystemPlaybackSettings> {

    public SystemPlaybackMusicProvider() {
        super("System", "system", SystemPlaybackSettings.class);
    }

    @Override
    public abstract void init();

    protected abstract void update();

    @Override
    protected void onSettingsUpdate() {
        if (this.isEnabled()) {
            this.update();
        } else {
            this.setPlaybackStateInactive();
        }
    }

    @Override
    public void signout() {} // NO-OP

    public boolean isEnabled() {
        return this.getSettings().enabled;
    }

    @JsonClass(exposeAll = true)
    public static class SystemPlaybackSettings {
        boolean enabled = false;

    }

}
