package co.casterlabs.caffeinated.app.api;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV1Listener;
import co.casterlabs.emoji.generator.WebUtil;
import co.casterlabs.kaimen.webview.bridge.JavascriptFunction;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.LogLevel;

public class KofiApi extends JavascriptObject implements KinokoV1Listener {
    private KinokoV1Connection connection = new KinokoV1Connection(this);

    @SneakyThrows
    @Override
    public void onMessage(String message) {
        if (message.startsWith("data=")) {
            message = message.substring("data=".length());
        }

        JsonObject json = Rson.DEFAULT.fromJson(message, JsonObject.class);

        switch (json.getString("type")) {
            case "Donation": {
                break;
            }

            case "Subscription": {
                break;
            }

            case "Shop Order": {
                break;
            }
        }
    }

    @Override
    public void onOpen() {
        this.connection.getLogger().setCurrentLevel(LogLevel.INFO);
    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.connection.connect(
                this.getChannel(),
                true,
                false
            );
        }
    }

    @Override
    public void onOrphaned() {}

    @Override
    public void onAdopted() {}

    public String getChannel() {
        return String.format(
            "caffeinated_api:%s:kofi",
            CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
        );
    }

    @JavascriptFunction
    public String getUrl() {
        return "https://api.casterlabs.co/v1/kinoko?channel=" + WebUtil.encodeURIComponent(this.getChannel());
    }

}
