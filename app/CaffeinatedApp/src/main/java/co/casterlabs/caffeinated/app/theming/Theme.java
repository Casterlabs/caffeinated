package co.casterlabs.caffeinated.app.theming;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class Theme {
    private @NonNull @With(AccessLevel.NONE) String id;
    private @NonNull @With(AccessLevel.NONE) String name;

    private @With(AccessLevel.PRIVATE) String[] css;
    private @With(AccessLevel.PRIVATE) boolean isInlineCss;

    private @NonNull @With String classes;

    private @With boolean isDark;

    public Theme(@NonNull String id, @NonNull String name) {
        this(id, name, new String[0], false, "", false);
    }

    public Theme withCssFiles(boolean isInlineCss, @NonNull String... css) {
        return this.withCss(css)
            .withInlineCss(isInlineCss);
    }

}
