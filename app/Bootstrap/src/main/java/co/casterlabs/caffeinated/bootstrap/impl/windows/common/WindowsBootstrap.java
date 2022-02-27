package co.casterlabs.caffeinated.bootstrap.impl.windows.common;

import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.WindowsSystemPlaybackMusicProvider;

public class WindowsBootstrap implements NativeBootstrap {

    @SuppressWarnings("deprecation")
    @Override
    public void init() throws Exception {
        NativeSystem.initialize(new WindowsSystemPlaybackMusicProvider());
    }

}
