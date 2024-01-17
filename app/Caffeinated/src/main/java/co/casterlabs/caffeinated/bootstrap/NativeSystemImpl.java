package co.casterlabs.caffeinated.bootstrap;

import java.awt.TrayIcon.MessageType;

import co.casterlabs.caffeinated.app.NativeSystem;
import co.casterlabs.caffeinated.app.NotificationType;
import lombok.NonNull;

public class NativeSystemImpl implements NativeSystem {

    @Override
    public void notify(@NonNull String message, @NonNull NotificationType type) throws IllegalStateException {
        TrayHandler.notify(message, MessageType.valueOf(type.name()));
    }

}
