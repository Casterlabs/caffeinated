package co.casterlabs.caffeinated.app;

import lombok.NonNull;

public interface NativeSystem {

    // Desktop Notifications
    public void notify(@NonNull String message, @NonNull NotificationType type) throws IllegalStateException;;

}
