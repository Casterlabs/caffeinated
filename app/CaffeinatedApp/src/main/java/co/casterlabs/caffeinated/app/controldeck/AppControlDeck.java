package co.casterlabs.caffeinated.app.controldeck;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.caffeinated.app.CaffeinatedApp;
import co.casterlabs.caffeinated.app.controldeck.ControlDeckPreferences.DeckConfig;
import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckConnection;
import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.caffeinated.controldeck.protocol.deck.ControlDeck;
import co.casterlabs.caffeinated.controldeck.protocol.packets.ControlDeckPacket;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV2Connection;
import co.casterlabs.caffeinated.pluginsdk.kinoko.KinokoV2Listener;
import co.casterlabs.caffeinated.util.collections.ValueIdentityHashMap;
import co.casterlabs.commons.async.AsyncTask;
import co.casterlabs.kaimen.webview.bridge.JavascriptObject;
import co.casterlabs.kaimen.webview.bridge.JavascriptValue;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;

public class AppControlDeck extends JavascriptObject {
    private KinokoV2Connection connection = new KinokoV2Connection(new ApiListener());

    @JavascriptValue(allowSet = false)
    private Map<String, ControlDeck> decks = new ValueIdentityHashMap<>();

    public void init() {
        this.reconnect();
    }

    private void onReady(ControlDeck deck) {
        DeckConfig conf = CaffeinatedApp.getInstance().getControlDeckPreferences().get().getDecks().get(deck.getDeckId());

        if (conf == null) {
            conf = DeckConfig.create(deck);
            CaffeinatedApp.getInstance().getControlDeckPreferences().get().getDecks().put(deck.getDeckId(), conf);
            CaffeinatedApp.getInstance().getControlDeckPreferences().save();
        }

        // TODO load routine for linking sources with volume items.
//        for (int idx = 0; idx < conf.getVolumeItems().length; idx++) {
//            deck.getVolumeItems()[idx]
//                
//        }

        decks.put(deck.getDeckId(), deck);
    }

    @SneakyThrows
    private void reconnect() {
        this.connection.connect(
            String.format(
                "caffeinated_app:%s:controldeck",
                CaffeinatedApp.getInstance().getAppPreferences().get().getDeveloperApiKey()
            )
        );
    }

    private class ApiListener implements KinokoV2Listener {
        private Map<String, ControlDeck> instances = new HashMap<>();

        private void sendTransportMessage(String type, JsonObject data, @Nullable String sender) {
            connection.send(
                new JsonObject()
                    .put("t_type", type)
                    .put("t_data", data),
                sender
            );
        }

        @Override
        public void onMessage(JsonObject message, final String sender) {
            AsyncTask.create(() -> {
                try {
                    String transportType = message.getString("t_type");

                    switch (transportType) {
                        case "init": {
                            ControlDeck deck = new ControlDeck(new ControlDeckConnection() {

                                @Override
                                public void sendPacket(ControlDeckPacket packet) {
                                    sendTransportMessage("packet", (JsonObject) Rson.DEFAULT.toJson(packet), sender);
                                }

                                @Override
                                public void onInit() {
                                    onReady(instances.get(sender));
                                }

                            });

                            instances.put(sender, deck);

                            deck.init();
                            break;
                        }

                        case "packet": {
                            ControlDeck deck = instances.get(sender);
                            JsonObject transportData = message.getObject("t_data");

                            deck.processPacket(ControlDeckPacketType.getPacket(transportData));
                            break;
                        }
                    }
                } catch (Exception e) {
                    FastLogger.logException(e);
                }
            });
        }

        @Override
        public void onClose(boolean remote) {
            if (remote) {
                decks.clear();
                instances.clear();

                reconnect();
            }
        }

        /* Unused */

        @Override
        public void onOpen(String id) {}

        @Override
        public void onJoin(String id) {}

        @Override
        public void onLeave(String id) {
            ControlDeck instance = instances.remove(id);

            if (instance != null) {
                decks.remove(instance.getDeckId());
            }
        }

    }

}
