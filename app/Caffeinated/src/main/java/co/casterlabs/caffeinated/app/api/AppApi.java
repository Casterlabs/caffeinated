package co.casterlabs.caffeinated.app.api;

import co.casterlabs.saucer.bridge.JavascriptObject;

@JavascriptObject
public class AppApi {
    public final MusicApi musicApi = new MusicApi();

    public void init() {
        musicApi.onClose(true);
    }

}
