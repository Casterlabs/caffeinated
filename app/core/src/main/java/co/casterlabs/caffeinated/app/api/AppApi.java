package co.casterlabs.caffeinated.app.api;

public class AppApi {
    public static final MusicApi musicApi = new MusicApi();

    public static void init() {
        musicApi.onClose(true);
    }

}
