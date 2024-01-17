package co.casterlabs.caffeinated.app.api;

import dev.webview.webview_java.bridge.JavascriptObject;

public class AppApi extends JavascriptObject {
    public final MusicApi musicApi = new MusicApi();

    public void init() {
        musicApi.onClose(true);
    }

}
