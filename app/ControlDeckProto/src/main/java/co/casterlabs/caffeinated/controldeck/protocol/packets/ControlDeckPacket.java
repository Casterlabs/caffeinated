package co.casterlabs.caffeinated.controldeck.protocol.packets;

import co.casterlabs.caffeinated.controldeck.protocol.ControlDeckPacketType;
import co.casterlabs.rakurai.json.annotating.JsonSerializationMethod;
import co.casterlabs.rakurai.json.element.JsonElement;
import co.casterlabs.rakurai.json.element.JsonString;

public abstract class ControlDeckPacket {

    public abstract ControlDeckPacketType getType();

    @JsonSerializationMethod("type")
    private JsonElement $serialize_type() {
        return new JsonString(this.getType().name());
    }

}
