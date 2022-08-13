package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class TextFragment extends ChatFragment {
    private String text;

    @Override
    public FragmentType getType() {
        return FragmentType.TEXT;
    }

}
