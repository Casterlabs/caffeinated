package co.casterlabs.caffeinated.bootstrap.impl.linux.common;

import co.casterlabs.caffeinated.app.music_integration.MusicIntegration;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;
import xyz.e3ndr.reflectionlib.ReflectionLib;

public class LinuxBootstrap implements NativeBootstrap {

    @Override
    public void init() throws Exception {
        if (LinuxSystemPlaybackMusicProvider.isPlayerCtlInstalled()) {
            ReflectionLib.setStaticValue(MusicIntegration.class, "systemPlaybackMusicProvider", new LinuxSystemPlaybackMusicProvider());
        }

    }

}
