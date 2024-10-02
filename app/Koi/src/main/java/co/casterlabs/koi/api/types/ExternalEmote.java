package co.casterlabs.koi.api.types;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonClass(exposeAll = true)
public class ExternalEmote {
    private String name;
    private String imageLink;
    private String from;

}
