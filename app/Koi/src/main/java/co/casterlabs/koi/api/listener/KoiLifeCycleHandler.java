package co.casterlabs.koi.api.listener;

/**
 * @implNote This listener is also a valid subsitute for KoiEventListener and is
 *           treated much the same.
 */
public interface KoiLifeCycleHandler {

    default void onClose(boolean remote) {}

    default void onServerMessage(String message) {}

    default void onError(String errorCode) {}

    default void onException(Exception e) {
        e.printStackTrace();
    }

}
