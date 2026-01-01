package co.casterlabs.caffeinated.bootstrap.impl.windows.common;

import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import co.casterlabs.caffeinated.bootstrap.impl.windows.common.music.WindowsSystemPlaybackMusicProvider;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class WindowsBootstrap implements NativeBootstrap {

    @Override
    public void init() throws Exception {
        ReflectionLib.setStaticValue(MusicImpl.class, "systemPlaybackMusicProvider", new WindowsSystemPlaybackMusicProvider());
    }

}
