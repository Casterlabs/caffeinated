package co.casterlabs.koi.api.types;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
public class ExternalEmote {
    private String name;
    private String imageLink;
    private String from;

}
