package co.casterlabs.caffeinated.app.theming;

import co.casterlabs.kaimen.app.App.Appearance;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class Theme {
    private @NonNull String id;
    private @NonNull Appearance appearance;
    private boolean isAuto;

}
