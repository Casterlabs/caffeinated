package co.casterlabs.caffeinated.pluginsdk.kinoko;

import co.casterlabs.rakurai.json.element.JsonObject;

public interface KinokoV2Listener {

    public void onOpen(String id);

    public void onMessage(JsonObject message, String sender);

    public void onJoin(String id);

    public void onLeave(String id);

    public void onClose(boolean remote);

    default void onException(Exception e) {
        e.printStackTrace();
    }

}
