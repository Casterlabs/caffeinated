package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class LinkFragment extends ChatFragment {
    private String url;
//    private JsonObject og_properties;

    @Override
    public FragmentType getType() {
        return FragmentType.LINK;
    }

}
