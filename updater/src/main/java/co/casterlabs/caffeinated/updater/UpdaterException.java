package co.casterlabs.caffeinated.updater;

import lombok.Getter;

public class UpdaterException extends Exception {
    private static final long serialVersionUID = -7563002341328958687L;

    private @Getter Error error;

    public UpdaterException(Error error, String message, Exception cause) {
        super(message, cause);
        this.error = error;
    }

    public static enum Error {
        DOWNLOAD_FAILED,
        LAUNCH_FAILED;

    }

}
