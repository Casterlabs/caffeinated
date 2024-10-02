package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class EmojiFragment extends ChatFragment {
//    private Emoji.Variation variation;

    /**
     * @deprecated Use this only if necessary. In the future it will be a real
     *             object.
     */
    @Deprecated
    private JsonObject variation;

    @Override
    public FragmentType getType() {
        return FragmentType.EMOJI;
    }

}
