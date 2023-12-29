package co.casterlabs.koi.api.types.events;

import java.util.Map;

import co.casterlabs.rakurai.json.annotating.JsonClass;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonClass(exposeAll = true)
@EqualsAndHashCode(callSuper = true)
public class ConnectionStateEvent extends KoiEvent {
    private Map<String, ConnectionState> states;

    @Override
    public KoiEventType getType() {
        return KoiEventType.CONNECTION_STATE;
    }

    public static enum ConnectionState {
        CONNECTED,
        DISCONNECTED,
        WAITING;
    }

}
