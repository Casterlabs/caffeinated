package co.casterlabs.caffeinated.app.controldeck;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV2Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV2Listener;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;

public class AppControlDeck implements KinokoV2Listener {
    private KinokoV2Connection connection = new KinokoV2Connection(this);

    public void init() {
//        this.onClose(true);
    }

    @Override
    public void onMessage(JsonObject message, String sender) {

    }

    @SneakyThrows
    @Override
    public void onClose(boolean remote) {
        if (remote) {
            this.connection.connect(
                String.format(
                    "caffeinated_app:%s:controldeck",
                    CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
                )
            );
        }
    }

    /* Unused */

    @Override
    public void onOpen(String id) {}

    @Override
    public void onJoin(String id) {}

    @Override
    public void onLeave(String id) {}

}
