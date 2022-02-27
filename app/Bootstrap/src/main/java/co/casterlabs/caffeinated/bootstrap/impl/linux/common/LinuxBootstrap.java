package co.casterlabs.caffeinated.bootstrap.impl.linux.common;

import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;

public class LinuxBootstrap implements NativeBootstrap {

    @SuppressWarnings("deprecation")
    @Override
    public void init() throws Exception {
        LinuxSystemPlaybackMusicProvider playbackProvider = null;

        if (LinuxSystemPlaybackMusicProvider.isPlayerCtlInstalled()) {
            playbackProvider = new LinuxSystemPlaybackMusicProvider();
        }

        NativeSystem.initialize(playbackProvider);
    }

}
