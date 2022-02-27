package co.casterlabs.caffeinated.pluginsdk;

import lombok.NonNull;

public class ApiException extends Exception {
    private static final long serialVersionUID = 4745921453729137378L;

    public ApiException(@NonNull String reason) {
        super(reason);
    }

}
