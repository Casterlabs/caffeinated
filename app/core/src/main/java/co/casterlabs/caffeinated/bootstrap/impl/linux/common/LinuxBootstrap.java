package co.casterlabs.caffeinated.bootstrap.impl.linux.common;

import co.casterlabs.caffeinated.app.music_integration.MusicImpl;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class LinuxBootstrap implements NativeBootstrap {

    @Override
    public void init() throws Exception {
        if (LinuxSystemPlaybackMusicProvider.isPlayerCtlInstalled()) {
            ReflectionLib.setStaticValue(MusicImpl.class, "systemPlaybackMusicProvider", new LinuxSystemPlaybackMusicProvider());
        }

    }

}
