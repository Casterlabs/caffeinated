package co.casterlabs.koi.api.types.events.rich;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class EmoteFragment extends ChatFragment {
    private String emoteName;
    private String imageLink;
    private @Nullable String provider;

    @Override
    public FragmentType getType() {
        return FragmentType.EMOTE;
    }

}
