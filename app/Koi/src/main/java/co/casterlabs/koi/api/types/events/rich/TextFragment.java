package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class TextFragment extends ChatFragment {
    private String text;

    public TextFragment(String text) {
        super(
            text,
            String.format(
                "<span data-rich-type='text'>%s</span>",
                ChatFragment.escapeHtml(text)
            )
        );
        this.text = text;
    }

    @Override
    public FragmentType getType() {
        return FragmentType.TEXT;
    }

}
