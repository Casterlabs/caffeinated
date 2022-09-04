package co.casterlabs.koi.api.types.events.rich;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import co.casterlabs.rakurai.json.element.JsonElement;
import lombok.Data;

@Data
@JsonClass(exposeAll = true)
public class Attachment {
    private Attachment.AttachmentType type;
    private JsonElement content;
    private String html;

    public static enum AttachmentType {
        GIF;
    }
}
