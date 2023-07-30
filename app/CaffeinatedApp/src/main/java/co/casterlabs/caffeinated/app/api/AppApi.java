package co.casterlabs.caffeinated.app.api;

import co.casterlabs.kaimen.webview.bridge.JavascriptObject;

public class AppApi extends JavascriptObject {
    public final MusicApi musicApi = new MusicApi();
    public final KofiApi kofiApi = new KofiApi();

    public void init() {
        musicApi.onClose(true);
        kofiApi.onClose(true);
    }

}
