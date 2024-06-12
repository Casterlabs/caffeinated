package co.casterlabs.koi.api.types.events;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import co.casterlabs.koi.api.types.events.rich.TextFragment;
import co.casterlabs.koi.api.types.user.User;
import co.casterlabs.koi.api.types.user.UserPlatform;
import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class PlatformMessageEvent extends RichMessageEvent {
    private static final User SYSTEM = new User("system", "system", UserPlatform.CASTERLABS_SYSTEM, Collections.emptyList(), Collections.emptyList(), "#ea4c4c", "Casterlabs", "Casterlabs", "", "https://casterlabs.co", "https://cdn.casterlabs.co/branding/casterlabs/icon.png", -1, -1);

    public PlatformMessageEvent(String message) {
        super(
            SYSTEM.clone(),
            SYSTEM,
            Arrays.asList(new TextFragment(message)),
            Collections.emptyList(),
            Collections.emptyList(),
            UUID.randomUUID().toString(),
            null
        );
    }

    @Override
    public KoiEventType getType() {
        return KoiEventType.PLATFORM_MESSAGE;
    }

}
