package co.casterlabs.caffeinated.pluginsdk.kinoko;

public interface KinokoV1Listener {

    public void onOpen();

    default void onOrphaned() {}

    default void onAdopted() {}

    public void onMessage(String message);

    public void onClose(boolean remote);

    default void onException(Exception e) {
        e.printStackTrace();
    }

}
