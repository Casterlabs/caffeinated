package co.casterlabs.caffeinated.app.api;

import co.casterlabs.kaimen.webview.bridge.JavascriptObject;

public class AppApi extends JavascriptObject {
    public final MusicApi musicApi = new MusicApi();

    public void init() {
        musicApi.onClose(true);
    }

}
