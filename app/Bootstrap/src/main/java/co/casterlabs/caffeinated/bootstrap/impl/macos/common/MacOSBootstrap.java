package co.casterlabs.caffeinated.bootstrap.impl.macos.common;

import co.casterlabs.caffeinated.bootstrap.NativeSystem;
import co.casterlabs.caffeinated.bootstrap.impl.NativeBootstrap;

public class MacOSBootstrap implements NativeBootstrap {

    @SuppressWarnings("deprecation")
    @Override
    public void init() throws Exception {
        NativeSystem.initialize(null);
    }

}
