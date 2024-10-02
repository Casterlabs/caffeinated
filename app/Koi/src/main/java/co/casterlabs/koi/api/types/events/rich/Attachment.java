package co.casterlabs.koi.api.types.events.rich;

import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class Attachment {
    private Attachment.AttachmentType type;
    private JsonElement content;
    private String html;
    private @Nullable Donation donation;

    public static enum AttachmentType {
        @Deprecated
        GIF,
        IMAGE,
        INTERACTIVE;
    }
}
